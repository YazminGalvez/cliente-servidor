import java.io.*;
import java.net.*;
import java.util.Scanner;

public class cliente {
    public static void main(String[] args) {
        String host = "localhost";
        int puerto = 8080;

        try (Socket socket = new Socket(host, puerto)) {
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in);

            boolean seguirJugando = true;

            while (seguirJugando) {
                String mensaje = entrada.readLine();
                if (mensaje == null) break;
                System.out.println(mensaje);

                while (true) {
                    System.out.print("Ingresa un numero (1-10): ");
                    String intento = scanner.nextLine();
                    salida.println(intento);

                    String respuesta = entrada.readLine();
                    if (respuesta == null) {
                        seguirJugando = false;
                        break;
                    }

                    System.out.println(respuesta);

                    if (respuesta.startsWith("¡ADIVINASTE") || respuesta.startsWith("NO ADIVINASTE")) {
                        break;
                    }
                }

                String pregunta = entrada.readLine();
                if (pregunta == null) break;
                System.out.println(pregunta);

                String decision = scanner.nextLine();
                salida.println(decision);

                if (decision.equalsIgnoreCase("no")) {
                    System.out.println(entrada.readLine());
                    seguirJugando = false;
                }
            }

            socket.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }
}