package Servidor;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class Logger {

    private static final Lock lock = new ReentrantLock();   //Lock compartido protege fichero hawkins.txt
    private static BufferedWriter writer;   //BufferedWriter compartido, static para que haya un único escritor para todo el programa

    static { // Usamos static para no crear muchos objetos buffer y hacerlo directamente en la clase
        try {   //Modo append, no borra el contenido anterior, sino que añade líneas al final
            writer = new BufferedWriter(new FileWriter("hawkins.txt", true)); //El true hace que lo escriba al final
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void log(String mensaje) {
        lock.lock();    // Solo un hilo puede escribir en el log a la vez
        try {
            String tiempoActual = LocalDateTime.now()   // Se genera la fecha y hora del evento
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")); // Pasa la fecha y la hora a un formato mas legible

            String linea = tiempoActual + " - " + mensaje;  // Formato común para todas las líneas del fichero

            System.out.println(linea); // Imprime por consola
            //Lo escribe en el fichero compartido
            writer.write(linea);
            writer.newLine();
            writer.flush(); // Fuerza que el mensaje se guarde inmediatamente en el fichero

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}

