package com.smartfactory.ai.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.smartfactory.ai.data.local.database.dao.MachineDao
import com.smartfactory.ai.data.local.database.dao.TelemetryDao
import com.smartfactory.ai.data.local.database.entity.MachineEntity
import com.smartfactory.ai.data.local.database.entity.SensorTelemetryEntity

@Database(
    entities = [
        MachineEntity::class,
        SensorTelemetryEntity::class,
        // Personne B ajoutera ici : MaintenanceDocEntity, DocChunkEntity, ...
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun machineDao(): MachineDao
    abstract fun telemetryDao(): TelemetryDao
}