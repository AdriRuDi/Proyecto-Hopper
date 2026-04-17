package interfaz;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class Logger {

    private static final Lock lock = new ReentrantLock();
    private static BufferedWriter writer;

    static { // Usamos static para no crear muchos objetos buffer y hacerlo directamente en la clase
        try {
            writer = new BufferedWriter(new FileWriter("hawkins.txt", true)); //El true hace que lo escriba al final
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void log(String mensaje) {
        lock.lock();
        try {
            String tiempoActual = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")); // Pasa la fecha y la hora a un formato mas legible

            String linea = tiempoActual + " - " + mensaje;

            System.out.println(linea); // Imprime por consola
            //Lo escribe en el fichero
            writer.write(linea);
            writer.newLine();
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}

