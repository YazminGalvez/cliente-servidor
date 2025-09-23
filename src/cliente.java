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

                System.out.print("Ingresa tu contrasena: ");
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
                String linea;
                while (!(linea = entrada.readLine()).startsWith("Por favor")) {
                    System.out.println(linea);
                }
                System.out.println(linea);

                System.out.print("Tu eleccion: ");
                String opcion = scanner.nextLine();
                salida.println(opcion);

                if (opcion.equalsIgnoreCase("1")) {
                    jugarAdivinaNumero(entrada, salida, scanner);
                } else if (opcion.equalsIgnoreCase("2")) {
                    chatearConServidor(entrada, salida, scanner);
                } else if (opcion.equalsIgnoreCase("3")) {
                    String confirmacion = entrada.readLine();
                    System.out.println("Servidor: " + confirmacion);
                    if (confirmacion.startsWith("CONFIRMACION")) {
                        System.out.print("Tu eleccion: ");
                        String respuestaConfirmacion = scanner.nextLine();
                        salida.println(respuestaConfirmacion);
                        String respuestaServidor = entrada.readLine();
                        System.out.println("Servidor: " + respuestaServidor);
                        if (respuestaConfirmacion.equalsIgnoreCase("si")) {
                            enSesion = false;
                        }
                    }
                } else if (opcion.equalsIgnoreCase("4")) {
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
        String linea;
        while (!(linea = entrada.readLine()).startsWith("Por favor")) {
            System.out.println(linea);
        }
        System.out.println(linea);

        boolean enChat = true;
        while (enChat) {
            System.out.print("Tu eleccion: ");
            String opcion = scanner.nextLine();
            salida.println(opcion);

            switch (opcion) {
                case "1":
                    String respuesta = entrada.readLine();
                    System.out.println("Servidor: " + respuesta);
                    System.out.print("Destinatario: ");
                    String destinatario = scanner.nextLine();
                    salida.println(destinatario);
                    String respuesta2 = entrada.readLine();
                    System.out.println("Servidor: " + respuesta2);
                    System.out.print("Mensaje: ");
                    String contenido = scanner.nextLine();
                    salida.println(contenido);
                    System.out.println(entrada.readLine());
                    break;
                case "2":
                    manejarLecturaPaginada(entrada, salida, scanner);
                    break;
                case "3":
                    String respuestaEliminar = entrada.readLine();
                    System.out.println("Servidor: " + respuestaEliminar);

                    if (respuestaEliminar.startsWith("LISTA_MENSAJES_ELIMINAR:")) {
                        while (true) {
                            String lineaEliminar = entrada.readLine();
                            if (lineaEliminar.equals("LISTA_FIN")) {
                                break;
                            }
                            System.out.println(lineaEliminar);
                        }
                        String peticionId = entrada.readLine();
                        System.out.println(peticionId);
                        String idMensaje = scanner.nextLine();
                        salida.println(idMensaje);
                        System.out.println(entrada.readLine());
                    }
                    break;
                case "4":
                    String respuestaVolver = entrada.readLine();
                    System.out.println("Servidor: " + respuestaVolver);
                    enChat = false;
                    break;
                default:
                    String respuestaServidor = entrada.readLine();
                    System.out.println("Servidor: " + respuestaServidor);
                    break;
            }

            if (enChat) {
                String chatMenuPrompt;
                while (!(chatMenuPrompt = entrada.readLine()).startsWith("Por favor")) {
                    System.out.println(chatMenuPrompt);
                }
                System.out.println(chatMenuPrompt);
            }
        }
    }

    private static void manejarLecturaPaginada(BufferedReader entrada, PrintWriter salida, Scanner scanner) throws IOException {
        boolean enPaginacion = true;
        while (enPaginacion) {
            String estadoPaginacion = entrada.readLine();
            if (estadoPaginacion.startsWith("PAGINACION_INICIO")) {
                String[] partes = estadoPaginacion.split(":");
                int paginaActual = Integer.parseInt(partes[1]);
                int totalPaginas = Integer.parseInt(partes[2]);
                System.out.println("\n--- Mensajes (Página " + paginaActual + " de " + totalPaginas + ") ---");

                while (true) {
                    String lineaMensaje = entrada.readLine();
                    if (lineaMensaje.equals("PAGINACION_FIN")) {
                        break;
                    }
                    System.out.println(lineaMensaje);
                }

                String opciones = entrada.readLine();
                System.out.println(opciones);
                System.out.print("Tu comando: ");
                String comando = scanner.nextLine();
                salida.println(comando);

                String respuestaServidor = entrada.readLine();
                System.out.println("Servidor: " + respuestaServidor);

                if (respuestaServidor.startsWith("MENSAJE_SALIDA_LECTURA")) {
                    enPaginacion = false;
                }
            } else if (estadoPaginacion.startsWith("INFO: No tienes mensajes para leer.")) {
                System.out.println("Servidor: " + estadoPaginacion);
                enPaginacion = false;
            } else {
                System.out.println("Servidor: " + estadoPaginacion);
                enPaginacion = false;
            }
        }
    }
}