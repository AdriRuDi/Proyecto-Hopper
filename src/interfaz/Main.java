package interfaz;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        EstadoSimulacion estado = new EstadoSimulacion();
        InterfazServidor gui = new InterfazServidor();
        gui.setVisible(true);

        for (int i = 1; i <= 10; i++) {
            String idNino = String.format("N%04d", i);
            try {

                Thread.sleep(500);

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

            }
            Nino nino = new Nino(idNino, estado);

            estado.getZona("CALLE_PRINCIPAL").entrarNino(nino);
            nino.start();
        }

        for (int i = 1; i <= 2; i++) {
            String idDemogorgon = String.format("D%04d", i);
            String zonaInicial;

            if (i == 1) {
                zonaInicial = "BOSQUE";
            } else {
                zonaInicial = "LABORATORIO";
            }

            Demogorgon demogorgon = new Demogorgon(idDemogorgon, estado, zonaInicial);
            demogorgon.start();
        }

        new Thread(() -> {
            while (true) {
                try {
                    SwingUtilities.invokeLater(() ->
                            gui.updateSnapshot(estado.crearSnapshot())
                    );
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }
}