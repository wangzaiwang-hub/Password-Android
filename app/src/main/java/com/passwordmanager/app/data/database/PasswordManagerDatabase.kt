package com.passwordmanager.app.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.passwordmanager.app.data.dao.*
import com.passwordmanager.app.data.entities.*

/**
 * 密码管理应用的主数据库
 * 包含所有实体和数据访问对象
 */
@Database(
    entities = [
        PasswordEntry::class,
        LoginMethod::class,
        UserSettings::class,
        AccountHistory::class
    ],
    version = 1,
    exportSchema = true
)
abstract class PasswordManagerDatabase : RoomDatabase() {
    
    abstract fun passwordDao(): PasswordDao
    abstract fun loginMethodDao(): LoginMethodDao
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun accountHistoryDao(): AccountHistoryDao
    
    companion object {
        private const val DATABASE_NAME = "password_manager_database"
        
        @Volatile
        private var INSTANCE: PasswordManagerDatabase? = null
        
        /**
         * 获取数据库实例（单例模式）
         */
        fun getDatabase(context: Context): PasswordManagerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PasswordManagerDatabase::class.java,
                    DATABASE_NAME
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * 数据库回调，用于初始化默认数据
         */
        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // 数据库创建后的初始化操作将在Repository中处理
            }
        }
        
        /**
         * 用于测试的数据库实例
         */
        fun getInMemoryDatabase(context: Context): PasswordManagerDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                PasswordManagerDatabase::class.java
            ).build()
        }
        
        /**
         * 清理数据库实例（用于测试）
         */
        fun clearInstance() {
            INSTANCE = null
        }
    }
}