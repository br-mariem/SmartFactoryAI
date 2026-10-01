package com.smartfactory.ai.ai.embedding

import android.content.Context
import android.util.Log
import com.smartfactory.ai.data.local.database.converter.VectorConverter
import com.smartfactory.ai.data.local.database.dao.EmbeddingDao
import com.smartfactory.ai.data.local.database.entity.DocChunkEntity
import com.smartfactory.ai.data.local.parser.MaintenanceDocParser
import com.smartfactory.ai.data.local.parser.TextChunker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class IndexReport(
    val documents: Int,
    val chunks: Int,
    val totalMs: Long,
    val skipped: Boolean = false
) {
    val msParChunk: Long get() = if (chunks == 0) 0 else totalMs / chunks
}

/**
 * Pipeline complet de T05 :
 *   fichiers .txt de assets/manuels -> parser -> chunks (250 mots, chevauchement 50)
 *   -> vecteur ONNX (384, normalisé) -> BLOB -> doc_chunks + chunk_embeddings
 */
@Singleton
class ManualIndexer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val engine: OnnxEmbeddingEngine,
    private val parser: MaintenanceDocParser,
    private val chunker: TextChunker,
    private val embeddingDao: EmbeddingDao
) {
    private val converter = VectorConverter()

    /** @param force true = efface l'index existant et recommence. */
    suspend fun indexIfNeeded(force: Boolean = false): IndexReport = withContext(Dispatchers.Default) {
        if (!force && embeddingDao.countEmbeddings() > 0) {
            Log.i(TAG, "Index déjà présent : rien à faire")
            return@withContext IndexReport(0, embeddingDao.countEmbeddings(), 0, skipped = true)
        }
        if (force) embeddingDao.clearIndex()

        val debut = System.currentTimeMillis()
        val fichiers = context.assets.list(MANUALS_DIR).orEmpty().filter { it.endsWith(".txt") }.sorted()
        var nbChunks = 0

        for (nomFichier in fichiers) {
            val texte = context.assets.open("$MANUALS_DIR/$nomFichier").use {
                it.bufferedReader(Charsets.UTF_8).readText()
            }
            val doc = parser.parse(texte)
            val docId = nomFichier.removeSuffix(".txt")       // ex. "sop_001"

            for (chunk in chunker.chunkDocument(doc)) {
                // Le titre du chapitre aide la recherche à savoir de quoi parle le morceau.
                val texteChunk = "${chunk.chapitre} : ${chunk.contenu}"
                val vecteur = engine.embed(texteChunk)
                val blob = requireNotNull(converter.fromFloatArray(vecteur))

                embeddingDao.insertChunkWithEmbedding(
                    DocChunkEntity(docId = docId, chunkText = texteChunk),
                    blob
                )
                nbChunks++
            }
            Log.d(TAG, "$docId indexé")
        }

        val rapport = IndexReport(fichiers.size, nbChunks, System.currentTimeMillis() - debut)
        Log.i(TAG, "Indexation : ${rapport.documents} fiches, ${rapport.chunks} chunks, " +
                "${rapport.totalMs} ms (${rapport.msParChunk} ms/chunk)")
        rapport
    }

    private companion object {
        const val MANUALS_DIR = "manuels"
        const val TAG = "ManualIndexer"
    }
}