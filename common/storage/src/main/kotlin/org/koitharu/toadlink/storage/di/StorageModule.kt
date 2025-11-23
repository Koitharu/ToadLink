package org.koitharu.toadlink.storage.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.koitharu.toadlink.storage.ToadDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal class StorageModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): ToadDatabase = Room.databaseBuilder(context, ToadDatabase::class.java, "toaddb")
        .build()
}