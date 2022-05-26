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
import ch.and.pokemonpastropgo.db.models.NotAPokemon
import ch.and.pokemonpastropgo.db.models.PokemonToHunt
import kotlin.concurrent.thread

@Database(
    entities =[HuntZone::class,NotAPokemon::class,PokemonToHunt::class],
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
                val isEmpty = database.huntZoneDAO().getAllZonesCount().value == null &&  database.huntZoneDAO().getAllZonesCount().value == null
                Log.d("",isEmpty.toString())
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
                            PokemonToHunt("pukachi_1","pukachi",1,"somewhere",false,false,0.0,0.0),
                            PokemonToHunt("ratatouille_1","ratatouille",1,"somewhere",false,false,0.0,0.0),
                            PokemonToHunt("dreakeau_1","dreakeau",1,"somewhere",false,false,0.0,0.0),
                            PokemonToHunt("boulenormal_1","boulenormal",1,"somewhere",false,false,0.0,0.0),
                            PokemonToHunt("nidoroi_1","nidoroi",1,"somewhere",false,false,0.0,0.0),
                            PokemonToHunt("herbivore_1","herbivore",1,"somewhere",false,false,0.0,0.0),

                            PokemonToHunt("salam_2","salam",2,"somewhere",false,false,0.0,0.0),
                            PokemonToHunt("ratatouille_2","ratatouille",2,"somewhere",false,false,0.0,0.0),
                            PokemonToHunt("boulenormal_2","boulenormal",2,"somewhere",false,false,0.0,0.0),
                            PokemonToHunt("herbivore_2","herbivore",2,"somewhere",false,false,0.0,0.0),
                            PokemonToHunt("Macho_2","Macho",2,"somewhere",false,false,0.0,0.0),

                            PokemonToHunt("nidoroi_3","nidoroi",3,"somewhere",false,false,0.0,0.0),
                            PokemonToHunt("Macho_3","Macho",3,"somewhere",false,false,0.0,0.0),
                            PokemonToHunt("salam_3","salam",3,"somewhere",false,false,0.0,0.0),
                            PokemonToHunt("dreakeau_3","dreakeau",3,"somewhere",false,false,0.0,0.0),
                            PokemonToHunt("herbivore_3","herbivore",3,"somewhere",false,false,0.0,0.0),
                            PokemonToHunt("pukachi_3","pukachi",3,"somewhere",false,false,0.0,0.0),
                            PokemonToHunt("boulenormal_3","boulenormal",3,"somewhere",false,false,0.0,0.0),

                            PokemonToHunt("boulenormal_4","boulenormal",4,"somewhere",false,false,0.0,0.0),
                            PokemonToHunt("Macho_4","Macho",4,"somewhere",false,false,0.0,0.0),
                            PokemonToHunt("dreakeau_4","dreakeau",4,"somewhere",false,false,0.0,0.0),
                            PokemonToHunt("salam_4","salam",4,"somewhere",false,false,0.0,0.0),
                            PokemonToHunt("nidoroi_4","nidoroi",4,"somewhere",false,false,0.0,0.0),
                            PokemonToHunt("ratatouille_4","ratatouille",4,"somewhere",false,false,0.0,0.0),

                            )
                    }
                }
            }
        }

    }
}