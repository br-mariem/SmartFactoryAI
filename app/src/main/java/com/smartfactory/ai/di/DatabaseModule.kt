package com.smartfactory.ai.di

import android.content.Context
import androidx.room.Room
import com.smartfactory.ai.data.local.database.AppDatabase
import com.smartfactory.ai.data.local.database.dao.EmbeddingDao
import com.smartfactory.ai.data.local.database.dao.MachineDao
import com.smartfactory.ai.data.local.database.dao.TelemetryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "smartfactory.db")
            .fallbackToDestructiveMigration(dropAllTables = true) // phase de développement uniquement
            .build()

    @Provides
    fun provideMachineDao(db: AppDatabase): MachineDao = db.machineDao()

    @Provides
    fun provideTelemetryDao(db: AppDatabase): TelemetryDao = db.telemetryDao()

    @Provides
    fun provideEmbeddingDao(db: AppDatabase): EmbeddingDao = db.embeddingDao()
}