package com.smartfactory.ai.ai.embedding

import kotlin.math.sqrt

/**
 * Les deux calculs qui suivent le modèle ONNX :
 * 1. meanPool  : un vecteur par token  ->  un seul vecteur pour le chunk (moyenne colonne par colonne)
 * 2. normalize : ramène ce vecteur à une longueur de 1 (seule la direction compte)
 */
object EmbeddingPooling {

    /**
     * @param tokenVectors une ligne par token, chacune de 384 nombres  ([seq][384])
     * @param attentionMask 1 = vrai token, 0 = remplissage (ignoré dans la moyenne)
     */
    fun meanPool(tokenVectors: Array<FloatArray>, attentionMask: LongArray): FloatArray {
        require(tokenVectors.isNotEmpty()) { "Aucun token" }
        require(tokenVectors.size == attentionMask.size) {
            "Tokens (${tokenVectors.size}) et masque (${attentionMask.size}) de tailles différentes"
        }
        val dim = tokenVectors[0].size
        val somme = FloatArray(dim)
        var nbTokens = 0

        for (t in tokenVectors.indices) {
            if (attentionMask[t] == 0L) continue      // on saute le remplissage
            val v = tokenVectors[t]
            for (d in 0 until dim) somme[d] += v[d]   // addition colonne par colonne
            nbTokens++
        }
        val diviseur = maxOf(nbTokens, 1).toFloat()   // jamais de division par 0
        for (d in 0 until dim) somme[d] /= diviseur
        return somme
    }

    /** Divise chaque nombre par la longueur du vecteur : la nouvelle longueur vaut 1. */
    fun normalize(vector: FloatArray): FloatArray {
        var sommeCarres = 0.0
        for (x in vector) sommeCarres += x * x
        val longueur = sqrt(sommeCarres).toFloat()
        if (longueur < 1e-12f) return vector.copyOf()
        return FloatArray(vector.size) { vector[it] / longueur }
    }

    /** Similarité cosinus de deux vecteurs DÉJÀ normalisés = simple produit scalaire. */
    fun dot(a: FloatArray, b: FloatArray): Float {
        require(a.size == b.size) { "Dimensions différentes : ${a.size} vs ${b.size}" }
        var s = 0f
        for (i in a.indices) s += a[i] * b[i]
        return s
    }
}