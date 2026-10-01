package com.smartfactory.ai.ai.embedding

import java.io.InputStream
import java.text.Normalizer

/** Les 3 tenseurs d'entrée attendus par le modèle all-MiniLM-L6-v2 (type int64 = Long). */
class TokenizedInput(
    val inputIds: LongArray,
    val attentionMask: LongArray,
    val tokenTypeIds: LongArray
)

/**
 * Tokeniseur WordPiece (famille BERT, version "uncased").
 * 1. Tokenisation de base : minuscules, suppression des accents, séparation de la ponctuation.
 * 2. WordPiece : chaque mot est découpé en sous-mots présents dans vocab.txt ("##" = suite d'un mot).
 * 3. Ajout de [CLS] au début et [SEP] à la fin.
 */
class WordPieceTokenizer(
    private val vocab: Map<String, Int>,
    private val maxLength: Int = 256
) {
    private val clsId = vocab.getValue("[CLS]")
    private val sepId = vocab.getValue("[SEP]")
    private val unkId = vocab.getValue("[UNK]")

    /** Transforme un texte en tenseurs d'entrée, tronqués à [maxLength] tokens. */
    fun encode(text: String): TokenizedInput {
        val ids = ArrayList<Long>()
        ids += clsId.toLong()
        remplissage@ for (mot in basicTokenize(text)) {
            for (id in wordPiece(mot)) {
                if (ids.size >= maxLength - 1) break@remplissage   // garder la place du [SEP]
                ids += id.toLong()
            }
        }
        ids += sepId.toLong()

        val n = ids.size
        return TokenizedInput(
            inputIds = ids.toLongArray(),
            attentionMask = LongArray(n) { 1L },   // 1 = "ce token compte"
            tokenTypeIds = LongArray(n) { 0L }     // 0 = une seule phrase
        )
    }

    /** Nombre de tokens SANS troncature, [CLS] et [SEP] compris. */
    fun countTokens(text: String): Int = basicTokenize(text).sumOf { wordPiece(it).size } + 2

    /** Étape 1 : minuscules, sans accents, ponctuation séparée. */
    private fun basicTokenize(text: String): List<String> {
        val sansAccents = Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD)
            .filter { Character.getType(it) != Character.NON_SPACING_MARK.toInt() }

        val mots = ArrayList<String>()
        val courant = StringBuilder()
        fun couper() {
            if (courant.isNotEmpty()) { mots += courant.toString(); courant.clear() }
        }
        for (c in sansAccents) {
            when {
                c.code == 0 || c.code == 0xFFFD || isControl(c) -> {}   // caractères parasites ignorés
                c.isWhitespace() || c == ' ' || c == ' ' -> couper()
                isPunctuation(c) || isCjk(c) -> { couper(); mots += c.toString() }
                else -> courant.append(c)
            }
        }
        couper()
        return mots
    }

    /** Étape 2 : WordPiece, algorithme glouton du plus long préfixe présent dans le vocabulaire. */
    private fun wordPiece(mot: String): List<Int> {
        if (mot.length > MAX_CHARS_PER_WORD) return listOf(unkId)
        val morceaux = ArrayList<Int>()
        var debut = 0
        while (debut < mot.length) {
            var fin = mot.length
            var trouve: Int? = null
            while (debut < fin) {
                val sousMot = (if (debut > 0) "##" else "") + mot.substring(debut, fin)
                val id = vocab[sousMot]
                if (id != null) { trouve = id; break }
                fin--
            }
            if (trouve == null) return listOf(unkId)   // un morceau introuvable = mot inconnu
            morceaux += trouve
            debut = fin
        }
        return morceaux
    }

    private fun isPunctuation(c: Char): Boolean {
        val code = c.code
        // Les symboles ASCII comme $ + < = > ^ ` | ~ sont aussi traités comme ponctuation par BERT
        if (code in 33..47 || code in 58..64 || code in 91..96 || code in 123..126) return true
        return when (Character.getType(c).toByte()) {
            Character.CONNECTOR_PUNCTUATION, Character.DASH_PUNCTUATION,
            Character.START_PUNCTUATION, Character.END_PUNCTUATION,
            Character.INITIAL_QUOTE_PUNCTUATION, Character.FINAL_QUOTE_PUNCTUATION,
            Character.OTHER_PUNCTUATION -> true
            else -> false
        }
    }

    /** Idéogrammes chinois/japonais : BERT en fait un token chacun. */
    private fun isCjk(c: Char): Boolean {
        val code = c.code
        return code in 0x4E00..0x9FFF || code in 0x3400..0x4DBF || code in 0xF900..0xFAFF
    }

    private fun isControl(c: Char): Boolean {
        if (c == '\t' || c == '\n' || c == '\r') return false
        val type = Character.getType(c).toByte()
        return type == Character.CONTROL || type == Character.FORMAT
    }

    companion object {
        private const val MAX_CHARS_PER_WORD = 100

        /** Charge vocab.txt : la ligne n contient le token numéro n. */
        fun fromStream(input: InputStream, maxLength: Int = 256): WordPieceTokenizer {
            val vocab = HashMap<String, Int>(32_000)
            input.bufferedReader(Charsets.UTF_8).useLines { lignes ->
                lignes.forEachIndexed { index, token -> vocab[token.trimEnd('\r')] = index }
            }
            return WordPieceTokenizer(vocab, maxLength)
        }
    }
}
