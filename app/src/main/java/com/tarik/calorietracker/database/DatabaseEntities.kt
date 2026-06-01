package com.tarik.calorietracker.database

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Entity
data class MealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo("name")
    val name: String,
    @ColumnInfo("calories")
    val calories: Int,
    @ColumnInfo("timestamp")
    val timestamp: Long
)

@Dao
interface MealDao {
    @Query("SELECT * FROM MealEntity ORDER BY timestamp DESC")
    suspend fun getAllMeals(): List<MealEntity>

    @Query("SELECT * FROM MealEntity")
    suspend fun getAll(): List<MealEntity>

    @Insert
    suspend fun insertMeal(meal: MealEntity)
}

@Database(entities = [MealEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getMealDao(): MealDao
}