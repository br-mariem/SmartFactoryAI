package com.smartfactory.ai.ai.vector

import kotlin.math.sqrt

object VectorMath {

    /** Produit scalaire : somme des a[i] * b[i]. */
    fun dot(a: FloatArray, b: FloatArray): Float {
        require(a.size == b.size) { "Tailles différentes : ${a.size} vs ${b.size}" }
        var somme = 0f
        for (i in a.indices) somme += a[i] * b[i]
        return somme
    }

    /** Norme euclidienne (longueur du vecteur). */
    fun norm(v: FloatArray): Float = sqrt(dot(v, v))

    /** Renvoie une copie du vecteur de longueur 1 (inchangé si nul). */
    fun l2Normalize(v: FloatArray): FloatArray {
        val n = norm(v)
        if (n == 0f) return v.copyOf()
        return FloatArray(v.size) { v[it] / n }
    }

    /** Similarité cosinus : 1 = même sens, 0 = sans rapport, -1 = opposé. */
    fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        val na = norm(a)
        val nb = norm(b)
        if (na == 0f || nb == 0f) return 0f
        return dot(a, b) / (na * nb)
    }

    /**
     * Mean pooling : le modèle renvoie un vecteur PAR TOKEN (seqLen x dim, à plat).
     * On fait la moyenne des vecteurs des tokens réels (attentionMask = 1)
     * pour obtenir UN vecteur pour toute la phrase.
     */
    fun meanPooling(tokenEmbeddings: FloatArray, attentionMask: LongArray, dim: Int): FloatArray {
        val seqLen = attentionMask.size
        require(tokenEmbeddings.size == seqLen * dim) { "Taille incohérente : ${tokenEmbeddings.size} != $seqLen x $dim" }
        val resultat = FloatArray(dim)
        var compte = 0
        for (t in 0 until seqLen) {
            if (attentionMask[t] == 0L) continue
            compte++
            val base = t * dim
            for (d in 0 until dim) resultat[d] += tokenEmbeddings[base + d]
        }
        if (compte > 0) for (d in 0 until dim) resultat[d] /= compte
        return resultat
    }
}