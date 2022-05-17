package ch.and.pokemonpastropgo.db

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import ch.and.pokemonpastropgo.db.dao.HuntZoneDAO
import ch.and.pokemonpastropgo.db.dao.NotAPokemonDAO
import ch.and.pokemonpastropgo.db.dao.PokemonToHuntDAO
import ch.and.pokemonpastropgo.db.models.HuntZone
import ch.and.pokemonpastropgo.db.models.HuntZoneCrossReff
import ch.and.pokemonpastropgo.db.models.NotAPokemon
import ch.and.pokemonpastropgo.db.models.PokemonToHunt
import kotlin.concurrent.thread

@Database(
    entities =[HuntZone::class,NotAPokemon::class,HuntZoneCrossReff::class,PokemonToHunt::class],
    version = 1,
    exportSchema = true
)
abstract  class PPTGDatabase:  RoomDatabase() {
    abstract fun huntZoneDAO(): HuntZoneDAO
    abstract fun pokemonToHuntDAO(): PokemonToHuntDAO
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

                INSTANCE!!.query("select 1",null)
                INSTANCE!!
            }
        }
    }

    private class PPTGDatabaseCallBack : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let{ database ->
                val isEmpty = database.huntZoneDAO().getCount().value == null &&  database.huntZoneDAO().getCount().value == null
                if(isEmpty){
                    thread {
                        //TODO populate database
                        database.huntZoneDAO().insertAll(
                            HuntZone(1,"HEIG - Cheseaux","Une zone bondée de pokémouille dans chaque recoins du bâtiment",46.779380,6.659500,40.0),
                            HuntZone(2,"Y-Plage","vous voulez des pokémouilles aquatique, ici c'est l'endroit idéal pour en trouver",46.785060,6.651450,20.0),
                            HuntZone(3,"HEIG - St-Roch","Deuxième domaine le plus populaire pour trouver des pokémouilles unique",46.781230,6.647310,40.0),
                            HuntZone(4,"Chez manu","Un pokémouille spécial rôde dans les parages, faites attention trouvez le avant qu'il vous trouves",46.540680,6.581140,10.0),
                            )
                        database.notAPokemonDAO().insertAll(
                            NotAPokemon("pukachi",""),
                            NotAPokemon("ratatouille",""),
                            NotAPokemon("dreakeau",""),
                            NotAPokemon("boulenormal",""),
                            NotAPokemon("carapace",""),
                            NotAPokemon("nidoroi",""),
                            NotAPokemon("herbivore",""),
                            NotAPokemon("salam",""),
                            NotAPokemon("roukoul",""),
                            NotAPokemon("Macho",""),
                        )
                        database.pokemonToHuntDAO().insertAll(
                            PokemonToHunt(null,"pukachi",1,"somewhere",false),
                            PokemonToHunt(null,"ratatouille",1,"somewhere",false),
                            PokemonToHunt(null,"dreakeau",1,"somewhere",false),
                            PokemonToHunt(null,"boulenormal",1,"somewhere",false),
                            PokemonToHunt(null,"nidoroi",1,"somewhere",false),
                            PokemonToHunt(null,"herbivore",1,"somewhere",false),

                            PokemonToHunt(null,"salam",2,"somewhere",false),
                            PokemonToHunt(null,"ratatouille",2,"somewhere",false),
                            PokemonToHunt(null,"boulenormal",2,"somewhere",false),
                            PokemonToHunt(null,"herbivore",2,"somewhere",false),
                            PokemonToHunt(null,"Macho",2,"somewhere",false),

                            PokemonToHunt(null,"nidoroi",3,"somewhere",false),
                            PokemonToHunt(null,"Macho",3,"somewhere",false),
                            PokemonToHunt(null,"salam",3,"somewhere",false),
                            PokemonToHunt(null,"dreakeau",3,"somewhere",false),
                            PokemonToHunt(null,"herbivore",3,"somewhere",false),
                            PokemonToHunt(null,"pukachi",3,"somewhere",false),
                            PokemonToHunt(null,"boulenormal",3,"somewhere",false),

                            PokemonToHunt(null,"boulenormal",4,"somewhere",false),
                            PokemonToHunt(null,"Macho",4,"somewhere",false),
                            PokemonToHunt(null,"dreakeau",4,"somewhere",false),
                            PokemonToHunt(null,"salam",4,"somewhere",false),
                            PokemonToHunt(null,"nidoroi",4,"somewhere",false),
                            PokemonToHunt(null,"ratatouille",4,"somewhere",false),

                            )
                        database.huntZoneDAO().insertAll(
                            HuntZoneCrossReff(1,1),
                            HuntZoneCrossReff(1,2),
                            HuntZoneCrossReff(1,3),
                            HuntZoneCrossReff(1,4),
                            HuntZoneCrossReff(1,5),
                            HuntZoneCrossReff(1,6),

                            HuntZoneCrossReff(2,7),
                            HuntZoneCrossReff(2,8),
                            HuntZoneCrossReff(2,9),
                            HuntZoneCrossReff(2,10),
                            HuntZoneCrossReff(2,11),

                            HuntZoneCrossReff(3,12),
                            HuntZoneCrossReff(3,13),
                            HuntZoneCrossReff(3,14),
                            HuntZoneCrossReff(3,15),
                            HuntZoneCrossReff(3,16),
                            HuntZoneCrossReff(3,17),
                            HuntZoneCrossReff(3,18),

                            HuntZoneCrossReff(4,19),
                            HuntZoneCrossReff(4,20),
                            HuntZoneCrossReff(4,21),
                            HuntZoneCrossReff(4,22),
                            HuntZoneCrossReff(4,23),
                            HuntZoneCrossReff(4,24),
                        )
                    }
                }
            }
        }

    }
}