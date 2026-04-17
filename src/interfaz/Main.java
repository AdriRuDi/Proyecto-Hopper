package interfaz;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {

        EstadoSimulacion estado = new EstadoSimulacion();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                InterfazServidor interfaz = new InterfazServidor();
                interfaz.setVisible(true);

                lanzarRefrescoInterfaz(interfaz, estado);
            }
        });

        crearYArrancarNinosIniciales(estado, 8);
    }

    private static void crearYArrancarNinosIniciales(EstadoSimulacion estado, int cantidad) {
        for (int i = 1; i <= cantidad; i++) {
            String id = String.format("N%04d", i);

            Nino nino = new Nino(id, estado);
            estado.getZona("CALLE_PRINCIPAL").entrarNino(nino);

            Logger.log("Se crea el niño " + id + " en CALLE_PRINCIPAL");

            nino.start();
        }
    }

    private static void lanzarRefrescoInterfaz(InterfazServidor interfaz, EstadoSimulacion estado) {
        Thread refrescador = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                interfaz.updateSnapshot(estado.crearSnapshot());
                            }
                        });

                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        Logger.log("El hilo de refresco de interfaz fue interrumpido");
                        break;
                    }
                }
            }
        });

        refrescador.setDaemon(true);
        refrescador.start();
    }
}