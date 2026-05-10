package Servidor;

public class Main {

    public static void main(String[] args) {
        try {
            EstadoSimulacion estado = new EstadoSimulacion();

            InterfazServidor gui = new InterfazServidor();
            gui.setVisible(true);

            Thread refrescador = new Thread(() -> {
                while (true) { //La interfaz se actualiza continuamente miesntras dura la simulacion
                    try {
                        gui.updateSnapshot(estado.crearSnapshot());
                        Thread.sleep(500); //Se refresca cada 0.5 segundos
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

            GeneradorNinos generadorNinos = new GeneradorNinos(estado,100);
            generadorNinos.start();

            GestorEventos gestorEventos = new GestorEventos(estado);
            gestorEventos.start();

        } catch (Exception e) {
            Logger.log("Error en servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}