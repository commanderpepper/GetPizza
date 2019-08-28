package commanderpepper.getpizza.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import commanderpepper.getpizza.room.entity.PizzaFav

@Database(entities = [PizzaFav::class], version = 1)
abstract class PizzaDatabase : RoomDatabase() {
    abstract fun pizzaDao(): PizzaDAO

    companion object {

        @Volatile
        private var instance: PizzaDatabase? = null

        private val lock = Any()

        fun getInstance(context: Context): PizzaDatabase {
            synchronized(lock) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        PizzaDatabase::class.java,
                        "pizza-db"
                    ).build()
                }
            }

            return instance!!
        }
    }
}