package cinematix.view;

import cinematix.model.Booking;
import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class BookingConfirmationPanel extends JPanel {
    private Booking booking;
    private JButton btnDone;
    private Runnable onDone;

    public BookingConfirmationPanel(Booking booking, Runnable onDone) {
        this.booking = booking;
        this.onDone = onDone;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JLabel headerLabel = new JLabel("TIKET BIOSKOP", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setForeground(new Color(25, 25, 112));

        JLabel subHeaderLabel = new JLabel("CinemaTix", SwingConstants.CENTER);
        subHeaderLabel.setFont(new Font("Arial", Font.ITALIC, 14));

        // Ticket Panel
        JTextArea ticketArea = new JTextArea();
        ticketArea.setEditable(false);
        ticketArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ticketArea.setBackground(Color.WHITE);
        ticketArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("           CINEMATIX TICKET             \n");
        sb.append("========================================\n\n");
        sb.append("Kode Booking : ").append(booking.getBookingCode()).append("\n");
        sb.append("----------------------------------------\n");
        sb.append("Film         : ").append(booking.getSchedule().getMovie().getTitle()).append("\n");
        sb.append("Studio       : ").append(booking.getSchedule().getStudio()).append("\n");
        sb.append("Tanggal      : ").append(booking.getSchedule().getShowTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
        sb.append("Jam Tayang   : ").append(booking.getSchedule().getShowTime().format(DateTimeFormatter.ofPattern("HH:mm"))).append("\n");
        sb.append("----------------------------------------\n");
        sb.append("Kursi        : ").append(booking.getSeatsDisplay()).append("\n");
        sb.append("Jumlah       : ").append(booking.getSelectedSeats().size()).append(" tiket\n");
        sb.append("Total Bayar  : Rp ").append(String.format("%,.0f", booking.getTotalPrice())).append("\n");
        sb.append("----------------------------------------\n");

        // PERBAIKAN: Tampilkan metode pembayaran dengan pengecekan null
        sb.append("Metode Bayar : ");
        if (booking.getPaymentMethod() != null && !booking.getPaymentMethod().isEmpty()) {
            sb.append(booking.getPaymentMethod()).append("\n");
        } else {
            sb.append("- (Belum dibayar)\n");
        }

        // PERBAIKAN: Tampilkan waktu pembayaran dengan pengecekan null
        sb.append("Waktu Bayar  : ");
        if (booking.getPaymentTime() != null) {
            sb.append(booking.getPaymentTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
        } else {
            sb.append("- (Belum dibayar)\n");
        }

        sb.append("========================================\n");
        sb.append("   Terima kasih telah menggunakan       \n");
        sb.append("            CinemaTix!                  \n");
        sb.append("========================================\n");

        ticketArea.setText(sb.toString());
        JScrollPane scrollPane = new JScrollPane(ticketArea);
        scrollPane.setPreferredSize(new Dimension(400, 450));

        // Button Panel
        JPanel buttonPanel = new JPanel();
        JButton btnDone = new JButton("Selesai");
        btnDone.setBackground(new Color(70, 130, 200));
        btnDone.setForeground(Color.WHITE);
        btnDone.setFont(new Font("Arial", Font.BOLD, 14));
        btnDone.setPreferredSize(new Dimension(150, 40));
        btnDone.addActionListener(e -> {
            if (onDone != null) onDone.run();
        });
        buttonPanel.add(btnDone);

        JPanel centerPanel = new JPanel();
        centerPanel.add(scrollPane);

        add(headerLabel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}