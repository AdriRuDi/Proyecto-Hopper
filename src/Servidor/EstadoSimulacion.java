package Servidor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class EstadoSimulacion {

    private Map<String, Zona> zonas;
    private Map<String, Portal> portales;

    private AtomicInteger sangreVecna;
    private AtomicInteger ninosEnColmena;
    private AtomicInteger capturasTotalesHistoricas;
    private AtomicInteger siguienteIdDemogorgon;

    private String eventoActivo;
    private String tiempoRestanteEvento;

    private boolean apagonActivo;
    private boolean tormentaActiva;
    private boolean intervencionElevenActiva;
    private boolean redMentalActiva;

    public EstadoSimulacion() {
        zonas = new HashMap<>();
        portales = new HashMap<>();

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
        portales.put("BOSQUE", new Portal("PORTAL_BOSQUE", "BOSQUE", 2));
        portales.put("LABORATORIO", new Portal("PORTAL_LABORATORIO", "LABORATORIO", 3));
        portales.put("CENTRO_COMERCIAL", new Portal("PORTAL_CENTRO_COMERCIAL", "CENTRO_COMERCIAL", 4));
        portales.put("ALCANTARILLADO", new Portal("PORTAL_ALCANTARILLADO", "ALCANTARILLADO", 2));

        sangreVecna = new AtomicInteger(0);
        ninosEnColmena = new AtomicInteger(0);
        capturasTotalesHistoricas = new AtomicInteger(0);
        siguienteIdDemogorgon = new AtomicInteger(1);

        eventoActivo = "SIN EVENTO ACTIVO";
        tiempoRestanteEvento = "00:00";

        apagonActivo = false;
        tormentaActiva = false;
        intervencionElevenActiva = false;
        redMentalActiva = false;
    }

    public Zona getZona(String nombre) {
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

    public int sumarSangre(int cantidad) {
        return sangreVecna.addAndGet(cantidad);
    }

    public int getSangreVecna() {
        return sangreVecna.get();
    }

    public synchronized int incrementarCapturadosColmena() {
        int actualEnColmena = ninosEnColmena.incrementAndGet();
        int totalHistorico = capturasTotalesHistoricas.incrementAndGet();

        if (totalHistorico % 8 == 0) {
            crearNuevoDemogorgon();
        }

        return actualEnColmena;
    }

    private void crearNuevoDemogorgon() {
        String id = String.format("D%04d", siguienteIdDemogorgon.getAndIncrement());
        String zonaInicial = elegirZonaPeligrosaAleatoria();

        Demogorgon nuevo = new Demogorgon(id, this, zonaInicial);
        nuevo.start();

        Logger.log("Vecna genera un nuevo demogorgon: " + id + " en " + zonaInicial);
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

    public synchronized InterfazServidor.SimulationSnapshot crearSnapshot() {
        Map<String, InterfazServidor.ZoneData> mapaZonas = new HashMap<>();
        Map<String, InterfazServidor.PortalData> mapaPortales = new HashMap<>();

        int totalNinosActivos = 0;
        int totalDemogorgonsActivos = 0;

        for (Map.Entry<String, Zona> entry : zonas.entrySet()) {
            String nombre = entry.getKey();
            Zona zona = entry.getValue();

            int numeroNinos = zona.getNumeroNinos();
            int numeroDemogorgons = zona.getNumeroDemogorgons();

            InterfazServidor.ZoneData dataZona = new InterfazServidor.ZoneData(
                    numeroNinos,
                    numeroDemogorgons,
                    zona.getIdsNinos(),
                    zona.getIdsDemogorgons()
            );

            mapaZonas.put(nombre, dataZona);

            if (!nombre.equals("COLMENA")) {
                totalNinosActivos += numeroNinos;
            }

            totalDemogorgonsActivos += numeroDemogorgons;
        }

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

        return new InterfazServidor.SimulationSnapshot(
                eventoActivo,
                tiempoRestanteEvento,
                sangreVecna.get(),
                totalNinosActivos,
                totalDemogorgonsActivos,
                mapaZonas,
                mapaPortales,
                List.of(),
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
        intervencionElevenActiva = true;
        eventoActivo = "INTERVENCION DE ELEVEN";
        paralizarDemogorgons();
    }

    public synchronized void desactivarIntervencionEleven() {
        intervencionElevenActiva = false;
        eventoActivo = "SIN EVENTO ACTIVO";
        notifyAll();
    }

    public synchronized void esperarFinIntervencionEleven() throws InterruptedException {
        while (intervencionElevenActiva) {
            wait();
        }
    }
    public synchronized void liberarNinosColmenaSegunSangreDisponible() {
        Zona colmena = getZona("COLMENA");
        Zona callePrincipal = getZona("CALLE_PRINCIPAL");

        int sangreDisponible = sangreVecna.get();
        int liberados = 0;

        for (int i = 0; i < sangreDisponible; i++) {
            Nino nino = colmena.getNinoAleatorio();

            if (nino == null) {
                break;
            }

            colmena.salirNino(nino);
            callePrincipal.entrarNino(nino);
            nino.liberarDeColmena();

            liberados++;

            Logger.log("Eleven libera al niño " + nino.getIdNino()
                    + " y regresa a CALLE_PRINCIPAL");
        }

        if (liberados > 0) {
            int sangreRestante = sangreVecna.addAndGet(-liberados);
            int colmenaRestante = ninosEnColmena.addAndGet(-liberados);

            Logger.log("Eleven ha liberado " + liberados
                    + " niños de la COLMENA usando " + liberados
                    + " unidades de sangre");

            Logger.log("Sangre restante: " + sangreRestante
                    + " | Niños restantes en COLMENA: " + colmenaRestante);
        } else {
            Logger.log("Eleven no libera ningún niño de la COLMENA");
        }
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
        redMentalActiva = true;
        eventoActivo = "LA RED MENTAL";
        despertarDemogorgons();
    }

    public synchronized void desactivarRedMental() {
        redMentalActiva = false;
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

        // Si no está en una de las mejores, va a una de las candidatas
        int posicion = (int)(Math.random() * candidatas.size());
        return candidatas.get(posicion);
    }


    public synchronized String crearTextoRemoto() {
        InterfazServidor.SimulationSnapshot snapshot = crearSnapshot();

        return "TOTAL NIÑOS: " + snapshot.totalNinosActivos() + "\n" +
                "TOTAL DEMOGORGONS: " + snapshot.totalDemogorgonsActivos() + "\n" +
                "SANGRE: " + snapshot.sangreVecna() + "\n" +
                "EVENTO: " + snapshot.eventoActivo() + "\n" +
                "TIEMPO EVENTO: " + snapshot.tiempoRestanteEvento();
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
}