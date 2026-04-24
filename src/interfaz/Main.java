package interfaz;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        EstadoSimulacion estado = new EstadoSimulacion();

        final InterfazServidor[] guiRef = new InterfazServidor[1];

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                guiRef[0] = new InterfazServidor();
                guiRef[0].setVisible(true);
            }
        });

        Thread generadorNinos = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 1; i <= 10; i++) {
                    String idNino = String.format("N%04d", i);

                    try {
                        Thread.sleep(500 + (int)(Math.random() * 1500));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }

                    Nino nino = new Nino(idNino, estado);
                    estado.getZona("CALLE_PRINCIPAL").entrarNino(nino);
                    Logger.log("Se crea el niño " + idNino + " en CALLE_PRINCIPAL");
                    nino.start();
                }
            }
        });

        generadorNinos.start();

        Demogorgon demogorgonInicial = new Demogorgon("D0000", estado, "BOSQUE");
        demogorgonInicial.start();

        Thread refrescador = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (guiRef[0] != null) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    guiRef[0].updateSnapshot(estado.crearSnapshot());
                                }
                            });
                        }

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