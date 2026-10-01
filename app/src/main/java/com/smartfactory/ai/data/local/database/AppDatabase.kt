package com.smartfactory.ai.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.smartfactory.ai.data.local.database.converter.VectorConverter
import com.smartfactory.ai.data.local.database.dao.AuditDao
import com.smartfactory.ai.data.local.database.dao.MachineDao
import com.smartfactory.ai.data.local.database.dao.MaintenanceDocDao
import com.smartfactory.ai.data.local.database.dao.TelemetryDao
import com.smartfactory.ai.data.local.database.entity.AgentAuditEntity
import com.smartfactory.ai.data.local.database.entity.ChunkEmbeddingEntity
import com.smartfactory.ai.data.local.database.entity.DocChunkEntity
import com.smartfactory.ai.data.local.database.entity.FtsDocChunkEntity
import com.smartfactory.ai.data.local.database.entity.MachineEntity
import com.smartfactory.ai.data.local.database.entity.MaintenanceDocEntity
import com.smartfactory.ai.data.local.database.entity.SensorTelemetryEntity

@Database(
    entities = [
        // --- Tâche 03 (Personne A) ---
        MachineEntity::class,
        SensorTelemetryEntity::class,
        // --- Tâche 04 (Personne B) ---
        MaintenanceDocEntity::class,
        DocChunkEntity::class,
        ChunkEmbeddingEntity::class,
        FtsDocChunkEntity::class,
        AgentAuditEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(VectorConverter::class)
abstract class AppDatabase : RoomDatabase() {
    // --- Tâche 03 (Personne A) ---
    abstract fun machineDao(): MachineDao
    abstract fun telemetryDao(): TelemetryDao
    
    // --- Tâche 04 (Personne B) ---
    abstract fun maintenanceDocDao(): MaintenanceDocDao
    abstract fun auditDao(): AuditDao
}
