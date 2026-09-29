package com.smartfactory.ai.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sensor_telemetry",
    foreignKeys = [
        ForeignKey(
            entity = MachineEntity::class,
            parentColumns = ["machine_id"],
            childColumns = ["machine_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            value = ["machine_id", "timestamp"],
            orders = [Index.Order.ASC, Index.Order.DESC],
            name = "idx_telemetry_machine_time"
        )
    ]
)
data class SensorTelemetryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "machine_id") val machineId: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long,          // millisecondes Unix
    @ColumnInfo(name = "temperature") val temperature: Double,    // °C
    @ColumnInfo(name = "vibration") val vibration: Double,        // mm/s
    @ColumnInfo(name = "pression") val pression: Double,          // bar
    @ColumnInfo(name = "vitesse_rotation") val vitesseRotation: Double, // RPM
    @ColumnInfo(name = "is_anomalie", defaultValue = "0") val isAnomalie: Boolean = false
)