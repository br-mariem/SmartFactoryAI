package com.smartfactory.ai.data.local.database.entity

import androidx.room.Entity
import androidx.room.Fts4

/**
 * Table Virtuelle "FTS" (Full-Text Search) de SQLite.
 * C'est le moteur de recherche Google interne. Il indexe automatiquement les mots
 * présents dans "DocChunkEntity" pour les retrouver en 2 millisecondes.
 */
@Fts4(contentEntity = DocChunkEntity::class)
@Entity(tableName = "fts_doc_chunks")
data class FtsDocChunkEntity(
    val chunkText: String
)
