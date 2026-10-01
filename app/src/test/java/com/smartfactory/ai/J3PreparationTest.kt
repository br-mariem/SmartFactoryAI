package com.smartfactory.ai

import com.smartfactory.ai.ai.embedding.WordPieceTokenizer
import com.smartfactory.ai.data.local.parser.MaintenanceDocParser
import com.smartfactory.ai.data.local.parser.TextChunker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Tests JVM (dossier "test", PAS "androidTest") : ils tournent sur le PC, sans émulateur.
 * Le répertoire de travail est le module "app", d'où les chemins "src/main/assets/...".
 */
class J3PreparationTest {

    private val chunker = TextChunker()
    private val parser = MaintenanceDocParser()
    private val dossierManuels = File("src/main/assets/manuels")
    private val fichierVocab = File("src/main/assets/models/vocab.txt")

    // ---------- Chunking ----------

    @Test
    fun texteCourt_donneUnSeulChunk() {
        assertEquals(1, chunker.chunkText("un deux trois", chunkSize = 250, overlap = 50).size)
    }

    @Test
    fun texteDe600Mots_donne3ChunksAvecChevauchement() {
        val texte = (1..600).joinToString(" ") { "mot$it" }
        val chunks = chunker.chunkText(texte, chunkSize = 250, overlap = 50)

        assertEquals(3, chunks.size)
        assertEquals(listOf(250, 250, 200), chunks.map { it.split(" ").size })
        // Les 50 derniers mots du chunk 0 = les 50 premiers du chunk 1
        assertEquals(chunks[0].split(" ").takeLast(50), chunks[1].split(" ").take(50))
    }

    @Test(expected = IllegalArgumentException::class)
    fun overlapTropGrand_estRefuse() {
        chunker.chunkText("a b c", chunkSize = 100, overlap = 100)
    }

    // ---------- Fiches techniques ----------

    @Test
    fun lesDixFiches_sontLuesCorrectement() {
        val docs = dossierManuels.listFiles()!!.filter { it.name.endsWith(".txt") }
            .sortedBy { it.name }
            .map { parser.parse(it.readText(Charsets.UTF_8)) }

        assertEquals(10, docs.size)
        assertTrue(docs.all { it.titre.startsWith("SOP-") })
        assertTrue(docs.all { it.chapitres.size == 5 })
        assertEquals(
            setOf("POMPE", "COMPRESSEUR", "MOTEUR_ASYNCHRONE", "MACHINE_OUTIL", "PRESSE"),
            docs.map { it.typeEquipement }.toSet()
        )
        // Chaque chapitre fait moins de 250 mots : 1 chunk par chapitre, 50 au total
        assertEquals(50, docs.sumOf { chunker.chunkDocument(it).size })
    }

    // ---------- Tokenizer (nécessite vocab.txt) ----------

    private val tokenizer by lazy { fichierVocab.inputStream().use { WordPieceTokenizer.fromStream(it) } }

    @Test
    fun helloWorld_donneLesBonsNumeros() {
        assertEquals(listOf(101L, 7592L, 2088L, 102L), tokenizer.encode("hello world").inputIds.toList())
    }

    @Test
    fun majusculesEtPonctuation_sontGerees() {
        // "Hello, World!" -> [CLS] hello , world ! [SEP]
        assertEquals(
            listOf(101L, 7592L, 1010L, 2088L, 999L, 102L),
            tokenizer.encode("Hello, World!").inputIds.toList()
        )
    }

    @Test
    fun texteTresLong_estTronqueA256Tokens() {
        val long = (1..1000).joinToString(" ") { "temperature" }
        val t = tokenizer.encode(long)
        assertEquals(256, t.inputIds.size)
        assertEquals(102L, t.inputIds.last())            // [SEP] toujours présent à la fin
        assertEquals(256, t.attentionMask.size)
    }
}
