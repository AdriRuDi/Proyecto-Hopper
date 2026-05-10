package Servidor;

public class Nino extends Thread{
    private String idNino;
    private EstadoSimulacion estado;
    private boolean capturado = false;
    private int sangreRecogida = 0;
    private boolean siendoAtacado = false;
    private boolean ataqueResuelto = false;
    private boolean vulnerableAtaque = false;

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
    @Override
    public void run() {
        try {
            estado.esperarSiPausado();

            // El niño ya ha sido creado previamente en CALLE_PRINCIPAL por el GeneradorNinos.
            // Hacemos una pequeña espera inicial para que se vea en la interfaz antes de empezar su ciclo normal.
            Logger.log("El niño " + idNino + " inicia su vida en CALLE_PRINCIPAL");

            estado.dormirConPausa(1000 + (int)(Math.random() * 1000));

            estado.esperarSiPausado();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.log("El niño " + idNino + " ha sido interrumpido al iniciar");
            return;
        }

        while (true) {
            try {
                estado.esperarSiPausado();

                esperarSiCapturado();   // Si el niño está capturado espera a ser liberado

                cicloDeVida(); // Ejecuta continuamente su ciclo de vida: Calle Principal -> Sótano -> Upside Down -> Hawkins -> Radio -> Calle Principal.

            } catch (InterruptedException e) {
                synchronized (this) {
                    if (siendoAtacado) {    // Si la interrupción viene de un ataque, el niño espera a que el ataque se resuelva
                        try {
                            esperarFinAtaque(); // Espera hasta que el demogorgon resuelva si el niño resiste o es capturado.
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            Logger.log("El niño " + idNino + " ha sido interrumpido");
                            break;
                        }
                    } else {    // Para que no se interrumpa el niño si recibe otras interrupciones ajenas al ataque
                        Logger.log("El niño " + idNino + " recibe una interrupción fuera de ataque y continúa");
                    }
                }
            }
        }
    }
    private void cicloDeVida() throws InterruptedException {
        estado.esperarSiPausado();
        irASotanoByers();

        estado.esperarSiPausado();
        Portal portal = elegirPortal();

        estado.esperarSiPausado();
        explorarUpsideDown(portal); //Espera al grupo, cruza el portal y explora el UpsideDown

        if (capturado) return;

        estado.esperarSiPausado();
        volverAHawkins(portal);

        if (capturado) return;

        depositarSangre();
        descansarEnRadio();

        estado.esperarSiPausado();
        deambularEnCallePrincipal();
    }

    private void irASotanoByers() throws InterruptedException{
        estado.getZona("CALLE_PRINCIPAL").salirNino(this);
        estado.getZona("SOTANO_BYERS").entrarNino(this);

        Logger.log("El niño " + idNino + " entra en SOTANO_BYERS");

        estado.dormirConPausa(1000 + (int)(Math.random() * 1000));
    }

    private Portal elegirPortal() {
        int opcion = (int)(Math.random()*4);

        switch (opcion){
            case 0:
                return estado.getPortal("BOSQUE");  // Portal hacia BOSQUE, grupo de 2 niños
            case 1:
                return estado.getPortal("LABORATORIO"); // Portal hacia LABORATORIO, grupo de 3 niños
            case 2:
                return estado.getPortal("CENTRO_COMERCIAL");    // Portal hacia CENTRO_COMERCIAL, grupo de 4 niños
            default:
                return estado.getPortal("ALCANTARILLADO");  // Portal hacia ALCANTARILLADO, grupo de 2 niños
        }
    }

    private void explorarUpsideDown(Portal portal) throws InterruptedException {
        estado.esperarSiPausado();
        Logger.log("El niño " + idNino + " elige " + portal.getNombre());

        portal.solicitarIda(idNino);    //Entra en cola ida y espera a formar grupo
        estado.esperarSiPausado();
        estado.getZona("SOTANO_BYERS").salirNino(this);
        estado.esperarSiPausado();

        portal.cruzarIda(idNino);   // Cruza el portal de ida, pero si ya estaba cruzando al pausar, termina el cruce

        estado.getZona(portal.getZonaDestino()).entrarNino(this);   //Después de cruzar, primero aparece en la zona destino para que no se quede invisible
        Logger.log("El niño " + idNino + " entra en " + portal.getZonaDestino());

        estado.esperarSiPausado();  // Una vez ya está colocado en su zona, entonces sí respeta la pausa

        int tiempo = 3000 + (int)(Math.random() * 2000);

        if (estado.isTormentaActiva()) {    //Si hay tormenta activa se duplica el tiempo de exploración
            tiempo = tiempo * 2;
        }

        synchronized (this) {   //Puede ser atacado mientras está en el UpsideDown
            vulnerableAtaque = true;
        }

        try {
            dormirReanudableZonaPeligrosa(tiempo);  //Está dormido durnate la expliración pero puede ser interrumpido si le atacan
        } finally {
            synchronized (this) {   //Deja de ser vulnerable a ataques cuando le capturan/ interrumpen
                vulnerableAtaque = false;
            }
        }

        if (capturado) {    //Si ha sido capturado no recoge sangre
            return;
        }
        estado.esperarSiPausado();

        sangreRecogida = 1; //Sino si recoge sangre
        Logger.log("El niño " + idNino + " recoge sangre en " + portal.getZonaDestino()
                + " (lleva " + sangreRecogida + " unidad)");
    }

    public synchronized boolean iniciarAtaque() {
        // El ataque solo puede empezar si:
        // 1. El niño no está ya capturado.
        // 2. El niño no está siendo atacado por otro demogorgon.
        // 3. El niño está en una zona peligrosa y vulnerable.
        if (capturado || siendoAtacado || !vulnerableAtaque) {
            return false;
        }

        siendoAtacado = true;
        ataqueResuelto = false;
        interrupt();    //Se le interrumpe hasta saber el resultado del ataque
        return true;
    }

    public synchronized void resolverAtaque(boolean capturado) {
        if (capturado) {
            this.capturado = true;  //Si el demogorgon gana el ataque pasa a estar capturado
            this.vulnerableAtaque = false;  //Capturado no puede ser atacado por otros
        }

        siendoAtacado = false;
        ataqueResuelto = true;
        notifyAll();    //Se despierta al niño que estaba esperando el final del ataque
    }

    public synchronized void esperarFinAtaque() throws InterruptedException {
        while (siendoAtacado && !ataqueResuelto) {
            wait(); //Niño bloqueado hasta que se resuelva el ataque
        }
        ataqueResuelto = false;
    }

    private void volverAHawkins(Portal portal) throws InterruptedException {
        synchronized (this) {   //al iniciar la vuelta ya no le pueden atacar
            vulnerableAtaque = false;
        }

        if (capturado) {    //Si fue capturado no pueden volver a Hawkins
            return;
        }

        estado.esperarSiPausado();

        estado.getZona(portal.getZonaDestino()).salirNino(this);    //Sale de la zona insegura
        Logger.log("El niño " + idNino + " regresa desde " + portal.getZonaDestino());

        estado.esperarSiPausado();

        portal.solicitarVuelta(idNino); //Solicita volver por el portal que corresponde solo
        estado.esperarSiPausado();
        portal.cruzarVuelta(idNino);    // Cruza de vuelta, pero si ya estaba cruzando al pausar, termina el cruce

        estado.getZona("CALLE_PRINCIPAL").entrarNino(this); //Después de cruzar, primero vuelve a aparecer en Hawkins
        Logger.log("El niño " + idNino + " vuelve a Hawkins");
    }

    private void depositarSangre(){
        if(sangreRecogida > 0){
            int totalSangre = estado.sumarSangre(sangreRecogida);   //Se deposita sumando atómicamente

            Logger.log("El niño " + idNino + " deposita " + sangreRecogida +
                    " unidad de sangre (sangre total: " + totalSangre + ")");

            sangreRecogida = 0; //Ya no lleva sangre encima al depositarla
        }
    }

    private void descansarEnRadio() throws InterruptedException {

        estado.getZona("CALLE_PRINCIPAL").salirNino(this);
        estado.getZona("RADIO_WSQK").entrarNino(this);

        Logger.log("El niño " + idNino + " entra en RADIO_WSQK");

        estado.esperarSiPausado();

        estado.dormirConPausa(2000 + (int)(Math.random() * 2000));  //si se ha pausado mientras descansaba, se queda aquí

        estado.getZona("RADIO_WSQK").salirNino(this);
        estado.getZona("CALLE_PRINCIPAL").entrarNino(this);

        Logger.log("El niño " + idNino + " sale de RADIO_WSQK y vuelve a CALLE_PRINCIPAL");

        estado.esperarSiPausado();
    }

    private void deambularEnCallePrincipal() throws InterruptedException {
        estado.esperarSiPausado();
        Logger.log("El niño " + idNino + " deambula por CALLE_PRINCIPAL");
        estado.dormirConPausa(3000 + (int)(Math.random() * 2000));
        estado.esperarSiPausado();
    }

    private void dormirReanudableZonaPeligrosa(int tiempoTotal) throws InterruptedException {
        long restante = tiempoTotal;

        while (restante > 0) {
            estado.esperarSiPausado();

            long tramo = Math.min(100, restante);
            long inicio = System.currentTimeMillis();

            try {
                Thread.sleep(tramo);    //Si no resulta atacado, reanuda su marcha (exploración) el tiempo que le falte
                restante = restante - tramo;

            } catch (InterruptedException e) {
                long ahora = System.currentTimeMillis();    //Calculo tiempo que llevaba explorando
                long transcurrido = ahora - inicio;
                restante = restante - transcurrido;

                if (restante < 0) {
                    restante = 0;
                }

                synchronized (this) {
                    if (!siendoAtacado) {   //Si se le interrumpe fuera del ataque no gestiono la interrupcion aqui
                        throw e;
                    }
                }

                esperarFinAtaque(); //Si viene de un ataque espera resultado

                if (capturado) {
                    return;
                }
            }
        }
    }
    public synchronized void liberarDeColmena() {
        capturado = false;
        notifyAll();    //Despierta al hilo del niño si estaba esperando en esperarSiCapturado()
    }

    public synchronized void esperarSiCapturado() throws InterruptedException {
        while (capturado) {
            wait();
        }
    }


}
