package Servidor;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class EstadoSimulacion {

    private Map<String, Zona> zonas;    // Permite acceder rápidamente a cualquier zona por su nombre
    private Map<String, Portal> portales;
    private List<Demogorgon> demogorgonsActivos;

    private AtomicInteger sangreVecna;  //Contador global de sangre de Vecna
    private AtomicInteger sangreRecolectadaDuranteEleven;
    private AtomicInteger ninosEnColmena;
    private AtomicInteger capturasTotalesHistoricas;    //Para ver cuándo vecna tiene que crear más demogorgons no resta nunca
    private AtomicInteger siguienteIdDemogorgon;

    private String eventoActivo;
    private String tiempoRestanteEvento;

    private boolean apagonActivo;
    private boolean tormentaActiva;
    private boolean intervencionElevenActiva;
    private boolean redMentalActiva;
    private boolean programaPausado;

    public EstadoSimulacion() {
        zonas = new HashMap<>();
        portales = new HashMap<>();
        demogorgonsActivos = Collections.synchronizedList(new ArrayList<>());

        // Zonas seguras
        zonas.put("CALLE_PRINCIPAL", new ZonaSegura("CALLE_PRINCIPAL"));
        zonas.put("SOTANO_BYERS", new ZonaSegura("SOTANO_BYERS"));
        zonas.put("RADIO_WSQK", new ZonaSegura("RADIO_WSQK"));

        // Zonas peligrosas
        zonas.put("BOSQUE", new ZonaPeligrosa("BOSQUE"));
        zonas.put("LABORATORIO", new ZonaPeligrosa("LABORATORIO"));
        zonas.put("CENTRO_COMERCIAL", new ZonaPeligrosa("CENTRO_COMERCIAL"));
        zonas.put("ALCANTARILLADO", new ZonaPeligrosa("ALCANTARILLADO"));

        // Colmena
        zonas.put("COLMENA", new ZonaPeligrosa("COLMENA"));

        // Portales
        portales.put("BOSQUE", new Portal("PORTAL_BOSQUE", "BOSQUE", 2, this));
        portales.put("LABORATORIO", new Portal("PORTAL_LABORATORIO", "LABORATORIO", 3, this));
        portales.put("CENTRO_COMERCIAL", new Portal("PORTAL_CENTRO_COMERCIAL", "CENTRO_COMERCIAL", 4, this));
        portales.put("ALCANTARILLADO", new Portal("PORTAL_ALCANTARILLADO", "ALCANTARILLADO", 2, this));

        sangreVecna = new AtomicInteger(0);
        sangreRecolectadaDuranteEleven = new AtomicInteger(0);
        ninosEnColmena = new AtomicInteger(0);
        capturasTotalesHistoricas = new AtomicInteger(0);
        siguienteIdDemogorgon = new AtomicInteger(1);

        eventoActivo = "SIN EVENTO ACTIVO";
        tiempoRestanteEvento = "00:00";

        apagonActivo = false;
        tormentaActiva = false;
        intervencionElevenActiva = false;
        redMentalActiva = false;
        programaPausado = false;
    }

    public Zona getZona(String nombre) {    // Permite obtener una zona a partir de su nombre
        if (!zonas.containsKey(nombre)) {
            throw new IllegalArgumentException("Zona no existe: " + nombre);
        }
        return zonas.get(nombre);
    }

    public Portal getPortal(String nombreZonaDestino) {
        if (!portales.containsKey(nombreZonaDestino)) {
            throw new IllegalArgumentException("Portal no existe para zona: " + nombreZonaDestino);
        }
        return portales.get(nombreZonaDestino);
    }

    public int getCapturasTotalesHistoricas() {
        return capturasTotalesHistoricas.get();
    }

    public synchronized int sumarSangre(int cantidad) {
        int totalSangre = sangreVecna.addAndGet(cantidad);  //Incremento atómico del contador global de sangre

        if (intervencionElevenActiva) {
            int sangreDuranteEleven = sangreRecolectadaDuranteEleven.addAndGet(cantidad);   //Incremento atómico del contador durante el evento

            Logger.log("Sangre recolectada durante INTERVENCION DE ELEVEN: "
                    + sangreDuranteEleven);
        }

        return totalSangre;
    }

    public int getSangreVecna() {
        return sangreVecna.get();
    }

    public synchronized int incrementarCapturadosColmena() {
        int actualEnColmena = ninosEnColmena.incrementAndGet(); //Aumenta niños en colmena
        int totalHistorico = capturasTotalesHistoricas.incrementAndGet();   //El número de capturas globales

        if (totalHistorico % 8 == 0) {  //Por cada 8 capturas se crea demogorgon
            crearNuevoDemogorgon();
        }

        return actualEnColmena;
    }

    private void crearNuevoDemogorgon() {
        String id = String.format("D%04d", siguienteIdDemogorgon.getAndIncrement());
        String zonaInicial = elegirZonaPeligrosaAleatoria();

        Demogorgon nuevo = new Demogorgon(id, this, zonaInicial);
        registrarDemogorgon(nuevo);
        nuevo.start();

        Logger.log("Vecna genera un nuevo demogorgon: " + id + " en " + zonaInicial);
    }

    public String elegirZonaPeligrosaAleatoria() {
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

    public int getCapturadosColmena() {
        return ninosEnColmena.get();
    }

    public synchronized void setEventoActivo(String eventoActivo) {
        this.eventoActivo = eventoActivo;
    }

    public synchronized String getEventoActivo() {
        return eventoActivo;
    }

    public synchronized void setTiempoRestanteEvento(String tiempoRestanteEvento) {
        this.tiempoRestanteEvento = tiempoRestanteEvento;
    }

    public synchronized String getTiempoRestanteEvento() {
        return tiempoRestanteEvento;
    }

    public Map<String, Zona> getZonas() {
        return new HashMap<>(zonas);
    }

    public Map<String, Portal> getPortales() {
        return new HashMap<>(portales);
    }

    public synchronized void registrarDemogorgon(Demogorgon demogorgon) {
        if (!demogorgonsActivos.contains(demogorgon)) {
            demogorgonsActivos.add(demogorgon);
        }
    }

    private synchronized List<String> crearRankingDemogorgons() {
        List<Demogorgon> copia = new ArrayList<>(demogorgonsActivos);

        Collections.sort(copia, new Comparator<Demogorgon>() {
            @Override
            public int compare(Demogorgon d1, Demogorgon d2) {
                return Integer.compare(d2.getCapturas(), d1.getCapturas());
            }
        });

        List<String> ranking = new ArrayList<>();

        int limite = Math.min(3, copia.size());

        for (int i = 0; i < limite; i++) {
            Demogorgon d = copia.get(i);
            ranking.add((i + 1) + ". " + d.getIdDemogorgon()
                    + " - " + d.getCapturas() + " capturas");
        }

        return ranking;
    }

    public synchronized InterfazServidor.SimulationSnapshot crearSnapshot() { //
        Map<String, InterfazServidor.ZoneData> mapaZonas = new HashMap<>();
        Map<String, InterfazServidor.PortalData> mapaPortales = new HashMap<>();

        int totalNinosActivos = 0;
        int totalDemogorgonsActivos = 0;
        //Recorremos todas las zonas para obtener el número de niños y demogorgons de cada una
        for (Map.Entry<String, Zona> entry : zonas.entrySet()) {
            String nombre = entry.getKey();
            Zona zona = entry.getValue();

            int numeroNinos = zona.getNumeroNinos();
            int numeroDemogorgons = zona.getNumeroDemogorgons();
            //Guardamos la informacion de cada zona
            InterfazServidor.ZoneData dataZona = new InterfazServidor.ZoneData(
                    numeroNinos,
                    numeroDemogorgons,
                    zona.getIdsNinos(),
                    zona.getIdsDemogorgons()
            );

            mapaZonas.put(nombre, dataZona);
            //Los niños de la colmena no se cuentan como niños en Hawkins
            if (!nombre.equals("COLMENA")) {
                totalNinosActivos += numeroNinos;
            }

            totalDemogorgonsActivos += numeroDemogorgons;
        }
        //Recogemos el estado de los portales
        for (Map.Entry<String, Portal> entry : portales.entrySet()) {
            String nombreZona = entry.getKey();
            Portal portal = entry.getValue();

            InterfazServidor.PortalData dataPortal = new InterfazServidor.PortalData(
                    portal.getColaIda(),
                    portal.getColaVuelta(),
                    portal.isOcupado(),
                    portal.getCruzandoAhora()
            );

            mapaPortales.put(nombreZona, dataPortal);
        }
        //Devolvemos la snapshoy de toda la informacion
        return new InterfazServidor.SimulationSnapshot(
                eventoActivo,
                tiempoRestanteEvento,
                sangreVecna.get(),
                totalNinosActivos,
                totalDemogorgonsActivos,
                mapaZonas,
                mapaPortales,
                crearRankingDemogorgons(),
                List.of()
        );
    }

    public synchronized boolean isApagonActivo() {
        return apagonActivo;
    }

    public synchronized void activarApagon() {
        apagonActivo = true;
        eventoActivo = "APAGON DEL LABORATORIO";
    }

    public synchronized void desactivarApagon() {
        apagonActivo = false;
        eventoActivo = "SIN EVENTO ACTIVO";
    }

    public synchronized boolean isTormentaActiva() {
        return tormentaActiva;
    }

    public synchronized void activarTormenta() {
        tormentaActiva = true;
        eventoActivo = "TORMENTA DEL UPSIDE DOWN";
    }

    public synchronized void desactivarTormenta() {
        tormentaActiva = false;
        eventoActivo = "SIN EVENTO ACTIVO";
    }

    public synchronized boolean isIntervencionElevenActiva() {
        return intervencionElevenActiva;
    }

    public synchronized void activarIntervencionEleven() {
        sangreRecolectadaDuranteEleven.set(0);

        intervencionElevenActiva = true;
        eventoActivo = "INTERVENCION DE ELEVEN";

        paralizarDemogorgons();     //Para demogorgons para recoger más sangre sin ser capturados

        Logger.log("Contador de sangre durante Eleven reiniciado a 0");
    }

    public synchronized void desactivarIntervencionEleven() {
        intervencionElevenActiva = false;
        eventoActivo = "SIN EVENTO ACTIVO";
        notifyAll();    //Termina despertando a los demogorgons
    }

    public synchronized void esperarFinIntervencionEleven() throws InterruptedException {
        while (intervencionElevenActiva) {
            wait();
        }
    }

    public synchronized void liberarNinosColmenaSegunSangreDisponible() {
        Zona colmena = getZona("COLMENA");
        Zona callePrincipal = getZona("CALLE_PRINCIPAL");

        int sangreDisponible = sangreRecolectadaDuranteEleven.get();
        int liberados = 0;

        Logger.log("Eleven intenta liberar niños usando la sangre recolectada durante el evento: "
                + sangreDisponible);


        for (int i = 0; i < sangreDisponible; i++) {    //Se libera niño por unidad de sangre
            Nino nino = colmena.getNinoAleatorio();

            if (nino == null) {
                break;
            }

            colmena.salirNino(nino); //Sale de la colmena
            callePrincipal.entrarNino(nino);    //Vuelve a calle principal
            nino.liberarDeColmena();    //Deja de estar capturado y se despierta su hilo

            liberados++;

            Logger.log("Eleven libera al niño " + nino.getIdNino()
                    + " y regresa a CALLE_PRINCIPAL");
        }

        if (liberados > 0) {
            int sangreRestante = sangreVecna.addAndGet(-liberados); //Se resta una unidad de sangre por cada niño liberado
            if (sangreRestante < 0) {
                sangreVecna.set(0);
                sangreRestante = 0;
            }

            int colmenaRestante = ninosEnColmena.addAndGet(-liberados); //Se actualiza el número de niños en colmena
            if (colmenaRestante < 0) {
                ninosEnColmena.set(0);
                colmenaRestante = 0;
            }

            Logger.log("Eleven ha liberado " + liberados
                    + " niños de la COLMENA usando " + liberados
                    + " unidades de sangre recolectadas durante el evento");

            Logger.log("Sangre restante: " + sangreRestante
                    + " | Niños restantes en COLMENA: " + colmenaRestante);
        } else {
            Logger.log("Eleven no libera ningún niño de la COLMENA");
        }
        sangreRecolectadaDuranteEleven.set(0);
    }

    private void paralizarDemogorgons() {
        String[] zonasPeligrosas = {"BOSQUE", "LABORATORIO", "CENTRO_COMERCIAL", "ALCANTARILLADO"};

        for (String nombreZona : zonasPeligrosas) {
            for (Demogorgon demogorgon : zonas.get(nombreZona).getDemogorgons()) {
                demogorgon.interrupt();
            }
        }
    }
    public synchronized boolean isRedMentalActiva() {
        return redMentalActiva;
    }

    public synchronized void activarRedMental() {
        redMentalActiva = true; // Marca el evento como activo
        eventoActivo = "RED MENTAL";
        despertarDemogorgons(); // Interrumpe a los demogorgons para que reaccionen al evento
    }

    public synchronized void desactivarRedMental() {
        redMentalActiva = false;    //Termina
        eventoActivo = "SIN EVENTO ACTIVO";
    }
    public String getZonaPeligrosaConMasNinos(String zonaActual) {
        String[] zonasPeligrosas = {
                "BOSQUE",
                "LABORATORIO",
                "CENTRO_COMERCIAL",
                "ALCANTARILLADO"
        };

        int maximo = -1;
        java.util.List<String> candidatas = new java.util.ArrayList<>();

        //Recorre todas las zonas peligrosas
        for (String nombreZona : zonasPeligrosas) {
            int numeroNinos = getZona(nombreZona).getNumeroNinos();

            if (numeroNinos > maximo) {
                maximo = numeroNinos;
                candidatas.clear();
                candidatas.add(nombreZona);
            } else if (numeroNinos == maximo) {
                candidatas.add(nombreZona);
            }
        }

        // Si la zona actual está empatada como una de las mejores, se queda ahí
        if (candidatas.contains(zonaActual)) {
            return zonaActual;
        }

        // Si hay empate entre varias se elige aleatoriamente
        int posicion = (int)(Math.random() * candidatas.size());
        return candidatas.get(posicion);
    }


    public synchronized String crearTextoRemoto() {
        InterfazServidor.SimulationSnapshot snapshot = crearSnapshot();

        StringBuilder sb = new StringBuilder();

        sb.append("TOTAL_NINOS=").append(snapshot.totalNinosActivos()).append("\n");
        sb.append("TOTAL_DEMOGORGONS=").append(snapshot.totalDemogorgonsActivos()).append("\n");
        sb.append("SANGRE=").append(snapshot.sangreVecna()).append("\n");
        sb.append("EVENTO=").append(snapshot.eventoActivo()).append("\n");
        sb.append("TIEMPO_EVENTO=").append(snapshot.tiempoRestanteEvento()).append("\n");

        sb.append("PORTAL_BOSQUE=").append(snapshot.portales().get("BOSQUE").idsIda().size()).append("\n");
        sb.append("PORTAL_LABORATORIO=").append(snapshot.portales().get("LABORATORIO").idsIda().size()).append("\n");
        sb.append("PORTAL_CENTRO=").append(snapshot.portales().get("CENTRO_COMERCIAL").idsIda().size()).append("\n");
        sb.append("PORTAL_ALCANTARILLADO=").append(snapshot.portales().get("ALCANTARILLADO").idsIda().size()).append("\n");

        sb.append("NINOS_BOSQUE=").append(snapshot.zonas().get("BOSQUE").ninos()).append("\n");
        sb.append("NINOS_LABORATORIO=").append(snapshot.zonas().get("LABORATORIO").ninos()).append("\n");
        sb.append("NINOS_CENTRO=").append(snapshot.zonas().get("CENTRO_COMERCIAL").ninos()).append("\n");
        sb.append("NINOS_ALCANTARILLADO=").append(snapshot.zonas().get("ALCANTARILLADO").ninos()).append("\n");
        sb.append("NINOS_COLMENA=").append(snapshot.zonas().get("COLMENA").ninos()).append("\n");

        sb.append("DEMOS_BOSQUE=").append(snapshot.zonas().get("BOSQUE").demogorgons()).append("\n");
        sb.append("DEMOS_LABORATORIO=").append(snapshot.zonas().get("LABORATORIO").demogorgons()).append("\n");
        sb.append("DEMOS_CENTRO=").append(snapshot.zonas().get("CENTRO_COMERCIAL").demogorgons()).append("\n");
        sb.append("DEMOS_ALCANTARILLADO=").append(snapshot.zonas().get("ALCANTARILLADO").demogorgons()).append("\n");

        sb.append("RANKING=").append(String.join(";", snapshot.topDemogorgons())).append("\n");

        return sb.toString();
    }

    private void despertarDemogorgons() {
        String[] zonasPeligrosas = {
                "BOSQUE",
                "LABORATORIO",
                "CENTRO_COMERCIAL",
                "ALCANTARILLADO"
        };

        for (String nombreZona : zonasPeligrosas) {
            for (Demogorgon demogorgon : zonas.get(nombreZona).getDemogorgons()) {
                demogorgon.interrupt();
            }
        }
    }

    public void eliminarNinoDePortales(String idNino) {
        for (Portal portal : portales.values()) {
            portal.eliminarNino(idNino);
        }
    }
    public synchronized void alternarPausa() {
        programaPausado = !programaPausado;

        if (programaPausado) {
            Logger.log("Programa pausado desde el módulo remoto");
        } else {
            Logger.log("Programa reanudado desde el módulo remoto");
            notifyAll();
        }
    }

    public synchronized boolean isProgramaPausado() {
        return programaPausado;
    }

    public synchronized void esperarSiPausado() throws InterruptedException {
        while (programaPausado) {
            wait();
        }
    }
}