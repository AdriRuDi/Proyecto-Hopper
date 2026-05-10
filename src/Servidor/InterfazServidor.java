package Servidor;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InterfazServidor extends JFrame {

    private int mouseX;
    private int mouseY;

    private static final Color BG = new Color(6, 18, 12);
    private static final Color PANEL = new Color(8, 35, 20);
    private static final Color PANEL_DARK = new Color(4, 20, 12);
    private static final Color LINE_SOFT = new Color(55, 180, 100);
    private static final Color TEXT = new Color(180, 255, 190);
    private static final Color TEXT_SOFT = new Color(120, 210, 140);
    private static final Color ALERT_BG = new Color(70, 20, 20);
    private static final Color ALERT_LINE = new Color(255, 100, 100);

    private final JLabel lblTitulo = createTitleLabel("STRANGER THINGS");
    private final JLabel lblSubtitulo = createSectionLabel("HAWKINS", true);
    private final JLabel lblSangre = createCounterLabel("");
    private final JLabel lblColmena = createCounterLabel("");

    private final RetroListPanel callePrincipalPanel = new RetroListPanel("CALLE PRINCIPAL");
    private final RetroListPanel sotanoPanel = new RetroListPanel("SOTANO BYERS");
    private final RetroRadioPanel radioPanel = new RetroRadioPanel("RADIO WSQK", lblSangre);

    private final PortalPanel portalBosque = new PortalPanel();
    private final PortalPanel portalLaboratorio = new PortalPanel();
    private final PortalPanel portalCentro = new PortalPanel();
    private final PortalPanel portalAlcantarillado = new PortalPanel();

    private final ZoneDangerPanel bosquePanel = new ZoneDangerPanel("BOSQUE");
    private final ZoneDangerPanel laboratorioPanel = new ZoneDangerPanel("LABORATORIO");
    private final ZoneDangerPanel centroPanel = new ZoneDangerPanel("CENTRO COMERCIAL");
    private final ZoneDangerPanel alcantarilladoPanel = new ZoneDangerPanel("ALCANTARILLADO");

    public InterfazServidor() {
        super("La Batalla de Hawkins");
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 760);
        setLocationRelativeTo(null);
        setContentPane(buildUI());
    }

    private JPanel buildUI() {
        JPanel root = new MonitorPanel();
        root.setLayout(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildMain(), BorderLayout.CENTER);

        return root;
    }

    private JComponent buildHeader() {
        JPanel header = transparentPanel(new BorderLayout());

        JPanel left = transparentPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(lblTitulo);
        left.add(Box.createVerticalStrut(2));
        left.add(lblSubtitulo);

        JPanel right = transparentPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.add(createWindowButton("−"));
        right.add(createWindowButton("▢"));
        right.add(createWindowButton("X"));

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        header.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });

        header.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                setLocation(
                        e.getXOnScreen() - mouseX,
                        e.getYOnScreen() - mouseY
                );
            }
        });

        return header;
    }

    private JComponent buildMain() {
        JPanel main = transparentPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 10);

        gbc.gridx = 0;
        gbc.weightx = 1.05;
        main.add(buildHawkinsColumn(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.45;
        main.add(buildPortalColumn(), gbc);

        gbc.gridx = 2;
        gbc.weightx = 1.00;
        main.add(buildUpsideDownColumn(), gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.50;
        main.add(buildColmenaColumn(), gbc);

        return main;
    }

    private JComponent buildHawkinsColumn() {
        JPanel panel = createOuterColumn();
        panel.add(callePrincipalPanel);
        panel.add(Box.createVerticalStrut(16));
        panel.add(sotanoPanel);
        panel.add(Box.createVerticalStrut(16));
        panel.add(radioPanel);
        return panel;
    }

    private JComponent buildPortalColumn() {
        JPanel column = createOuterColumn();
        column.setLayout(new BorderLayout(6, 0));

        JPanel portals = transparentPanel(new GridLayout(4, 1, 0, 12));
        portals.add(portalBosque);
        portals.add(portalLaboratorio);
        portals.add(portalCentro);
        portals.add(portalAlcantarillado);

        JLabel leftLabel = verticalLabel("P<br>O<br>R<br>T<br>A<br>L");
        column.add(leftLabel, BorderLayout.WEST);
        column.add(portals, BorderLayout.CENTER);
        return column;
    }

    private JComponent buildUpsideDownColumn() {
        JPanel column = createOuterColumn();
        column.setLayout(new BorderLayout(6, 0));

        JPanel zones = transparentPanel(new GridLayout(4, 1, 0, 12));
        zones.add(bosquePanel);
        zones.add(laboratorioPanel);
        zones.add(centroPanel);
        zones.add(alcantarilladoPanel);

        JLabel rightLabel = verticalLabel("N<br>W<br>O<br>D<br>&nbsp;<br>E<br>D<br>I<br>S<br>P<br>U");
        column.add(zones, BorderLayout.CENTER);
        column.add(rightLabel, BorderLayout.EAST);
        return column;
    }

    private JComponent buildColmenaColumn() {
        JPanel panel = createOuterColumn();
        panel.setPreferredSize(new Dimension(145, 0));
        panel.setLayout(new BorderLayout());

        JLabel title = createSectionLabel("COLMENA\nNIÑOS", false);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(title, BorderLayout.NORTH);

        JPanel center = transparentPanel(new GridBagLayout());
        JPanel counter = createAlertBox(lblColmena);
        counter.setPreferredSize(new Dimension(88, 62));
        center.add(counter);
        panel.add(center, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createOuterColumn() {
        JPanel panel = transparentPanel();
        panel.setBorder(new NeonBorder());
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    private JButton createWindowButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(PANEL_DARK);
        button.setForeground(TEXT);
        button.setFont(strangerFont(Font.BOLD, 16f));
        button.setBorder(new NeonBorder());
        button.setPreferredSize(new Dimension(42, 32));

        button.addActionListener(e -> {
            switch (text) {
                case "−" -> setState(Frame.ICONIFIED);
                case "▢" -> {
                    int state = getExtendedState();
                    if ((state & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
                        setExtendedState(Frame.NORMAL);
                        setLocationRelativeTo(null);
                    } else {
                        setExtendedState(Frame.MAXIMIZED_BOTH);
                    }
                }
                case "X" -> dispose();
            }
        });

        return button;
    }

    private JPanel createAlertBox(JLabel value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(true);
        panel.setBackground(ALERT_BG);
        panel.setBorder(BorderFactory.createLineBorder(ALERT_LINE, 2));
        value.setForeground(TEXT);
        panel.add(value, BorderLayout.CENTER);
        return panel;
    }

    private JLabel verticalLabel(String htmlText) {
        JLabel label = new JLabel(
                "<html><div style='text-align:center; line-height:0.9;'>" + htmlText + "</div></html>"
        );
        label.setForeground(TEXT_SOFT);
        label.setFont(strangerFont(Font.BOLD, 20f));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setPreferredSize(new Dimension(34, 140));
        return label;
    }

    public void updateSnapshot(SimulationSnapshot snapshot) {
        lblSangre.setText(String.valueOf(snapshot.sangreVecna()));
        lblColmena.setText(String.valueOf(
                snapshot.zonas().getOrDefault("COLMENA", ZoneData.empty()).ninos()
        ));

        callePrincipalPanel.update(snapshot.zonas().getOrDefault("CALLE_PRINCIPAL", ZoneData.empty()).idsNinos());
        sotanoPanel.update(snapshot.zonas().getOrDefault("SOTANO_BYERS", ZoneData.empty()).idsNinos());
        radioPanel.update(snapshot.zonas().getOrDefault("RADIO_WSQK", ZoneData.empty()).idsNinos(), snapshot.sangreVecna());

        portalBosque.update(snapshot.portales().getOrDefault("BOSQUE", PortalData.empty()));
        portalLaboratorio.update(snapshot.portales().getOrDefault("LABORATORIO", PortalData.empty()));
        portalCentro.update(snapshot.portales().getOrDefault("CENTRO_COMERCIAL", PortalData.empty()));
        portalAlcantarillado.update(snapshot.portales().getOrDefault("ALCANTARILLADO", PortalData.empty()));

        bosquePanel.update(snapshot.zonas().getOrDefault("BOSQUE", ZoneData.empty()));
        laboratorioPanel.update(snapshot.zonas().getOrDefault("LABORATORIO", ZoneData.empty()));
        centroPanel.update(snapshot.zonas().getOrDefault("CENTRO_COMERCIAL", ZoneData.empty()));
        alcantarilladoPanel.update(snapshot.zonas().getOrDefault("ALCANTARILLADO", ZoneData.empty()));
    }

    private static JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT);
        label.setFont(strangerFont(Font.BOLD, 30f));
        return label;
    }

    private static JLabel createSectionLabel(String text, boolean oneLine) {
        String html;
        if (oneLine) {
            html = "<html><div style='text-align:center; white-space:nowrap;'>" + text + "</div></html>";
        } else {
            html = "<html><div style='text-align:center;'>" + text.replace("\n", "<br>") + "</div></html>";
        }
        JLabel label = new JLabel(html);
        label.setForeground(TEXT);
        label.setFont(strangerFont(Font.BOLD, 20f));
        return label;
    }

    private static JLabel createCounterLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(TEXT);
        label.setFont(strangerFont(Font.BOLD, 28f));
        return label;
    }

    private static Font retroFont(int style, float size) {
        return new Font("Monospaced", style, Math.round(size));
    }

    private static Font strangerFont(int style, float size) {
        Font base = new Font("Serif", style, Math.round(size));
        Map<TextAttribute, Object> attributes = new HashMap<>(base.getAttributes());
        attributes.put(TextAttribute.TRACKING, 0.08);
        attributes.put(TextAttribute.WEIGHT, style == Font.BOLD
                ? TextAttribute.WEIGHT_BOLD
                : TextAttribute.WEIGHT_REGULAR);
        return base.deriveFont(attributes).deriveFont(size);
    }

    private static JPanel transparentPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        return panel;
    }

    private static JPanel transparentPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    public record ZoneData(int ninos, int demogorgons, List<String> idsNinos, List<String> idsDemogorgons) {
        static ZoneData empty() {
            return new ZoneData(0, 0, List.of(), List.of());
        }
    }

    public record PortalData(List<String> idsIda, List<String> idsVuelta, boolean ocupado, String cruzando) {
        static PortalData empty() {
            return new PortalData(List.of(), List.of(), false, "");
        }
    }

    public record SimulationSnapshot(
            String eventoActivo,
            String tiempoRestanteEvento,
            int sangreVecna,
            int totalNinosActivos,
            int totalDemogorgonsActivos,
            Map<String, ZoneData> zonas,
            Map<String, PortalData> portales,
            List<String> topDemogorgons,
            List<String> eventosRecientes
    ) {
        static SimulationSnapshot demo() {
            return new SimulationSnapshot(
                    "SIN EVENTO ACTIVO",
                    "00:00",
                    35,
                    1500,
                    2,
                    Map.of(
                            "CALLE_PRINCIPAL", new ZoneData(0, 0, List.of(), List.of()),
                            "SOTANO_BYERS", new ZoneData(0, 0, List.of(), List.of()),
                            "RADIO_WSQK", new ZoneData(0, 0, List.of(), List.of()),
                            "BOSQUE", new ZoneData(0, 0, List.of(), List.of()),
                            "LABORATORIO", new ZoneData(0, 0, List.of(), List.of()),
                            "CENTRO_COMERCIAL", new ZoneData(0, 0, List.of(), List.of()),
                            "ALCANTARILLADO", new ZoneData(0, 0, List.of(), List.of()),
                            "COLMENA", new ZoneData(7, 0, List.of(), List.of())
                    ),
                    Map.of(
                            "BOSQUE", new PortalData(List.of(), List.of(), false, ""),
                            "LABORATORIO", new PortalData(List.of(), List.of(), false, ""),
                            "CENTRO_COMERCIAL", new PortalData(List.of(), List.of(), false, ""),
                            "ALCANTARILLADO", new PortalData(List.of(), List.of(), false, "")
                    ),
                    List.of(),
                    List.of()
            );
        }
    }

    private static class MonitorPanel extends JPanel {
        MonitorPanel() {
            setBackground(BG);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gp = new GradientPaint(0, 0, new Color(6, 25, 15), getWidth(), getHeight(), new Color(3, 12, 8));
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(new Color(40, 120, 70, 25));
            for (int y = 0; y < getHeight(); y += 4) {
                g2.drawLine(0, y, getWidth(), y);
            }

            g2.setColor(new Color(90, 255, 140, 20));
            g2.fillRoundRect(8, 8, getWidth() - 16, getHeight() - 16, 24, 24);
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
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(LINE_SOFT);
            g2.drawRoundRect(x + 1, y + 1, width - 3, height - 3, 12, 12);
            g2.setColor(new Color(120, 255, 170, 70));
            g2.drawRoundRect(x + 3, y + 3, width - 7, height - 7, 10, 10);
            g2.dispose();
        }
    }

    private static class RetroListPanel extends JPanel {
        private final JLabel title;
        private final JTextArea textArea;

        RetroListPanel(String titleText) {
            setOpaque(false);
            setLayout(new BorderLayout(8, 8));
            setBorder(new NeonBorder());

            title = createSectionLabel(titleText, true);
            title.setHorizontalAlignment(SwingConstants.CENTER);

            textArea = new JTextArea(6, 16);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setBackground(PANEL);
            textArea.setForeground(TEXT);
            textArea.setFont(strangerFont(Font.PLAIN, 16f));
            textArea.setBorder(new NeonBorder());

            add(title, BorderLayout.NORTH);
            add(textArea, BorderLayout.CENTER);
        }

        void update(List<String> ids) {
            textArea.setText(ids.isEmpty() ? "" : String.join(", ", ids));
        }
    }

    private static class RetroRadioPanel extends JPanel {
        private final JLabel title;
        private final JTextArea peopleArea;
        private final JLabel sangreValue;

        RetroRadioPanel(String titleText, JLabel sangreValue) {
            this.sangreValue = sangreValue;
            setOpaque(false);
            setLayout(new BorderLayout(8, 8));
            setBorder(new NeonBorder());

            title = createSectionLabel(titleText, true);
            title.setHorizontalAlignment(SwingConstants.CENTER);

            peopleArea = new JTextArea(4, 12);
            peopleArea.setEditable(false);
            peopleArea.setLineWrap(true);
            peopleArea.setWrapStyleWord(true);
            peopleArea.setBackground(PANEL);
            peopleArea.setForeground(TEXT);
            peopleArea.setFont(strangerFont(Font.PLAIN, 16f));
            peopleArea.setBorder(new NeonBorder());

            JPanel bottom = transparentPanel(new BorderLayout(8, 0));
            bottom.add(peopleArea, BorderLayout.CENTER);

            JPanel sangreBox = transparentPanel(new BorderLayout());
            JLabel titleSangre = createSectionLabel("SANGRE", true);
            titleSangre.setHorizontalAlignment(SwingConstants.CENTER);

            JPanel valueBox = new JPanel(new BorderLayout());
            valueBox.setBackground(ALERT_BG);
            valueBox.setBorder(BorderFactory.createLineBorder(ALERT_LINE, 2));
            valueBox.add(this.sangreValue, BorderLayout.CENTER);

            sangreBox.add(titleSangre, BorderLayout.NORTH);
            sangreBox.add(valueBox, BorderLayout.CENTER);
            sangreBox.setPreferredSize(new Dimension(86, 96));

            bottom.add(sangreBox, BorderLayout.EAST);

            add(title, BorderLayout.NORTH);
            add(bottom, BorderLayout.CENTER);
        }

        void update(List<String> ids, int sangre) {
            peopleArea.setText(ids.isEmpty() ? "" : String.join(", ", ids));
            sangreValue.setText(String.valueOf(sangre));
        }
    }

    private static class PortalPanel extends JPanel {
        private final JTextArea waitingArea = new JTextArea();
        private final JLabel crossingLabel = new JLabel("", SwingConstants.CENTER);
        private final JTextArea returnArea = new JTextArea();

        PortalPanel() {
            setOpaque(false);
            setLayout(new GridBagLayout());
            setBorder(new NeonBorder());
            setPreferredSize(new Dimension(300, 125));

            configureArea(waitingArea);
            configureArea(returnArea);

            JPanel leftBox = transparentPanel(new BorderLayout());
            leftBox.setBorder(new NeonBorder());
            leftBox.setPreferredSize(new Dimension(84, 110));
            leftBox.add(waitingArea, BorderLayout.CENTER);

            JPanel centerBox = transparentPanel(new BorderLayout());
            centerBox.setBorder(new NeonBorder());
            centerBox.setPreferredSize(new Dimension(72, 42));
            crossingLabel.setForeground(TEXT);
            crossingLabel.setFont(strangerFont(Font.BOLD, 13f));
            centerBox.add(crossingLabel, BorderLayout.CENTER);

            JPanel rightBox = transparentPanel(new BorderLayout());
            rightBox.setBorder(new NeonBorder());
            rightBox.setPreferredSize(new Dimension(84, 110));
            rightBox.add(returnArea, BorderLayout.CENTER);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridy = 0;
            gbc.insets = new Insets(0, 0, 0, 8);
            gbc.gridx = 0;
            add(leftBox, gbc);

            gbc.gridx = 1;
            add(centerBox, gbc);

            gbc.gridx = 2;
            gbc.insets = new Insets(0, 0, 0, 0);
            add(rightBox, gbc);
        }

        private void configureArea(JTextArea area) {
            area.setEditable(false);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setBackground(PANEL);
            area.setForeground(TEXT);
            area.setFont(strangerFont(Font.PLAIN, 14f));
            area.setMargin(new Insets(6, 6, 6, 6));
        }

        void update(PortalData data) {
            if (data.idsIda().isEmpty()) {
                waitingArea.setText("");
            } else {
                waitingArea.setText(String.join("\n", data.idsIda()));
            }

            if (data.idsVuelta().isEmpty()) {
                returnArea.setText("");
            } else {
                returnArea.setText(String.join("\n", data.idsVuelta()));
            }

            crossingLabel.setText(data.ocupado() ? data.cruzando() : "");
        }
    }

    private static class ZoneDangerPanel extends JPanel {
        private final JLabel title;
        private final JTextArea kidsArea;
        private final JTextArea demoArea;

        ZoneDangerPanel(String titleText) {
            setOpaque(false);
            setLayout(new BorderLayout(8, 8));
            setBorder(new NeonBorder());

            title = createSectionLabel(titleText, true);
            title.setHorizontalAlignment(SwingConstants.CENTER);

            JPanel content = transparentPanel(new GridLayout(1, 2, 10, 0));
            kidsArea = createMiniArea();
            demoArea = createMiniArea();
            content.add(kidsArea);
            content.add(demoArea);

            add(title, BorderLayout.NORTH);
            add(content, BorderLayout.CENTER);
        }

        private JTextArea createMiniArea() {
            JTextArea area = new JTextArea(3, 8);
            area.setEditable(false);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setBackground(PANEL_DARK);
            area.setForeground(TEXT);
            area.setFont(strangerFont(Font.PLAIN, 16f));
            area.setBorder(new NeonBorder());
            return area;
        }

        void update(ZoneData data) {
            kidsArea.setText(String.join(", ", data.idsNinos()));
            demoArea.setText(String.join(", ", data.idsDemogorgons()));
        }
    }
}