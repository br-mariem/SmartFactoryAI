package com.smartfactory.ai.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.smartfactory.ai.data.local.database.entity.MachineEntity

@Dao
interface MachineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(machines: List<MachineEntity>)

    @Query("SELECT * FROM machines")
    suspend fun getAllMachines(): List<MachineEntity>

    @Query("SELECT * FROM machines WHERE machine_id = :machineId")
    suspend fun getMachineById(machineId: String): MachineEntity?
}

