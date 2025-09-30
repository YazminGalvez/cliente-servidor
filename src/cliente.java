import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

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
                    if (respuestaRegistro.equalsIgnoreCase("si")) {
                        String respuestaRegistroServidor = entrada.readLine();
                        System.out.println("Servidor: " + respuestaRegistroServidor);
                        if (respuestaRegistroServidor.equals("REGISTRO_EXITOSO")) {
                            break;
                        }
                    }
                }
            }

            boolean enSesion = true;
            while (enSesion) {
                String linea;
                while ((linea = entrada.readLine()) != null && !linea.startsWith("Por favor")) {
                    System.out.println(linea);
                }
                System.out.println(linea);

                System.out.print("Tu eleccion: ");
                String opcion = scanner.nextLine();
                salida.println(opcion);

                if (opcion.equalsIgnoreCase("1")) {
                    jugarAdivinaNumero(entrada, salida, scanner);
                } else if (opcion.equalsIgnoreCase("2")) {
                    manejarListaUsuarios(entrada);
                } else if (opcion.equalsIgnoreCase("3")) {
                    chatearConServidor(entrada, salida, scanner);
                } else if (opcion.equalsIgnoreCase("4")) {
                    String confirmacion = entrada.readLine();
                    System.out.println("Servidor: " + confirmacion);
                    if (confirmacion.startsWith("CONFIRMACION")) {
                        System.out.print("Tu eleccion: ");
                        String respuestaConfirmacion = scanner.nextLine();
                        salida.println(respuestaConfirmacion);
                        String respuestaServidor = entrada.readLine();
                        System.out.println("Servidor: " + respuestaServidor);
                        if (respuestaConfirmacion.equalsIgnoreCase("si") && respuestaServidor.startsWith("USUARIO_ELIMINADO")) {
                            enSesion = false;
                        }
                    }
                } else if (opcion.equalsIgnoreCase("5")) {
                    manejarBloqueo(entrada, salida, scanner);
                } else if (opcion.equalsIgnoreCase("6")) {
                    manejarArchivosCliente(entrada, salida, scanner);
                } else if (opcion.equalsIgnoreCase("7")) {
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

    private static void manejarArchivosCliente(BufferedReader entrada, PrintWriter salida, Scanner scanner) throws IOException {
        boolean enArchivosMenu = true;
        while (enArchivosMenu) {
            String archivosMenuPrompt;
            while ((archivosMenuPrompt = entrada.readLine()) != null && !archivosMenuPrompt.startsWith("Por favor")) {
                System.out.println(archivosMenuPrompt);
            }
            System.out.println(archivosMenuPrompt);

            System.out.print("Tu eleccion: ");
            String opcion = scanner.nextLine();
            salida.println(opcion);

            switch (opcion) {
                case "1":
                    gestionarArchivoDeTextoCliente(entrada, salida, scanner);
                    break;
                case "2":
                    listarArchivosCliente(entrada);
                    break;
                case "3":
                    String respuestaSalida = entrada.readLine();
                    System.out.println("Servidor: " + respuestaSalida);
                    enArchivosMenu = false;
                    break;
                default:
                    String respuestaServidor = entrada.readLine();
                    System.out.println("Servidor: " + respuestaServidor);
                    break;
            }
        }
    }

    private static void gestionarArchivoDeTextoCliente(BufferedReader entrada, PrintWriter salida, Scanner scanner) throws IOException {
        String promptNombre = entrada.readLine();
        System.out.println("Servidor: " + promptNombre);

        if (!promptNombre.startsWith("GESTION_ARCHIVO_NOMBRE")) return;

        System.out.print("Nombre del archivo: ");
        String nombreArchivo = scanner.nextLine();
        salida.println(nombreArchivo);

        String respuestaServidor = entrada.readLine();
        System.out.println("Servidor: " + respuestaServidor);

        if (respuestaServidor.startsWith("ERROR") || respuestaServidor.startsWith("OPERACION_CANCELADA")) {
            return;
        }

        String promptContenido = entrada.readLine();
        System.out.println("Servidor: " + promptContenido);

        if (promptContenido.startsWith("GESTION_ARCHIVO_CONTENIDO")) {
            System.out.println("Empieza a escribir el contenido. (Termina con 'FIN_ARCHIVO' en una línea separada):");
            String linea;
            while (scanner.hasNextLine() && !(linea = scanner.nextLine()).equals("FIN_ARCHIVO")) {
                salida.println(linea);
            }
            salida.println("FIN_ARCHIVO");

            String confirmacion = entrada.readLine();
            System.out.println("Servidor: " + confirmacion);
        }
    }

    private static void listarArchivosCliente(BufferedReader entrada) throws IOException {
        String respuesta = entrada.readLine();
        if (respuesta.equals("LISTA_ARCHIVOS:")) {
            System.out.println("\n--- Archivos en tu carpeta ---");
            String archivo;
            while (!(archivo = entrada.readLine()).equals("FIN_LISTA_ARCHIVOS:")) {
                if (archivo.startsWith("INFO:") || archivo.startsWith("ERROR:")) {
                    System.out.println(archivo.substring(6));
                    break;
                }
                System.out.println(archivo);
            }
            System.out.println("------------------------------\n");
        } else {
            System.out.println("Servidor: " + respuesta);
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

    private static void manejarListaUsuarios(BufferedReader entrada) throws IOException {
        String respuesta = entrada.readLine();
        if (respuesta.equals("LISTA_USUARIOS:")) {
            System.out.println("\n--- Usuarios registrados ---");
            String usuario;
            while (!(usuario = entrada.readLine()).equals("FIN_LISTA:")) {
                System.out.println("- " + usuario);
            }
            System.out.println("----------------------------\n");
        } else {
            System.out.println("Servidor: " + respuesta);
        }
    }

    private static void chatearConServidor(BufferedReader entrada, PrintWriter salida, Scanner scanner) throws IOException {
        boolean enChat = true;
        while (enChat) {
            String chatMenuPrompt;
            while ((chatMenuPrompt = entrada.readLine()) != null && !chatMenuPrompt.startsWith("Por favor")) {
                System.out.println(chatMenuPrompt);
            }
            System.out.println(chatMenuPrompt);

            System.out.print("Tu eleccion: ");
            String opcion = scanner.nextLine();
            salida.println(opcion);

            switch (opcion) {
                case "1":
                    String respuesta = entrada.readLine();
                    System.out.println("Servidor: " + respuesta);
                    if (respuesta.startsWith("MENSAJE_DESTINATARIO")) {
                        System.out.print("Destinatario: ");
                        String destinatario = scanner.nextLine();
                        salida.println(destinatario);

                        String respuesta2 = entrada.readLine();
                        System.out.println("Servidor: " + respuesta2);

                        if (respuesta2.startsWith("MENSAJE_CONTENIDO")) {
                            System.out.print("Mensaje: ");
                            String contenido = scanner.nextLine();
                            salida.println(contenido);
                            System.out.println("Servidor: " + entrada.readLine());
                        }
                    }
                    break;
                case "2":
                    manejarLecturaPaginada(entrada, salida, scanner);
                    break;
                case "3":
                    manejarEliminacionPaginada(entrada, salida, scanner);
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

    private static void manejarEliminacionPaginada(BufferedReader entrada, PrintWriter salida, Scanner scanner) throws IOException {
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

                if (respuestaServidor.startsWith("MENSAJE_SALIDA_ELIMINAR") || respuestaServidor.startsWith("MENSAJE_ELIMINADO_EXITO")) {
                    enPaginacion = false;
                }

            } else if (estadoPaginacion.startsWith("INFO: No tienes mensajes para eliminar.")) {
                System.out.println("Servidor: " + estadoPaginacion);
                enPaginacion = false;
            } else {
                System.out.println("Servidor: " + estadoPaginacion);
                enPaginacion = false;
            }
        }
    }

    private static void manejarBloqueo(BufferedReader entrada, PrintWriter salida, Scanner scanner) throws IOException {
        boolean enBloqueoMenu = true;
        while (enBloqueoMenu) {
            String bloqueoMenuPrompt;
            while ((bloqueoMenuPrompt = entrada.readLine()) != null && !bloqueoMenuPrompt.startsWith("Por favor")) {
                System.out.println(bloqueoMenuPrompt);
            }
            System.out.println(bloqueoMenuPrompt);

            System.out.print("Tu eleccion: ");
            String opcion = scanner.nextLine();
            salida.println(opcion);

            String respuestaServidor = "";
            switch (opcion) {
                case "1":
                    respuestaServidor = entrada.readLine();
                    System.out.println("Servidor: " + respuestaServidor);
                    if (respuestaServidor.startsWith("BLOQUEO_USUARIO")) {
                        System.out.print("Usuario a bloquear: ");
                        String aBloquear = scanner.nextLine();
                        salida.println(aBloquear);
                        respuestaServidor = entrada.readLine();
                        System.out.println("Servidor: " + respuestaServidor);
                    }
                    break;
                case "2":
                    respuestaServidor = entrada.readLine();
                    System.out.println("Servidor: " + respuestaServidor);
                    if (respuestaServidor.startsWith("DESBLOQUEO_USUARIO")) {
                        System.out.print("Usuario a desbloquear: ");
                        String aDesbloquear = scanner.nextLine();
                        salida.println(aDesbloquear);
                        respuestaServidor = entrada.readLine();
                        System.out.println("Servidor: " + respuestaServidor);
                    }
                    break;
                case "3":
                    manejarListaBloqueados(entrada);
                    break;
                case "4":
                    respuestaServidor = entrada.readLine();
                    System.out.println("Servidor: " + respuestaServidor);
                    if (respuestaServidor.startsWith("BLOQUEO_SALIDA")) {
                        enBloqueoMenu = false;
                    }
                    break;
                default:
                    respuestaServidor = entrada.readLine();
                    System.out.println("Servidor: " + respuestaServidor);
                    break;
            }
        }
    }

    private static void manejarListaBloqueados(BufferedReader entrada) throws IOException {
        String respuesta = entrada.readLine();
        if (respuesta.equals("LISTA_BLOQUEADOS:")) {
            System.out.println("\n--- Usuarios bloqueados ---");
            String usuario;
            while (!(usuario = entrada.readLine()).equals("FIN_LISTA_BLOQUEADOS:")) {
                if (usuario.startsWith("INFO:")) {
                    System.out.println(usuario.substring(6));
                    break;
                }
                System.out.println(usuario);
            }
            System.out.println("---------------------------\n");
        } else {
            System.out.println("Servidor: " + respuesta);
        }
    }
}