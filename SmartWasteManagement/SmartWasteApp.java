import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SmartWasteApp extends JFrame {
    private final WasteManager manager;
    private DefaultTableModel tableModel;
    private JTable binTable;
    private JTextArea logArea;

    // KPI Labels
    private JLabel lblTotalBins;
    private JLabel lblCriticalBins;
    private JLabel lblAvgFill;
    private JLabel lblDispatches;

    // Auto-simulation timer
    private Timer autoSimTimer;
    private JButton btnAutoSim;

    public SmartWasteApp() {
        this.manager = new WasteManager();
        setTitle("Smart Waste Management System");
        setSize(950, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Top Header + KPI Dashboard
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Main Tabbed Workspace
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.addTab("  Live Bin Monitor  ", createMonitorPanel());
        tabbedPane.addTab("  Add Smart Bin  ", createAddBinPanel());
        tabbedPane.addTab("  Dispatch & System Logs  ", createLogsPanel());
        add(tabbedPane, BorderLayout.CENTER);

        // Auto-sim timer (polls every 3 seconds when active)
        autoSimTimer = new Timer(3000, e -> {
            manager.simulateSensors();
            refreshUI();
        });

        refreshUI();
    }

    private JPanel createHeaderPanel() {
        JPanel headerWrapper = new JPanel(new BorderLayout(10, 10));
        headerWrapper.setBackground(new Color(24, 34, 48));
        headerWrapper.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("Smart Waste Management System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Real-Time IoT Bin Monitoring & Route Optimization");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(180, 195, 210));

        JPanel titleBox = new JPanel(new GridLayout(2, 1));
        titleBox.setOpaque(false);
        titleBox.add(title);
        titleBox.add(subtitle);
        headerWrapper.add(titleBox, BorderLayout.NORTH);

        // KPI Cards Grid
        JPanel kpiGrid = new JPanel(new GridLayout(1, 4, 12, 0));
        kpiGrid.setOpaque(false);
        kpiGrid.setBorder(new EmptyBorder(12, 0, 0, 0));

        lblTotalBins = createKpiValueLabel();
        lblCriticalBins = createKpiValueLabel();
        lblAvgFill = createKpiValueLabel();
        lblDispatches = createKpiValueLabel();

        kpiGrid.add(createKpiCard("Total Smart Bins", lblTotalBins, new Color(41, 128, 185)));
        kpiGrid.add(createKpiCard("Critical Alerts (>=80%)", lblCriticalBins, new Color(192, 57, 43)));
        kpiGrid.add(createKpiCard("Average Fill Level", lblAvgFill, new Color(211, 84, 0)));
        kpiGrid.add(createKpiCard("Truck Dispatches", lblDispatches, new Color(39, 174, 96)));

        headerWrapper.add(kpiGrid, BorderLayout.CENTER);
        return headerWrapper;
    }

    private JLabel createKpiValueLabel() {
        JLabel label = new JLabel("0");
        label.setFont(new Font("Segoe UI", Font.BOLD, 22));
        label.setForeground(Color.WHITE);
        return label;
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(accentColor);
        card.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLbl.setForeground(new Color(240, 240, 240));

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createMonitorPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 15, 15, 15));

        String[] columns = {"Bin ID", "Location", "Waste Type", "Fill Level", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        binTable = new JTable(tableModel);
        binTable.setRowHeight(32);
        binTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        binTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        binTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Custom renderers for progress bar and status badge
        binTable.getColumnModel().getColumn(3).setCellRenderer(new ProgressBarRenderer());
        binTable.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());

        panel.add(new JScrollPane(binTable), BorderLayout.CENTER);

        // Action Controls Bar
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        JButton btnSimulate = createStyledButton("Simulate IoT Poll", new Color(52, 152, 219));
        btnSimulate.addActionListener(e -> {
            manager.simulateSensors();
            refreshUI();
        });

        btnAutoSim = createStyledButton("Start Auto-Sim (3s)", new Color(142, 68, 173));
        btnAutoSim.addActionListener(e -> toggleAutoSim());

        JButton btnDispatchCritical = createStyledButton("Dispatch Truck (Critical)", new Color(231, 76, 60));
        btnDispatchCritical.addActionListener(e -> {
            int count = manager.dispatchCriticalTrucks();
            refreshUI();
            if (count == 0) {
                JOptionPane.showMessageDialog(this, "No critical bins (>=80%) require collection right now.");
            } else {
                JOptionPane.showMessageDialog(this, "Truck dispatched! Emptied " + count + " critical bin(s) in priority order.");
            }
        });

        JButton btnEmptySelected = createStyledButton("Empty Selected Bin", new Color(46, 204, 113));
        btnEmptySelected.addActionListener(e -> {
            int row = binTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a bin from the table first.");
                return;
            }
            String binId = (String) tableModel.getValueAt(row, 0);
            manager.emptyBin(binId);
            refreshUI();
        });

        JButton btnDelete = createStyledButton("Delete Bin", new Color(127, 140, 141));
        btnDelete.addActionListener(e -> {
            int row = binTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a bin to delete.");
                return;
            }
            String binId = (String) tableModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Remove bin " + binId + "?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                manager.removeBin(binId);
                refreshUI();
            }
        });

        controls.add(btnSimulate);
        controls.add(btnAutoSim);
        controls.add(btnDispatchCritical);
        controls.add(btnEmptySelected);
        controls.add(btnDelete);

        panel.add(controls, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createAddBinPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        JPanel form = new JPanel(new GridLayout(5, 2, 12, 15));
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Register New Smart Waste Bin"),
            new EmptyBorder(20, 25, 20, 25)
        ));
        form.setPreferredSize(new Dimension(460, 270));

        JTextField txtBinId = new JTextField("BIN-107");
        JTextField txtLocation = new JTextField();
        JComboBox<String> cmbType = new JComboBox<>(new String[]{"General", "Organic", "Plastic", "Paper", "E-Waste", "Metal"});
        JSpinner spnFill = new JSpinner(new SpinnerNumberModel(0, 0, 100, 5));

        form.add(new JLabel("Bin ID (Unique):"));
        form.add(txtBinId);
        form.add(new JLabel("Location / Zone:"));
        form.add(txtLocation);
        form.add(new JLabel("Waste Category:"));
        form.add(cmbType);
        form.add(new JLabel("Initial Fill Level (%):"));
        form.add(spnFill);

        JButton btnAdd = createStyledButton("Register Smart Bin", new Color(39, 174, 96));
        btnAdd.addActionListener(e -> {
            String id = txtBinId.getText().trim();
            String loc = txtLocation.getText().trim();
            String type = (String) cmbType.getSelectedItem();
            int fill = (int) spnFill.getValue();

            if (id.isEmpty() || loc.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Bin ID and Location cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean added = manager.addBin(new SmartBin(id, loc, type, fill));
            if (!added) {
                JOptionPane.showMessageDialog(this, "A bin with ID '" + id + "' already exists.", "Duplicate ID", JOptionPane.ERROR_MESSAGE);
                return;
            }

            txtLocation.setText("");
            spnFill.setValue(0);
            refreshUI();
            JOptionPane.showMessageDialog(this, "Smart Bin [" + id + "] registered successfully!");
        });

        form.add(new JLabel(""));
        form.add(btnAdd);

        wrapper.add(form);
        return wrapper;
    }

    private JPanel createLogsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        logArea.setBackground(new Color(248, 249, 250));

        JButton btnClear = createStyledButton("Clear Logs", new Color(127, 140, 141));
        btnClear.addActionListener(e -> {
            manager.clearLogs();
            refreshUI();
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnClear);

        panel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void toggleAutoSim() {
        if (autoSimTimer.isRunning()) {
            autoSimTimer.stop();
            btnAutoSim.setText("Start Auto-Sim (3s)");
            btnAutoSim.setBackground(new Color(142, 68, 173));
        } else {
            autoSimTimer.start();
            btnAutoSim.setText("Stop Auto-Sim");
            btnAutoSim.setBackground(new Color(192, 57, 43));
        }
    }

    private void refreshUI() {
        // Refresh Table
        tableModel.setRowCount(0);
        List<SmartBin> bins = manager.getBins();
        for (SmartBin bin : bins) {
            tableModel.addRow(new Object[]{
                bin.getBinId(),
                bin.getLocation(),
                bin.getWasteType(),
                bin.getFillLevel(),
                bin.getStatus()
            });
        }

        // Refresh KPI Cards
        lblTotalBins.setText(String.valueOf(bins.size()));
        lblCriticalBins.setText(String.valueOf(manager.getCriticalCount()));
        lblAvgFill.setText(String.format("%.1f%%", manager.getAverageFillLevel()));
        lblDispatches.setText(String.valueOf(manager.getDispatchCount()));

        // Refresh Logs
        StringBuilder sb = new StringBuilder();
        for (String log : manager.getLogs()) {
            sb.append(log).append("\n");
        }
        logArea.setText(sb.toString());
        logArea.setCaretPosition(0);
    }

    // Custom Renderer: Color-coded Progress Bar for Fill Level
    static class ProgressBarRenderer extends JProgressBar implements javax.swing.table.TableCellRenderer {
        public ProgressBarRenderer() {
            super(0, 100);
            setStringPainted(true);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            int fill = (int) value;
            setValue(fill);
            setString(fill + "%");
            if (fill >= 80) {
                setForeground(new Color(231, 76, 60)); // Red
            } else if (fill >= 50) {
                setForeground(new Color(243, 156, 18)); // Orange
            } else {
                setForeground(new Color(46, 204, 113)); // Green
            }
            return this;
        }
    }

    // Custom Renderer: Colored Status Text
    static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = (String) value;
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 12));

            if (!isSelected) {
                switch (status) {
                    case "CRITICAL" -> setForeground(new Color(192, 57, 43));
                    case "WARNING"  -> setForeground(new Color(211, 84, 0));
                    default         -> setForeground(new Color(39, 174, 96));
                }
            }
            return c;
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            SmartWasteApp app = new SmartWasteApp();
            app.setVisible(true);
        });
    }
}
