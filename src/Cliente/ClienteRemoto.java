package Cliente;

import java.rmi.Naming;

public class ClienteRemoto {
    public static void main(String[] args){
        try{
            InterfaceHawkins servicio = (InterfaceHawkins) Naming.lookup("//127.0.0.1/ServicioHawkins");

            InterfazRemota gui = new InterfazRemota();
            gui.setVisible(true);

            gui.getBtnDetener().addActionListener(e -> {
                try {
                    servicio.detenerPrograma();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            while (true){
                String datos = servicio.obtenerDatosRemotos();
                gui.actualizar(datos);
                Thread.sleep(1000);
            }
        } catch(Exception e){
            System.out.println("Error en cliente: " + e.getMessage());

            e.printStackTrace();
        }
    }
}
