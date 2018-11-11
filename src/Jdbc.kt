import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement

// SQLを実行するオブジェクト
object SqlExecutor {
    object Config {
        const val USERNAME = "root"
        const val PASSWORD = "root"
        const val URL = "jdbc:mysql://localhost:3306/"
    }
    private var connection: Connection

    // SQLに接続するための設定をする
    init {
        Class.forName("com.mysql.jdbc.Driver").getDeclaredConstructor().newInstance()
        connection = DriverManager.getConnection(Config.URL, Config.USERNAME, Config.PASSWORD)
    }



    // SQL文を実行する関数(情報の更新用)
    fun executeSql(SQL: String): Int {
        // SQL文を実行し、その結果をresultSetに代入する
        val stmt = connection.createStatement()
        return stmt.executeUpdate(SQL, Statement.RETURN_GENERATED_KEYS)
    }

    // SQL文を実行する関数(情報の取得用)
    // SQL文と欲しいカラム数を引数にとって、結果のデータを持った２次元配列を返す
    fun executeSql(SQL: String, COLUMN_NUM: Int): List<List<String>> {
        // SQL文を実行し、その結果をresultSetに代入する
        val stmt = connection.createStatement()
        stmt.executeQuery(SQL)
        val resultSet = stmt.resultSet

        // return するためのデータを保存する変数
        val result = mutableListOf<List<String>>()

        // 帰ってきたレコード(行)の数だけ繰り返す
        while (resultSet.next()) {
            // 1レコード(行)内のデータを一時保存
            val recordTmp = mutableListOf<String>()

            // 指定されたカラム数回分実行する
            for (i in 1..COLUMN_NUM) {
                recordTmp += resultSet.getString(i)
            }

            // resultに追加
            result += recordTmp
        }
        return result
    }
}