package interfaz;

public class Demogorgon extends Thread {

    private String idDemogorgon;
    private EstadoSimulacion estado;
    private String zonaActual;
    private int capturas;

    public Demogorgon(String idDemogorgon, EstadoSimulacion estado, String zonaActual) {
        this.idDemogorgon = idDemogorgon;
        this.estado = estado;
        this.zonaActual = zonaActual;
        this.capturas = 0;
    }

    public String getIdDemogorgon() {
        return idDemogorgon;
    }

    public String getZonaActual() {
        return zonaActual;
    }

    public int getCapturas() {
        return capturas;
    }

    public void incrementarCapturas() {
        capturas++;
    }

    @Override
    public void run() {
        estado.getZona(zonaActual).entrarDemogorgon(this);
        Logger.log("El demogorgon " + idDemogorgon + " aparece en " + zonaActual);

        while (true) {
            try {
                cicloDeVida();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Logger.log("El demogorgon " + idDemogorgon + " ha sido interrumpido");
                break;
            }
        }
    }

    private void cicloDeVida() throws InterruptedException {
        if (hayNinosEnZona()) {
            atacar();
        } else {
            esperarEnZona();
        }

        cambiarDeZona();
    }

    private boolean hayNinosEnZona() {
        return estado.getZona(zonaActual).getNumeroNinos() > 0;
    }

    private void atacar() throws InterruptedException {
        Zona zona = estado.getZona(zonaActual);
        Nino ninoObjetivo = zona.getNinoAleatorio();

        if (ninoObjetivo == null) {
            return;
        }

        Logger.log("El demogorgon " + idDemogorgon +
                " ataca al niño " + ninoObjetivo.getIdNino());

        // Duración del ataque: entre 0,5 y 1,5 s
        Thread.sleep(500 + (int)(Math.random() * 1000));

        boolean capturado = decidirCaptura();

        if (capturado) {
            llevarAColmena(ninoObjetivo, zona);
        } else {
            Logger.log("El niño " + ninoObjetivo.getIdNino() +
                    " resiste el ataque del demogorgon " + idDemogorgon);
        }
    }

    private boolean decidirCaptura() {
        // 2/3 resiste, 1/3 es capturado
        return Math.random() < (1.0 / 3.0);
    }

    private void llevarAColmena(Nino ninoObjetivo, Zona zona) throws InterruptedException {
        zona.salirNino(ninoObjetivo);
        ninoObjetivo.setCapturado(true);

        Logger.log("El niño " + ninoObjetivo.getIdNino() + " ha sido capturado");

        estado.getZona("COLMENA").entrarNino(ninoObjetivo);

        // Tiempo de introducir al niño en colmena: entre 0,5 y 1 s
        Thread.sleep(500 + (int)(Math.random() * 500));

        incrementarCapturas();
        estado.incrementarCapturadosColmena();

        Logger.log("El demogorgon " + idDemogorgon +
                " deposita al niño " + ninoObjetivo.getIdNino() +
                " en la COLMENA (capturas: " + capturas + ")");
    }

    private void esperarEnZona() throws InterruptedException {
        Logger.log("El demogorgon " + idDemogorgon +
                " no encuentra niños en " + zonaActual);

        // Si no hay niños, espera entre 4 y 5 s
        Thread.sleep(4000 + (int)(Math.random() * 1000));
    }

    private void cambiarDeZona() {
        String nuevaZona = elegirZonaPeligrosaAleatoria();

        if (nuevaZona.equals(zonaActual)) {
            return;
        }

        estado.getZona(zonaActual).salirDemogorgon(this);
        Logger.log("El demogorgon " + idDemogorgon + " sale de " + zonaActual);

        zonaActual = nuevaZona;

        estado.getZona(zonaActual).entrarDemogorgon(this);
        Logger.log("El demogorgon " + idDemogorgon + " entra en " + zonaActual);
    }

    private String elegirZonaPeligrosaAleatoria() {
        int opcion = (int)(Math.random() * 4);

        switch (opcion) {
            case 0:
                return "BOSQUE";
            case 1:
                return "LABORATORIO";
            case 2:
                return "CENTRO_COMERCIAL";
            default:
                return "ALCANTARILLADO";
        }
    }
}