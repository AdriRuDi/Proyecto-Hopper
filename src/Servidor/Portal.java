package Servidor;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Portal {
    private String nombre;
    private String zonaDestino;
    private int tamGrupoIda;

    private ReentrantLock lock = new ReentrantLock(true);   //Cerrojo principal del portal
    private Condition condIda = lock.newCondition();    // Condición para niños esperando formar grupo de ida
    private Condition condVuelta = lock.newCondition(); // Condición para niños esperando volver a Hawkins
    private Condition condCruce = lock.newCondition();  // Condición para controlar cuándo puede cruzarse
    private Semaphore pasoPortal = new Semaphore(1, true);  //Semáforo con un único permiso para que crucen de 1 en 1

    private LinkedList<String> colaIda = new LinkedList<>();    //para niños esperando grupo en sótano byers
    private LinkedList<String> colaVuelta = new LinkedList<>(); //cola con preferencia de niños que vuelven del UpsideDown

    //Grupo cruzando y actualizándose
    private LinkedHashSet<String> grupoActualIda = new LinkedHashSet<>();

    private boolean grupoIdaActivo = false;         //grupo de ida en marcha
    private int pendientesGrupoIda = 0;             //pendientes de cruzar
    private String cruzandoAhora = "";              //para que se vea que nadie está cruzando
    private boolean apagonActivo = false;           // Indica si el portal está bloqueado por el evento de apagón

    private EstadoSimulacion estado;

    public Portal(String nombre, String zonaDestino, int tamGrupoIda, EstadoSimulacion estado){
        this.nombre = nombre;
        this.zonaDestino = zonaDestino;
        this.tamGrupoIda = tamGrupoIda;
        this.estado = estado;
    }

    public void solicitarIda(String idNino) throws InterruptedException {
        lock.lock();
        try {
            if (!colaIda.contains(idNino)) {    //Entra en la cola de ida si no estaba ya
                colaIda.addLast(idNino);
                Logger.log(idNino + " entra en cola de ida del " + nombre);
            }

            intentarFormarGrupoIda();

            while (!grupoActualIda.contains(idNino)) {  //Si niño no pertenece a grupoActual se queda esperando
                condIda.await();
                intentarFormarGrupoIda();   //Comprueba si puede formar grupo al despertarse
            }

        } finally {
            lock.unlock();
        }
    }
    public void cruzarIda(String idNino) throws InterruptedException {
        estado.esperarSiPausado();

        lock.lock();
        try {
            while (apagonActivo || !grupoIdaActivo || !grupoActualIda.contains(idNino)) {
                condCruce.await();
            }
        } finally {
            lock.unlock();
        }

        estado.esperarSiPausado();

        pasoPortal.acquire();

        try {
            estado.esperarSiPausado();

            lock.lock();
            try {
                while (apagonActivo || !grupoIdaActivo || !grupoActualIda.contains(idNino)) {
                    condCruce.await();
                }

                colaIda.remove(idNino);
                cruzandoAhora = idNino;
                Logger.log(idNino + " empieza a cruzar ida por " + nombre +
                        " hacia " + zonaDestino);
            } finally {
                lock.unlock();
            }

            Thread.sleep(1000);

            lock.lock();
            try {
                grupoActualIda.remove(idNino);
                pendientesGrupoIda--;
                Logger.log(idNino + " termina de cruzar ida por " + nombre +
                        " hacia " + zonaDestino);
                cruzandoAhora = "";

                if (pendientesGrupoIda == 0) {
                    grupoIdaActivo = false;
                    grupoActualIda.clear();
                    pendientesGrupoIda = 0;
                    Logger.log("Termina el grupo de ida del " + nombre);

                    intentarFormarGrupoIda();
                }

                condCruce.signalAll();
                condIda.signalAll();
                condVuelta.signalAll();

            } finally {
                lock.unlock();
            }
        } finally {
            pasoPortal.release();
        }
    }
    public void solicitarVuelta(String idNino) throws InterruptedException {
        estado.esperarSiPausado();

        lock.lock();
        try {
            if (!colaVuelta.contains(idNino)) {
                colaVuelta.addLast(idNino);
                Logger.log(idNino + " entra en cola de vuelta del " + nombre);
                condVuelta.signalAll();
            }

            while (apagonActivo || colaVuelta.isEmpty() || !idNino.equals(colaVuelta.getFirst())) {
                condVuelta.await();
            }
        } finally {
            lock.unlock();
        }
    }

    public void cruzarVuelta(String idNino) throws InterruptedException {
        lock.lock();
        try {
            while (apagonActivo || colaVuelta.isEmpty() || !idNino.equals(colaVuelta.getFirst())) {
                condVuelta.await();
            }
        } finally {
            lock.unlock();
        }

        pasoPortal.acquire();
        try {
            lock.lock();
            try {
                while (apagonActivo || colaVuelta.isEmpty() || !idNino.equals(colaVuelta.getFirst())) {
                    condVuelta.await();
                }

                colaVuelta.removeFirst();
                cruzandoAhora = idNino;
                Logger.log(idNino + " empieza a cruzar vuelta por " + nombre +
                        " hacia Hawkins");
            } finally {
                lock.unlock();
            }

            Thread.sleep(1000);

            lock.lock();
            try {
                Logger.log(idNino + " termina de cruzar vuelta por " + nombre +
                        " hacia Hawkins");
                cruzandoAhora = "";

                condVuelta.signalAll();
                condIda.signalAll();
                condCruce.signalAll();
            } finally {
                lock.unlock();
            }
        } finally {
            pasoPortal.release();
        }
    }

    public void activarApagon() {
        lock.lock();
        try {
            apagonActivo = true;
            Logger.log("Se activa apagón en " + nombre);
            intentarFormarGrupoIda();

            condVuelta.signalAll();
            condIda.signalAll();
            condCruce.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void desactivarApagon() {
        lock.lock();
        try {
            apagonActivo = false;
            Logger.log("Se desactiva apagón en " + nombre);

            intentarFormarGrupoIda();

            condIda.signalAll();
            condVuelta.signalAll();
            condCruce.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public String getNombre(){
        return nombre;
    }
    public String getZonaDestino(){
        return zonaDestino;
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
    public boolean isOcupado() {
        return pasoPortal.availablePermits() == 0;
    }

    private void intentarFormarGrupoIda() {
        if (!apagonActivo &&
                colaVuelta.isEmpty() &&     //se forma grupo de ida si no hay niños esperando a volver
                !grupoIdaActivo &&  //garantiza que no se mezclen niños nuevos con un grupo que ya se ha formado
                colaIda.size() >= tamGrupoIda) {    //cuando haya suficientes niños

            grupoActualIda.clear();

            for (int i = 0; i < tamGrupoIda; i++) {     //Se seleccionan en el orden de llegada para hacer grupos
                grupoActualIda.add(colaIda.get(i));
            }

            grupoIdaActivo = true;  //Se marca que ya hay un grupo activo
            pendientesGrupoIda = tamGrupoIda;   //Cuantos niños quedan por cruzar

            Logger.log("Se forma grupo de ida en " + nombre + " hacia " + zonaDestino +
                    ": " + grupoActualIda);

            condIda.signalAll();
            condCruce.signalAll();
        }
    }

    public void eliminarNino(String idNino) {
        lock.lock();
        try {
            colaIda.remove(idNino);
            colaVuelta.remove(idNino);
            grupoActualIda.remove(idNino);

            if (grupoIdaActivo && pendientesGrupoIda > 0 && grupoActualIda.isEmpty()) {
                grupoIdaActivo = false;
                pendientesGrupoIda = 0;
            }

            intentarFormarGrupoIda();

            condIda.signalAll();
            condVuelta.signalAll();
            condCruce.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public InterfazServidor.PortalData crearPortalData() {
        lock.lock();
        try {
            return new InterfazServidor.PortalData(
                    new ArrayList<>(colaIda),
                    new ArrayList<>(colaVuelta),
                    pasoPortal.availablePermits() == 0,
                    cruzandoAhora
            );
        } finally {
            lock.unlock();
        }
    }

}
