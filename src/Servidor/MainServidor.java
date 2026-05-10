package Servidor;

import javax.swing.*;

public class MainServidor {

    public static void main(String[] args) {
        try {
            EstadoSimulacion estado = new EstadoSimulacion();

            InterfazServidor gui = new InterfazServidor();
            gui.setVisible(true);

            Thread refrescador = new Thread(() -> {
                while (true) {
                    try {
                        InterfazServidor.SimulationSnapshot snapshot = estado.crearSnapshot();

                        SwingUtilities.invokeLater(() -> {
                            gui.updateSnapshot(snapshot);
                        });

                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });

            refrescador.setDaemon(true);
            refrescador.start();

            ServicioHawkins servicio = new ServicioHawkins(estado);

            java.rmi.registry.LocateRegistry.createRegistry(1099);
            java.rmi.Naming.rebind("//127.0.0.1/ServicioHawkins", servicio);

            Logger.log("ServicioHawkins registrado correctamente");

            String zonaInicialAlpha = estado.elegirZonaPeligrosaAleatoria();

            Demogorgon alpha = new Demogorgon("D0000", estado, zonaInicialAlpha);   //Se crea único demogorgon inicial
            estado.registrarDemogorgon(alpha);  //Se registra para que pueda aparecer en el ranking
            alpha.start();

            GeneradorNinos generadorNinos = new GeneradorNinos(estado,1500);
            generadorNinos.start();

            GestorEventos gestorEventos = new GestorEventos(estado);
            gestorEventos.start();

        } catch (Exception e) {
            Logger.log("Error en servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}