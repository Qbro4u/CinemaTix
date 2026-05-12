package cinematix.view;

import cinematix.model.Booking;
import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class BookingHistoryDetailDialog extends JDialog {
    private Booking booking;

    public BookingHistoryDetailDialog(JFrame parent, Booking booking) {
        super(parent, "Detail Tiket - " + booking.getBookingCode(), true);
        this.booking = booking;
        initComponents();
        setSize(500, 550);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel headerLabel = new JLabel("DETAIL TIKET BIOSKOP", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(new Color(25, 25, 112));
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel ticketCard = new JPanel();
        ticketCard.setLayout(new BoxLayout(ticketCard, BoxLayout.Y_AXIS));
        ticketCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        ticketCard.setBackground(Color.WHITE);

        addInfoRow(ticketCard, "Kode Booking", booking.getBookingCode());
        addInfoRow(ticketCard, "Status", formatStatus(booking.getStatus()));
        addSeparator(ticketCard);

        if (booking.getSchedule() != null) {
            addInfoRow(ticketCard, "Film", booking.getSchedule().getMovie().getTitle());
            addInfoRow(ticketCard, "Genre", booking.getSchedule().getMovie().getGenre());
            addInfoRow(ticketCard, "Durasi", booking.getSchedule().getMovie().getDuration() + " menit");
            addSeparator(ticketCard);

            addInfoRow(ticketCard, "Studio", booking.getSchedule().getStudio());
            addInfoRow(ticketCard, "Tanggal Tayang",
                    booking.getSchedule().getShowTime().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")));
            addInfoRow(ticketCard, "Jam Tayang",
                    booking.getSchedule().getShowTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            addSeparator(ticketCard);
        }

        addInfoRow(ticketCard, "Nomor Kursi", booking.getSeatsDisplay());
        addInfoRow(ticketCard, "Jumlah Tiket", String.valueOf(booking.getSelectedSeats().size()));
        addInfoRow(ticketCard, "Total Harga", "Rp " + String.format("%,.0f", booking.getTotalPrice()));

        if (booking.getPaymentMethod() != null && !booking.getPaymentMethod().isEmpty()) {
            addInfoRow(ticketCard, "Metode Pembayaran", booking.getPaymentMethod());
        } else {
            addInfoRow(ticketCard, "Metode Pembayaran", "-");
        }

        if (booking.getPaymentTime() != null) {
            addInfoRow(ticketCard, "Waktu Pembayaran",
                    booking.getPaymentTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
        } else {
            addInfoRow(ticketCard, "Waktu Pembayaran", "-");
        }

        addInfoRow(ticketCard, "Waktu Pemesanan", booking.getFormattedCreatedAt());

        JPanel qrPanel = new JPanel();
        qrPanel.setBorder(BorderFactory.createTitledBorder("QR Code Tiket"));
        JLabel qrLabel = new JLabel("📱 [QR CODE SIMULASI]");
        qrLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
        qrLabel.setForeground(Color.BLUE);
        qrPanel.add(qrLabel);

        JPanel buttonPanel = new JPanel();
        JButton btnClose = new JButton("Tutup");
        btnClose.addActionListener(e -> dispose());
        buttonPanel.add(btnClose);

        mainPanel.add(headerLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(ticketCard);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(qrPanel);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Di method initComponents(), cari bagian penambahan info row:


    }

    private void addInfoRow(JPanel panel, String label, String value) {
        JPanel rowPanel = new JPanel(new BorderLayout());
        rowPanel.setOpaque(false);

        JLabel labelLabel = new JLabel(label + ":");
        labelLabel.setFont(new Font("Arial", Font.BOLD, 12));
        labelLabel.setForeground(Color.GRAY);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        rowPanel.add(labelLabel, BorderLayout.WEST);
        rowPanel.add(valueLabel, BorderLayout.EAST);

        panel.add(rowPanel);
        panel.add(Box.createVerticalStrut(8));

    }

    private void addSeparator(JPanel panel) {
        JSeparator separator = new JSeparator();
        separator.setForeground(Color.LIGHT_GRAY);
        panel.add(separator);
        panel.add(Box.createVerticalStrut(8));
    }

    private String formatStatus(String status) {
        switch (status) {
            case "CONFIRMED": return "✓ TERKONFIRMASI (Lunas)";
            case "PENDING": return "⏳ MENUNGGU PEMBAYARAN";
            case "CANCELLED": return "✗ DIBATALKAN";
            case "EXPIRED": return "⌛ KADALUARSA";
            default: return status;
        }
    }
}