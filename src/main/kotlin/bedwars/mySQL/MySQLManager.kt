package bedwars.mySQL

import ru.nukkit.dblib.DbLib
import java.sql.Connection
import java.sql.ResultSet

object MySQLManager {

    val connection: Connection
        get() = DbLib.getDefaultConnection()

    fun init() {
        connection.use { con ->
            con.prepareStatement("CREATE TABLE IF NOT EXISTS bedwars (" +
                    "name VARCHAR(20) PRIMARY KEY," +
                    "kills INT DEFAULT 0," +
                    "deaths INT DEFAULT 0," +
                    "wins INT DEFAULT 0," +
                    "losses INT DEFAULT 0," +
                    "beds INT DEFAULT 0" +
                    ")"
            )
        }
    }
}

fun ResultSet.toMap(): Map<String, Any> {
    val map = mutableMapOf<String, Any>()
    val metadata = this.metaData

    for (i in 1..metadata.columnCount) {
        map[metadata.getColumnName(i)] = this.getObject(i)
    }

    return map
}