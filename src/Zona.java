import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.List;
import java.util.ArrayList;

public abstract class Zona
{
    private String nombre;
    private List <Nino> ninos;
    private List <Demogorgon> demogorgons;
    private Lock lockZona;

    public Zona(String nombre)
    {
        this.nombre = nombre;
        this.ninos = new ArrayList<>();
        this.demogorgons = new ArrayList<>();
        this.lockZona = new ReentrantLock();
    }
    public String getNombre()
    {
        return nombre;
    }
    public void entrarNino(Nino nino)
    {
        lockZona.lock();
        try {
            ninos.add(nino);
        } finally {
            lockZona.unlock();
        }
    }
    public void salirNino(Nino nino)
    {
        lockZona.lock();
        try{
            ninos.remove(nino);
        } finally {
            lockZona.unlock();
        }
    }
    public void entrarDemogorgons(Demogorgon demogorgon)
    {
        lockZona.lock();
        try{
            ninos.add(demogorgon);
        } finally {
            lockZona.unlock();
        }
    }
    public void salirNino(Demogorgon demogorgon)
    {
        lockZona.lock();
        try{
            ninos.remove(demogorgon);
        } finally {
            lockZona.unlock();
        }
    }
    public int getNumeroNinos()
    {
        lockZona.lock();
        try{
            return ninos.size();
        } finally {
            lockZona.unlock();
        }
    }
    public int getNumeroDemogorgons()
    {
        lockZona.lock();
        try{
            return demogorgons.size();
        } finally {
            lockZona.unlock();
        }
    }
    public List<String> getIdsNinos()
    {
        lockZona.lock();
        try{
            List<String> listaIds = new ArrayList<>();
            for (Nino n: ninos){
                listaIds.add(n.getIdNino());
            }
            return listaIds;
        } finally {
            lockZona.unlock();
        }
    }
    public List<String> getIdsDemogorgons()
    {
        lockZona.lock();
        try{
            List<String> listaIds = new ArrayList<>();
            for (Demogorgon d: demogorgons){
                listaIds.add(d.getIdDemogorgon());
            }
            return listaIds;
        } finally {
            lockZona.unlock();
        }
    }



}