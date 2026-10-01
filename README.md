# SmartFactory Edge-AI & Agent RAG 🏭🤖

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?&style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-003B57?style=for-the-badge&logo=sqlite&logoColor=white)

**Application de Supervision Industrielle 100% Hors-Ligne** avec Modèles Vectoriels Locaux et Inférence LLM On-Device.

Ce projet a été réalisé pour maîtriser le développement Android (Embarqué) couplé à l'Intelligence Artificielle Générative (GenAI) tournant localement sur processeur mobile.

---

## 🎯 1. Objectif du Projet

Dans l'industrie manufacturière moderne, les pannes coûtent des millions. Les systèmes de supervision dépendent souvent du Cloud, posant des problèmes de confidentialité des données (secret industriel) et de disponibilité (coupure réseau).

L'objectif de SmartFactory AI est de fournir **une application Android sur tablette/smartphone industriel capable de fonctionner de manière 100% autonome et hors-ligne**.

Elle combine :
1. **Un socle Embarqué & Temps Réel** : Écoute des capteurs industriels (température, vibrations) via MQTT, stockage ultra-rapide (Room/SQLite), et affichage en temps réel avec des jauges réactives en Jetpack Compose.
2. **Un moteur de Recherche RAG Hybride On-Device** : Indexation vectorielle des manuels de maintenance d'usine grâce à un modèle d'embeddings compact exécuté localement (**ONNX Runtime**) combiné à un moteur de recherche plein texte (**SQLite FTS5**).
3. **Un Modèle de Langage Local (SLM)** : Exécution d'un LLM directement sur le processeur mobile (via llama.cpp en C++ natif) pour analyser les pannes sans aucun serveur externe.
4. **Un Agent Décisionnel Autonome avec Sécurité** : L'IA analyse l'incident, consulte la documentation, élabore une solution et propose des actions (Tool Calling) soumises à la validation obligatoire par l'opérateur humain (**Human-in-the-Loop**).

---

## 🚀 2. Contraintes Techniques Respectées

- **Zéro dépendance Cloud** : Fonctionne totalement en Mode Avion.
- **Budget Mémoire RAM optimisé** : Conçu pour ne pas dépasser 1.8 Go de RAM.
- **Latence d'insertion** : SQLite optimisé pour ingérer de nombreuses mesures par seconde sans ralentissement de l'interface (Thread UI à 60 FPS constants).
- **Architecture Modulaire** : Clean Architecture stricte (Hilt, Flow, Coroutines).

---

## ⚙️ 3. Fonctionnalités Clés

* **UI / Dashboard Jetpack Compose** : Jauges circulaires colorées, courbes Sparklines animées et simulation de données en temps réel.
* **Moteur d'Embeddings On-Device** : Génération des vecteurs et indexation intelligente des manuels par chunks via ONNX Runtime.
* **Recherche Hybride** : Recherche mathématique vectorielle combinée à la recherche textuelle ultra-rapide (FTS5 SQLite).
* **LLM Local C++** : Intégration Native C++ du Moteur LLM Local via Llama.cpp et JNI.
* **Agent Autonome & Tool Calling** : Moteur de raisonnement ReAct avec un catalogue d'outils industriels.
* **Écran de Chat Copilot** : Interface avec streaming de tokens (effet machine à écrire) et formatage de prompts structurés.
* **Sécurité Industrielle "Human-in-the-Loop"** : Bouton "Swipe to Confirm" pour valider les décisions critiques de l'IA.
* **Journal d'Audit** : Traçabilité inaltérable dans SQLite (AI Act compliance).
* **Simulation d'Alertes Push** : Détection d'anomalies et déclenchement de notifications Android.

---

## 🔧 4. Comment lancer le projet

1. **Pré-requis** : Android Studio.
2. Cloner ce dépôt.
3. Le modèle d'intelligence artificielle (`all-MiniLM-L6-v2.onnx`) doit être présent dans le dossier `app/src/main/assets/models/`.
4. Compiler et lancer l'application sur un terminal physique Android (le mode émulateur fonctionne mais sera moins performant pour l'inférence de l'IA locale).

---
*Projet développé avec passion pour la révolution de l'Industrie 4.0 et l'Edge-AI.*
