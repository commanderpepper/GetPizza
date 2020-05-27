package commanderpepper.getpizza.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import commanderpepper.getpizza.room.entity.PizzaFav

@Database(entities = [PizzaFav::class], version = 3)
abstract class PizzaDatabase : RoomDatabase() {
    abstract fun pizzaDao(): PizzaDAO


    companion object {

        //Migration object made to add name to the PizzaFav table
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE pizzafav ADD COLUMN name TEXT DEFAULT '' NOT NULL")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2,3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE pizzafav ADD COLUMN favorite INTEGER DEFAULT 0 NOT NULL")
            }
        }

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
                    )
                        .addMigrations(MIGRATION_1_2)
                        .addMigrations(MIGRATION_2_3)
                        .build()
                }
            }

            return instance!!
        }
    }
}