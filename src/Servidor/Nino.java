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
    public void setCapturado(boolean capturado){
        this.capturado = capturado;
    }
    public int getSangreRecogida(){
        return sangreRecogida;
    }
    @Override
    public void run() {
        while (true) {
            try {
                esperarSiCapturado();   //Si el niño está capturado espera a ser liberado
                cicloDeVida();
            } catch (InterruptedException e) {
                synchronized (this) {
                    if (siendoAtacado) {    //Si la interrupcion viene de un ataque el niño espera a que el ataque se resuelva
                        try {
                            esperarFinAtaque();
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            Logger.log("El niño " + idNino + " ha sido interrumpido");
                            break;
                        }
                    } else {    //Para que no se interrumpa el niño si recibe otras interrupciones
                        Logger.log("El niño " + idNino + " recibe una interrupción fuera de ataque y continúa");
                    }
                }
            }
        }
    }
    private void cicloDeVida() throws InterruptedException{
        irASotanoByers();

        Portal portal = elegirPortal();   //Elige un portala aleatorio
        explorarUpsideDown(portal);  // Espera grupo, cruza el portal y explora el Upside Down

        if (capturado) {
            return;
        }

        volverAHawkins(portal);

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
        Logger.log("El niño " + idNino + " elige " + portal.getNombre());

        portal.solicitarIda(idNino);    //Entra en cola ida y espera a formar grupo
        estado.getZona("SOTANO_BYERS").salirNino(this);

        portal.cruzarIda(idNino);   //Cruza el portal de ida (de 1 en 1)

        estado.getZona(portal.getZonaDestino()).entrarNino(this);   //Entra en la zona insegura
        Logger.log("El niño " + idNino + " entra en " + portal.getZonaDestino());

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

        estado.getZona(portal.getZonaDestino()).salirNino(this);    //Sale de la zona insegura
        Logger.log("El niño " + idNino + " regresa desde " + portal.getZonaDestino());

        portal.solicitarVuelta(idNino); //Solicita volver por el portal que corresponde solo
        portal.cruzarVuelta(idNino);    //Cruza

        estado.getZona("CALLE_PRINCIPAL").entrarNino(this); //Vuelve a Hawkins
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

        Thread.sleep(2000 + (int) (Math.random()*2000));

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
                Thread.sleep(restante); //Si no resulta atacado, reanuda su marcha (exploración) el tiempo que le falte
                return; //Termina la exploración
            } catch (InterruptedException e) {
                long ahora = System.currentTimeMillis();    //Calculo tiempo que llevaba explorando
                long transcurrido = ahora - inicio;
                restante = tiempoTotal - transcurrido;

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

                inicio = System.currentTimeMillis() - (tiempoTotal - restante); //Si resiste continua el tiempo de exploración que le quedaba
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
