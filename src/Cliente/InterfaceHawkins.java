package Cliente;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InterfaceHawkins extends Remote {
    String obtenerDatosRemotos() throws RemoteException;

    boolean cambiarPausa() throws RemoteException;
}