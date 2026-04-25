package interfaz;

public class Nino extends Thread{
    private String idNino;
    private EstadoSimulacion estado;
    private boolean capturado = false;
    private int sangreRecogida = 0;
    private boolean siendoAtacado = false;
    private boolean ataqueResuelto = false;

    public Nino(String idNino, EstadoSimulacion estado) {
        this.idNino = idNino;
        this.estado = estado;
    }

    public String getIdNino() {
        return idNino;
    }
    public boolean isCapturado(){
        return capturado;
    }
    public void setCapturado(boolean capturado){
        this.capturado = capturado;
    }
    public int getSangreRecogida(){
        return sangreRecogida;
    }
    @Override
    public void run(){
        while(!capturado){
            try{
                cicloDeVida();
            } catch(InterruptedException e){
                Thread.currentThread().interrupt();
                Logger.log("El niño " + idNino + " ha sido interrumpido");
                break;
            }
        }
    }
    private void cicloDeVida() throws InterruptedException{
        irASotanoByers();

        if (capturado) return;

        Portal portal = elegirPortal();
        explorarUpsideDown(portal);

        if (capturado) return;

        volverAHawkins(portal);

        if (capturado) return;

        depositarSangre();
        descansarEnRadio();
        deambularEnCallePrincipal();
    }

    private void irASotanoByers() throws InterruptedException{
        estado.getZona("CALLE_PRINCIPAL").salirNino(this);
        estado.getZona("SOTANO_BYERS").entrarNino(this);

        Logger.log("El niño " + idNino + " entra en SOTANO_BYERS");

        Thread.sleep(1000 + (int)(Math.random() * 1000));
    }

    private Portal elegirPortal() {
        int opcion = (int)(Math.random()*4);

        switch (opcion){
            case 0:
                return estado.getPortal("BOSQUE");
            case 1:
                return estado.getPortal("LABORATORIO");
            case 2:
                return estado.getPortal("CENTRO_COMERCIAL");
            default:
                return estado.getPortal("ALCANTARILLADO");
        }
    }

    private void explorarUpsideDown(Portal portal) throws InterruptedException {
        Logger.log("El niño " + idNino + " elige " + portal.getNombre());

        portal.solicitarIda(idNino);
        estado.getZona("SOTANO_BYERS").salirNino(this);

        portal.cruzarIda(idNino);

        estado.getZona(portal.getZonaDestino()).entrarNino(this);
        Logger.log("El niño " + idNino + " entra en " + portal.getZonaDestino());

        int tiempo = 3000 + (int)(Math.random() * 2000);

        if (estado.isTormentaActiva()) {
            tiempo = tiempo * 2;
        }

        dormirReanudableZonaPeligrosa(tiempo);

        if (capturado) {
            return;
        }

        sangreRecogida = 1;
        Logger.log("El niño " + idNino + " recoge sangre en " + portal.getZonaDestino());
    }

    public synchronized boolean iniciarAtaque() {
        if (capturado || siendoAtacado) {
            return false;
        }
        siendoAtacado = true;
        ataqueResuelto = false;
        interrupt();
        return true;
    }

    public synchronized void resolverAtaque(boolean capturado) {
        if (capturado) {
            this.capturado = true;
        }
        siendoAtacado = false;
        ataqueResuelto = true;
        notifyAll();
    }

    public synchronized void esperarFinAtaque() throws InterruptedException {
        while (siendoAtacado && !ataqueResuelto) {
            wait();
        }
        ataqueResuelto = false;
    }

    private void volverAHawkins(Portal portal) throws InterruptedException {
        if (capturado) {
            return;
        }
        estado.getZona(portal.getZonaDestino()).salirNino(this);
        Logger.log("El niño " + idNino + " regresa desde " + portal.getZonaDestino());

        portal.solicitarVuelta(idNino);
        portal.cruzarVuelta(idNino);

        estado.getZona("CALLE_PRINCIPAL").entrarNino(this);
        Logger.log("El niño " + idNino + " vuelve a Hawkins");
    }

    private void depositarSangre(){
        if(sangreRecogida > 0){
            estado.sumarSangre(sangreRecogida);
            Logger.log("El niño " + idNino + " deposita " + sangreRecogida + " unidad de sangre");
            sangreRecogida = 0;
        }
    }

    private void descansarEnRadio() throws InterruptedException {
        estado.getZona("CALLE_PRINCIPAL").salirNino(this);
        estado.getZona("RADIO_WSQK").entrarNino(this);

        Logger.log("El niño " + idNino + " entra en RADIO_WSQK");

        Thread.sleep(2000 + (int) (Math.random()*2000));

        if (capturado) {
            return;
        }

        estado.getZona("RADIO_WSQK").salirNino(this);
        estado.getZona("CALLE_PRINCIPAL").entrarNino(this);
    }

    private void deambularEnCallePrincipal() throws InterruptedException {
        Logger.log("El niño " + idNino + " deambula por CALLE_PRINCIPAL");
        Thread.sleep(3000+(int)(Math.random()*2000));
    }

    private void dormirReanudableZonaPeligrosa(int tiempoTotal) throws InterruptedException {
        long inicio = System.currentTimeMillis();
        long restante = tiempoTotal;

        while (restante > 0) {
            try {
                Thread.sleep(restante);
                return;
            } catch (InterruptedException e) {
                long ahora = System.currentTimeMillis();
                long transcurrido = ahora - inicio;
                restante = tiempoTotal - transcurrido;

                if (restante < 0) {
                    restante = 0;
                }

                synchronized (this) {
                    if (!siendoAtacado) {
                        throw e;
                    }
                }

                esperarFinAtaque();

                if (capturado) {
                    return;
                }

                inicio = System.currentTimeMillis() - (tiempoTotal - restante);
            }
        }
    }
}
