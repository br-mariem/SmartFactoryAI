package com.smartfactory.ai.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.smartfactory.ai.data.local.database.entity.MachineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MachineDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(machines: List<MachineEntity>)

    @Query("SELECT * FROM machines ORDER BY machine_id")
    fun observeAll(): Flow<List<MachineEntity>>

    @Query("UPDATE machines SET statut_courant = :statut WHERE machine_id = :machineId")
    suspend fun updateStatut(machineId: String, statut: String)

    @Query("SELECT COUNT(*) FROM machines")
    suspend fun count(): Int
}