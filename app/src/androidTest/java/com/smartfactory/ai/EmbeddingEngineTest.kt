package com.smartfactory.ai

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.smartfactory.ai.ai.embedding.EmbeddingPooling
import com.smartfactory.ai.ai.embedding.ManualIndexer
import com.smartfactory.ai.ai.embedding.OnnxEmbeddingEngine
import com.smartfactory.ai.data.local.database.AppDatabase
import com.smartfactory.ai.data.local.database.converter.VectorConverter
import com.smartfactory.ai.data.local.parser.MaintenanceDocParser
import com.smartfactory.ai.data.local.parser.TextChunker
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.sqrt

/**
 * Tests instrumentés (dossier "androidTest") : ils ont besoin de l'émulateur,
 * car le modèle ONNX et les assets ne fonctionnent que sur Android.
 */
@RunWith(AndroidJUnit4::class)
class EmbeddingEngineTest {

    companion object {
        private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        private val engine = OnnxEmbeddingEngine(context)   // chargé une fois pour tous les tests

        @JvmStatic @AfterClass
        fun fermer() = engine.close()
    }

    @Test
    fun unTexte_donne384NombresDeLongueur1() {
        val v = engine.embed("hello world")
        assertEquals(384, v.size)
        assertEquals(1f, sqrt(EmbeddingPooling.dot(v, v)), 1e-3f)
    }

    @Test
    fun phrasesProches_plusSimilairesQuePhrasesSansRapport() {
        val surchauffe = engine.embed("pump overheating, temperature too high")
        val proche = engine.embed("the pump is too hot")
        val sansRapport = engine.embed("the weather is sunny at the beach")

        val simProche = EmbeddingPooling.dot(surchauffe, proche)
        val simLoin = EmbeddingPooling.dot(surchauffe, sansRapport)
        println("similarité proche = $simProche, sans rapport = $simLoin")
        assertTrue("$simProche doit être > $simLoin", simProche > simLoin)
    }

    @Test
    fun indexation_des10Fiches_enBaseEnMemoire() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val dao = db.embeddingDao()
            val indexer = ManualIndexer(context, engine, MaintenanceDocParser(), TextChunker(), dao)

            val rapport = indexer.indexIfNeeded()
            println("Indexation : $rapport")

            assertEquals(10, rapport.documents)
            assertEquals(50, rapport.chunks)
            assertEquals(dao.countChunks(), dao.countEmbeddings())   // 1 vecteur par chunk

            val premier = dao.getAllEmbeddings().first()
            val vecteur = VectorConverter().toFloatArray(premier.embeddingBlob)!!
            assertEquals(384, vecteur.size)

            // 2e appel : l'index existe déjà, rien n'est recalculé
            assertTrue(indexer.indexIfNeeded().skipped)
        } finally {
            db.close()
        }
    }
}
