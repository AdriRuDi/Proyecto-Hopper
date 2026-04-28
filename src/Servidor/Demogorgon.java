package Servidor;

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
                if (estado.isIntervencionElevenActiva()) {
                    Logger.log("El demogorgon " + idDemogorgon + " queda paralizado por Eleven");
                    try {
                        estado.esperarFinIntervencionEleven();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        Logger.log("El demogorgon " + idDemogorgon + " ha sido interrumpido");
                        break;
                    }
                } else {
                    Thread.currentThread().interrupt();
                    Logger.log("El demogorgon " + idDemogorgon + " ha sido interrumpido");
                    break;
                }
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

        if (!ninoObjetivo.iniciarAtaque()) {
            return;
        }

        Logger.log("El demogorgon " + idDemogorgon +
                " ataca al niño " + ninoObjetivo.getIdNino());

        try {
            Thread.sleep(500 + (int)(Math.random() * 1000));
        } catch (InterruptedException e) {
            ninoObjetivo.resolverAtaque(false);

            if (estado.isIntervencionElevenActiva()) {
                Logger.log("El ataque del demogorgon " + idDemogorgon +
                        " al niño " + ninoObjetivo.getIdNino() + " se interrumpe por Eleven");
            }

            throw e;
        }

        boolean capturado = decidirCaptura();

        if (capturado) {
            ninoObjetivo.resolverAtaque(true);
            llevarAColmena(ninoObjetivo, zona);
        } else {
            ninoObjetivo.resolverAtaque(false);
            Logger.log("El niño " + ninoObjetivo.getIdNino() +
                    " resiste el ataque del demogorgon " + idDemogorgon);
        }
    }

    private boolean decidirCaptura() {
        // 2/3 resiste, 1/3 es capturado
        return Math.random() < (1.0 / 3.0);
    }

    private void llevarAColmena(Nino ninoObjetivo, Zona zona) throws InterruptedException {
        if (ninoObjetivo == null || !ninoObjetivo.isCapturado()) {
            return;
        }

        zona.salirNino(ninoObjetivo);
        Logger.log("El niño " + ninoObjetivo.getIdNino() + " ha sido capturado");

        estado.getZona("COLMENA").entrarNino(ninoObjetivo);

        incrementarCapturas();
        estado.incrementarCapturadosColmena();

        Thread.sleep(500 + (int)(Math.random() * 500));

        Logger.log("El demogorgon " + idDemogorgon +
                " deposita al niño " + ninoObjetivo.getIdNino() +
                " en la COLMENA (capturas: " + capturas + ")");
    }

    private void esperarEnZona() throws InterruptedException {
        Logger.log("El demogorgon " + idDemogorgon +
                " no encuentra niños en " + zonaActual);

        int tiempo = 4000 + (int)(Math.random() * 1000);

        if (estado.isTormentaActiva()) {
            tiempo = tiempo / 2;
        }

        Thread.sleep(tiempo);
    }

    private void cambiarDeZona() {
        if (estado.isApagonActivo()) {
            Logger.log("El demogorgon " + idDemogorgon + " permanece en " + zonaActual + " por el apagón");
            return;
        }

        String nuevaZona;

        if (estado.isRedMentalActiva()) {
            nuevaZona = estado.getZonaPeligrosaConMasNinos();

            if (nuevaZona.equals(zonaActual)) {
                Logger.log("El demogorgon " + idDemogorgon + " ya está en la zona con más niños: " + zonaActual);
                return;
            }

            Logger.log("La Red Mental guía al demogorgon " + idDemogorgon + " hacia " + nuevaZona);
        } else {
            nuevaZona = elegirZonaPeligrosaAleatoriaDistinta();
        }

        estado.getZona(zonaActual).salirDemogorgon(this);
        Logger.log("El demogorgon " + idDemogorgon + " sale de " + zonaActual);

        zonaActual = nuevaZona;

        estado.getZona(zonaActual).entrarDemogorgon(this);
        Logger.log("El demogorgon " + idDemogorgon + " entra en " + zonaActual);
    }

    private String elegirZonaPeligrosaAleatoriaDistinta() {
        String nuevaZona;

        do {
            int opcion = (int)(Math.random() * 4);

            switch (opcion) {
                case 0:
                    nuevaZona = "BOSQUE";
                    break;
                case 1:
                    nuevaZona = "LABORATORIO";
                    break;
                case 2:
                    nuevaZona = "CENTRO_COMERCIAL";
                    break;
                default:
                    nuevaZona = "ALCANTARILLADO";
                    break;
            }
        } while (nuevaZona.equals(zonaActual));

        return nuevaZona;
    }
}