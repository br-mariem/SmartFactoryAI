package com.smartfactory.ai.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.smartfactory.ai.data.local.database.entity.SensorTelemetryEntity

@Dao
interface TelemetryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(telemetryList: List<SensorTelemetryEntity>)

    @Query("SELECT COUNT(*) FROM sensor_telemetry")
    suspend fun count(): Int

    @Query("SELECT * FROM sensor_telemetry WHERE machine_id = :machineId AND timestamp >= :currentTimestamp - 60000 ORDER BY timestamp DESC")
    suspend fun getLatest60Seconds(
        machineId: String,
        currentTimestamp: Long = System.currentTimeMillis(),
    ): List<SensorTelemetryEntity>

    @Query("SELECT * FROM sensor_telemetry WHERE is_anomalie = 1 ORDER BY timestamp DESC")
    suspend fun getAnomalies(): List<SensorTelemetryEntity>
}

