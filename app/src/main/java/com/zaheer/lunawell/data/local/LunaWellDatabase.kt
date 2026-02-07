package com.zaheer.lunawell.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.zaheer.lunawell.data.local.dao.AppointmentDao
import com.zaheer.lunawell.data.local.dao.BreastLogDao
import com.zaheer.lunawell.data.local.dao.CycleDao
import com.zaheer.lunawell.data.local.dao.DailyLogDao
import com.zaheer.lunawell.data.local.dao.EntitlementDao
import com.zaheer.lunawell.data.local.dao.PregnancyLogDao
import com.zaheer.lunawell.data.local.dao.ProfileDao
import com.zaheer.lunawell.data.local.dao.ReminderDao
import com.zaheer.lunawell.data.local.dao.SymptomLogDao
import com.zaheer.lunawell.data.local.entity.AppointmentEntity
import com.zaheer.lunawell.data.local.entity.BreastLogEntity
import com.zaheer.lunawell.data.local.entity.CycleEntity
import com.zaheer.lunawell.data.local.entity.DailyLogEntity
import com.zaheer.lunawell.data.local.entity.EntitlementEntity
import com.zaheer.lunawell.data.local.entity.PregnancyLogEntity
import com.zaheer.lunawell.data.local.entity.ProfileEntity
import com.zaheer.lunawell.data.local.entity.ReminderEntity
import com.zaheer.lunawell.data.local.entity.SymptomLogEntity

@Database(
    entities = [
        ProfileEntity::class,
        CycleEntity::class,
        DailyLogEntity::class,
        SymptomLogEntity::class,
        PregnancyLogEntity::class,
        BreastLogEntity::class,
        ReminderEntity::class,
        AppointmentEntity::class,
        EntitlementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LunaWellDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao
    abstract fun cycleDao(): CycleDao
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun symptomLogDao(): SymptomLogDao
    abstract fun pregnancyLogDao(): PregnancyLogDao
    abstract fun breastLogDao(): BreastLogDao
    abstract fun reminderDao(): ReminderDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun entitlementDao(): EntitlementDao

    companion object {
        @Volatile
        private var INSTANCE: LunaWellDatabase? = null

        fun getInstance(context: Context): LunaWellDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LunaWellDatabase::class.java,
                    "lunawell_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
