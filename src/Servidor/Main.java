package Servidor;

public class Main {

    public static void main(String[] args) {
        try {
            EstadoSimulacion estado = new EstadoSimulacion();

            InterfazServidor gui = new InterfazServidor();
            gui.setVisible(true);

            Thread refrescador = new Thread(() -> {
                while (true) {
                    try {
                        gui.updateSnapshot(estado.crearSnapshot());
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

            // aquí ya puedes arrancar niños, demogorgons, eventos, etc.

        } catch (Exception e) {
            Logger.log("Error en servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}