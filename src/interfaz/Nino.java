package interfaz;

public class Nino extends Thread{
    private String idNino;
    private EstadoSimulacion estado;
    private boolean capturado = false;

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
    @Override
    public void run(){
        while(true){
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
        Portal portal = elegirPortal();
        explorarUpsideDown(portal);
        volverAHawkins(portal);
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

    private void explorarUpsideDown(Portal portal) throws InterruptedException{
        Logger.log("El niño " + idNino + " elige " + portal.getNombre());

        portal.solicitarIda(idNino);
        estado.getZona("SOTANO_BYERS").salirNino(this);

        portal.cruzarIda(idNino);

        estado.getZona(portal.getZonaDestino()).entrarNino(this);
        Logger.log("El niño " + idNino + " entra en " + portal.getZonaDestino());

        Thread.sleep(3000 + (int)(Math.random() * 2000));
    }

    private void volverAHawkins(Portal portal) throws InterruptedException {
        estado.getZona(portal.getZonaDestino()).salirNino(this);
        Logger.log("El niño " + idNino + " regresa desde " + portal.getZonaDestino());

        portal.solicitarVuelta(idNino);
        portal.cruzarVuelta(idNino);

        estado.getZona("CALLE_PRINCIPAL").entrarNino(this);
        Logger.log("El niño " + idNino + " vuelve a Hawkins");
    }

    private void descansarEnRadio() throws InterruptedException {
        estado.getZona("CALLE_PRINCIPAL").salirNino(this);
        estado.getZona("RADIO_WSQK").entrarNino(this);

        Logger.log("El niño " + idNino + " entra en RADIO_WSQK");

        Thread.sleep(2000 + (int) (Math.random()*2000));

        estado.getZona("RADIO_WSQK").salirNino(this);
        estado.getZona("CALLE_PRINCIPAL").entrarNino(this);
    }

    private void deambularEnCallePrincipal() throws InterruptedException {
        Logger.log("El niño " + idNino + " deambula por CALLE_PRINCIPAL");
        Thread.sleep(3000+(int)(Math.random()*2000));
    }


}
