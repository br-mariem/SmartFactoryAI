package com.smartfactory.ai.data.local.parser

import com.smartfactory.ai.data.local.database.MachineSeed
import com.smartfactory.ai.data.local.database.entity.SensorTelemetryEntity
import java.io.InputStream
import javax.inject.Inject

class CsvSensorParser @Inject constructor() {

    /**
     * Lit le fichier ai4i2020.csv et le convertit en mesures de télémétrie.
     * @param startTimestamp horodatage de la première mesure (ms)
     * @param intervalMs écart entre deux lignes (100 ms → chaque machine a 1 mesure/seconde)
     */
    fun parse(
        input: InputStream,
        startTimestamp: Long,
        intervalMs: Long = 100L
    ): List<SensorTelemetryEntity> {
        val machines = MachineSeed.machines
        val result = ArrayList<SensorTelemetryEntity>(10_000)

        input.bufferedReader().useLines { lines ->
            lines.drop(1) // on saute la ligne d'en-tête
                .forEachIndexed { index, line ->
                    val c = line.split(',')
                    if (c.size < 9) return@forEachIndexed // ligne vide ou malformée

                    val udi = c[0].trim().toIntOrNull() ?: return@forEachIndexed
                    val processTempK = c[4].trim().toDoubleOrNull() ?: return@forEachIndexed
                    val rpm = c[5].trim().toDoubleOrNull() ?: return@forEachIndexed
                    val torque = c[6].trim().toDoubleOrNull() ?: return@forEachIndexed
                    val toolWear = c[7].trim().toDoubleOrNull() ?: return@forEachIndexed

                    result += SensorTelemetryEntity(
                        machineId = machines[(udi - 1) % machines.size].machineId,
                        timestamp = startTimestamp + index * intervalMs,
                        temperature = processTempK - 273.15,
                        vibration = torque / 10.0,
                        pression = 5.0 + toolWear / 100.0,
                        vitesseRotation = rpm,
                        isAnomalie = c[8].trim() == "1"
                    )
                }
        }
        return result
    }
}