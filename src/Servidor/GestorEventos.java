package Servidor;

public class GestorEventos extends Thread {

    private final EstadoSimulacion estado;

    public GestorEventos(EstadoSimulacion estado) {
        this.estado = estado;
    }

    @Override
    public void run() {
        while (true) {
            try {
                estado.esperarSiPausado();

                int espera = 30000 + (int)(Math.random() * 30000);
                estado.dormirConPausa(espera);

                estado.esperarSiPausado();

                int tipoEvento = (int)(Math.random() * 4);

                if (tipoEvento == 0) {
                    ejecutarApagon();
                } else if (tipoEvento == 1) {
                    ejecutarTormenta();
                } else if (tipoEvento == 2) {
                    ejecutarIntervencionEleven();
                } else {
                    ejecutarRedMental();
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Logger.log("El gestor de eventos ha sido interrumpido");
                break;
            }
        }
    }

    private void ejecutarApagon() {
        try {
            int duracion = 5000 + (int)(Math.random() * 5000);

            estado.esperarSiPausado();

            Logger.log("Comienza el evento global: APAGON DEL LABORATORIO");
            estado.activarApagon();

            estado.getPortal("BOSQUE").activarApagon();
            estado.getPortal("LABORATORIO").activarApagon();
            estado.getPortal("CENTRO_COMERCIAL").activarApagon();
            estado.getPortal("ALCANTARILLADO").activarApagon();

            estado.dormirEventoConCuentaAtras(duracion);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.log("ERROR: evento APAGON interrumpido: " + e.getMessage());

        } catch (Exception e) {
            Logger.log("ERROR en APAGON: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();

        } finally {
            estado.getPortal("BOSQUE").desactivarApagon();
            estado.getPortal("LABORATORIO").desactivarApagon();
            estado.getPortal("CENTRO_COMERCIAL").desactivarApagon();
            estado.getPortal("ALCANTARILLADO").desactivarApagon();

            estado.desactivarApagon();
            Logger.log("Finaliza el evento global: APAGON DEL LABORATORIO");
        }
    }

    private void ejecutarTormenta() {
        try {
            int duracion = 5000 + (int)(Math.random() * 5000);

            estado.esperarSiPausado();

            Logger.log("Comienza el evento global: TORMENTA DEL UPSIDE DOWN");
            estado.activarTormenta();

            estado.dormirEventoConCuentaAtras(duracion);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.log("ERROR: evento TORMENTA interrumpido: " + e.getMessage());

        } catch (Exception e) {
            Logger.log("ERROR en TORMENTA: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();

        } finally {
            estado.desactivarTormenta();
            Logger.log("Finaliza el evento global: TORMENTA DEL UPSIDE DOWN");
        }
    }

    private void ejecutarIntervencionEleven() {
        try {
            int duracion = 5000 + (int)(Math.random() * 5000);

            estado.esperarSiPausado();

            Logger.log("Comienza el evento global: INTERVENCION DE ELEVEN");
            estado.activarIntervencionEleven();

            estado.dormirEventoConCuentaAtras(duracion);

            estado.liberarNinosColmenaSegunSangreDisponible();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.log("ERROR: evento INTERVENCION ELEVEN interrumpido: " + e.getMessage());

        } catch (Exception e) {
            Logger.log("ERROR en INTERVENCION ELEVEN: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();

        } finally {
            estado.desactivarIntervencionEleven();
            Logger.log("Finaliza el evento global: INTERVENCION DE ELEVEN");
        }
    }

    private void ejecutarRedMental() {
        try {
            int duracion = 5000 + (int)(Math.random() * 5000);

            estado.esperarSiPausado();

            Logger.log("Comienza el evento global: LA RED MENTAL");
            estado.activarRedMental();

            estado.dormirEventoConCuentaAtras(duracion);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.log("ERROR: evento RED MENTAL interrumpido: " + e.getMessage());

        } catch (Exception e) {
            Logger.log("ERROR en RED MENTAL: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();

        } finally {
            estado.desactivarRedMental();
            Logger.log("Finaliza el evento global: LA RED MENTAL");
        }
    }
}