package cinematix.view;

import cinematix.model.Booking;
import cinematix.model.Customer;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

public class BookingHistoryPanel extends JPanel {
    private Customer customer;
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JButton btnViewDetail;
    private JButton btnRefresh;
    private JComboBox<String> statusFilter;

    public BookingHistoryPanel(Customer customer) {
        this.customer = customer;
        initComponents();
        loadHistory();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title Panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Riwayat Pemesanan Tiket");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(25, 25, 112));
        titlePanel.add(titleLabel);

        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter Status:"));

        statusFilter = new JComboBox<>(new String[]{"Semua", "CONFIRMED", "PENDING", "CANCELLED", "EXPIRED"});
        statusFilter.addActionListener(e -> loadHistory());
        filterPanel.add(statusFilter);

        btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadHistory());
        filterPanel.add(btnRefresh);

        // Table
        String[] columns = {"Kode Booking", "Film", "Studio", "Tanggal Tayang", "Jumlah Tiket", "Total Harga", "Status", "Waktu Booking"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setRowHeight(25);
        historyTable.getTableHeader().setReorderingAllowed(false);

        // Set column widths
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(70);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        historyTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        historyTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        historyTable.getColumnModel().getColumn(7).setPreferredWidth(150);

        JScrollPane scrollPane = new JScrollPane(historyTable);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout());

        btnViewDetail = new JButton("Lihat Detail Tiket");
        btnViewDetail.setBackground(new Color(70, 130, 200));
        btnViewDetail.setForeground(Color.WHITE);
        btnViewDetail.setFont(new Font("Arial", Font.BOLD, 12));
        btnViewDetail.setPreferredSize(new Dimension(150, 35));
        btnViewDetail.addActionListener(e -> viewDetail());

        JButton btnPrint = new JButton("Cetak Tiket");
        btnPrint.setBackground(new Color(100, 150, 100));
        btnPrint.setForeground(Color.WHITE);
        btnPrint.addActionListener(e -> printTicket());

        buttonPanel.add(btnViewDetail);
        buttonPanel.add(btnPrint);

        add(titlePanel, BorderLayout.NORTH);
        add(filterPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadHistory() {
        tableModel.setRowCount(0);

        List<Booking> bookings = Booking.getBookingsByCustomer(customer.getId());
        String selectedStatus = (String) statusFilter.getSelectedItem();

        for (Booking booking : bookings) {
            if (!selectedStatus.equals("Semua") && !booking.getStatus().equals(selectedStatus)) {
                continue;
            }

            tableModel.addRow(new Object[]{
                    booking.getBookingCode(),
                    booking.getSchedule() != null ? booking.getSchedule().getMovie().getTitle() : "-",
                    booking.getSchedule() != null ? booking.getSchedule().getStudio() : "-",
                    booking.getSchedule() != null ? booking.getSchedule().getFormattedShowTime() : "-",
                    booking.getSelectedSeats().size(),
                    "Rp " + String.format("%,.0f", booking.getTotalPrice()),
                    getStatusText(booking.getStatus()),
                    booking.getFormattedCreatedAt()
            });
        }
    }

    private String getStatusText(String status) {
        switch (status) {
            case "CONFIRMED": return "✓ Terkonfirmasi";
            case "PENDING": return "⏳ Menunggu Pembayaran";
            case "CANCELLED": return "✗ Dibatalkan";
            case "EXPIRED": return "⌛ Kadaluarsa";
            default: return status;
        }
    }

    private void viewDetail() {
        int selectedRow = historyTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih tiket terlebih dahulu!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String bookingCode = (String) tableModel.getValueAt(selectedRow, 0);
        List<Booking> bookings = Booking.getBookingsByCustomer(customer.getId());

        Booking selectedBooking = null;
        for (Booking booking : bookings) {
            if (booking.getBookingCode().equals(bookingCode)) {
                selectedBooking = booking;
                break;
            }
        }

        if (selectedBooking != null) {
            // PERBAIKAN: Cast ke JFrame
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            BookingHistoryDetailDialog dialog = new BookingHistoryDetailDialog(parentFrame, selectedBooking);
            dialog.setVisible(true);
        }
    }

    private void printTicket() {
        int selectedRow = historyTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih tiket terlebih dahulu!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String bookingCode = (String) tableModel.getValueAt(selectedRow, 0);
        String status = (String) tableModel.getValueAt(selectedRow, 6);

        if (!status.contains("Terkonfirmasi")) {
            JOptionPane.showMessageDialog(this,
                    "Hanya tiket dengan status TERKONFIRMASI yang dapat dicetak!",
                    "Info", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Booking> bookings = Booking.getBookingsByCustomer(customer.getId());
        Booking selectedBooking = null;
        for (Booking booking : bookings) {
            if (booking.getBookingCode().equals(bookingCode)) {
                selectedBooking = booking;
                break;
            }
        }

        if (selectedBooking != null) {
            showPrintPreview(selectedBooking);
        }
    }

    private void showPrintPreview(Booking booking) {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        JDialog printDialog = new JDialog(parentFrame, "Cetak Tiket", true);
        printDialog.setSize(500, 600);
        printDialog.setLocationRelativeTo(this);

        BookingConfirmationPanel confirmationPanel = new BookingConfirmationPanel(booking,
                () -> printDialog.dispose());

        JButton btnPrint = new JButton("Cetak / Simpan sebagai Teks");
        btnPrint.addActionListener(e -> saveTicketToFile(booking));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnPrint);

        printDialog.add(confirmationPanel, BorderLayout.CENTER);
        printDialog.add(buttonPanel, BorderLayout.SOUTH);
        printDialog.setVisible(true);
    }

    private void saveTicketToFile(Booking booking) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File("tiket_" + booking.getBookingCode() + ".txt"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(fileChooser.getSelectedFile())) {
                writer.println("========================================");
                writer.println("           CINEMATIX TICKET             ");
                writer.println("========================================\n");
                writer.println("Kode Booking : " + booking.getBookingCode());
                writer.println("----------------------------------------");
                writer.println("Film         : " + booking.getSchedule().getMovie().getTitle());
                writer.println("Studio       : " + booking.getSchedule().getStudio());
                writer.println("Tanggal      : " + booking.getSchedule().getShowTime().toLocalDate());
                writer.println("Jam Tayang   : " + booking.getSchedule().getShowTime().toLocalTime());
                writer.println("----------------------------------------");
                writer.println("Kursi        : " + booking.getSeatsDisplay());
                writer.println("Jumlah       : " + booking.getSelectedSeats().size() + " tiket");
                writer.println("Total Bayar  : Rp " + String.format("%,.0f", booking.getTotalPrice()));
                writer.println("----------------------------------------");
                writer.println("Metode Bayar : " + (booking.getPaymentMethod() != null ? booking.getPaymentMethod() : "-"));
                writer.println("========================================");
                writer.println("   Terima kasih telah menggunakan       ");
                writer.println("            CinemaTix!                  ");
                writer.println("========================================");

                JOptionPane.showMessageDialog(this,
                        "Tiket berhasil disimpan di:\n" + fileChooser.getSelectedFile().getAbsolutePath(),
                        "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal menyimpan file: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}