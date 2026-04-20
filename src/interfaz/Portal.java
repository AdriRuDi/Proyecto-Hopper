package interfaz;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Portal {
    private String nombre;
    private String zonaDestino;
    private int tamGrupoIda;

    private ReentrantLock lock = new ReentrantLock(true);
    private Condition condIda = lock.newCondition();
    private Condition condVuelta = lock.newCondition();
    private Condition condCruce = lock.newCondition();
    private Condition condApagon = lock.newCondition();
    private CyclicBarrier barreraIda;

    private LinkedList<String> colaIda = new LinkedList<>();    //para niños esperando grupo en sótano byers
    private LinkedList<String> colaVuelta = new LinkedList<>(); //cola con preferencia de niños que vuelven del UpsideDown

    //Grupo cruzando y actualizándose
    private LinkedHashSet<String> grupoActualIda = new LinkedHashSet<>();

    private boolean ocupado = false;                //porque solo puede cruzar un niño a la vez
    private boolean grupoIdaActivo = false;         //grupo de ida en marcha
    private int pendientesGrupoIda = 0;             //pendientes de cruzar
    private String cruzandoAhora = "";              //para que se vea que nadie está cruzando
    private boolean apagonActivo = false;           //terminan de cruzar los niños que estaban cruzando

    public Portal(String nombre, String zonaDestino, int tamGrupoIda){
        this.nombre = nombre;
        this.zonaDestino = zonaDestino;
        this.tamGrupoIda = tamGrupoIda;
        this.barreraIda = new CyclicBarrier(tamGrupoIda);
    }

    public void solicitarIda(String idNino) throws InterruptedException {
        boolean ultimoEnLlegar = false;

        lock.lock();
        try {
            colaIda.addLast(idNino);
            Logger.log(idNino + " entra en cola de ida del " + nombre);

            while (apagonActivo || !colaVuelta.isEmpty() || grupoIdaActivo ||
                    colaIda.size() < tamGrupoIda ||
                    !colaIda.subList(0, tamGrupoIda).contains(idNino)) {
                condIda.await();
            }

            grupoActualIda.add(idNino);

        } finally {
            lock.unlock();
        }

        try {
            int indice = barreraIda.await();
            if (indice == 0) {
                ultimoEnLlegar = true;
            }
        } catch (BrokenBarrierException e) {
            throw new InterruptedException("Barrera de ida rota en " + nombre);
        }

        lock.lock();
        try {
            if (ultimoEnLlegar) {
                grupoIdaActivo = true;
                pendientesGrupoIda = tamGrupoIda;

                Logger.log("Se forma grupo de ida en " + nombre + " hacia " + zonaDestino +
                        ": " + grupoActualIda);

                condCruce.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }
    public void cruzarIda(String idNino) throws InterruptedException{
        lock.lock();
        try{
            while(apagonActivo || ocupado || !grupoIdaActivo || !grupoActualIda.contains(idNino)){
                condCruce.await();
            }
            ocupado = true;
            colaIda.remove(idNino); //quito al niño que va a cruzar de la cola
            cruzandoAhora = idNino;
            Logger.log(idNino + " empieza a cruzar ida por " + nombre +
                    " hacia " + zonaDestino);

        } finally{
            lock.unlock();
        }
        try{
            Thread.sleep(1000);
        } finally{
            lock.lock();
            try{
                grupoActualIda.remove(idNino);
                pendientesGrupoIda--;
                Logger.log(idNino + " termina de cruzar ida por " + nombre +
                        " hacia " + zonaDestino);
                cruzandoAhora= "";
                ocupado = false;

                //Si era el ultimo del grupo se cierra el grupo actual
                if(pendientesGrupoIda == 0){
                    grupoIdaActivo = false;
                    grupoActualIda.clear();
                    pendientesGrupoIda = 0;
                    Logger.log("Termina el grupo de ida del " + nombre);
                }
                condCruce.signalAll();
                condIda.signalAll();
                condVuelta.signalAll();
                condApagon.signalAll();

            } finally{
                lock.unlock();
            }
        }
    }

    public void solicitarVuelta(String idNino) throws InterruptedException{
        lock.lock();
        try{
            colaVuelta.addLast(idNino);
            Logger.log(idNino + " entra en cola de vuelta del " + nombre);
            while (apagonActivo || ocupado || colaVuelta.isEmpty() || !idNino.equals(colaVuelta.getFirst())){
                condVuelta.await();
            }
        } finally {
            lock.unlock();
        }
    }
    public void cruzarVuelta(String idNino) throws InterruptedException{
        lock.lock();
        try{
            while(apagonActivo || ocupado || colaVuelta.isEmpty() || !idNino.equals(colaVuelta.getFirst())) {
                condVuelta.await();
            }
            ocupado = true;
            colaVuelta.removeFirst(); //quito al niño de la cola en cuanto va a cruzar
            cruzandoAhora = idNino;
            Logger.log(idNino + " empieza a cruzar vuelta por " + nombre +
                    " hacia Hawkins");
        } finally{
            lock.unlock();
        }
        try{
            Thread.sleep(1000);
        } finally{
            lock.lock();
            try{
                Logger.log(idNino + " termina de cruzar vuelta por " + nombre +
                        " hacia Hawkins");
                cruzandoAhora = "";
                ocupado = false;

                condVuelta.signalAll();
                condIda.signalAll();
                condCruce.signalAll();
                condApagon.signalAll();
            } finally{
                lock.unlock();
            }
        }
    }

    public void activarApagon(){
        lock.lock();
        try{
            apagonActivo = true;
            Logger.log("Se activa apagón en " + nombre);
        } finally{
            lock.unlock();
        }
    }
    public void desactivarApagon(){
        lock.lock();
        try{
            apagonActivo = false;
            Logger.log("Se desactiva apagón en " + nombre);
            condApagon.signalAll();
            condIda.signalAll();
            condVuelta.signalAll();
            condCruce.signalAll();
        } finally{
            lock.unlock();
        }
    }

    public String getNombre(){
        return nombre;
    }
    public String getZonaDestino(){
        return zonaDestino;
    }
    public int getTamGrupoIda(){
        return tamGrupoIda;
    }
    public List<String> getColaIda(){
        lock.lock();
        try{
            return new ArrayList<>(colaIda);
        } finally{
            lock.unlock();
        }
    }
    public List<String> getColaVuelta(){
        lock.lock();
        try{
            return new ArrayList<>(colaVuelta);
        } finally{
            lock.unlock();
        }
    }
    public String getCruzandoAhora(){
        lock.lock();
        try{
            return cruzandoAhora;
        } finally {
            lock.unlock();
        }
    }
    public boolean isOcupado(){
        lock.lock();
        try{
            return ocupado;
        } finally {
            lock.unlock();
        }
    }
    public boolean isApagonActivo(){
        lock.lock();
        try{
            return apagonActivo;
        } finally{
            lock.unlock();
        }
    }
    public int getPendientesGrupoIda() {
        lock.lock();
        try {
            return pendientesGrupoIda;
        } finally {
            lock.unlock();
        }
    }
}
