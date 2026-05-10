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
                Thread.sleep(espera);

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

    private void ejecutarApagon() throws InterruptedException {
        int duracion = 5000 + (int)(Math.random() * 5000);

        Logger.log("Comienza el evento global: APAGON DEL LABORATORIO");
        estado.activarApagon();

        estado.getPortal("BOSQUE").activarApagon();
        estado.getPortal("LABORATORIO").activarApagon();
        estado.getPortal("CENTRO_COMERCIAL").activarApagon();
        estado.getPortal("ALCANTARILLADO").activarApagon();

        dormirEventoConCuentaAtras(duracion);

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

        dormirEventoConCuentaAtras(duracion);

        estado.desactivarTormenta();
        Logger.log("Finaliza el evento global: TORMENTA DEL UPSIDE DOWN");
    }

    private void ejecutarIntervencionEleven() {
        try {
            int duracion = 5000 + (int)(Math.random() * 5000);

            Logger.log("Comienza el evento global: INTERVENCION DE ELEVEN");
            estado.activarIntervencionEleven();

            dormirEventoConCuentaAtras(duracion);

            estado.liberarNinosColmenaSegunSangreDisponible();
            estado.desactivarIntervencionEleven();

            Logger.log("Finaliza el evento global: INTERVENCION DE ELEVEN");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.log("ERROR: evento INTERVENCION ELEVEN interrumpido: " + e.getMessage());

        } catch (Exception e) {
            Logger.log("ERROR en INTERVENCION ELEVEN: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void ejecutarRedMental() {
        try {
            int duracion = 5000 + (int)(Math.random() * 5000);

            Logger.log("Comienza el evento global: LA RED MENTAL");
            estado.activarRedMental();

            dormirEventoConCuentaAtras(duracion);

            estado.desactivarRedMental();
            Logger.log("Finaliza el evento global: LA RED MENTAL");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.log("ERROR: evento RED MENTAL interrumpido: " + e.getMessage());

        } catch (Exception e) {
            Logger.log("ERROR en RED MENTAL: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void dormirEventoConCuentaAtras(int duracion) throws InterruptedException {
        int segundosRestantes = (int) Math.ceil(duracion / 1000.0);

        while (segundosRestantes > 0) {
            estado.setTiempoRestanteEvento(String.format("00:%02d", segundosRestantes));
            Thread.sleep(1000);
            segundosRestantes--;
        }

        estado.setTiempoRestanteEvento("00:00");
    }
}