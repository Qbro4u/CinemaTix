package cinematix.view;

import cinematix.model.Booking;
import cinematix.model.Customer;
import cinematix.controller.PaymentController;
import cinematix.controller.BookingController;
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
    private BookingController bookingController;
    private PaymentController paymentController;

    public BookingHistoryPanel(Customer customer) {
        this.customer = customer;
        this.bookingController = new BookingController();
        this.paymentController = new PaymentController();
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

        // ============================================================
        // TABEL DENGAN KOLOM BARU: Metode Pembayaran
        // ============================================================
        String[] columns = {"Kode Booking", "Film", "Studio", "Tanggal Tayang", "Jumlah Tiket", "Total Harga", "Metode Bayar", "Status", "Waktu Booking", "Aksi"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setRowHeight(30);
        historyTable.getTableHeader().setReorderingAllowed(false);

        // Set column widths
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(150);  // Kode Booking
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(180);  // Film
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(70);   // Studio
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(150);  // Tanggal Tayang
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(80);   // Jumlah Tiket
        historyTable.getColumnModel().getColumn(5).setPreferredWidth(100);  // Total Harga
        historyTable.getColumnModel().getColumn(6).setPreferredWidth(100);  // Metode Bayar (BARU)
        historyTable.getColumnModel().getColumn(7).setPreferredWidth(120);  // Status
        historyTable.getColumnModel().getColumn(8).setPreferredWidth(150);  // Waktu Booking
        historyTable.getColumnModel().getColumn(9).setPreferredWidth(120);  // Aksi

        // Mouse Listener untuk klik tombol bayar
        historyTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = historyTable.rowAtPoint(e.getPoint());
                int col = historyTable.columnAtPoint(e.getPoint());

                if (col == 9) { // Kolom Aksi (sekarang index 9)
                    String action = (String) tableModel.getValueAt(row, 9);
                    if (action.equals("💳 Bayar Sekarang")) {
                        String bookingCode = (String) tableModel.getValueAt(row, 0);
                        List<Booking> bookings = Booking.getBookingsByCustomer(customer.getId());
                        for (Booking booking : bookings) {
                            if (booking.getBookingCode().equals(bookingCode)) {
                                payNow(booking);
                                break;
                            }
                        }
                    }
                }
            }
        });

        // Custom cell renderer untuk status
        historyTable.setDefaultRenderer(Object.class, new StatusCellRenderer());

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
        add(filterPanel, BorderLayout.NORTH);
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

            String statusText = getStatusText(booking.getStatus());
            String actionText = getActionButtonText(booking.getStatus());

            // ============================================================
            // TAMPILKAN METODE PEMBAYARAN
            // ============================================================
            String paymentMethod = getPaymentMethodText(booking);

            tableModel.addRow(new Object[]{
                    booking.getBookingCode(),
                    booking.getSchedule() != null ? booking.getSchedule().getMovie().getTitle() : "-",
                    booking.getSchedule() != null ? booking.getSchedule().getStudio() : "-",
                    booking.getSchedule() != null ? booking.getSchedule().getFormattedShowTime() : "-",
                    booking.getSelectedSeats().size(),
                    "Rp " + String.format("%,.0f", booking.getTotalPrice()),
                    paymentMethod,  // KOLOM METODE BAYAR (BARU)
                    statusText,
                    booking.getFormattedCreatedAt(),
                    actionText
            });
        }
    }

    // ============================================================
    // METHOD BARU: Mendapatkan teks metode pembayaran
    // ============================================================
    private String getPaymentMethodText(Booking booking) {
        if (booking.getStatus().equals("PENDING")) {
            return "⏳ Belum dibayar";
        } else if (booking.getStatus().equals("CONFIRMED")) {
            String method = booking.getPaymentMethod();
            if (method == null || method.isEmpty()) {
                return "✓ Lunas (Tunai)";
            }
            switch (method) {
                case "Cash":
                    return "💵 Tunai";
                case "Transfer Bank":
                    return "🏦 Transfer Bank";
                case "QRIS":
                    return "📱 QRIS";
                case "E-Wallet":
                    return "📱 E-Wallet";
                default:
                    return "✓ Lunas (" + method + ")";
            }
        } else if (booking.getStatus().equals("CANCELLED")) {
            return "✗ Dibatalkan";
        } else if (booking.getStatus().equals("EXPIRED")) {
            return "⌛ Kadaluarsa";
        }
        return "-";
    }

    private String getActionButtonText(String status) {
        switch (status) {
            case "PENDING":
                return "💳 Bayar Sekarang";
            case "CONFIRMED":
                return "✓ Lunas";
            case "CANCELLED":
                return "✗ Dibatalkan";
            case "EXPIRED":
                return "⌛ Kadaluarsa";
            default:
                return "-";
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

    private void payNow(Booking booking) {
        if (booking == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Selesaikan pembayaran untuk booking:\n" +
                        "Kode Booking: " + booking.getBookingCode() + "\n" +
                        "Film: " + booking.getSchedule().getMovie().getTitle() + "\n" +
                        "Jumlah Tiket: " + booking.getSelectedSeats().size() + "\n" +
                        "Total: Rp " + String.format("%,.0f", booking.getTotalPrice()) + "\n\n" +
                        "Lanjutkan ke pembayaran?",
                "Konfirmasi Pembayaran",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        JDialog paymentDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Pembayaran Tiket", true);
        paymentDialog.setSize(500, 550);
        paymentDialog.setLocationRelativeTo(this);

        PaymentPanel paymentPanel = new PaymentPanel(booking, paymentController,
                () -> {
                    paymentDialog.dispose();
                    JOptionPane.showMessageDialog(this,
                            "Pembayaran berhasil!\nKode Booking: " + booking.getBookingCode(),
                            "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    loadHistory();
                },
                () -> {
                    paymentDialog.dispose();
                });

        paymentDialog.add(paymentPanel);
        paymentDialog.setVisible(true);
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
        String statusText = (String) tableModel.getValueAt(selectedRow, 7); // Index status berubah jadi 7

        if (!statusText.contains("Terkonfirmasi")) {
            JOptionPane.showMessageDialog(this,
                    "Hanya tiket dengan status TERKONFIRMASI yang dapat dicetak!\n" +
                            "Silakan selesaikan pembayaran terlebih dahulu.",
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

    // ============================================================
    // CUSTOM CELL RENDERER (DIPERBAIKI UNTUK KOLOM BARU)
    // ============================================================
    class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                // Kolom Status (index 7)
                if (column == 7) {
                    String status = (String) value;
                    if (status.contains("Terkonfirmasi")) {
                        c.setForeground(new Color(0, 150, 0));
                    } else if (status.contains("Menunggu")) {
                        c.setForeground(new Color(255, 140, 0));
                    } else if (status.contains("Dibatalkan")) {
                        c.setForeground(Color.RED);
                    } else if (status.contains("Kadaluarsa")) {
                        c.setForeground(Color.GRAY);
                    }
                }
                // Kolom Metode Bayar (index 6)
                else if (column == 6) {
                    String method = (String) value;
                    if (method.contains("Lunas")) {
                        c.setForeground(new Color(0, 150, 0));
                    } else if (method.contains("Belum dibayar")) {
                        c.setForeground(new Color(255, 140, 0));
                    } else if (method.contains("Dibatalkan") || method.contains("Kadaluarsa")) {
                        c.setForeground(Color.GRAY);
                    } else {
                        c.setForeground(new Color(70, 130, 200));
                    }
                }
                // Kolom Aksi (index 9)
                else if (column == 9) {
                    String action = (String) value;
                    if (action.equals("💳 Bayar Sekarang")) {
                        c.setForeground(new Color(70, 130, 200));
                        c.setFont(new Font("Arial", Font.BOLD, 11));
                    } else if (action.equals("✓ Lunas")) {
                        c.setForeground(new Color(0, 150, 0));
                    } else if (action.equals("✗ Dibatalkan") || action.equals("⌛ Kadaluarsa")) {
                        c.setForeground(Color.GRAY);
                    }
                } else {
                    c.setForeground(Color.BLACK);
                }
            }

            return c;
        }
    }
}