import java.io.*;
import java.net.*;
import java.util.Random;

public class servidor {
    private static final String USUARIOS = "usuarios.txt";

    public static void main(String[] args) {
        int puerto = 8080;
        try (ServerSocket servidor = new ServerSocket(puerto)) {
            System.out.println("Servidor iniciado. Esperando cliente...");
            Socket socket = servidor.accept();
            System.out.println("Cliente conectado.");

            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);

            boolean loggedIn = false;
            while (!loggedIn) {
                String usuario = entrada.readLine();
                String contrasena = entrada.readLine();

                if (usuario == null || contrasena == null) {
                    salida.println("LOGIN_FALLIDO: Datos no recibidos.");
                    continue;
                }

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
                salida.println("MENU: ¿Qué quieres hacer? (jugar/mensaje/salir)");
                String opcion = entrada.readLine();

                if (opcion == null) {
                    seguirEnSesion = false;
                    continue;
                }

                if (opcion.equalsIgnoreCase("jugar")) {
                    jugarAdivinaNumero(entrada, salida);
                } else if (opcion.equalsIgnoreCase("mensaje")) {
                    chatearConCliente(entrada, salida);
                } else if (opcion.equalsIgnoreCase("salir")) {
                    salida.println("Sesión cerrada. Adiós.");
                    seguirEnSesion = false;
                } else {
                    salida.println("Opción no válida. Por favor, elige 'jugar', 'mensaje' o 'salir'.");
                }
            }

            socket.close();
            System.out.println("Conexión cerrada.");
        } catch (IOException e) {
            e.printStackTrace();
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

    private static void chatearConCliente(BufferedReader entrada, PrintWriter salida) throws IOException {
        salida.println("CHAT: Has entrado al modo de mensajería. Escribe un mensaje. Escribe 'salir' para volver al menú.");
        boolean enChat = true;
        while (enChat) {
            String mensaje = entrada.readLine();
            if (mensaje == null || mensaje.equalsIgnoreCase("salir")) {
                salida.println("CHAT_SALIDA: Saliendo del chat.");
                enChat = false;
                continue;
            }
            System.out.println("Mensaje del cliente: " + mensaje);
            salida.println("Mensaje recibido por el servidor.");
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
}