package br.com.opensheets.companion.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import br.com.opensheets.companion.data.local.dao.AppConfigDao
import br.com.opensheets.companion.data.local.dao.KeywordsSettingsDao
import br.com.opensheets.companion.data.local.dao.NotificationDao
import br.com.opensheets.companion.data.local.entities.AppConfigEntity
import br.com.opensheets.companion.data.local.entities.KeywordsSettingsEntity
import br.com.opensheets.companion.data.local.entities.NotificationEntity

@Database(
    entities = [
        NotificationEntity::class,
        AppConfigEntity::class,
        KeywordsSettingsEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
    abstract fun appConfigDao(): AppConfigDao
    abstract fun keywordsSettingsDao(): KeywordsSettingsDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS keywords_settings (
                        id INTEGER PRIMARY KEY NOT NULL,
                        trigger_keywords TEXT NOT NULL,
                        expense_keywords TEXT NOT NULL,
                        income_keywords TEXT NOT NULL
                    )
                """.trimIndent())
                
                db.execSQL("""
                    INSERT OR IGNORE INTO keywords_settings (id, trigger_keywords, expense_keywords, income_keywords) 
                    VALUES (
                        1,
                        '${KeywordsSettingsEntity.DEFAULT_TRIGGER_KEYWORDS}',
                        '${KeywordsSettingsEntity.DEFAULT_EXPENSE_KEYWORDS}',
                        '${KeywordsSettingsEntity.DEFAULT_INCOME_KEYWORDS}'
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add trigger_keywords column to existing table
                db.execSQL("""
                    ALTER TABLE keywords_settings 
                    ADD COLUMN trigger_keywords TEXT NOT NULL DEFAULT '${KeywordsSettingsEntity.DEFAULT_TRIGGER_KEYWORDS}'
                """.trimIndent())
            }
        }
    }
}

