package com.smartfactory.ai

import android.util.Log
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.smartfactory.ai.data.local.database.AppDatabase
import com.smartfactory.ai.data.local.database.MachineSeed
import com.smartfactory.ai.data.local.database.entity.SensorTelemetryEntity
import com.smartfactory.ai.data.local.parser.CsvSensorParser
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TelemetryPerformanceTest {

    private lateinit var db: AppDatabase
    private lateinit var mesures: List<SensorTelemetryEntity>

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        mesures = context.assets.open("data/ai4i2020.csv").use {
            CsvSensorParser().parse(it, startTimestamp = 1_000_000L)
        }
        runBlocking { db.machineDao().insertAll(MachineSeed.machines) }
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun insertion10000Mesures_moinsDe1Seconde() = runBlocking {
        assertEquals(10_000, mesures.size)

        val debut = System.nanoTime()
        db.telemetryDao().insertBatch(mesures)
        val dureeMs = (System.nanoTime() - debut) / 1_000_000

        Log.i("PERF", "Insertion de 10 000 mesures : $dureeMs ms")
        assertEquals(10_000, db.telemetryDao().count())
        assertTrue("Trop lent : $dureeMs ms", dureeMs < 1000)
    }

    @Test
    fun requete60Secondes_moinsDe5Ms() = runBlocking {
        db.telemetryDao().insertBatch(mesures)
        val now = mesures.last().timestamp

        db.telemetryDao().getLatest60Seconds("PUMP_A1", now) // échauffement (1re exécution plus lente)

        val debut = System.nanoTime()
        val resultat = db.telemetryDao().getLatest60Seconds("PUMP_A1", now)
        val dureeMs = (System.nanoTime() - debut) / 1_000_000.0

        Log.i("PERF", "Requête 60 s : ${resultat.size} lignes en $dureeMs ms")
        assertTrue("Aucune mesure trouvée", resultat.isNotEmpty())
        assertTrue("Trop lent : $dureeMs ms", dureeMs < 5.0)
    }
}