package Servidor;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.List;
import java.util.ArrayList;

public abstract class Zona
{
    private String nombre;
    private List <Nino> ninos;  // Recurso compartido, lista de niños que se encuentran en esta zona
    private List <Demogorgon> demogorgons;  // Recurso compartido, lista de demogorgons que se encuentran en esta zona
    private Lock lockZona;

    public Zona(String nombre)
    {
        this.nombre = nombre;
        this.ninos = new ArrayList<>();
        this.demogorgons = new ArrayList<>();
        this.lockZona = new ReentrantLock(true);    //Niños esperan de forma ordenada
    }
    public String getNombre()
    {
        return nombre;
    }
    public void entrarNino(Nino nino)
    {
        lockZona.lock();
        try {
            if (!ninos.contains(nino)) {    //Evita insertar dos veces al mismo niño en la misma zona
                ninos.add(nino);
            }
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
    public void entrarDemogorgon(Demogorgon demogorgon)
    {
        lockZona.lock();
        try{
            if (!demogorgons.contains(demogorgon)) {    //Evita insertar dos veces al mismo demogorgon en la misma zona
                demogorgons.add(demogorgon);
            }
        } finally {
            lockZona.unlock();
        }
    }
    public void salirDemogorgon(Demogorgon demogorgon)
    {
        lockZona.lock();
        try{
            demogorgons.remove(demogorgon);
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

    public List<Nino> getNinos(){
        lockZona.lock();
        try{
            return new ArrayList<>(ninos);  //Devuelve una copia de la lista de niños
        } finally{
            lockZona.unlock();
        }
    }

    public List<Demogorgon> getDemogorgons(){
        lockZona.lock();
        try{
            return new ArrayList<>(demogorgons);    //Devuelve una copia de la lista de demogorgons
        } finally {
            lockZona.unlock();
        }
    }

    public Nino getNinoAleatorio() {
        lockZona.lock();
        try {
            if (ninos.isEmpty()) {
                return null;
            }
            int posicion = (int)(Math.random() * ninos.size());
            return ninos.get(posicion);
        } finally {
            lockZona.unlock();
        }
    }

    public boolean isVaciaDeNinos(){
        lockZona.lock();
        try{
            return ninos.isEmpty();
        } finally {
            lockZona.unlock();
        }
    }

    public boolean isVaciaDeDemogorgons() {
        lockZona.lock();
        try{
            return demogorgons.isEmpty();
        } finally{
            lockZona.unlock();
        }
    }
}