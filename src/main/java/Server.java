import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static spark.Spark.get;
import static spark.Spark.port;

public class Server {
    static int PORT = 7000;

    private static int getPort() {
        String herokuPort = System.getenv("PORT");
        if (herokuPort != null) {
            PORT = Integer.parseInt(herokuPort);
        }
        return PORT;
    }

    public static void main(String[] args) {
        port(getPort());
        get("/", (req, res) -> "Hi Heroku!");
        workWithDatabase();
    }

    private static Connection getConnection() throws URISyntaxException, SQLException {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl == null) {
            // Not on Heroku, so use SQLite
            return DriverManager.getConnection("jdbc:sqlite:./JBApp.db");
        }
        URI dbUri = new URI(System.getenv("DATABASE_URL"));

        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();

        return DriverManager.getConnection(dbUrl, username, password);

    }

    private static void workWithDatabase(){
        try (Connection conn = getConnection()) {
            String sql = " ";

            if ("SQLite".equalsIgnoreCase(conn.getMetaData().getDatabaseProductName())) { // running locally
                sql = "CREATE TABLE IF NOT EXISTS employers (id INTEGER PRIMARY KEY, " +
                        "name VARCHAR(100) NOT NULL UNIQUE, sector VARCHAR(100), summary VARCHAR(10000));";
            }
            else {
                sql = "CREATE TABLE IF NOT EXISTS employers (id serial PRIMARY KEY, name VARCHAR(100) NOT NULL UNIQUE," +
                        " sector VARCHAR(100), summary VARCHAR(10000));";
            }

            Statement st = conn.createStatement();
            st.execute(sql);

            sql = "INSERT INTO employers(name, sector, summary) VALUES ('Boeing', 'Aerospace', '');";
            st.execute(sql);

        } catch (URISyntaxException | SQLException e) {
            e.printStackTrace();
        }
    }

}
