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

            while (true) {
                System.out.print("Ingresa tu usuario: ");
                String usuario = scanner.nextLine();
                salida.println(usuario);

                System.out.print("Ingresa tu contraseña: ");
                String contrasena = scanner.nextLine();
                salida.println(contrasena);

                String respuesta = entrada.readLine();
                System.out.println("Servidor: " + respuesta);

                if (respuesta.equals("LOGIN_EXITOSO")) {
                    break;
                } else if (respuesta.startsWith("USUARIO_NO_EXISTE")) {
                    System.out.print("¿Quieres registrarte con este usuario? (si/no): ");
                    String respuestaRegistro = scanner.nextLine();
                    salida.println(respuestaRegistro);
                    String respuestaRegistroServidor = entrada.readLine();
                    System.out.println("Servidor: " + respuestaRegistroServidor);
                    if (respuestaRegistroServidor.equals("REGISTRO_EXITOSO")) {
                        break;
                    }
                }
            }

            boolean enSesion = true;
            while (enSesion) {
                String menu = entrada.readLine();
                System.out.println(menu);
                System.out.print("Tu elección: ");
                String opcion = scanner.nextLine();
                salida.println(opcion);

                if (opcion.equalsIgnoreCase("jugar")) {
                    jugarAdivinaNumero(entrada, salida, scanner);
                } else if (opcion.equalsIgnoreCase("mensaje")) {
                    chatearConServidor(entrada, salida, scanner);
                } else if (opcion.equalsIgnoreCase("salir")) {
                    String respuestaSalida = entrada.readLine();
                    System.out.println("Servidor: " + respuestaSalida);
                    enSesion = false;
                } else {
                    String respuestaInvalida = entrada.readLine();
                    System.out.println(respuestaInvalida);
                }
            }

            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void jugarAdivinaNumero(BufferedReader entrada, PrintWriter salida, Scanner scanner) throws IOException {
        // ... (el código de jugarAdivinaNumero se mantiene igual)
        boolean seguirJugando = true;
        while (seguirJugando) {
            String mensaje = entrada.readLine();
            System.out.println(mensaje);

            if (mensaje.startsWith("JUEGO_SALIDA")) {
                seguirJugando = false;
                break;
            }

            boolean juegoActivo = true;
            while (juegoActivo) {
                System.out.print("Ingresa un numero (1-10): ");
                String intento = scanner.nextLine();
                salida.println(intento);

                String respuesta = entrada.readLine();
                System.out.println(respuesta);

                if (respuesta.startsWith("¡GANASTE") || respuesta.startsWith("PERDISTE")) {
                    String pregunta = entrada.readLine();
                    System.out.println(pregunta);
                    String decision = scanner.nextLine();
                    salida.println(decision);
                    if (decision.equalsIgnoreCase("no")) {
                        seguirJugando = false;
                    }
                    juegoActivo = false;
                }
            }
        }
    }

    private static void chatearConServidor(BufferedReader entrada, PrintWriter salida, Scanner scanner) throws IOException {
        String mensajeInicial = entrada.readLine();
        System.out.println(mensajeInicial);
        boolean enChat = true;
        while (enChat) {
            System.out.print("Tú: ");
            String comando = scanner.nextLine();
            salida.println(comando);

            if (comando.equalsIgnoreCase("leer")) {
                String respuestaServidor;

                while (!(respuestaServidor = entrada.readLine()).equalsIgnoreCase("MENSAJES_FIN")) {
                    System.out.println("Servidor: " + respuestaServidor);
                }
            } else if (comando.equalsIgnoreCase("volver")) {
                String respuestaSalida = entrada.readLine();
                System.out.println("Servidor: " + respuestaSalida);
                enChat = false;
            } else {
                String respuestaServidor = entrada.readLine();
                System.out.println("Servidor: " + respuestaServidor);
            }
        }
    }
}