import java.util.Random;

public class Nino extends Thread{
    private String idNino;
    private EstadoSimulacion estado;
    private Random random = new Random();
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
}
