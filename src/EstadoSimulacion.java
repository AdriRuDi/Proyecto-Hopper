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
}
