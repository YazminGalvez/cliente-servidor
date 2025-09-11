import java.io.*;
import java.net.*;
import java.util.Scanner;
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

            System.out.println("--- Sistema de Inicio de Sesión y Registro ---");
            boolean loggedIn = false;
            while (!loggedIn) {
                System.out.print("Ingresa tu usuario: ");
                String usuario = scanner.nextLine();
                salida.println(usuario);

                System.out.print("Ingresa tu contraseña: ");
                String contrasena = scanner.nextLine();
                salida.println(contrasena);

                String respuestaLogin = entrada.readLine();
                if (respuestaLogin == null) break;

                System.out.println(respuestaLogin);

                if (respuestaLogin.startsWith("LOGIN_EXITOSO")) {
                    loggedIn = true;
                } else if (respuestaLogin.startsWith("USUARIO_NO_EXISTE")) {
                    System.out.print("¿Deseas registrar este usuario? (si/no): ");
                    String respuestaRegistro = scanner.nextLine();
                    salida.println(respuestaRegistro);

                    if (respuestaRegistro.equalsIgnoreCase("si")) {
                        String resultadoRegistro = entrada.readLine();
                        if (resultadoRegistro.startsWith("REGISTRO_EXITOSO")) {
                            System.out.println("Registro exitoso. ¡Iniciando sesión!");
                            loggedIn = true;
                        } else {
                            System.out.println("Fallo en el registro. Intenta de nuevo.");
                        }
                    }
                }
            }

            if (!loggedIn) {
                System.out.println("No se pudo iniciar sesión. Cerrando...");
                return;
            }
            System.out.println("\n¡Inicio de sesión exitoso! ¡A Jugar!");

            boolean seguirJugando = true;
            while (seguirJugando) {
                String mensaje = entrada.readLine();
                if (mensaje == null) break;
                System.out.println(mensaje);

                while (true) {
                    System.out.print("Ingresa un número (1-10): ");
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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}