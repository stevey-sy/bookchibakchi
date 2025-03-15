package com.example.bookchigibakchigi.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.bookchigibakchigi.data.dao.BookDao
import com.example.bookchigibakchigi.data.dao.PhotoCardDao
import com.example.bookchigibakchigi.data.entity.BookEntity
import com.example.bookchigibakchigi.data.entity.CardTextEntity
import com.example.bookchigibakchigi.data.entity.PhotoCardEntity

@Database(
    entities = [BookEntity::class, PhotoCardEntity::class, CardTextEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun photoCardDao(): PhotoCardDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "book_database"
                )
                    // 아래에 마이그레이션을 등록
//                    .addMigrations(MIGRATION_4_5)

                    /*
                     * destructiveMigration을 활성화하면 이전 버전 DB 데이터를 날려버린 뒤(=테이블 삭제 후 재생성)
                     * 신규 스키마로 DB를 다시 만들어줍니다. 만약 기존 데이터 보존이 필요하면 주석 처리하세요.
                     */
                     .fallbackToDestructiveMigration()

                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
