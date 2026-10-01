package com.smartfactory.ai.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Représente un petit paragraphe (chunk) découpé d'un manuel de maintenance.
 */
@Entity(tableName = "doc_chunks")
data class DocChunkEntity(
    @PrimaryKey(autoGenerate = true) val chunkId: Int = 0,
    val docId: String,
    val chunkText: String
)
