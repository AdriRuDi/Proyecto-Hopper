import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
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
    }

    public void solicitarIda(String idNino) throws InterruptedException{
        lock.lock();
        try{
            colaIda.addLast(idNino);
            Logger.log(idNino + " entra en cola de ida del " + nombre);

            while(true){
                //Si hay un grupo de ida cruzando solo esperan a que se termine
                if(grupoIdaActivo){
                    if(grupoActualIda.contains(idNino)){
                        return;
                    }
                    condIda.await();
                    continue;
                }
                //Si hay un apagon o alguien esperando volver,no se puede formar un grupo nuevo
                if(apagonActivo || !colaVuelta.isEmpty()){
                    condIda.await();
                    continue;
                }
                //Si no hay suficientes para formar grupo espera
                if(colaIda.size() < tamGrupoIda){
                    condIda.await();
                    continue;
                }
                //Si lo anterior no se cumple se forma grupo nuevo con los primeros tamGrupoIda de la cola
                grupoActualIda.clear();
                for(int i = 0; i < tamGrupoIda;i++){
                    grupoActualIda.add(colaIda.get(i));
                }

                grupoIdaActivo = true;
                pendientesGrupoIda = tamGrupoIda;

                Logger.log("Se forma grupo de ida en " + nombre + " hacia " + zonaDestino +
                        ": " + grupoActualIda);

                //Si pertenece al grupo recien formado entonces puede salir hacia el UpsideDown
                if(grupoActualIda.contains(idNino)){
                    condCruce.signalAll();
                    return;
                }

                //Sino espera a otro grupo
                condIda.await();
            }
        } finally{
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
                colaIda.remove(idNino);
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
            while (apagonActivo || ocupado || !idNino.equals(colaVuelta.getFirst())){
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
                if(!colaVuelta.isEmpty() && idNino.equals(colaVuelta.getFirst())){
                    colaVuelta.removeFirst();
                } else{
                    colaVuelta.remove(idNino);
                }
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
}
