package com.smartfactory.ai

import ai.onnxruntime.OrtEnvironment
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class OnnxSmokeTest {

    @Test
    fun testOnnxInitialization() {
        // Le chemin est relatif depuis le dossier 'app' lors de l'exécution des tests unitaires
        val modelFile = File("src/main/assets/models/all-MiniLM-L6-v2.onnx")
        
        assertTrue("Le fichier modèle doit exister dans les assets", modelFile.exists())

        // 1. Initialisation de l'environnement ONNX
        val env = OrtEnvironment.getEnvironment()
        assertNotNull("L'environnement ONNX ne doit pas être nul", env)

        // 2. Tentative de chargement du modèle
        val session = env.createSession(modelFile.absolutePath)
        assertNotNull("La session ONNX ne doit pas être nulle", session)

        println("✅ SUCCÈS : Le modèle ONNX a été chargé avec succès en mémoire RAM mobile !")

        // 3. Libération de la mémoire
        session.close()
        env.close()
    }
}
