package com.smartfactory.ai.data.local.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.smartfactory.ai.data.local.database.entity.DocChunkEntity

@Dao
interface MaintenanceDocDao {
    /**
     * Recherche instantanée de mots-clés dans les manuels 
     * en utilisant la magie de la table virtuelle FTS.
     */
    @Query("""
        SELECT doc_chunks.* FROM doc_chunks 
        JOIN fts_doc_chunks ON doc_chunks.chunkId = fts_doc_chunks.rowid 
        WHERE fts_doc_chunks MATCH :query
    """)
    suspend fun searchFts(query: String): List<DocChunkEntity>
}
