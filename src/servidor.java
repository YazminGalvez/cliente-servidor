import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

public class servidor {
    private static final String USUARIOS = "usuarios.txt";
    private static final String MENSAJES = "mensajes.txt";
    private static final String SEPARADOR = "::";

    public static void main(String[] args) {
        int puerto = 8080;
        try (ServerSocket servidor = new ServerSocket(puerto)) {
            System.out.println("Servidor iniciado. Esperando cliente...");
            Socket socket = servidor.accept();
            System.out.println("Cliente conectado.");

            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);

            String usuarioLogueado = "";
            boolean loggedIn = false;
            while (!loggedIn) {
                String usuario = entrada.readLine();
                String contrasena = entrada.readLine();

                if (usuario == null || contrasena == null) {
                    salida.println("LOGIN_FALLIDO: Datos no recibidos.");
                    continue;
                }
                usuarioLogueado = usuario;

                int resultado = validarUsuario(usuario, contrasena);

                if (resultado == 1) {
                    salida.println("LOGIN_EXITOSO");
                    loggedIn = true;
                } else if (resultado == 0) {
                    salida.println("LOGIN_FALLIDO: Contraseña incorrecta.");
                } else if (resultado == -1) {
                    salida.println("USUARIO_NO_EXISTE: El usuario no existe.");
                    String respuestaCliente = entrada.readLine();
                    if (respuestaCliente != null && respuestaCliente.equalsIgnoreCase("si")) {
                        if (registrarUsuario(usuario, contrasena)) {
                            salida.println("REGISTRO_EXITOSO");
                            loggedIn = true;
                        } else {
                            salida.println("REGISTRO_FALLIDO: Error al guardar usuario.");
                        }
                    }
                }
            }

            boolean seguirEnSesion = true;
            while (seguirEnSesion) {
                salida.println("MENU: ¿Qué quieres hacer? (jugar/mensaje/eliminar/salir)");
                String opcion = entrada.readLine();

                if (opcion == null) {
                    seguirEnSesion = false;
                    continue;
                }

                if (opcion.equalsIgnoreCase("jugar")) {
                    jugarAdivinaNumero(entrada, salida);
                } else if (opcion.equalsIgnoreCase("mensaje")) {
                    manejarMensajeria(entrada, salida, usuarioLogueado);
                } else if (opcion.equalsIgnoreCase("eliminar")) {
                    salida.println("CONFIRMACION: ¿Estás seguro de que quieres eliminar tu usuario? (si/no)");
                    String confirmacion = entrada.readLine();
                    if (confirmacion != null && confirmacion.equalsIgnoreCase("si")) {
                        if (eliminarUsuario(usuarioLogueado)) {
                            salida.println("USUARIO_ELIMINADO: Tu usuario y todos sus datos han sido eliminados.");
                            seguirEnSesion = false;
                        } else {
                            salida.println("ERROR: No se pudo eliminar el usuario.");
                        }
                    } else {
                        salida.println("OPERACION_CANCELADA: La eliminación del usuario ha sido cancelada.");
                    }
                } else if (opcion.equalsIgnoreCase("salir")) {
                    salida.println("Sesión cerrada. Adiós.");
                    seguirEnSesion = false;
                } else {
                    salida.println("Opción no válida. Por favor, elige 'jugar', 'mensaje', 'eliminar' o 'salir'.");
                }
            }

            socket.close();
            System.out.println("Conexión cerrada.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int validarUsuario(String usuario, String contrasena) {
        try (BufferedReader br = new BufferedReader(new FileReader(USUARIOS))) {
            String linea;
            boolean usuarioEncontrado = false;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");
                if (partes.length == 2) {
                    String userEnArchivo = partes[0].trim();
                    String passEnArchivo = partes[1].trim();
                    if (userEnArchivo.equals(usuario)) {
                        usuarioEncontrado = true;
                        if (passEnArchivo.equals(contrasena)) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                }
            }
            if (!usuarioEncontrado) {
                return -1;
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo de usuarios: " + e.getMessage());
        }
        return -2;
    }

    private static boolean registrarUsuario(String usuario, String contrasena) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USUARIOS, true))) {
            bw.write(usuario + "," + contrasena);
            bw.newLine();
            return true;
        } catch (IOException e) {
            System.err.println("Error al registrar el usuario: " + e.getMessage());
            return false;
        }
    }

    private static void jugarAdivinaNumero(BufferedReader entrada, PrintWriter salida) throws IOException {
        Random rand = new Random();
        boolean seguirJugando = true;

        while (seguirJugando) {
            int secreto = rand.nextInt(10) + 1;
            int intentos = 0;
            salida.println("JUEGO: Bienvenido. Adivina un numero del 1 al 10. Tienes 3 intentos.");

            boolean juegoActivo = true;
            while (juegoActivo) {
                String mensaje = entrada.readLine();
                if (mensaje == null) {
                    juegoActivo = false;
                    seguirJugando = false;
                    continue;
                }

                int numero;
                try {
                    numero = Integer.parseInt(mensaje);
                } catch (NumberFormatException e) {
                    salida.println("Caracter no válido. Ingresa un numero entre 1 y 10.");
                    continue;
                }

                if (numero < 1 || numero > 10) {
                    salida.println("El numero debe estar entre 1 y 10.");
                    continue;
                }

                intentos++;

                if (numero == secreto) {
                    salida.println("¡GANASTE! El numero era: " + secreto);
                    juegoActivo = false;
                } else if (intentos >= 3) {
                    salida.println("PERDISTE. El numero correcto era: " + secreto);
                    juegoActivo = false;
                } else if (numero < secreto) {
                    salida.println("El numero es mayor. Intentos restantes: " + (3 - intentos));
                } else {
                    salida.println("El numero es menor. Intentos restantes: " + (3 - intentos));
                }
            }

            salida.println("JUEGO_TERMINADO: ¿Quieres jugar otra vez? (si/no)");
            String respuesta = entrada.readLine();
            if (respuesta == null || respuesta.equalsIgnoreCase("no")) {
                salida.println("JUEGO_SALIDA: Gracias por jugar.");
                seguirJugando = false;
            }
        }
    }

    private static void manejarMensajeria(BufferedReader entrada, PrintWriter salida, String usuarioLogueado) throws IOException {
        salida.println("CHAT_MENU: Elige una opción: (enviar/leer/eliminar/volver)");
        boolean enMensajeria = true;
        while (enMensajeria) {
            String comando = entrada.readLine();
            if (comando == null) {
                enMensajeria = false;
                continue;
            }
            if (comando.equalsIgnoreCase("enviar")) {
                salida.println("MENSAJE_DESTINATARIO: Ingresa el usuario destinatario.");
                String destinatario = entrada.readLine();
                salida.println("MENSAJE_CONTENIDO: Ingresa tu mensaje.");
                String contenido = entrada.readLine();
                if (destinatario != null && contenido != null) {
                    guardarMensaje(usuarioLogueado, destinatario, contenido);
                    salida.println("MENSAJE_ENVIADO: Mensaje enviado exitosamente.");
                } else {
                    salida.println("ERROR: Datos no válidos.");
                }
            } else if (comando.equalsIgnoreCase("leer")) {
                salida.println("MENSAJES_RECIBIDOS:");
                leerMensajes(salida, usuarioLogueado);
                salida.println("MENSAJES_FIN");
            } else if (comando.equalsIgnoreCase("eliminar")) {
                eliminarMensaje(entrada, salida, usuarioLogueado);
            } else if (comando.equalsIgnoreCase("volver")) {
                salida.println("MENSAJE_SALIDA: Saliendo de la mensajería.");
                enMensajeria = false;
            } else {
                salida.println("OPCION_INVALIDA: Comando no válido.");
            }
        }
    }

    private static void guardarMensaje(String remitente, String destinatario, String mensaje) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(MENSAJES, true))) {
            bw.write(remitente + SEPARADOR + destinatario + SEPARADOR + mensaje);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error al guardar el mensaje: " + e.getMessage());
        }
    }

    private static void leerMensajes(PrintWriter salida, String usuarioLogueado) {
        try (BufferedReader br = new BufferedReader(new FileReader(MENSAJES))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(SEPARADOR);
                if (partes.length >= 3) {
                    String remitente = partes[0].trim();
                    String destinatario = partes[1].trim();
                    String mensaje = partes[2].trim();

                    if (remitente.equals(usuarioLogueado) || destinatario.equals(usuarioLogueado)) {
                        salida.println("De: " + remitente + ", Para: " + destinatario + ", Mensaje: " + mensaje);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer los mensajes: " + e.getMessage());
        }
    }

    private static void eliminarMensaje(BufferedReader entrada, PrintWriter salida, String usuarioLogueado) throws IOException {
        List<String> mensajesAMostrar = new ArrayList<>();
        List<String> lineasOriginales = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(MENSAJES))) {
            String linea;
            int contador = 1;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(SEPARADOR);
                lineasOriginales.add(linea);
                if (partes.length >= 3 && (partes[0].equals(usuarioLogueado) || partes[1].equals(usuarioLogueado))) {
                    mensajesAMostrar.add(contador + ". De: " + partes[0] + ", Para: " + partes[1] + ", Mensaje: " + partes[2]);
                    contador++;
                }
            }
        } catch (IOException e) {
            salida.println("ERROR: No se pudo leer el archivo de mensajes.");
            return;
        }

        if (mensajesAMostrar.isEmpty()) {
            salida.println("INFO: No tienes mensajes para eliminar.");
            return;
        }

        salida.println("LISTA_MENSAJES_ELIMINAR:");
        for (String msg : mensajesAMostrar) {
            salida.println(msg);
        }
        salida.println("LISTA_FIN");
        salida.println("ELIMINAR_MENSAJE_ID: Ingresa el número del mensaje que quieres eliminar o 0 para cancelar.");

        String idMensaje = entrada.readLine();
        try {
            int opcion = Integer.parseInt(idMensaje);
            if (opcion > 0 && opcion <= mensajesAMostrar.size()) {
                String mensajeSeleccionado = mensajesAMostrar.get(opcion - 1);
                String mensajeOriginal = null;
                try (BufferedReader br = new BufferedReader(new FileReader(MENSAJES))) {
                    String linea;
                    int contador = 0;
                    while ((linea = br.readLine()) != null) {
                        String[] partes = linea.split(SEPARADOR);
                        if (partes.length >= 3 && (partes[0].equals(usuarioLogueado) || partes[1].equals(usuarioLogueado))) {
                            if (contador == (opcion - 1)) {
                                mensajeOriginal = linea;
                                break;
                            }
                            contador++;
                        }
                    }
                }
                if (mensajeOriginal != null) {
                    if (eliminarLineaDeArchivo(mensajeOriginal, MENSAJES)) {
                        salida.println("MENSAJE_ELIMINADO_EXITO: Mensaje eliminado exitosamente.");
                    } else {
                        salida.println("ERROR: No se pudo eliminar el mensaje.");
                    }
                } else {
                    salida.println("ERROR: El mensaje no se encontró para su eliminación.");
                }
            } else if (opcion == 0) {
                salida.println("MENSAJE_ELIMINACION_CANCELADA: Operación cancelada.");
                return;
            }
        } catch (NumberFormatException e) {
            salida.println("ERROR: Opción no válida. Ingresa un número.");
        }
    }

    private static boolean eliminarUsuario(String usuario) {
        if (eliminarUsuarioDeArchivo(usuario) && eliminarMensajesDeUsuario(usuario)) {
            return true;
        }
        return false;
    }

    private static boolean eliminarUsuarioDeArchivo(String usuarioAEliminar) {
        File inputFile = new File(USUARIOS);
        File tempFile = new File("temp_" + USUARIOS);
        boolean usuarioEncontrado = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String lineaActual;
            while ((lineaActual = reader.readLine()) != null) {
                if (lineaActual.startsWith(usuarioAEliminar + ",")) {
                    usuarioEncontrado = true;
                    continue;
                }
                writer.write(lineaActual);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (usuarioEncontrado) {
            inputFile.delete();
            return tempFile.renameTo(inputFile);
        } else {
            tempFile.delete();
            return false;
        }
    }

    private static boolean eliminarMensajesDeUsuario(String usuario) {
        File inputFile = new File(MENSAJES);
        File tempFile = new File("temp_" + MENSAJES);

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String lineaActual;
            while ((lineaActual = reader.readLine()) != null) {
                String[] partes = lineaActual.split(SEPARADOR);
                if (partes.length >= 2 && (partes[0].equals(usuario) || partes[1].equals(usuario))) {
                    continue;
                }
                writer.write(lineaActual);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        inputFile.delete();
        return tempFile.renameTo(inputFile);
    }

    private static boolean eliminarLineaDeArchivo(String lineaAEliminar, String nombreArchivo) {
        File inputFile = new File(nombreArchivo);
        File tempFile = new File("temp_" + nombreArchivo);
        boolean exito = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String lineaActual;
            while ((lineaActual = reader.readLine()) != null) {
                if (lineaActual.equals(lineaAEliminar)) {
                    exito = true;
                    continue;
                }
                writer.write(lineaActual);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (exito) {
            inputFile.delete();
            return tempFile.renameTo(inputFile);
        } else {
            tempFile.delete();
            return false;
        }
    }
}