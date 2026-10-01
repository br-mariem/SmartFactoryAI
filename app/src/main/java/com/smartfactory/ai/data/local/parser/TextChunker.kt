package com.smartfactory.ai.data.local.parser

import javax.inject.Inject

/** Un morceau de texte prêt à être transformé en vecteur. */
data class TextChunk(
    val chapitre: String,
    val index: Int,
    val contenu: String
)

/**
 * Découpe un texte en chunks de [chunkSize] mots, avec [overlap] mots en commun
 * entre deux chunks qui se suivent (fenêtre glissante).
 *
 * Exemple avec 600 mots, chunkSize = 250, overlap = 50 :
 *   chunk 0 : mots   0 à 249
 *   chunk 1 : mots 200 à 449   (les 50 premiers = les 50 derniers du chunk 0)
 *   chunk 2 : mots 400 à 599
 */
class TextChunker @Inject constructor() {

    fun chunkText(text: String, chunkSize: Int = 250, overlap: Int = 50): List<String> {
        require(chunkSize > 0) { "chunkSize doit être > 0" }
        require(overlap in 0 until chunkSize) { "overlap doit être entre 0 et chunkSize - 1" }

        val mots = text.trim().split(ESPACES).filter { it.isNotEmpty() }
        if (mots.isEmpty()) return emptyList()
        if (mots.size <= chunkSize) return listOf(mots.joinToString(" "))

        val pas = chunkSize - overlap          // on avance de 200 mots à chaque chunk
        val chunks = ArrayList<String>()
        var debut = 0
        while (true) {
            val fin = minOf(debut + chunkSize, mots.size)
            chunks += mots.subList(debut, fin).joinToString(" ")
            if (fin == mots.size) break        // dernier chunk atteint
            debut += pas
        }
        return chunks
    }

    /** Découpe une fiche chapitre par chapitre : un chunk ne mélange jamais deux chapitres. */
    fun chunkDocument(doc: ParsedDocument, chunkSize: Int = 250, overlap: Int = 50): List<TextChunk> {
        var index = 0
        return doc.chapitres.flatMap { chapitre ->
            chunkText(chapitre.texte, chunkSize, overlap).map { morceau ->
                TextChunk(chapitre = chapitre.titre, index = index++, contenu = morceau)
            }
        }
    }

    private companion object {
        // Espaces classiques + espaces insécables fréquents en français
        val ESPACES = Regex("[\\s\\u00A0\\u202F]+")
    }
}
