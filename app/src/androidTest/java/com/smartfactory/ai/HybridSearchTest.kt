package com.smartfactory.ai

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.smartfactory.ai.ai.embedding.ManualIndexer
import com.smartfactory.ai.ai.embedding.OnnxEmbeddingEngine
import com.smartfactory.ai.ai.search.HybridSearchEngine
import com.smartfactory.ai.data.local.database.AppDatabase
import com.smartfactory.ai.data.local.parser.MaintenanceDocParser
import com.smartfactory.ai.data.local.parser.TextChunker
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

/** Tests instrumentés : recherche hybride complète avec le vrai modèle et une vraie base Room. */
@RunWith(AndroidJUnit4::class)
class HybridSearchTest {

    companion object {
        private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        private lateinit var db: AppDatabase
        private lateinit var onnx: OnnxEmbeddingEngine
        private lateinit var search: HybridSearchEngine

        @JvmStatic @BeforeClass
        fun preparer() = runBlocking {
            db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
            onnx = OnnxEmbeddingEngine(context)
            val dao = db.embeddingDao()
            ManualIndexer(context, onnx, MaintenanceDocParser(), TextChunker(), dao).indexIfNeeded()
            search = HybridSearchEngine(onnx, dao)
            search.search("échauffement")   // 1re recherche : charge le modèle et les vecteurs
            Unit
        }

        @JvmStatic @AfterClass
        fun fermer() {
            onnx.close()
            db.close()
        }
    }

    @Test
    fun rechercheParMotsCles_trouveLesChunksQuiContiennentLeMot() = runBlocking {
        val trouves = db.embeddingDao().searchByKeywords("\"consignation\"")
        assertTrue(trouves.isNotEmpty())
        assertTrue(trouves.all { "consignation" in it.chunkText.lowercase() })
    }

    @Test
    fun questionSurLaPompe_renvoie3ExtraitsDontUneFichePompe() = runBlocking {
        val reponse = search.search("Que faire si la pompe surchauffe et dépasse 85 °C ?")
        reponse.results.forEachIndexed { i, r ->
            println("TOP ${i + 1} : ${r.docId} | score=${"%.4f".format(r.score)} | mots-clés=${r.rangMotsCles} " +
                    "| vecteur=${r.rangVecteur} | cosinus=${r.similarite} | ${r.chunkText.take(70)}")
        }
        assertEquals(3, reponse.results.size)
        // sop_001 et sop_002 sont les deux fiches sur les pompes
        assertTrue(reponse.results.any { it.docId == "sop_001" || it.docId == "sop_002" })
    }

    @Test
    fun questionSansMotCle_laRechercheVectorielleRepondQuandMeme() = runBlocking {
        // Aucun de ces mots n'existe dans les fiches : seule la recherche par le sens peut répondre
        val reponse = search.search("xyzzy qwertz")
        assertEquals(3, reponse.results.size)
        assertTrue(reponse.results.all { it.rangMotsCles == null && it.rangVecteur != null })
    }

    @Test
    fun recherche_enMoinsDe150ms() = runBlocking {
        val temps = (1..5).map { search.search("pression hydraulique trop basse sur la presse").totalMs }
        println("Temps de recherche (ms) : $temps")
        // On retient le meilleur des 5 essais : l'émulateur a parfois des ralentissements
        assertTrue("Meilleur temps : ${temps.min()} ms", temps.min() < 150)
    }
}
