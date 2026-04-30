package Servidor;

import Cliente.InterfaceHawkins;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ServicioHawkins extends UnicastRemoteObject implements InterfaceHawkins {

    private EstadoSimulacion estado;

    public ServicioHawkins(EstadoSimulacion estado) throws RemoteException {
        this.estado = estado;
    }

    @Override
    public String obtenerDatosRemotos() throws RemoteException {
        return estado.crearTextoRemoto();
    }

    @Override
    public void detenerPrograma() throws RemoteException {
        Logger.log("Programa principal detenido desde el módulo remoto");
        System.exit(0);
    }
}