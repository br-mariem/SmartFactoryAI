package com.smartfactory.ai.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Représente le "Vecteur mathématique" généré par l'IA (ONNX) pour un paragraphe précis.
 * C'est stocké en ByteArray (BLOB) pour optimiser la mémoire.
 */
@Entity(tableName = "chunk_embeddings")
data class ChunkEmbeddingEntity(
    @PrimaryKey val chunkId: Int,
    val embeddingBlob: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ChunkEmbeddingEntity
        if (chunkId != other.chunkId) return false
        return embeddingBlob.contentEquals(other.embeddingBlob)
    }

    override fun hashCode(): Int {
        var result = chunkId
        result = 31 * result + embeddingBlob.contentHashCode()
        return result
    }
}
