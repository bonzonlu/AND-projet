package ch.and.pokemonpastropgo.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import ch.and.pokemonpastropgo.db.dao.HuntZoneDAO
import ch.and.pokemonpastropgo.db.dao.NotAPokemonDAO
import ch.and.pokemonpastropgo.db.models.HuntZone
import ch.and.pokemonpastropgo.db.models.NotAPokemon
import kotlin.concurrent.thread

@Database(
    entities =[HuntZone::class,NotAPokemon::class],
    version = 1,
    exportSchema = true
)
abstract  class PPTGDatabase:  RoomDatabase() {
    abstract fun huntZoneDAO(): HuntZoneDAO
    abstract fun notAPokemonDAO(): NotAPokemonDAO

    companion object{
        private var INSTANCE: PPTGDatabase? = null

        fun getDB(context: Context): PPTGDatabase{
            return INSTANCE?: synchronized(this){
                INSTANCE = Room.databaseBuilder(context.applicationContext,
                PPTGDatabase::class.java,"pptg_db.db")
                    .fallbackToDestructiveMigration()
                    .addCallback(PPTGDatabaseCallBack())
                    .build()
                INSTANCE!!
            }
        }
    }

    private class PPTGDatabaseCallBack : RoomDatabase.Callback() {


        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let{ database ->
                val isEmpty = database.huntZoneDAO().getCount().value == 0L && database.notAPokemonDAO().getCount().value == 0L
                if(isEmpty){
                    thread {
                        //TODO populate database
                    }
                }
            }
        }

    }
}