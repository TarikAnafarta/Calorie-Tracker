package com.tarik.calorietracker

import android.app.Application
import androidx.room.Room
import com.tarik.calorietracker.database.AppDatabase

class App : Application() {
    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "calorie_tracker_db"
        ).build()
    }
}