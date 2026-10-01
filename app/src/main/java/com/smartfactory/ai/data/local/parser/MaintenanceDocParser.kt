package com.smartfactory.ai.data.local.parser

import javax.inject.Inject

/** Un chapitre d'une fiche : la ligne "## Titre" et le texte qui suit. */
data class Chapitre(
    val titre: String,
    val texte: String
)

/** Une fiche technique lue et découpée en champs. */
data class ParsedDocument(
    val titre: String,
    val typeEquipement: String,
    val norme: String,
    val version: String,
    val chapitres: List<Chapitre>
)

/**
 * Lit une fiche au format :
 *
 *   TITRE: SOP-001 - ...
 *   TYPE_EQUIPEMENT: POMPE
 *   NORME: ISO-14224
 *   VERSION: v2.1 - 2026
 *
 *   ## Premier chapitre
 *   texte...
 *   ## Deuxième chapitre
 *   texte...
 */
class MaintenanceDocParser @Inject constructor() {

    fun parse(contenu: String): ParsedDocument {
        val entete = HashMap<String, String>()
        val chapitres = ArrayList<Chapitre>()

        var titreCourant: String? = null
        val texteCourant = StringBuilder()

        fun fermerChapitre() {
            val titre = titreCourant ?: return
            chapitres += Chapitre(titre, texteCourant.toString().trim())
            texteCourant.clear()
        }

        // removePrefix("﻿") : retire le BOM ajouté par certains éditeurs Windows
        // lines() gère aussi bien les fins de ligne Windows (\r\n) que Linux (\n)
        for (ligneBrute in contenu.removePrefix("﻿").lines()) {
            val ligne = ligneBrute.trimEnd()
            when {
                ligne.startsWith("## ") -> {
                    fermerChapitre()
                    titreCourant = ligne.removePrefix("## ").trim()
                }
                titreCourant == null && ':' in ligne -> {
                    val cle = ligne.substringBefore(':').trim().uppercase()
                    entete[cle] = ligne.substringAfter(':').trim()
                }
                titreCourant != null && ligne.isNotBlank() -> {
                    if (texteCourant.isNotEmpty()) texteCourant.append(' ')
                    texteCourant.append(ligne.trim())
                }
            }
        }
        fermerChapitre()

        return ParsedDocument(
            titre = entete["TITRE"].orEmpty(),
            typeEquipement = entete["TYPE_EQUIPEMENT"].orEmpty(),
            norme = entete["NORME"].orEmpty(),
            version = entete["VERSION"].orEmpty(),
            chapitres = chapitres
        )
    }
}
