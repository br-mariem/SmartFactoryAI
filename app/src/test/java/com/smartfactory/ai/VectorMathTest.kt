package com.smartfactory.ai

import com.smartfactory.ai.ai.vector.RrfFusion
import com.smartfactory.ai.ai.vector.VectorMath
import org.junit.Assert.assertEquals
import org.junit.Test

class VectorMathTest {

    private val delta = 1e-5f

    @Test
    fun cosinus_valeursDeReference() {
        val a = floatArrayOf(1f, 2f, 3f)
        assertEquals(1f, VectorMath.cosineSimilarity(a, floatArrayOf(2f, 4f, 6f)), delta)      // même sens
        assertEquals(-1f, VectorMath.cosineSimilarity(a, floatArrayOf(-1f, -2f, -3f)), delta)  // opposé
        assertEquals(0f, VectorMath.cosineSimilarity(floatArrayOf(1f, 0f), floatArrayOf(0f, 1f)), delta) // sans rapport
    }

    @Test
    fun normalisation_donneUneLongueurDe1() {
        val n = VectorMath.l2Normalize(floatArrayOf(3f, 4f))
        assertEquals(0.6f, n[0], delta)
        assertEquals(0.8f, n[1], delta)
        assertEquals(1f, VectorMath.norm(n), delta)
    }

    @Test
    fun vecteursNormalises_produitScalaireEgalCosinus() {
        val a = floatArrayOf(1f, 2f, 3f)
        val b = floatArrayOf(4f, -1f, 2f)
        val viaDot = VectorMath.dot(VectorMath.l2Normalize(a), VectorMath.l2Normalize(b))
        assertEquals(VectorMath.cosineSimilarity(a, b), viaDot, delta)
    }

    @Test
    fun meanPooling_ignoreLesTokensDeRemplissage() {
        // 3 tokens de dimension 2 ; le 3e est du remplissage (mask = 0)
        val embeddings = floatArrayOf(1f, 2f, 3f, 4f, 100f, 100f)
        val resultat = VectorMath.meanPooling(embeddings, longArrayOf(1, 1, 0), dim = 2)
        assertEquals(2f, resultat[0], delta)   // (1 + 3) / 2
        assertEquals(3f, resultat[1], delta)   // (2 + 4) / 2
    }

    @Test
    fun rrf_favoriseLesChunksBienClassesDansLesDeuxListes() {
        val fts = listOf(10L, 20L, 30L)
        val vecteur = listOf(20L, 40L, 10L)
        val top = RrfFusion.fuse(listOf(fts, vecteur), k = 60, topN = 3)

        assertEquals(listOf(20L, 10L, 40L), top.map { it.chunkId })
        assertEquals(1.0 / 62 + 1.0 / 61, top[0].score, 1e-12)
    }
}