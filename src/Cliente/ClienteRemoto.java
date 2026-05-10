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

                    String textoActual = gui.getBtnDetener().getText();

                    if (textoActual.contains("DETENER") || textoActual.contains("PAUSAR")) {
                        gui.getBtnDetener().setText("REANUDAR PROGRAMA PRINCIPAL");
                    } else {
                        gui.getBtnDetener().setText("PAUSAR PROGRAMA PRINCIPAL");
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            while (true) {
                try {
                    String datos = servicio.obtenerDatosRemotos();
                    gui.actualizar(datos);
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.out.println("No se pudieron obtener datos remotos: " + e.getMessage());
                    Thread.sleep(1000);
                }
            }
        } catch(Exception e){
            System.out.println("Error en cliente: " + e.getMessage());

            e.printStackTrace();
        }
    }
}
