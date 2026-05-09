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
                // Entre un evento y el siguiente pasan entre 30 y 60 segundos
                int espera = 30000 + (int)(Math.random() * 30000);
                Thread.sleep(espera);

                int tipoEvento = (int)(Math.random() * 4);  //Se elige evento aleatorio

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
        int duracion = 5000 + (int)(Math.random() * 5000);  // Cada evento dura entre 5 y 10 segundos

        Logger.log("Comienza el evento global: APAGON DEL LABORATORIO");
        estado.activarApagon(); //Se activa el apagón para que los demogorgons no puedan cambiar de zona

        estado.getPortal("BOSQUE").activarApagon(); //Se activa en todos los portales
        estado.getPortal("LABORATORIO").activarApagon();
        estado.getPortal("CENTRO_COMERCIAL").activarApagon();
        estado.getPortal("ALCANTARILLADO").activarApagon();

        Thread.sleep(duracion); //Permanece activo

        estado.getPortal("BOSQUE").desactivarApagon();  //Se desactiva en todos los portales
        estado.getPortal("LABORATORIO").desactivarApagon();
        estado.getPortal("CENTRO_COMERCIAL").desactivarApagon();
        estado.getPortal("ALCANTARILLADO").desactivarApagon();

        estado.desactivarApagon();  //Se desactiva el apagón
        Logger.log("Finaliza el evento global: APAGON DEL LABORATORIO");
    }

    private void ejecutarTormenta() throws InterruptedException {
        int duracion = 5000 + (int)(Math.random() * 5000);

        Logger.log("Comienza el evento global: TORMENTA DEL UPSIDE DOWN");
        estado.activarTormenta();   //Se activa en estado global

        Thread.sleep(duracion); //Activo

        estado.desactivarTormenta();    //Se desactiva
        Logger.log("Finaliza el evento global: TORMENTA DEL UPSIDE DOWN");
    }

    private void ejecutarIntervencionEleven() {
        try {
            int duracion = 5000 + (int)(Math.random() * 5000);

            Logger.log("Comienza el evento global: INTERVENCION DE ELEVEN");
            estado.activarIntervencionEleven(); //Activa en estado global para paralizar a los demogorgons mediante interrupciones

            Thread.sleep(duracion); //Quedan parados los demogorgons durante este tiempo

            estado.liberarNinosColmenaSegunSangreDisponible();
            estado.desactivarIntervencionEleven();  //Desactiva el evento despertando a los demogorgons

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
            estado.activarRedMental();  //Activa Red Mental

            Thread.sleep(duracion); //Activo

            estado.desactivarRedMental();   //Demogorgons vuelven a moverse con normalidad
            Logger.log("Finaliza el evento global: LA RED MENTAL");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.log("ERROR: evento RED MENTAL interrumpido: " + e.getMessage());

        } catch (Exception e) {
            Logger.log("ERROR en RED MENTAL: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }
}