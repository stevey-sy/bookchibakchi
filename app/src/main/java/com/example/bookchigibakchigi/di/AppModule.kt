package com.example.bookchigibakchigi.di

import android.content.Context
import androidx.room.Room
import com.example.bookchigibakchigi.data.dao.BookDao
import com.example.bookchigibakchigi.data.dao.MemoDao
import com.example.bookchigibakchigi.data.dao.TagDao
import com.example.bookchigibakchigi.data.database.AppDatabase
import com.example.bookchigibakchigi.data.dao.PhotoCardDao
import com.example.bookchigibakchigi.data.datasource.BookSearchRemoteDataSource
import com.example.bookchigibakchigi.data.repository.BookShelfRepository
import com.example.bookchigibakchigi.data.repository.MemoRepository
import com.example.bookchigibakchigi.data.repository.PhotoCardRepository
import com.example.bookchigibakchigi.data.repository.TagRepository
import com.example.bookchigibakchigi.data.repository.BookSearchRepository
import com.example.bookchigibakchigi.data.network.service.AladinBookApiService
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
        )
            .createFromAsset("book_database.db")
            .build()
    }

    @Provides
    fun provideBookDao(database: AppDatabase): BookDao {
        return database.bookDao()
    }

    @Provides
    fun provideBookShelfRepository(bookDao: BookDao): BookShelfRepository {
        return BookShelfRepository(bookDao)
    }

    @Provides
    fun providePhotoCardDao(database: AppDatabase): PhotoCardDao {
        return database.photoCardDao()
    }

    @Provides
    fun providePhotoCardRepository(photoCardDao: PhotoCardDao): PhotoCardRepository {
        return PhotoCardRepository(photoCardDao)
    }

    @Provides
    @Singleton
    fun provideAladinBookApiService(): AladinBookApiService {
        return AladinBookApiService.create()
    }

    @Provides
    @Singleton
    fun provideBookSearchRemoteDataSource(apiService: AladinBookApiService): BookSearchRemoteDataSource {
        return BookSearchRemoteDataSource(apiService)
    }

    @Provides
    @Singleton
    fun provideAladinBookRepository(dataSource: BookSearchRemoteDataSource): BookSearchRepository {
        return BookSearchRepository(dataSource)
    }

    @Provides
    fun provideMemoDao(database: AppDatabase): MemoDao {
        return database.memoDao()
    }

    @Provides
    @Singleton
    fun provideMemoRepository(memoDao: MemoDao): MemoRepository {
        return MemoRepository(memoDao)
    }

    @Provides
    fun provideTagDao(database: AppDatabase): TagDao {
        return database.tagDao()
    }

    @Provides
    @Singleton
    fun provideTagRepository(tagDao: TagDao): TagRepository {
        return TagRepository(tagDao)
    }
}