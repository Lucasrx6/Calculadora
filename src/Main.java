import java.sql.*;

public class Main {

    public static void main(String[] args) {
        Connection conn = null;
        try {
            // Conecta ao banco de dados
            conn = DriverManager.getConnection("jdbc:sqlite:calculadora.db");
            System.out.println("Conexão com o banco de dados estabelecida.");

            // Cria a tabela de histórico, se ela ainda não existe
            Statement stmt = conn.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS historico (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "num1 REAL," +
                    "num2 REAL," +
                    "operacao TEXT," +
                    "resultado REAL)";
            stmt.execute(sql);
            stmt.close();

            // Instancia a classe responsável por lidar com as requisições HTTP
            HttpServer server = new HttpServer(conn);
            server.start();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
