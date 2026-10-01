package com.smartfactory.ai.ai.search

import com.smartfactory.ai.ai.vector.VectorMath
import java.text.Normalizer

/** Un vecteur de chunk gardé en mémoire pour la recherche. */
class IndexedVector(val chunkId: Long, val vector: FloatArray)

/** Un résultat de la recherche vectorielle : le chunk et sa similarité avec la question. */
data class VectorHit(val chunkId: Long, val similarity: Float)

/**
 * Les calculs de classement de la recherche hybride.
 * Code Kotlin pur (ni Android, ni Room, ni ONNX) : testable sur le PC.
 */
object HybridRanker {

    /** Mots trop courants pour aider la recherche. */
    private val MOTS_VIDES = setOf(
        "les", "des", "une", "aux", "est", "sont", "pour", "par", "sur", "dans", "que", "qui",
        "quoi", "avec", "sans", "pas", "plus", "son", "ses", "leur", "leurs", "cette", "ces",
        "elle", "ils", "elles", "nous", "vous", "mon", "mes", "ton", "tes", "comment", "quand",
        "quel", "quelle", "quels", "quelles", "faire", "fait", "dois", "doit", "faut", "peut",
        "être", "avoir", "tout", "tous", "très", "trop", "the", "and", "what", "how"
    )

    private val SEPARATEURS = Regex("[^\\p{L}\\p{N}]+")
    private const val MAX_TERMES = 12

    /**
     * Question -> mots-clés utiles.
     * "Que faire si la pompe surchauffe ?" -> [pompe, surchauffe]
     */
    fun extractTerms(question: String): List<String> =
        question.lowercase()
            .split(SEPARATEURS)
            .filter { mot ->
                val estNombre = mot.all { it.isDigit() }
                (if (estNombre) mot.length >= 2 else mot.length >= 3) && mot !in MOTS_VIDES
            }
            .distinct()
            .take(MAX_TERMES)

    /**
     * Mots-clés -> requête FTS : "pompe" OR "surchauffe".
     * Les guillemets évitent qu'un mot soit pris pour un opérateur FTS.
     * @return null s'il n'y a aucun mot-clé utile.
     */
    fun buildFtsQuery(terms: List<String>): String? =
        if (terms.isEmpty()) null else terms.joinToString(" OR ") { "\"$it\"" }

    /**
     * Recherche par le SENS : similarité cosinus entre la question et chaque chunk.
     * Les vecteurs sont déjà normalisés (longueur 1), donc cosinus = produit scalaire.
     */
    fun rankByVector(query: FloatArray, vectors: List<IndexedVector>, topK: Int): List<VectorHit> =
        vectors
            .map { VectorHit(it.chunkId, VectorMath.dot(query, it.vector)) }
            .sortedByDescending { it.similarity }
            .take(topK)

    /**
     * Recherche par MOTS-CLÉS : classe les chunks trouvés par FTS.
     * FTS4 ne donne pas de score de pertinence (bm25 n'existe qu'en FTS5), on classe donc ici :
     *   1. d'abord le nombre de mots-clés DIFFÉRENTS présents dans le chunk,
     *   2. puis le nombre total d'occurrences.
     * @param candidates paires (chunkId, texte du chunk)
     * @return les chunkId, du meilleur au moins bon
     */
    fun rankByKeywords(terms: List<String>, candidates: List<Pair<Long, String>>, topK: Int): List<Long> {
        if (terms.isEmpty()) return emptyList()
        val termes = terms.map { sansAccents(it) }.toSet()

        class Note(val chunkId: Long, val distincts: Int, val occurrences: Int)

        return candidates
            .map { (chunkId, texte) ->
                val compte = HashMap<String, Int>()
                for (mot in sansAccents(texte.lowercase()).split(SEPARATEURS)) {
                    if (mot in termes) compte[mot] = (compte[mot] ?: 0) + 1
                }
                Note(chunkId, compte.size, compte.values.sum())
            }
            .filter { it.distincts > 0 }
            .sortedWith(
                compareByDescending<Note> { it.distincts }
                    .thenByDescending { it.occurrences }
                    .thenBy { it.chunkId }
            )
            .take(topK)
            .map { it.chunkId }
    }

    private fun sansAccents(texte: String): String =
        Normalizer.normalize(texte, Normalizer.Form.NFD)
            .filter { Character.getType(it) != Character.NON_SPACING_MARK.toInt() }
}