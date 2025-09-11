import java.io.*;
import java.net.*;
import java.util.Random;

public class servidor {
    public static void main(String[] args) {
        int puerto = 8080;
        try (ServerSocket servidor = new ServerSocket(puerto)) {
            System.out.println("Servidor iniciado. Esperando cliente...");
            Socket socket = servidor.accept();
            System.out.println("Cliente conectado.");

            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);

            Random rand = new Random();
            boolean seguirJugando = true;

            while (seguirJugando) {
                int secreto = rand.nextInt(10) + 1;
                int intentos = 0;
                String estadoActual = "INICIO";

                while (!estadoActual.equals("FIN") && !estadoActual.equals("PREGUNTAR_REINICIO")) {
                    if (estadoActual.equals("INICIO")) {
                        salida.println("Bienvenido. Adivina un numero del 1 al 10. Tienes 3 intentos.");
                        estadoActual = "JUGANDO";
                    } else if (estadoActual.equals("JUGANDO")) {

                        String mensaje = entrada.readLine();
                        if (mensaje == null) {
                            estadoActual = "FIN";
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
                            estadoActual = "GANASTE";
                        } else if (numero < secreto) {
                            if (intentos < 3) {
                                salida.println("El numero es mayor. Intentos restantes: " + (3 - intentos));
                            } else {
                                estadoActual = "PERDISTE";
                            }
                        } else {
                            if (intentos < 3) {
                                salida.println("El numero es menor. Intentos restantes: " + (3 - intentos));
                            } else {
                                estadoActual = "PERDISTE";
                            }
                        }
                    } else if (estadoActual.equals("GANASTE")) {
                        salida.println("¡ADIVINASTE! El numero era: " + secreto);
                        estadoActual = "PREGUNTAR_REINICIO";
                    } else {
                        salida.println("NO ADIVINASTE MENSO. El numero correcto era: " + secreto);
                        estadoActual = "PREGUNTAR_REINICIO";
                    }
                }

                salida.println("¿Quieres jugar otra vez? (si/no)");
                String respuesta = entrada.readLine();
                if (respuesta == null || respuesta.equalsIgnoreCase("no")) {
                    salida.println("Adiós, gracias por jugar.");
                    seguirJugando = false;
                }
            }

            socket.close();
            System.out.println("Conexión cerrada.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}