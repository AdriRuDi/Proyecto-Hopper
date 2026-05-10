package Servidor;

public class Demogorgon extends Thread {

    private String idDemogorgon;
    private EstadoSimulacion estado;
    private String zonaActual;
    private int capturas;   //Capturas individuales para el ranking

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

    public synchronized void incrementarCapturas() {    //Incrementa capturas de un demogorgon
        capturas++;
    }

    public synchronized int getCapturas() { //Capturas x demogorgon para el ranking
        return capturas;
    }

    @Override
    public void run() {
        estado.getZona(zonaActual).entrarDemogorgon(this);      //Entra en zona inicial
        Logger.log("El demogorgon " + idDemogorgon + " aparece en " + zonaActual);

        while (true) {
            try {
                cicloDeVida(); // Busca niños, ataca o espera, y después cambia de zona.
            } catch (InterruptedException e) {
                if (estado.isIntervencionElevenActiva()) {
                    Logger.log("El demogorgon " + idDemogorgon + " queda paralizado por Eleven");

                    try {
                        estado.esperarFinIntervencionEleven();  //Espera a que Eleven termine
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        Logger.log("El demogorgon " + idDemogorgon + " ha sido interrumpido");
                        break;
                    }

                } else if (estado.isRedMentalActiva()) {    //No termina, continua su ciclo
                    Logger.log("El demogorgon " + idDemogorgon + " siente la Red Mental y cambia de objetivo");

                } else {
                    Thread.currentThread().interrupt();
                    Logger.log("El demogorgon " + idDemogorgon + " ha sido interrumpido");
                    break;
                }
            }
        }
    }

    private void cicloDeVida() throws InterruptedException {
        estado.esperarSiPausado();
        estado.esperarFinIntervencionEleven();  //Si está activa la intervención de Eleven el demogorgon queda bloqueado

        if (hayNinosEnZona()) {     //Solo si hay niños en la zona cuando llega intenta atacar
            estado.esperarSiPausado();
            estado.esperarFinIntervencionEleven();
            atacar();
        } else {
            estado.esperarSiPausado();
            estado.esperarFinIntervencionEleven();
            esperarEnZona();    //Sino espera sin posibilidad de atacar hasta cambiar de zona
        }

        estado.esperarSiPausado();
        estado.esperarFinIntervencionEleven();
        cambiarDeZona();
    }

    private boolean hayNinosEnZona() {
        return estado.getZona(zonaActual).getNumeroNinos() > 0;
    }

    private void atacar() throws InterruptedException {
        estado.esperarSiPausado();
        estado.esperarFinIntervencionEleven();  //Si interviene Eleven el demogorgon queda parado

        Zona zona = estado.getZona(zonaActual);
        Nino ninoObjetivo = zona.getNinoAleatorio();    //Elige a un niño aleatorio

        if (ninoObjetivo == null) { //Si no hay niños no puede atacar
            return;
        }

        estado.esperarFinIntervencionEleven();

        if (!ninoObjetivo.iniciarAtaque()) {    //Intenta reservar ese niño porque solo un demogorgon puede atacarlo a la vez
            Logger.log("El demogorgon " + idDemogorgon +
                    " intenta atacar al niño " + ninoObjetivo.getIdNino() +
                    ", pero ya está siendo atacado o no es vulnerable");
            return;
        }

        Logger.log("El demogorgon " + idDemogorgon +
                " ataca al niño " + ninoObjetivo.getIdNino());

        try {
            Thread.sleep(500 + (int)(Math.random() * 1000));    //Ataque
        } catch (InterruptedException e) {
            ninoObjetivo.resolverAtaque(false);     //Si Eleven interrumpe el ataque, el niño no queda capturado.

            if (estado.isIntervencionElevenActiva()) {
                Logger.log("El ataque del demogorgon " + idDemogorgon +
                        " al niño " + ninoObjetivo.getIdNino() + " se interrumpe por Eleven");
            }

            throw e;
        }

        estado.esperarFinIntervencionEleven();

        boolean capturado = decidirCaptura();   //Decide si tiene éxito el ataque

        if (capturado) {
            ninoObjetivo.resolverAtaque(true);  //El niño es capturado
            llevarAColmena(ninoObjetivo, zona); //Se le traslada a la colmena al niño
        } else {
            ninoObjetivo.resolverAtaque(false); //El niño resiste el ataque y puede continuar
            Logger.log("El niño " + ninoObjetivo.getIdNino() +
                    " resiste el ataque del demogorgon " + idDemogorgon);
        }
    }

    private boolean decidirCaptura() {
        // 2/3 resiste, 1/3 es capturado
        return Math.random() < (1.0 / 3.0); //Probabilidad de captura
    }

    private void llevarAColmena(Nino ninoObjetivo, Zona zona) throws InterruptedException {
        if (ninoObjetivo == null || !ninoObjetivo.isCapturado()) {  //Si existe y ha sido capturado lo lleva a la colmena
            return;
        }

        zona.salirNino(ninoObjetivo);
        Logger.log("El niño " + ninoObjetivo.getIdNino() + " ha sido capturado");

        estado.eliminarNinoDePortales(ninoObjetivo.getIdNino());    //Por si acaso, se le elimina de cualquier cola de portal
        estado.getZona("COLMENA").entrarNino(ninoObjetivo);     //Entra en la colmena

        Logger.log("El demogorgon " + idDemogorgon +
                " traslada al niño " + ninoObjetivo.getIdNino() +
                " a la COLMENA");

        Thread.sleep(500 + (int)(Math.random() * 500));

        incrementarCapturas();  //Se actualizan las capturas del demogorgon que ha atacado

        int totalColmena = estado.incrementarCapturadosColmena();   //Se actualizan capturas totales

        Logger.log("El demogorgon " + idDemogorgon +
                " deposita al niño " + ninoObjetivo.getIdNino() +
                " en la COLMENA (niños en colmena: " + totalColmena +
                "| Capturas de " + idDemogorgon + ": " + getCapturas() +
                " | Capturas históricas: " + estado.getCapturasTotalesHistoricas() + ")");
    }

    private void esperarEnZona() throws InterruptedException {  //Cuando no hay niños en la zona
        Logger.log("El demogorgon " + idDemogorgon +
                " no encuentra niños en " + zonaActual);

        int tiempo = 4000 + (int)(Math.random() * 1000);

        if (estado.isTormentaActiva()) {    //Durante la tormenta esperan la mitad antes de volver a moverse
            tiempo = tiempo / 2;
        }

        Thread.sleep(tiempo);
    }

    private void cambiarDeZona() throws InterruptedException {
        estado.esperarSiPausado();
        estado.esperarFinIntervencionEleven();

        if (estado.isApagonActivo()) {      //Durante el apagón los demogorgons no pueden cambiar de zona
            Logger.log("El demogorgon " + idDemogorgon + " permanece en " + zonaActual + " por el apagón");
            return;
        }

        String nuevaZona;

        if (estado.isRedMentalActiva()) {       //Si se activa la Red Mental hay que elegir la zona con más niños
            nuevaZona = estado.getZonaPeligrosaConMasNinos(zonaActual);

            if (nuevaZona.equals(zonaActual)) {
                Logger.log("El demogorgon " + idDemogorgon +
                        " ya está en la zona con más niños: " + zonaActual);

                Thread.sleep(1000); //Para evitar que el demogorgon entre en un bucle de cambios continuos
                return;
            }

            Logger.log("La Red Mental guía al demogorgon " + idDemogorgon + " hacia " + nuevaZona);
        } else {    //Sino elige cualquiera de las 4 zonas
            nuevaZona = elegirZonaPeligrosaAleatoriaDistinta();
        }

        estado.getZona(zonaActual).salirDemogorgon(this);   //Sale de la zona actual
        Logger.log("El demogorgon " + idDemogorgon + " sale de " + zonaActual);

        zonaActual = nuevaZona;     //Cambia de zona

        estado.getZona(zonaActual).entrarDemogorgon(this);  //Entra en la nueva zona
        Logger.log("El demogorgon " + idDemogorgon + " entra en " + zonaActual);
        if (estado.isRedMentalActiva()) {
            Thread.sleep(1000);
        }
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