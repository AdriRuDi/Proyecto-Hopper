import interfaz.InterfazServidor;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class EstadoSimulacion
{
    private Map<String,Zona> zonas = new HashMap<>();
    private int sangreVecna;
    private int capturadosColmena;
    public EstadoSimulacion()
    {
        zonas.put("CALLE_PRINCIPAL", new ZonaSegura("CALLE_PRINCIPAL"));
        zonas.put("SOTANO_BYERS",new ZonaSegura("SOTANO_BYERS"));
        zonas.put("RADIO_WSQK",new ZonaSegura("RADIO_WSQK"));

        zonas.put("BOSQUE",new ZonaPeligrosa("BOSQUE"));
        zonas.put("LABORATORIO",new ZonaPeligrosa("LABORATORIO"));
        zonas.put("CENTRO_COMERCIAL",new ZonaPeligrosa("CENTRO_COMERCIAL"));
        zonas.put("ALCANTARILLADO",new ZonaPeligrosa("ALCANTARILLADO"));

        zonas.put("COLMENA",new ZonaPeligrosa("COLMENA")); //Cambiar luego a ZonaColmena (zona especial)

        this.sangreVecna = 0;
        this.capturadosColmena = 0;

    }
    public Zona getZona(String nombre)
    {
        if (!zonas.containsKey(nombre)) {
            throw new IllegalArgumentException("Zona no existe: " + nombre);
        }
        return zonas.get(nombre);
    }

    public synchronized void sumarSangre(int cantidad)
    {
        sangreVecna += cantidad;
    }
    public synchronized void incrementarCaputradosColmena()
    {
        capturadosColmena ++;
    }
    public synchronized int getCapturadosColmena()
    {
        return capturadosColmena;
    }

    public synchronized InterfazServidor.SimulationSnapshot crearSnapshot() {
        Map<String, InterfazServidor.ZoneData> mapaZonas = new HashMap<>();

        int totalNinosActivos = 0;
        int totalDemogorgonsActivos = 0;

        for (Map.Entry<String, Zona> entry : zonas.entrySet()) {
            String nombre = entry.getKey();
            Zona zona = entry.getValue();

            int numeroNinos = zona.getNumeroNinos();
            int numeroDemogorgons = zona.getNumeroDemogorgons();

            InterfazServidor.ZoneData data = new InterfazServidor.ZoneData(
                    numeroNinos,
                    numeroDemogorgons,
                    zona.getIdsNinos(),
                    zona.getIdsDemogorgons()
            );

            mapaZonas.put(nombre, data);

            if (!nombre.equals("COLMENA")) {
                totalNinosActivos += numeroNinos;
            }

            totalDemogorgonsActivos += numeroDemogorgons;
        }

        Map<String, InterfazServidor.PortalData> mapaPortales = new HashMap<>();
        mapaPortales.put("BOSQUE", new InterfazServidor.PortalData(0, 0, false, ""));
        mapaPortales.put("LABORATORIO", new InterfazServidor.PortalData(0, 0, false, ""));
        mapaPortales.put("CENTRO_COMERCIAL", new InterfazServidor.PortalData(0, 0, false, ""));
        mapaPortales.put("ALCANTARILLADO", new InterfazServidor.PortalData(0, 0, false, ""));

        return new InterfazServidor.SimulationSnapshot(
                "SIN EVENTO ACTIVO",
                "00:00",
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
