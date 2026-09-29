package com.smartfactory.ai.ai.vector

/** Un résultat final de la recherche hybride. */
data class ScoredChunk(val chunkId: Long, val score: Double)

object RrfFusion {

    /**
     * Reciprocal Rank Fusion : combine plusieurs classements (FTS5, vectoriel...).
     * Chaque classement = liste d'identifiants de chunks, du meilleur au moins bon.
     * score(chunk) = somme sur chaque classement de 1 / (k + rang), rang commençant à 1.
     */
    fun fuse(classements: List<List<Long>>, k: Int = 60, topN: Int = 3): List<ScoredChunk> {
        val scores = HashMap<Long, Double>()
        for (classement in classements) {
            classement.forEachIndexed { index, chunkId ->
                val rang = index + 1
                scores[chunkId] = (scores[chunkId] ?: 0.0) + 1.0 / (k + rang)
            }
        }
        return scores.entries
            .sortedByDescending { it.value }
            .take(topN)
            .map { ScoredChunk(it.key, it.value) }
    }
}