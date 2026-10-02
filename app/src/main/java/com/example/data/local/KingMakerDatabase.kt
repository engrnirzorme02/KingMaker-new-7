package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        DecisionEntity::class,
        AdrEntity::class,
        BlueprintEdgeEntity::class,
        OutcomeObservationEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KingMakerDatabase : RoomDatabase() {
    abstract fun decisionDao(): DecisionDao
    abstract fun adrDao(): AdrDao
    abstract fun blueprintDao(): BlueprintDao
    abstract fun outcomeDao(): OutcomeDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: KingMakerDatabase? = null

        fun getInstance(context: Context): KingMakerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KingMakerDatabase::class.java,
                    "kingmaker_sovereign.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
