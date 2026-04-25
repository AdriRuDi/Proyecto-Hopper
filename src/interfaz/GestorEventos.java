package interfaz;

public class GestorEventos extends Thread {

    private final EstadoSimulacion estado;

    public GestorEventos(EstadoSimulacion estado) {
        this.estado = estado;
    }

    @Override
    public void run() {
        while (true) {
            try {
                int espera = 30000 + (int)(Math.random() * 30000);
                Thread.sleep(espera);

                int tipoEvento = (int)(Math.random() * 2);

                if (tipoEvento == 0) {
                    ejecutarApagon();
                } else {
                    ejecutarTormenta();
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Logger.log("El gestor de eventos ha sido interrumpido");
                break;
            }
        }
    }

    private void ejecutarApagon() throws InterruptedException {
        int duracion = 5000 + (int)(Math.random() * 5000);

        Logger.log("Comienza el evento global: APAGON DEL LABORATORIO");
        estado.activarApagon();

        estado.getPortal("BOSQUE").activarApagon();
        estado.getPortal("LABORATORIO").activarApagon();
        estado.getPortal("CENTRO_COMERCIAL").activarApagon();
        estado.getPortal("ALCANTARILLADO").activarApagon();

        Thread.sleep(duracion);

        estado.getPortal("BOSQUE").desactivarApagon();
        estado.getPortal("LABORATORIO").desactivarApagon();
        estado.getPortal("CENTRO_COMERCIAL").desactivarApagon();
        estado.getPortal("ALCANTARILLADO").desactivarApagon();

        estado.desactivarApagon();
        Logger.log("Finaliza el evento global: APAGON DEL LABORATORIO");
    }

    private void ejecutarTormenta() throws InterruptedException {
        int duracion = 5000 + (int)(Math.random() * 5000);

        Logger.log("Comienza el evento global: TORMENTA DEL UPSIDE DOWN");
        estado.activarTormenta();

        Thread.sleep(duracion);

        estado.desactivarTormenta();
        Logger.log("Finaliza el evento global: TORMENTA DEL UPSIDE DOWN");
    }
}