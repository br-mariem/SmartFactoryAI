package com.smartfactory.ai.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.smartfactory.ai.data.local.database.entity.ChunkEmbeddingEntity
import com.smartfactory.ai.data.local.database.entity.DocChunkEntity

/**
 * Écriture des chunks et de leurs vecteurs (T05), et lecture pour la recherche (T07).
 *
 * La table FTS4 "fts_doc_chunks" est déclarée avec contentEntity = DocChunkEntity :
 * Room crée des triggers SQLite qui la remplissent AUTOMATIQUEMENT à chaque insertion
 * dans doc_chunks. Il n'y a donc rien à insérer à la main dans la table FTS.
 */
@Dao
interface EmbeddingDao {

    @Insert
    suspend fun insertChunk(chunk: DocChunkEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmbedding(embedding: ChunkEmbeddingEntity)

    /** Chunk + vecteur dans la même transaction : jamais de chunk sans vecteur. */
    @Transaction
    suspend fun insertChunkWithEmbedding(chunk: DocChunkEntity, vectorBlob: ByteArray): Int {
        val id = insertChunk(chunk).toInt()
        insertEmbedding(ChunkEmbeddingEntity(chunkId = id, embeddingBlob = vectorBlob))
        return id
    }

    @Query("SELECT COUNT(*) FROM doc_chunks")
    suspend fun countChunks(): Int

    @Query("SELECT COUNT(*) FROM chunk_embeddings")
    suspend fun countEmbeddings(): Int

    @Query("SELECT * FROM doc_chunks WHERE chunkId = :chunkId")
    suspend fun getChunk(chunkId: Int): DocChunkEntity?

    /** Pour T07 (recherche vectorielle) : tous les vecteurs, à comparer avec la question. */
    @Query("SELECT * FROM chunk_embeddings")
    suspend fun getAllEmbeddings(): List<ChunkEmbeddingEntity>

    /**
     * T07 : recherche par mots-clés dans l'index FTS.
     * @param ftsQuery requête FTS, par exemple : "pompe" OR "surchauffe"
     */
    @Query(
        "SELECT doc_chunks.* FROM doc_chunks " +
                "JOIN fts_doc_chunks ON doc_chunks.chunkId = fts_doc_chunks.rowid " +
                "WHERE fts_doc_chunks MATCH :ftsQuery LIMIT :limit"
    )
    suspend fun searchByKeywords(ftsQuery: String, limit: Int = 50): List<DocChunkEntity>

    /** T07 : le texte des chunks retenus par la fusion. */
    @Query("SELECT * FROM doc_chunks WHERE chunkId IN (:chunkIds)")
    suspend fun getChunksByIds(chunkIds: List<Int>): List<DocChunkEntity>

    @Query("DELETE FROM chunk_embeddings")
    suspend fun deleteAllEmbeddings()

    @Query("DELETE FROM doc_chunks")
    suspend fun deleteAllChunks()

    /** Pour ré-indexer depuis zéro (par exemple après avoir modifié une fiche). */
    @Transaction
    suspend fun clearIndex() {
        deleteAllEmbeddings()
        deleteAllChunks()
    }
}