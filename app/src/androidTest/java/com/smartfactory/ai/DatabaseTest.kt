package com.smartfactory.ai

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.smartfactory.ai.data.local.database.AppDatabase
import com.smartfactory.ai.data.local.database.entity.DocChunkEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun testFtsSearch() = runBlocking {
        // Obtenir le DAO en utilisant les fonctions qui étaient marquées "never used"
        val dao = db.maintenanceDocDao()
        
        // Comme la base est vide pour l'instant, on vérifie juste que l'appel ne plante pas
        val result = dao.searchFts("Huile")
        
        assertEquals(0, result.size)
    }
}
