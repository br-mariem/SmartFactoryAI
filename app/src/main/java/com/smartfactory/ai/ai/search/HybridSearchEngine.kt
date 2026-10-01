package com.smartfactory.ai.ai.search

import android.util.Log
import com.smartfactory.ai.ai.embedding.OnnxEmbeddingEngine
import com.smartfactory.ai.ai.vector.RrfFusion
import com.smartfactory.ai.data.local.database.converter.VectorConverter
import com.smartfactory.ai.data.local.database.dao.EmbeddingDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** Un extrait de manuel renvoyé par la recherche. */
data class SearchResult(
    val chunkId: Int,
    val docId: String,          // ex. "sop_001"
    val chunkText: String,
    val score: Double,          // score RRF
    val rangMotsCles: Int?,     // rang dans la recherche FTS (null = non trouvé par mots-clés)
    val rangVecteur: Int?,      // rang dans la recherche vectorielle
    val similarite: Float?      // cosinus avec la question (entre -1 et 1)
)

data class SearchResponse(
    val results: List<SearchResult>,
    val totalMs: Long,
    val embeddingMs: Long       // dont le temps passé dans le modèle ONNX
)

/**
 * Recherche hybride (T07) :
 *   question -> [mots-clés FTS]     -> classement 1
 *            -> [vecteur + cosinus] -> classement 2
 *            -> fusion RRF -> top 3
 */
@Singleton
class HybridSearchEngine @Inject constructor(
    private val engine: OnnxEmbeddingEngine,
    private val embeddingDao: EmbeddingDao
) {
    private val converter = VectorConverter()

    // Les vecteurs sont gardés en mémoire après la 1re recherche :
    // 50 chunks x 384 nombres = 77 Ko, bien plus rapide que de relire la base à chaque question.
    @Volatile
    private var cache: List<IndexedVector>? = null

    /** À appeler après une ré-indexation, pour recharger les vecteurs. */
    fun invalidateCache() {
        cache = null
    }

    suspend fun search(question: String, topN: Int = 3): SearchResponse = withContext(Dispatchers.Default) {
        val debut = System.nanoTime()

        // 1. Recherche par le sens
        val vecteurs = loadVectors()
        val debutEmbedding = System.nanoTime()
        val vecteurQuestion = engine.embed(question)
        val embeddingMs = (System.nanoTime() - debutEmbedding) / 1_000_000
        val parVecteur = HybridRanker.rankByVector(vecteurQuestion, vecteurs, CANDIDATS)

        // 2. Recherche par mots-clés
        val termes = HybridRanker.extractTerms(question)
        val requeteFts = HybridRanker.buildFtsQuery(termes)
        val parMotsCles: List<Long> = if (requeteFts == null) emptyList() else {
            val trouves = embeddingDao.searchByKeywords(requeteFts)
            HybridRanker.rankByKeywords(termes, trouves.map { it.chunkId.toLong() to it.chunkText }, CANDIDATS)
        }

        // 3. Fusion des deux classements
        val fusion = RrfFusion.fuse(listOf(parMotsCles, parVecteur.map { it.chunkId }), k = RRF_K, topN = topN)

        // 4. On récupère le texte des chunks gagnants
        val chunks = embeddingDao.getChunksByIds(fusion.map { it.chunkId.toInt() }).associateBy { it.chunkId }
        val resultats = fusion.mapNotNull { gagnant ->
            val chunk = chunks[gagnant.chunkId.toInt()] ?: return@mapNotNull null
            val rangVecteur = parVecteur.indexOfFirst { it.chunkId == gagnant.chunkId }
            val rangMotsCles = parMotsCles.indexOf(gagnant.chunkId)
            SearchResult(
                chunkId = chunk.chunkId,
                docId = chunk.docId,
                chunkText = chunk.chunkText,
                score = gagnant.score,
                rangMotsCles = if (rangMotsCles >= 0) rangMotsCles + 1 else null,
                rangVecteur = if (rangVecteur >= 0) rangVecteur + 1 else null,
                similarite = parVecteur.getOrNull(rangVecteur)?.similarity
            )
        }

        val totalMs = (System.nanoTime() - debut) / 1_000_000
        Log.i(TAG, "\"$question\" -> ${resultats.map { it.docId }} en $totalMs ms (ONNX : $embeddingMs ms)")
        SearchResponse(resultats, totalMs, embeddingMs)
    }

    private suspend fun loadVectors(): List<IndexedVector> {
        cache?.let { return it }
        val charges = embeddingDao.getAllEmbeddings().mapNotNull { ligne ->
            converter.toFloatArray(ligne.embeddingBlob)?.let { IndexedVector(ligne.chunkId.toLong(), it) }
        }
        if (charges.isNotEmpty()) cache = charges   // on ne met pas en cache un index encore vide
        return charges
    }

    private companion object {
        const val CANDIDATS = 10    // on garde les 10 meilleurs de chaque recherche avant la fusion
        const val RRF_K = 60
        const val TAG = "HybridSearch"
    }
}