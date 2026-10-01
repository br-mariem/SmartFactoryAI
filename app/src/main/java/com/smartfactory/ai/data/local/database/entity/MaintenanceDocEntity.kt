package com.smartfactory.ai.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Représente un document de maintenance complet (ex: Manuel de la Pompe A).
 */
@Entity(tableName = "maintenance_docs")
data class MaintenanceDocEntity(
    @PrimaryKey val docId: String,
    val title: String,
    val machineType: String
)
