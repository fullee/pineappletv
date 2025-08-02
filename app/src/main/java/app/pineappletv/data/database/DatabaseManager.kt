package app.pineappletv.data.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.pineappletv.database.PineappleTVDatabase

class DatabaseManager(private val context: Context) {
    
    private val driver: SqlDriver by lazy {
        AndroidSqliteDriver(PineappleTVDatabase.Schema, context, "pineappletv.db")
    }
    
    val database: PineappleTVDatabase by lazy {
        PineappleTVDatabase(driver)
    }
    
    fun close() {
        driver.close()
    }
}