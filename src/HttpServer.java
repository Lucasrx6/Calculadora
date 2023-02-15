import java.io.*;
import java.net.*;
import java.sql.*;
import com.sun.net.httpserver.*;

public class HttpServer {
    private Connection conn;

    public HttpServer(Connection conn) {
        this.conn = conn;
    }

    public void start() {
        try {
            // Cria o servidor HTTP
            com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer
                    .create(new InetSocketAddress(8000), 0);

            // Registra os handlers
            server.createContext("/", new IndexHandler());
            server.createContext("/calcular", new CalcularHandler());

            // Inicia o servidor
            server.setExecutor(null);
            server.start();
            System.out.println("Servidor HTTP iniciado. Acesse http://localhost:8000/ para utilizar a calculadora.");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    // Handler para a página inicial
    class IndexHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            // Lê o conteúdo do arquivo index.html
            File file = new File("index.html");
            byte[] bytes = new byte[(int) file.length()];
            FileInputStream fis = new FileInputStream(file);
            fis.read(bytes);
            fis.close();
            String response = new String(bytes);

            // Envia a resposta
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    // Handler para o endpoint /calcular
    class CalcularHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            try {
                // Obtém os parâmetros da requisição
                String query = exchange.getRequestURI().getQuery();
                String[] params = query.split("&");
                if (params.length < 2) {
                    throw new IllegalArgumentException("A requisição deve incluir dois parâmetros");
                }
                double num1 = Double.parseDouble(params[0].split("=")[1]);
                double num2 = Double.parseDouble(params[1].split("=")[1]);

                // Realiza o cálculo
                double resultado = num1 + num2;

                // Salva o histórico de operações no banco de dados
                String sql = "INSERT INTO operacoes (num1, num2, operador, resultado) VALUES (" +
                        num1 + ", " + num2 + ", '+', " + resultado + ")";
                Statement stmt = conn.createStatement();
                stmt.executeUpdate(sql);
                stmt.close();

                // Retorna a resposta
                String response = "{\"status\": \"ok\", \"resultado\": " + resultado + "}";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                String response = "{\"status\": \"erro\", \"mensagem\": \"" + e.getMessage() + "\"}";
                exchange.sendResponseHeaders(500, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }

    public Connection getConn() {
        return conn;
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }
}