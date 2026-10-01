package com.smartfactory.ai.ai.embedding

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * texte -> tokenizer -> ONNX (un vecteur par token) -> moyenne -> normalisation -> FloatArray(384)
 * Singleton Hilt : le modèle (~90 Mo) n'est chargé qu'UNE fois.
 */
@Singleton
class OnnxEmbeddingEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val env: OrtEnvironment = OrtEnvironment.getEnvironment()

    private val tokenizer: WordPieceTokenizer by lazy {
        context.assets.open(VOCAB_ASSET).use { WordPieceTokenizer.fromStream(it, MAX_TOKENS) }
    }

    private val session: OrtSession by lazy {
        val debut = System.currentTimeMillis()
        val options = OrtSession.SessionOptions().apply {
            setIntraOpNumThreads(4)
        }
        // Chargé depuis un FICHIER et pas un ByteArray : sinon 90 Mo en double dans la RAM.
        val s = env.createSession(modelFile().absolutePath, options)
        Log.i(TAG, "Modèle ONNX chargé en ${System.currentTimeMillis() - debut} ms, entrées = ${s.inputNames}")
        s
    }

    /** Texte -> vecteur normalisé de 384 nombres. À appeler hors du thread principal. */
    fun embed(text: String): FloatArray {
        val tokens = tokenizer.encode(text)
        val n = tokens.inputIds.size

        // Le modèle attend des tableaux [lot][tokens] : ici un lot de 1 texte.
        val tenseurs = HashMap<String, OnnxTensor>()
        try {
            tenseurs["input_ids"] = OnnxTensor.createTensor(env, arrayOf(tokens.inputIds))
            tenseurs["attention_mask"] = OnnxTensor.createTensor(env, arrayOf(tokens.attentionMask))
            tenseurs["token_type_ids"] = OnnxTensor.createTensor(env, arrayOf(tokens.tokenTypeIds))
            // Certains exports du modèle n'ont pas token_type_ids : on n'envoie que ce qu'il attend.
            val entrees = tenseurs.filterKeys { it in session.inputNames }

            session.run(entrees).use { resultat ->
                val sortie = resultat[0].value
                val brut: FloatArray = when (sortie) {
                    // Cas normal : [1][n][384] -> un vecteur par token
                    is Array<*> -> when (val premier = sortie[0]) {
                        is Array<*> -> {
                            @Suppress("UNCHECKED_CAST")
                            val parToken = premier as Array<FloatArray>
                            check(parToken.size == n) { "Sortie ONNX : ${parToken.size} tokens au lieu de $n" }
                            EmbeddingPooling.meanPool(parToken, tokens.attentionMask)
                        }
                        // Export déjà "poolé" : [1][384]
                        is FloatArray -> premier
                        else -> error("Format de sortie ONNX inattendu : ${premier?.javaClass}")
                    }
                    else -> error("Format de sortie ONNX inattendu : ${sortie?.javaClass}")
                }
                check(brut.size == DIMENSION) { "Vecteur de ${brut.size} nombres au lieu de $DIMENSION" }
                return EmbeddingPooling.normalize(brut)
            }
        } finally {
            tenseurs.values.forEach { it.close() }   // libère la mémoire native
        }
    }

    /** Copie le modèle des assets vers le stockage interne (une seule fois). */
    /**
     * Copie le modèle des assets vers le stockage interne.
     * La copie est refaite à chaque (ré)installation de l'app, pour ne jamais
     * garder une ancienne version du modèle.
     */
    private fun modelFile(): File {
        val dossier = File(context.filesDir, "models").apply { mkdirs() }
        val fichier = File(dossier, "all-MiniLM-L6-v2.onnx")
        val marqueur = File(dossier, "model.version")

        // lastUpdateTime change à chaque installation ou mise à jour de l'app
        val versionApk = context.packageManager
            .getPackageInfo(context.packageName, 0).lastUpdateTime.toString()
        val aJour = fichier.exists() && marqueur.exists() && marqueur.readText() == versionApk

        if (!aJour) {
            val temp = File(dossier, "model.tmp")
            context.assets.open(MODEL_ASSET).use { entree ->
                temp.outputStream().use { sortie -> entree.copyTo(sortie) }
            }
            fichier.delete()
            check(temp.renameTo(fichier)) { "Impossible de copier le modèle ONNX" }
            marqueur.writeText(versionApk)
            Log.i(TAG, "Modèle copié : ${fichier.length()} octets")
        }
        return fichier
    }

    fun close() {
        session.close()
    }

    companion object {
        const val DIMENSION = 384
        const val MAX_TOKENS = 256
        private const val MODEL_ASSET = "models/all-MiniLM-L6-v2.onnx"
        private const val VOCAB_ASSET = "models/vocab.txt"
        private const val TAG = "OnnxEmbedding"
    }
}