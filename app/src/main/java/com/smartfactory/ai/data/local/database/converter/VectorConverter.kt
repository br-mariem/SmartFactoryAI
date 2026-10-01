package com.smartfactory.ai.data.local.database.converter

import androidx.room.TypeConverter
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Utilitaire pour transformer les vecteurs mathématiques de l'IA (FloatArray)
 * en un format binaire compact (ByteArray ou BLOB) que SQLite peut stocker très rapidement.
 */
class VectorConverter {

    @TypeConverter
    fun fromFloatArray(array: FloatArray?): ByteArray? {
        if (array == null) return null
        // Un float fait 4 octets, donc on alloue la taille exacte
        val buffer = ByteBuffer.allocate(array.size * 4)
        // LITTLE_ENDIAN garantit que la lecture sera la même sur tous les processeurs de tablettes
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        for (value in array) {
            buffer.putFloat(value)
        }
        return buffer.array()
    }

    @TypeConverter
    fun toFloatArray(bytes: ByteArray?): FloatArray? {
        if (bytes == null) return null
        val buffer = ByteBuffer.wrap(bytes)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        val array = FloatArray(bytes.size / 4)
        for (i in array.indices) {
            array[i] = buffer.getFloat()
        }
        return array
    }
}
