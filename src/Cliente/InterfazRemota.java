package Cliente;

import Servidor.InterfazServidor;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

public class InterfazRemota extends JFrame {

    private static final Color BG = new Color(6, 18, 12);
    private static final Color PANEL = new Color(8, 35, 20);
    private static final Color LINE = new Color(55, 180, 100);
    private static final Color TEXT = new Color(180, 255, 190);
    private static final Color ALERT_BG = new Color(70, 20, 20);
    private final JTextArea miTextArea = new JTextArea();

    private final JLabel lblTotalHawkins = labelValor("");
    private final JLabel lblPortales = labelTexto("");
    private final JLabel lblNinosUpsideDown = labelTexto("");
    private final JLabel lblDemogorgonsUpsideDown = labelTexto("");
    private final JLabel lblRanking = labelTexto("");
    private final JLabel lblEvento = labelTexto("");


    private final JButton btnDetener = new JButton("DETENER PROGRAMA PRINCIPAL");

    public InterfazRemota() {
        super("Modulo Remoto - Hawkins");
        setSize(1150, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(buildUI());
    }

    private JPanel buildUI() {
        JPanel root = new MonitorPanel();
        root.setLayout(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        root.add(labelTitulo("STRANGER THINGS - MODULO REMOTO"), BorderLayout.NORTH);
        root.add(buildMain(), BorderLayout.CENTER);

        return root;
    }

    private JPanel buildMain() {
        JPanel main = panelTransparente(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        gbc.insets = new Insets(0, 0, 0, 12);

        gbc.gridx = 0;
        gbc.weightx = 0.9;
        main.add(buildResumenHawkins(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.1;
        main.add(buildUpsideDown(), gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.75;
        gbc.insets = new Insets(0, 0, 0, 0);
        main.add(buildPanelDerecho(), gbc);

        return main;
    }

    private JPanel buildResumenHawkins() {

        JPanel panel = columna();

        panel.add(seccion("TOTAL NIÑOS EN HAWKINS", lblTotalHawkins));

        panel.add(Box.createVerticalStrut(14));

        panel.add(seccion("ESTADO DE PORTALES (NIÑOS)", lblPortales));

        panel.add(Box.createVerticalGlue());

        panel.add(btnDetener);

        return panel;

    }

    private JPanel buildUpsideDown() {
        JPanel panel = columna();

        JLabel titulo = labelSeccion("ESTADO DEL UPSIDE DOWN");
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titulo);
        panel.add(Box.createVerticalStrut(10));

        JPanel columnas = panelTransparente(new GridLayout(1, 2, 12, 0));
        columnas.add(seccion("UBICACIONES (NIÑOS)", lblNinosUpsideDown));
        columnas.add(seccion("UBICACIONES (DEMOGORGONS)", lblDemogorgonsUpsideDown));

        panel.add(columnas);

        return panel;
    }

    private JPanel buildPanelDerecho() {
        JPanel panel = columna();

        panel.add(seccion("RANKING DEMOGORGONS\n(CAPTURAS)", lblRanking));
        panel.add(Box.createVerticalStrut(14));
        panel.add(seccion("EVENTO GLOBAL ACTIVO", lblEvento));

        return panel;
    }

    private JPanel seccion(String titulo, JLabel contenido) {
        JPanel panel = panelTransparente(new BorderLayout(8, 8));
        panel.setBorder(new NeonBorder());

        JLabel lblTitulo = labelSeccion(titulo);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(lblTitulo, BorderLayout.NORTH);
        panel.add(contenido, BorderLayout.CENTER);

        return panel;
    }

    public void updateSnapshot(InterfazServidor.SimulationSnapshot snapshot) {
        lblTotalHawkins.setText(String.valueOf(snapshot.totalNinosActivos()));

        lblPortales.setText(html(
                "PORTAL BOSQUE [" + snapshot.portales().get("BOSQUE").idsIda().size() + "] niños<br>" +
                        "PORTAL LABORATORIO [" + snapshot.portales().get("LABORATORIO").idsIda().size() + "] niños<br>" +
                        "PORTAL CENTRO [" + snapshot.portales().get("CENTRO_COMERCIAL").idsIda().size() + "] niños<br>" +
                        "PORTAL ALCANTARILLADO [" + snapshot.portales().get("ALCANTARILLADO").idsIda().size() + "] niños"
        ));

        lblNinosUpsideDown.setText(html(
                "BOSQUE (" + snapshot.zonas().get("BOSQUE").ninos() + ")<br>" +
                        "LABORATORIO (" + snapshot.zonas().get("LABORATORIO").ninos() + ")<br>" +
                        "CENTRO COMERCIAL (" + snapshot.zonas().get("CENTRO_COMERCIAL").ninos() + ")<br>" +
                        "ALCANTARILLADO (" + snapshot.zonas().get("ALCANTARILLADO").ninos() + ")<br><br>" +
                        "[!] COLMENA (" + snapshot.zonas().get("COLMENA").ninos() + ")"
        ));

        lblDemogorgonsUpsideDown.setText(html(
                "BOSQUE (" + snapshot.zonas().get("BOSQUE").demogorgons() + ")<br>" +
                        "LABORATORIO (" + snapshot.zonas().get("LABORATORIO").demogorgons() + ")<br>" +
                        "CENTRO COMERCIAL (" + snapshot.zonas().get("CENTRO_COMERCIAL").demogorgons() + ")<br>" +
                        "ALCANTARILLADO (" + snapshot.zonas().get("ALCANTARILLADO").demogorgons() + ")"
        ));

        lblRanking.setText(snapshot.topDemogorgons().isEmpty()
                ? html("Sin datos todavía")
                : html(String.join("<br>", snapshot.topDemogorgons())));

        lblEvento.setText(html(
                "TIPO: " + snapshot.eventoActivo() + "<br><br>" +
                        "TIEMPO RESTANTE:<br>" + snapshot.tiempoRestanteEvento()
        ));
    }

    public JButton getBtnDetener() {
        return btnDetener;
    }

    private static String html(String text) {
        return "<html><div style='text-align:center;'>" + text + "</div></html>";
    }

    private JPanel columna() {
        JPanel panel = panelTransparente();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new NeonBorder());
        return panel;
    }

    private static JPanel panelTransparente() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        return panel;
    }

    private static JPanel panelTransparente(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    private static JLabel labelTitulo(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT);
        label.setFont(fuente(Font.BOLD, 28f));
        return label;
    }

    private static JLabel labelSeccion(String text) {
        JLabel label = new JLabel(html(text.replace("\n", "<br>")));
        label.setForeground(TEXT);
        label.setFont(fuente(Font.BOLD, 18f));
        return label;
    }

    private static JLabel labelTexto(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(TEXT);
        label.setFont(fuente(Font.PLAIN, 17f));
        return label;
    }

    private static JLabel labelValor(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(TEXT);
        label.setFont(fuente(Font.BOLD, 30f));
        return label;
    }

    private static Font fuente(int style, float size) {
        Font base = new Font("Serif", style, Math.round(size));
        Map<TextAttribute, Object> attributes = new HashMap<>(base.getAttributes());
        attributes.put(TextAttribute.TRACKING, 0.06);
        return base.deriveFont(attributes).deriveFont(size);
    }

    private static class MonitorPanel extends JPanel {
        MonitorPanel() {
            setBackground(BG);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setPaint(new GradientPaint(
                    0, 0, new Color(6, 25, 15),
                    getWidth(), getHeight(), new Color(3, 12, 8)
            ));
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(new Color(40, 120, 70, 30));
            for (int y = 0; y < getHeight(); y += 4) {
                g2.drawLine(0, y, getWidth(), y);
            }

            g2.dispose();
        }
    }

    private static class NeonBorder extends AbstractBorder {
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(10, 10, 10, 10);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = insets.top = insets.bottom = 10;
            return insets;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(LINE);
            g2.drawRoundRect(x + 1, y + 1, width - 3, height - 3, 10, 10);
            g2.setColor(new Color(120, 255, 170, 70));
            g2.drawRoundRect(x + 3, y + 3, width - 7, height - 7, 8, 8);
            g2.dispose();
        }
    }
    public void actualizar(String datos) {
        SwingUtilities.invokeLater(() -> {
            Map<String, String> mapa = parsearDatos(datos);

            lblTotalHawkins.setText(mapa.getOrDefault("TOTAL_NINOS", "0"));

            lblPortales.setText(html(
                    "PORTAL 1 [" + mapa.getOrDefault("PORTAL_BOSQUE", "0") + "] niños<br><br>" +
                            "PORTAL 2 [" + mapa.getOrDefault("PORTAL_LABORATORIO", "0") + "] niños<br><br>" +
                            "PORTAL 3 [" + mapa.getOrDefault("PORTAL_CENTRO", "0") + "] niños<br><br>" +
                            "PORTAL 4 [" + mapa.getOrDefault("PORTAL_ALCANTARILLADO", "0") + "] niños"
            ));

            lblNinosUpsideDown.setText(html(
                    "BOSQUE (" + mapa.getOrDefault("NINOS_BOSQUE", "0") + ")<br><br>" +
                            "LABORATORIO (" + mapa.getOrDefault("NINOS_LABORATORIO", "0") + ")<br><br>" +
                            "CENTRO COMERCIAL<br>(" + mapa.getOrDefault("NINOS_CENTRO", "0") + ")<br><br>" +
                            "ALCANTARILLADO (" + mapa.getOrDefault("NINOS_ALCANTARILLADO", "0") + ")<br><br>" +
                            "[!] COLMENA<br>(CAPTURADOS) [" + mapa.getOrDefault("NINOS_COLMENA", "0") + "]"
            ));

            lblDemogorgonsUpsideDown.setText(html(
                    "BOSQUE (" + mapa.getOrDefault("DEMOS_BOSQUE", "0") + ")<br><br>" +
                            "LABORATORIO (" + mapa.getOrDefault("DEMOS_LABORATORIO", "0") + ")<br><br>" +
                            "CENTRO COMERCIAL<br>(" + mapa.getOrDefault("DEMOS_CENTRO", "0") + ")<br><br>" +
                            "ALCANTARILLADO (" + mapa.getOrDefault("DEMOS_ALCANTARILLADO", "0") + ")"
            ));

            String ranking = mapa.getOrDefault("RANKING", "");
            lblRanking.setText(ranking.isEmpty()
                    ? html("Sin datos todavía")
                    : html(ranking.replace(";", "<br>")));

            lblEvento.setText(html(
                    "TIPO: " + mapa.getOrDefault("EVENTO", "SIN EVENTO ACTIVO") + "<br><br>" +
                            "TIEMPO RESTANTE:<br>" +
                            mapa.getOrDefault("TIEMPO_EVENTO", "00:00")
            ));
        });
    }

    private Map<String, String> parsearDatos(String datos) {
        Map<String, String> mapa = new HashMap<>();

        String[] lineas = datos.split("\n");

        for (String linea : lineas) {
            String[] partes = linea.split("=", 2);

            if (partes.length == 2) {
                mapa.put(partes[0], partes[1]);
            }
        }

        return mapa;
    }
}