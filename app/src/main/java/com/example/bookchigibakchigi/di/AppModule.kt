package com.example.bookchigibakchigi.di

import android.content.Context
import androidx.room.Room
import com.example.bookchigibakchigi.data.database.AppDatabase
import com.example.bookchigibakchigi.data.dao.PhotoCardDao
import com.example.bookchigibakchigi.data.repository.PhotoCardRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "book_database"
        ).build()
    }

    @Provides
    fun providePhotoCardDao(database: AppDatabase): PhotoCardDao {
        return database.photoCardDao()
    }

    @Provides
    fun providePhotoCardRepository(photoCardDao: PhotoCardDao): PhotoCardRepository {
        return PhotoCardRepository(photoCardDao)
    }
}