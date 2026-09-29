package com.smartfactory.ai.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.smartfactory.ai.data.local.database.entity.SensorTelemetryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TelemetryDao {

    // Room enveloppe automatiquement une liste dans UNE seule transaction
    @Insert
    suspend fun insertBatch(mesures: List<SensorTelemetryEntity>)

    @Insert
    suspend fun insert(mesure: SensorTelemetryEntity)

    // Mesures des 60 dernières secondes, de la plus ancienne à la plus récente (pour les courbes)
    @Query(
        """
        SELECT * FROM sensor_telemetry
        WHERE machine_id = :machineId AND timestamp >= :now - 60000
        ORDER BY timestamp ASC
        """
    )
    suspend fun getLatest60Seconds(machineId: String, now: Long): List<SensorTelemetryEntity>

    // Les N dernières mesures en temps réel (pour le Dashboard de B)
    @Query(
        """
        SELECT * FROM sensor_telemetry
        WHERE machine_id = :machineId
        ORDER BY timestamp DESC
        LIMIT :limit
        """
    )
    fun observeLatest(machineId: String, limit: Int): Flow<List<SensorTelemetryEntity>>

    @Query("SELECT * FROM sensor_telemetry WHERE is_anomalie = 1 ORDER BY timestamp DESC")
    fun getAnomalies(): Flow<List<SensorTelemetryEntity>>

    @Query("SELECT COUNT(*) FROM sensor_telemetry")
    suspend fun count(): Int
}