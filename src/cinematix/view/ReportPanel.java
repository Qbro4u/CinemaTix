package cinematix.view;

import cinematix.model.Booking;
import cinematix.model.Movie;
import cinematix.model.Schedule;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

public class ReportPanel extends JPanel {
    private JFrame parent;
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> reportTypeCombo;
    private JLabel totalLabel;

    public ReportPanel(JFrame parent) {
        this.parent = parent;
        initComponents();
        loadReport();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel titleLabel = new JLabel("Laporan Penjualan");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

        // Control Panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(new JLabel("Jenis Laporan:"));

        reportTypeCombo = new JComboBox<>(new String[]{
                "Laporan Penjualan Harian",
                "Laporan Penjualan Bulanan",
                "Laporan Per Film",
                "Laporan Per Customer"
        });
        reportTypeCombo.addActionListener(e -> loadReport());
        controlPanel.add(reportTypeCombo);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadReport());
        controlPanel.add(btnRefresh);

        JButton btnExport = new JButton("Export ke CSV");
        btnExport.addActionListener(e -> exportToCSV());
        controlPanel.add(btnExport);

        // Table
        String[] columns = {"Tanggal", "Kode Booking", "Film", "Jumlah Tiket", "Total Harga", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        reportTable = new JTable(tableModel);
        reportTable.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(reportTable);

        // Total Panel
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalLabel = new JLabel("Total Pendapatan: Rp 0");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalLabel.setForeground(new Color(70, 130, 200));
        totalPanel.add(totalLabel);

        // Layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(controlPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(totalPanel, BorderLayout.SOUTH);
    }

    private void loadReport() {
        tableModel.setRowCount(0);
        String reportType = (String) reportTypeCombo.getSelectedItem();

        if (reportType == null) return;

        double totalRevenue = 0;

        if (reportType.equals("Laporan Penjualan Harian")) {
            totalRevenue = loadDailyReport();
        } else if (reportType.equals("Laporan Penjualan Bulanan")) {
            totalRevenue = loadMonthlyReport();
        } else if (reportType.equals("Laporan Per Film")) {
            totalRevenue = loadPerMovieReport();
        } else if (reportType.equals("Laporan Per Customer")) {
            totalRevenue = loadPerCustomerReport();
        }

        totalLabel.setText(String.format("Total Pendapatan: Rp %,.0f", totalRevenue));
    }

    private double loadDailyReport() {
        double total = 0;

        // Get all confirmed bookings
        List<Booking> allBookings = getAllConfirmedBookings();

        // Filter today's bookings
        LocalDate today = LocalDate.now();

        for (Booking booking : allBookings) {
            if (booking.getCreatedAt().toLocalDate().equals(today) &&
                    booking.getStatus().equals("CONFIRMED")) {

                tableModel.addRow(new Object[]{
                        booking.getFormattedCreatedAt(),
                        booking.getBookingCode(),
                        booking.getSchedule() != null ? booking.getSchedule().getMovie().getTitle() : "-",
                        booking.getSelectedSeats().size(),
                        "Rp " + String.format("%,.0f", booking.getTotalPrice()),
                        "Terkonfirmasi"
                });
                total += booking.getTotalPrice();
            }
        }

        if (tableModel.getRowCount() == 0) {
            tableModel.addRow(new Object[]{"Tidak ada data", "-", "-", "-", "-", "-"});
        }

        return total;
    }

    private double loadMonthlyReport() {
        double total = 0;

        List<Booking> allBookings = getAllConfirmedBookings();
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        for (Booking booking : allBookings) {
            if (booking.getCreatedAt().getMonthValue() == currentMonth &&
                    booking.getCreatedAt().getYear() == currentYear &&
                    booking.getStatus().equals("CONFIRMED")) {

                tableModel.addRow(new Object[]{
                        booking.getFormattedCreatedAt(),
                        booking.getBookingCode(),
                        booking.getSchedule() != null ? booking.getSchedule().getMovie().getTitle() : "-",
                        booking.getSelectedSeats().size(),
                        "Rp " + String.format("%,.0f", booking.getTotalPrice()),
                        "Terkonfirmasi"
                });
                total += booking.getTotalPrice();
            }
        }

        if (tableModel.getRowCount() == 0) {
            tableModel.addRow(new Object[]{"Tidak ada data", "-", "-", "-", "-", "-"});
        }

        return total;
    }

    private double loadPerMovieReport() {
        double total = 0;

        List<Booking> allBookings = getAllConfirmedBookings();

        // Group by movie
        java.util.Map<String, Double> movieRevenue = new java.util.HashMap<>();
        java.util.Map<String, Integer> movieTickets = new java.util.HashMap<>();

        for (Booking booking : allBookings) {
            if (booking.getStatus().equals("CONFIRMED") && booking.getSchedule() != null) {
                String movieTitle = booking.getSchedule().getMovie().getTitle();
                movieRevenue.put(movieTitle, movieRevenue.getOrDefault(movieTitle, 0.0) + booking.getTotalPrice());
                movieTickets.put(movieTitle, movieTickets.getOrDefault(movieTitle, 0) + booking.getSelectedSeats().size());
            }
        }

        for (String movie : movieRevenue.keySet()) {
            tableModel.addRow(new Object[]{
                    "-",
                    movie,
                    movie,
                    movieTickets.get(movie),
                    "Rp " + String.format("%,.0f", movieRevenue.get(movie)),
                    "Terkonfirmasi"
            });
            total += movieRevenue.get(movie);
        }

        if (tableModel.getRowCount() == 0) {
            tableModel.addRow(new Object[]{"Tidak ada data", "-", "-", "-", "-", "-"});
        }

        return total;
    }

    private double loadPerCustomerReport() {
        double total = 0;

        List<Booking> allBookings = getAllConfirmedBookings();

        // Group by customer
        java.util.Map<String, Double> customerRevenue = new java.util.HashMap<>();
        java.util.Map<String, Integer> customerTickets = new java.util.HashMap<>();

        for (Booking booking : allBookings) {
            if (booking.getStatus().equals("CONFIRMED")) {
                String customerName = "Customer ID: " + booking.getCustomerId();
                customerRevenue.put(customerName, customerRevenue.getOrDefault(customerName, 0.0) + booking.getTotalPrice());
                customerTickets.put(customerName, customerTickets.getOrDefault(customerName, 0) + booking.getSelectedSeats().size());
            }
        }

        for (String customer : customerRevenue.keySet()) {
            tableModel.addRow(new Object[]{
                    "-",
                    customer,
                    customer,
                    customerTickets.get(customer),
                    "Rp " + String.format("%,.0f", customerRevenue.get(customer)),
                    "Terkonfirmasi"
            });
            total += customerRevenue.get(customer);
        }

        if (tableModel.getRowCount() == 0) {
            tableModel.addRow(new Object[]{"Tidak ada data", "-", "-", "-", "-", "-"});
        }

        return total;
    }

    private List<Booking> getAllConfirmedBookings() {
        List<Booking> allBookings = new ArrayList<>();

        // Get all customers and their bookings
        // This is simplified - in real implementation, you'd query directly from database
        try {
            java.sql.Statement stmt = cinematix.model.DatabaseConnection.getConnection().createStatement();
            java.sql.ResultSet rs = stmt.executeQuery(
                    "SELECT DISTINCT customer_id FROM bookings WHERE status = 'CONFIRMED'"
            );

            while (rs.next()) {
                int customerId = rs.getInt("customer_id");
                allBookings.addAll(Booking.getBookingsByCustomer(customerId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return allBookings;
    }

    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File("laporan_penjualan.csv"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(fileChooser.getSelectedFile()))) {
                // Write header
                for (int i = 0; i < reportTable.getColumnCount(); i++) {
                    writer.print(reportTable.getColumnName(i));
                    if (i < reportTable.getColumnCount() - 1) writer.print(",");
                }
                writer.println();

                // Write data
                for (int row = 0; row < reportTable.getRowCount(); row++) {
                    for (int col = 0; col < reportTable.getColumnCount(); col++) {
                        Object value = reportTable.getValueAt(row, col);
                        writer.print(value != null ? value.toString() : "");
                        if (col < reportTable.getColumnCount() - 1) writer.print(",");
                    }
                    writer.println();
                }

                writer.println();
                writer.print("Total Pendapatan," + totalLabel.getText());

                JOptionPane.showMessageDialog(this,
                        "Laporan berhasil diexport ke:\n" + fileChooser.getSelectedFile().getAbsolutePath(),
                        "Sukses", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Gagal export: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}