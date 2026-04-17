package interfaz;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EstadoSimulacion {

    private Map<String, Zona> zonas;
    private Map<String, Portal> portales;

    private int sangreVecna;
    private int capturadosColmena;

    private String eventoActivo;
    private String tiempoRestanteEvento;

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

        sangreVecna = 0;
        capturadosColmena = 0;

        eventoActivo = "SIN EVENTO ACTIVO";
        tiempoRestanteEvento = "00:00";
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

    public synchronized void sumarSangre(int cantidad) {
        sangreVecna += cantidad;
    }

    public synchronized int getSangreVecna() {
        return sangreVecna;
    }

    public synchronized void incrementarCapturadosColmena() {
        capturadosColmena++;
    }

    public synchronized int getCapturadosColmena() {
        return capturadosColmena;
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
                sangreVecna,
                totalNinosActivos,
                totalDemogorgonsActivos,
                mapaZonas,
                mapaPortales,
                List.of(),
                List.of()
        );
    }
}