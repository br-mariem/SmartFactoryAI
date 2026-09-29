package com.smartfactory.ai.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "machines")
data class MachineEntity(
    @PrimaryKey
    @ColumnInfo(name = "machine_id") val machineId: String,
    @ColumnInfo(name = "nom") val nom: String,
    @ColumnInfo(name = "zone_usine") val zoneUsine: String,
    @ColumnInfo(name = "type_equipement") val typeEquipement: String,
    @ColumnInfo(name = "seuil_temp_critique") val seuilTempCritique: Double,
    @ColumnInfo(name = "seuil_vibration_max") val seuilVibrationMax: Double,
    @ColumnInfo(name = "statut_courant", defaultValue = "OK") val statutCourant: String = "OK"
)