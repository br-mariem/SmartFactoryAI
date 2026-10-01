package com.smartfactory.ai

import com.smartfactory.ai.ai.embedding.EmbeddingPooling
import com.smartfactory.ai.data.local.database.converter.VectorConverter
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.sqrt

/** Tests JVM (dossier "test") : tournent sur le PC, sans émulateur ni modèle. */
class EmbeddingPoolingTest {

    private val delta = 1e-5f

    @Test
    fun meanPool_faitLaMoyenneColonneParColonne() {
        // L'exemple "hello world" des explications : 4 tokens, 3 colonnes
        val tokens = arrayOf(
            floatArrayOf(0.10f, -0.32f, 0.05f),   // [CLS]
            floatArrayOf(0.44f, 0.12f, -0.20f),   // hello
            floatArrayOf(-0.08f, 0.51f, 0.17f),   // world
            floatArrayOf(0.02f, -0.11f, 0.30f)    // [SEP]
        )
        val moyenne = EmbeddingPooling.meanPool(tokens, longArrayOf(1, 1, 1, 1))
        assertArrayEquals(floatArrayOf(0.12f, 0.05f, 0.08f), moyenne, delta)
    }

    @Test
    fun meanPool_ignoreLeRemplissage() {
        val tokens = arrayOf(
            floatArrayOf(1f, 2f),
            floatArrayOf(3f, 4f),
            floatArrayOf(100f, 100f)              // remplissage : masque = 0
        )
        val moyenne = EmbeddingPooling.meanPool(tokens, longArrayOf(1, 1, 0))
        assertArrayEquals(floatArrayOf(2f, 3f), moyenne, delta)
    }

    @Test
    fun normalize_donneUneLongueurDe1() {
        val n = EmbeddingPooling.normalize(floatArrayOf(3f, 4f))
        assertArrayEquals(floatArrayOf(0.6f, 0.8f), n, delta)
        assertEquals(1f, sqrt(EmbeddingPooling.dot(n, n)), delta)
    }

    @Test
    fun normalize_vecteurNul_neDivisePasParZero() {
        assertArrayEquals(floatArrayOf(0f, 0f), EmbeddingPooling.normalize(floatArrayOf(0f, 0f)), delta)
    }

    @Test
    fun cosinus_memeDirection1_opposee_moins1() {
        val a = EmbeddingPooling.normalize(floatArrayOf(1f, 2f, 3f))
        val b = EmbeddingPooling.normalize(floatArrayOf(2f, 4f, 6f))   // même direction, 2x plus long
        val c = EmbeddingPooling.normalize(floatArrayOf(-1f, -2f, -3f))
        assertEquals(1f, EmbeddingPooling.dot(a, b), delta)
        assertEquals(-1f, EmbeddingPooling.dot(a, c), delta)
    }

    @Test
    fun vecteur384_allerRetourEnBlob_sansPerte() {
        val conv = VectorConverter()
        val v = EmbeddingPooling.normalize(FloatArray(384) { (it % 7) - 3f })
        val blob = conv.fromFloatArray(v)!!
        assertEquals(384 * 4, blob.size)                 // 1536 octets par vecteur
        assertArrayEquals(v, conv.toFloatArray(blob)!!, 0f)
    }
}
