import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class servidor {
    private static final String USUARIOS = "usuarios.txt";
    private static final String MENSAJES = "mensajes.txt";
    private static final String BLOQUEADOS = "bloqueados.txt";
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
                salida.println("MENU:");
                salida.println("1. Jugar a Adivina el Numero");
                salida.println("2. Ver usuarios registrados");
                salida.println("3. Mensajeria");
                salida.println("4. Eliminar mi cuenta");
                salida.println("5. Bloquear/Desbloquear Usuario");
                salida.println("6. Salir");
                salida.println("Por favor, ingresa el numero de la opcion que desees.");
                String opcionStr = entrada.readLine();

                if (opcionStr == null) {
                    seguirEnSesion = false;
                    continue;
                }

                try {
                    int opcion = Integer.parseInt(opcionStr);
                    switch (opcion) {
                        case 1:
                            jugarAdivinaNumero(entrada, salida);
                            break;
                        case 2:
                            salida.println("LISTA_USUARIOS:");
                            List<String> usuarios = listarUsuarios();
                            for (String user : usuarios) {
                                salida.println(user);
                            }
                            salida.println("FIN_LISTA:");
                            break;
                        case 3:
                            manejarMensajeria(entrada, salida, usuarioLogueado);
                            break;
                        case 4:
                            salida.println("CONFIRMACION: ¿Estas seguro de que quieres eliminar tu usuario? (si/no)");
                            String confirmacion = entrada.readLine();
                            if (confirmacion != null && confirmacion.equalsIgnoreCase("si")) {
                                if (eliminarUsuario(usuarioLogueado)) {
                                    salida.println("USUARIO_ELIMINADO: Tu usuario y todos sus datos han sido eliminados.");
                                    seguirEnSesion = false;
                                } else {
                                    salida.println("ERROR: No se pudo eliminar el usuario.");
                                }
                            } else {
                                salida.println("OPERACION_CANCELADA: La eliminacion del usuario ha sido cancelada.");
                            }
                            break;
                        case 5:
                            manejarBloqueoUsuarios(entrada, salida, usuarioLogueado);
                            break;
                        case 6:
                            salida.println("Sesion cerrada. Adios.");
                            seguirEnSesion = false;
                            break;
                        default:
                            salida.println("OPCION_INVALIDA: Opcion no valida. Por favor, elige un numero del 1 al 6.");
                            break;
                    }
                } catch (NumberFormatException e) {
                    salida.println("ERROR: Ingresa un numero valido para la opcion.");
                }
            }

            socket.close();
            System.out.println("Conexion cerrada.");
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
                    salida.println("Caracter no valido. Ingresa un numero entre 1 y 10.");
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

    private static List<String> listarUsuarios() {
        List<String> listaUsuarios = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(USUARIOS))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");
                if (partes.length >= 1) {
                    listaUsuarios.add(partes[0].trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo de usuarios: " + e.getMessage());
        }
        return listaUsuarios;
    }

    private static boolean usuarioExiste(String usuario) {
        try (BufferedReader br = new BufferedReader(new FileReader(USUARIOS))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");
                if (partes.length >= 1 && partes[0].trim().equals(usuario)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo de usuarios: " + e.getMessage());
        }
        return false;
    }

    private static void manejarMensajeria(BufferedReader entrada, PrintWriter salida, String usuarioLogueado) throws IOException {
        boolean enMensajeria = true;
        while (enMensajeria) {
            salida.println("CHAT_MENU:");
            salida.println("1. Enviar mensaje");
            salida.println("2. Leer mensajes");
            salida.println("3. Eliminar mensaje");
            salida.println("4. Volver al menu principal");
            salida.println("Por favor, ingresa el numero de la opcion que desees.");

            String comandoStr = entrada.readLine();
            if (comandoStr == null) {
                enMensajeria = false;
                continue;
            }

            try {
                int comando = Integer.parseInt(comandoStr);
                switch (comando) {
                    case 1:
                        salida.println("MENSAJE_DESTINATARIO: Ingresa el usuario destinatario.");
                        String destinatario = entrada.readLine();
                        if (destinatario != null) {
                            if (usuarioExiste(destinatario)) {
                                if (estaBloqueadoPor(usuarioLogueado, destinatario)) {
                                    salida.println("ERROR: No puedes enviar mensajes a este usuario. Te ha bloqueado.");
                                } else {
                                    salida.println("MENSAJE_CONTENIDO: Ingresa tu mensaje.");
                                    String contenido = entrada.readLine();
                                    if (contenido != null) {
                                        guardarMensaje(usuarioLogueado, destinatario, contenido);
                                        salida.println("MENSAJE_ENVIADO: Mensaje enviado exitosamente.");
                                    } else {
                                        salida.println("ERROR: No se recibió el contenido del mensaje.");
                                    }
                                }
                            } else {
                                salida.println("ERROR: El usuario '" + destinatario + "' no está registrado.");
                            }
                        }
                        break;
                    case 2:
                        leerMensajes(entrada, salida, usuarioLogueado);
                        break;
                    case 3:
                        eliminarMensaje(entrada, salida, usuarioLogueado);
                        break;
                    case 4:
                        salida.println("MENSAJE_SALIDA: Saliendo de la mensajeria.");
                        enMensajeria = false;
                        break;
                    default:
                        salida.println("OPCION_INVALIDA: Comando no valido. Por favor, elige un numero del 1 al 4.");
                        break;
                }
            } catch (NumberFormatException e) {
                salida.println("ERROR: Opcion no valida. Ingresa un numero.");
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

    private static void leerMensajes(BufferedReader entrada, PrintWriter salida, String usuarioLogueado) throws IOException {
        List<String> mensajesDelUsuario = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(MENSAJES))) {
            String linea;
            int contador = 1;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(SEPARADOR);
                if (partes.length >= 3 && (partes[0].equals(usuarioLogueado) || partes[1].equals(usuarioLogueado))) {
                    mensajesDelUsuario.add("De: " + partes[0] + ", Para: " + partes[1] + ", Mensaje: " + partes[2]);
                }
            }
        } catch (IOException e) {
            salida.println("ERROR: No se pudo leer el archivo de mensajes.");
            return;
        }

        if (mensajesDelUsuario.isEmpty()) {
            salida.println("INFO: No tienes mensajes para leer.");
            return;
        }

        final int MENSAJES_POR_PAGINA = 10;
        int totalMensajes = mensajesDelUsuario.size();
        int totalPaginas = (int) Math.ceil((double) totalMensajes / MENSAJES_POR_PAGINA);
        int paginaActual = 1;

        boolean enPaginacion = true;
        while (enPaginacion) {
            int indiceInicio = (paginaActual - 1) * MENSAJES_POR_PAGINA;
            int indiceFin = Math.min(indiceInicio + MENSAJES_POR_PAGINA, totalMensajes);

            salida.println("PAGINACION_INICIO:" + paginaActual + ":" + totalPaginas);

            for (int i = indiceInicio; i < indiceFin; i++) {
                salida.println((i + 1) + ". " + mensajesDelUsuario.get(i));
            }
            salida.println("PAGINACION_FIN");
            salida.println("OPCIONES_LECTURA: (S)iguiente, (A)nterior, (V)olver");

            String comandoCliente = entrada.readLine();
            if (comandoCliente == null) {
                enPaginacion = false;
                continue;
            }

            if (comandoCliente.equalsIgnoreCase("S")) {
                if (paginaActual < totalPaginas) {
                    paginaActual++;
                    salida.println("PAGINA_CAMBIADA");
                } else {
                    salida.println("INFO: Ya estás en la última página.");
                }
            } else if (comandoCliente.equalsIgnoreCase("A")) {
                if (paginaActual > 1) {
                    paginaActual--;
                    salida.println("PAGINA_CAMBIADA");
                } else {
                    salida.println("INFO: Ya estás en la primera página.");
                }
            } else if (comandoCliente.equalsIgnoreCase("V")) {
                salida.println("MENSAJE_SALIDA_LECTURA: Saliendo de la lectura de mensajes.");
                enPaginacion = false;
            } else {
                salida.println("OPCION_INVALIDA: Comando no reconocido.");
            }
        }
    }

    private static void eliminarMensaje(BufferedReader entrada, PrintWriter salida, String usuarioLogueado) throws IOException {
        List<String> mensajesDelUsuario = new ArrayList<>();
        List<String> lineasOriginales = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(MENSAJES))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(SEPARADOR);
                if (partes.length >= 3 && (partes[0].equals(usuarioLogueado) || partes[1].equals(usuarioLogueado))) {
                    mensajesDelUsuario.add("De: " + partes[0] + ", Para: " + partes[1] + ", Mensaje: " + partes[2]);
                    lineasOriginales.add(linea);
                }
            }
        } catch (IOException e) {
            salida.println("ERROR: No se pudo leer el archivo de mensajes.");
            return;
        }

        if (mensajesDelUsuario.isEmpty()) {
            salida.println("INFO: No tienes mensajes para eliminar.");
            return;
        }

        final int MENSAJES_POR_PAGINA = 10;
        int totalMensajes = mensajesDelUsuario.size();
        int totalPaginas = (int) Math.ceil((double) totalMensajes / MENSAJES_POR_PAGINA);
        int paginaActual = 1;

        boolean enPaginacion = true;
        while (enPaginacion) {
            int indiceInicio = (paginaActual - 1) * MENSAJES_POR_PAGINA;
            int indiceFin = Math.min(indiceInicio + MENSAJES_POR_PAGINA, totalMensajes);

            salida.println("PAGINACION_INICIO:" + paginaActual + ":" + totalPaginas);

            for (int i = indiceInicio; i < indiceFin; i++) {
                salida.println((i + 1) + ". " + mensajesDelUsuario.get(i));
            }
            salida.println("PAGINACION_FIN");
            salida.println("OPCIONES_PAGINACION: (S)iguiente, (A)nterior, (E)liminar [numero], (V)olver");

            String comandoCliente = entrada.readLine();
            if (comandoCliente == null) {
                enPaginacion = false;
                continue;
            }

            if (comandoCliente.equalsIgnoreCase("S")) {
                if (paginaActual < totalPaginas) {
                    paginaActual++;
                    salida.println("PAGINA_CAMBIADA");
                } else {
                    salida.println("INFO: Ya estás en la última página.");
                }
            } else if (comandoCliente.equalsIgnoreCase("A")) {
                if (paginaActual > 1) {
                    paginaActual--;
                    salida.println("PAGINA_CAMBIADA");
                } else {
                    salida.println("INFO: Ya estás en la primera página.");
                }
            } else if (comandoCliente.toLowerCase().startsWith("e")) {
                try {
                    int idMensaje = Integer.parseInt(comandoCliente.substring(1).trim());
                    if (idMensaje > 0 && idMensaje <= totalMensajes) {
                        String lineaAEliminar = lineasOriginales.get(idMensaje - 1);
                        if (eliminarLineaDeArchivo(lineaAEliminar, MENSAJES)) {
                            salida.println("MENSAJE_ELIMINADO_EXITO: Mensaje eliminado exitosamente.");
                            enPaginacion = false;
                            break;
                        } else {
                            salida.println("ERROR: No se pudo eliminar el mensaje.");
                        }
                    } else {
                        salida.println("ERROR: Número de mensaje no válido.");
                    }
                } catch (NumberFormatException e) {
                    salida.println("ERROR: Comando de eliminación no válido. Usa 'E' seguido del número.");
                }
            } else if (comandoCliente.equalsIgnoreCase("V")) {
                salida.println("MENSAJE_SALIDA_ELIMINAR: Saliendo de la eliminación de mensajes.");
                enPaginacion = false;
            } else {
                salida.println("OPCION_INVALIDA: Comando no reconocido.");
            }
        }
    }

    private static boolean eliminarUsuario(String usuario) {
        if (eliminarUsuarioDeArchivo(usuario) && eliminarMensajesDeUsuario(usuario) && eliminarBloqueosRelacionados(usuario)) {
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

    private static boolean estaBloqueadoPor(String remitente, String destinatario) {
        try (BufferedReader br = new BufferedReader(new FileReader(BLOQUEADOS))) {
            String linea;
            String lineaBusqueda = destinatario + SEPARADOR + remitente;
            while ((linea = br.readLine()) != null) {
                if (linea.equals(lineaBusqueda)) {
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            System.err.println("Error al leer el archivo de bloqueos: " + e.getMessage());
        }
        return false;
    }

    private static boolean usuarioBloqueoAUsuario(String usuarioA, String usuarioB) {
        try (BufferedReader br = new BufferedReader(new FileReader(BLOQUEADOS))) {
            String linea;
            String lineaBusqueda = usuarioA + SEPARADOR + usuarioB;
            while ((linea = br.readLine()) != null) {
                if (linea.equals(lineaBusqueda)) {
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            System.err.println("Error al leer el archivo de bloqueos: " + e.getMessage());
        }
        return false;
    }

    private static boolean bloquearUsuario(String usuarioA, String usuarioB) {
        if (usuarioA.equals(usuarioB)) return false;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(BLOQUEADOS, true))) {
            bw.write(usuarioA + SEPARADOR + usuarioB);
            bw.newLine();
            return true;
        } catch (IOException e) {
            System.err.println("Error al bloquear usuario: " + e.getMessage());
            return false;
        }
    }

    private static boolean desbloquearUsuario(String usuarioA, String usuarioB) {
        String lineaAEliminar = usuarioA + SEPARADOR + usuarioB;
        return eliminarLineaDeArchivo(lineaAEliminar, BLOQUEADOS);
    }

    private static boolean eliminarBloqueosRelacionados(String usuarioAEliminar) {
        File inputFile = new File(BLOQUEADOS);
        if (!inputFile.exists()) return true;

        File tempFile = new File("temp_" + BLOQUEADOS);
        boolean bloqueosEncontrados = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String lineaActual;
            while ((lineaActual = reader.readLine()) != null) {
                String[] partes = lineaActual.split(SEPARADOR);
                if (partes.length == 2 && (partes[0].equals(usuarioAEliminar) || partes[1].equals(usuarioAEliminar))) {
                    bloqueosEncontrados = true;
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

    private static void manejarBloqueoUsuarios(BufferedReader entrada, PrintWriter salida, String usuarioLogueado) throws IOException {
        boolean enBloqueoMenu = true;
        while (enBloqueoMenu) {
            salida.println("BLOQUEO_MENU:");
            salida.println("1. Bloquear usuario");
            salida.println("2. Desbloquear usuario");
            salida.println("3. Ver usuarios bloqueados");
            salida.println("4. Volver al menu principal");
            salida.println("Por favor, ingresa el numero de la opcion que desees.");

            String opcionStr = entrada.readLine();
            if (opcionStr == null) {
                enBloqueoMenu = false;
                continue;
            }

            try {
                int opcion = Integer.parseInt(opcionStr);
                switch (opcion) {
                    case 1:
                        salida.println("BLOQUEO_USUARIO: Ingresa el usuario a bloquear.");
                        String aBloquear = entrada.readLine();
                        if (aBloquear == null) break;

                        if (aBloquear.equals(usuarioLogueado)) {
                            salida.println("ERROR: No puedes bloquearte a ti mismo.");
                        } else if (!usuarioExiste(aBloquear)) {
                            salida.println("ERROR: El usuario '" + aBloquear + "' no existe.");
                        } else if (usuarioBloqueoAUsuario(usuarioLogueado, aBloquear)) {
                            salida.println("INFO: Ya bloqueaste a '" + aBloquear + "'.");
                        } else if (bloquearUsuario(usuarioLogueado, aBloquear)) {
                            salida.println("BLOQUEO_EXITOSO: Has bloqueado a '" + aBloquear + "'.");
                        } else {
                            salida.println("ERROR: No se pudo bloquear a '" + aBloquear + "'.");
                        }
                        break;
                    case 2:
                        salida.println("DESBLOQUEO_USUARIO: Ingresa el usuario a desbloquear.");
                        String aDesbloquear = entrada.readLine();
                        if (aDesbloquear == null) break;

                        if (aDesbloquear.equals(usuarioLogueado)) {
                            salida.println("ERROR: No puedes desbloquearte a ti mismo (no tiene sentido).");
                        } else if (!usuarioExiste(aDesbloquear)) {
                            salida.println("ERROR: El usuario '" + aDesbloquear + "' no existe.");
                        } else if (!usuarioBloqueoAUsuario(usuarioLogueado, aDesbloquear)) {
                            salida.println("INFO: No tienes bloqueado a '" + aDesbloquear + "'.");
                        } else if (desbloquearUsuario(usuarioLogueado, aDesbloquear)) {
                            salida.println("DESBLOQUEO_EXITOSO: Has desbloqueado a '" + aDesbloquear + "'.");
                        } else {
                            salida.println("ERROR: No se pudo desbloquear a '" + aDesbloquear + "'.");
                        }
                        break;
                    case 3:
                        verUsuariosBloqueados(salida, usuarioLogueado);
                        break;
                    case 4:
                        salida.println("BLOQUEO_SALIDA: Volviendo al menu principal.");
                        enBloqueoMenu = false;
                        break;
                    default:
                        salida.println("OPCION_INVALIDA: Opcion no valida. Por favor, elige un numero del 1 al 4.");
                        break;
                }
            } catch (NumberFormatException e) {
                salida.println("ERROR: Ingresa un numero valido para la opcion.");
            }
        }
    }

    private static void verUsuariosBloqueados(PrintWriter salida, String usuarioLogueado) {
        List<String> bloqueados = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(BLOQUEADOS))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(SEPARADOR);
                if (partes.length == 2 && partes[0].equals(usuarioLogueado)) {
                    bloqueados.add(partes[1]);
                }
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            System.err.println("Error al leer el archivo de bloqueos: " + e.getMessage());
            salida.println("ERROR: Error al leer la lista de bloqueos.");
            return;
        }

        salida.println("LISTA_BLOQUEADOS:");
        if (bloqueados.isEmpty()) {
            salida.println("INFO: No tienes usuarios bloqueados.");
        } else {
            for (String bloqueado : bloqueados) {
                salida.println("- " + bloqueado);
            }
        }
        salida.println("FIN_LISTA_BLOQUEADOS:");
    }
}