package com.smartfactory.ai

import com.smartfactory.ai.ai.search.HybridRanker
import com.smartfactory.ai.ai.search.IndexedVector
import com.smartfactory.ai.ai.vector.RrfFusion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Tests JVM (dossier "test") : tournent sur le PC, sans émulateur. */
class HybridRankerTest {

    // ---------- Mots-clés ----------

    @Test
    fun extractTerms_retireLesMotsVidesEtLaPonctuation() {
        assertEquals(
            listOf("pompe", "surchauffe"),
            HybridRanker.extractTerms("Que faire si la pompe surchauffe ?")
        )
    }

    @Test
    fun extractTerms_gardeLesNombresEtLesAccents() {
        assertEquals(
            listOf("température", "dépasse", "85"),
            HybridRanker.extractTerms("La température dépasse 85 °C")
        )
    }

    @Test
    fun buildFtsQuery_relieLesMotsParOR() {
        assertEquals("\"pompe\" OR \"surchauffe\"", HybridRanker.buildFtsQuery(listOf("pompe", "surchauffe")))
        assertNull(HybridRanker.buildFtsQuery(emptyList()))
    }

    @Test
    fun rankByKeywords_classeParNombreDeMotsDifferentsPuisParOccurrences() {
        val candidats = listOf(
            1L to "La pompe tourne. La pompe vibre. La pompe fuit.",      // 1 mot différent, 3 occurrences
            2L to "Surchauffe de la pompe : température trop haute.",     // 3 mots différents
            3L to "Le compresseur est en surchauffe.",                    // 1 mot différent, 1 occurrence
            4L to "Rien à voir avec la question."                         // aucun mot
        )
        val classement = HybridRanker.rankByKeywords(
            listOf("pompe", "surchauffe", "température"), candidats, topK = 10
        )
        assertEquals(listOf(2L, 1L, 3L), classement)
    }

    @Test
    fun rankByKeywords_ignoreLesAccentsEtLesMajuscules() {
        val classement = HybridRanker.rankByKeywords(
            listOf("temperature"), listOf(7L to "TEMPÉRATURE élevée"), topK = 10
        )
        assertEquals(listOf(7L), classement)
    }

    // ---------- Vecteurs ----------

    @Test
    fun rankByVector_metLePlusProcheEnPremier() {
        val question = floatArrayOf(1f, 0f)
        val vecteurs = listOf(
            IndexedVector(10L, floatArrayOf(0f, 1f)),       // perpendiculaire : cosinus 0
            IndexedVector(20L, floatArrayOf(1f, 0f)),       // même direction : cosinus 1
            IndexedVector(30L, floatArrayOf(0.6f, 0.8f)),   // cosinus 0,6
            IndexedVector(40L, floatArrayOf(-1f, 0f))       // direction opposée : cosinus -1
        )
        val top = HybridRanker.rankByVector(question, vecteurs, topK = 3)

        assertEquals(listOf(20L, 30L, 10L), top.map { it.chunkId })
        assertEquals(1f, top[0].similarity, 1e-5f)
        assertEquals(0.6f, top[1].similarity, 1e-5f)
    }

    // ---------- Fusion ----------

    @Test
    fun fusion_unChunkPresentDansLesDeuxListesPasseDevant() {
        val parMotsCles = listOf(10L, 20L, 30L)
        val parVecteur = listOf(40L, 20L, 50L)
        val top = RrfFusion.fuse(listOf(parMotsCles, parVecteur), k = 60, topN = 3)

        // 20 n'est premier nulle part, mais il est dans les deux listes : il gagne
        assertEquals(20L, top[0].chunkId)
        assertEquals(1.0 / 62 + 1.0 / 62, top[0].score, 1e-12)
    }
}
