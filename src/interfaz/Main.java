package interfaz;

public class Main {
    public static void main(String[] args) {
        EstadoSimulacion estado = new EstadoSimulacion();

        Nino n1 = new Nino("N0001", estado);
        Nino n2 = new Nino("N0002", estado);
        Nino n3 = new Nino("N0003", estado);

        Demogorgon d1 = new Demogorgon("D0001");
        Demogorgon d2 = new Demogorgon("D0002");

        estado.getZona("CALLE_PRINCIPAL").entrarNino(n1);
        estado.getZona("SOTANO_BYERS").entrarNino(n2);
        estado.getZona("COLMENA").entrarNino(n3);

        estado.getZona("BOSQUE").entrarDemogorgons(d1);
        estado.getZona("LABORATORIO").entrarDemogorgons(d2);

        estado.sumarSangre(35);
        estado.incrementarCapturadosColmena();
        estado.incrementarCapturadosColmena();

        InterfazServidor gui = new InterfazServidor();
        gui.setVisible(true);

        InterfazServidor.SimulationSnapshot snapshot = estado.crearSnapshot();
        gui.updateSnapshot(snapshot);
    }
}