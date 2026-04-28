package interfaz;

import javax.swing.SwingUtilities;
import java.util.concurrent.CountDownLatch;

public class Main {

    public static void main(String[] args) {
        EstadoSimulacion estado = new EstadoSimulacion();

        final InterfazServidor[] guiRef = new InterfazServidor[1];
        final CountDownLatch latchInterfaz = new CountDownLatch(1);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                guiRef[0] = new InterfazServidor();
                guiRef[0].setVisible(true);
                latchInterfaz.countDown();
            }
        });

        try {
            latchInterfaz.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        GeneradorNinos generadorNinos = new GeneradorNinos(estado, 30);
        generadorNinos.start();

        Demogorgon demogorgonInicial = new Demogorgon("D0000", estado, "BOSQUE");
        demogorgonInicial.start();

        GestorEventos gestorEventos = new GestorEventos(estado);
        gestorEventos.start();

        Thread refrescador = new Thread(new Runnable() {
            @Override
            public void run() {

                while (true) {
                    try {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                guiRef[0].updateSnapshot(estado.crearSnapshot());
                            }
                        });

                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });

        refrescador.setDaemon(true);
        refrescador.start();
    }
}