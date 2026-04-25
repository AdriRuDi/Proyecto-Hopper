package interfaz;

public class GeneradorNinos extends Thread {

    private final EstadoSimulacion estado;
    private final int totalNinos;

    public GeneradorNinos(EstadoSimulacion estado, int totalNinos) {
        this.estado = estado;
        this.totalNinos = totalNinos;
    }

    @Override
    public void run() {
        for (int i = 1; i <= totalNinos; i++) {
            String idNino = String.format("N%04d", i);
            Nino nino = new Nino(idNino, estado);

            estado.getZona("CALLE_PRINCIPAL").entrarNino(nino);
            Logger.log("Se crea el niño " + idNino + " en CALLE_PRINCIPAL");

            try {
                Thread.sleep(500 + (int)(Math.random() * 1500));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Logger.log("El generador de niños ha sido interrumpido");
                return;
            }

            nino.start();
        }
    }
}