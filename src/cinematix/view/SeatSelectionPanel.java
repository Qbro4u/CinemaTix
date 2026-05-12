package cinematix.view;

import cinematix.controller.BookingController;
import cinematix.exception.SeatAlreadyBookedException;
import cinematix.model.Schedule;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SeatSelectionPanel extends JPanel {
    private Schedule schedule;
    private BookingController controller;
    private JPanel seatGridPanel;
    private List<JToggleButton> seatButtons;
    private List<Integer> selectedSeats;
    private JLabel totalLabel;
    private JButton btnConfirm;
    private JLabel screenLabel;

    private static final int ROWS = 8;
    private static final int COLS = 10;

    public SeatSelectionPanel(Schedule schedule, BookingController controller) {
        this.schedule = schedule;
        this.controller = controller;
        this.selectedSeats = new ArrayList<>();
        this.seatButtons = new ArrayList<>();
        initComponents();
        loadSeatLayout();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Info Panel
        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Informasi Studio"));
        infoPanel.add(new JLabel("Studio: " + schedule.getStudio()));
        infoPanel.add(new JLabel("Film: " + schedule.getMovie().getTitle()));
        infoPanel.add(new JLabel("Jam Tayang: " + schedule.getFormattedShowTime()));

        // Screen
        JPanel screenPanel = new JPanel(new BorderLayout());
        screenLabel = new JLabel("LAYAR", SwingConstants.CENTER);
        screenLabel.setFont(new Font("Arial", Font.BOLD, 14));
        screenLabel.setOpaque(true);
        screenLabel.setBackground(Color.LIGHT_GRAY);
        screenLabel.setPreferredSize(new Dimension(400, 30));
        screenPanel.add(screenLabel, BorderLayout.CENTER);

        // Seat Grid
        seatGridPanel = new JPanel(new GridLayout(ROWS, COLS, 5, 5));
        seatGridPanel.setBorder(BorderFactory.createTitledBorder("Pilih Kursi Anda"));

        // Legend Panel
        JPanel legendPanel = new JPanel(new FlowLayout());
        legendPanel.add(createLegend("Tersedia", Color.GREEN));
        legendPanel.add(createLegend("Terpilih", Color.BLUE));
        legendPanel.add(createLegend("Terbooking", Color.RED));
        legendPanel.add(createLegend("Kosong", Color.LIGHT_GRAY));

        // Bottom Panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        totalLabel = new JLabel("Total: Rp 0", SwingConstants.CENTER);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalLabel.setForeground(new Color(70, 130, 200));

        btnConfirm = new JButton("Lanjutkan ke Pembayaran");
        btnConfirm.setBackground(new Color(70, 130, 200));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFont(new Font("Arial", Font.BOLD, 14));
        btnConfirm.setEnabled(false);
        btnConfirm.addActionListener(e -> confirmSeats());

        bottomPanel.add(totalLabel, BorderLayout.CENTER);
        bottomPanel.add(btnConfirm, BorderLayout.SOUTH);

        add(infoPanel, BorderLayout.NORTH);
        add(screenPanel, BorderLayout.CENTER);
        add(seatGridPanel, BorderLayout.SOUTH);
        add(legendPanel, BorderLayout.WEST);
        add(bottomPanel, BorderLayout.EAST);
    }

    private JPanel createLegend(String text, Color color) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel colorLabel = new JLabel("     ");
        colorLabel.setBackground(color);
        colorLabel.setOpaque(true);
        colorLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        panel.add(colorLabel);
        panel.add(new JLabel(text));
        panel.setOpaque(false);
        return panel;
    }

    private void loadSeatLayout() {
        seatGridPanel.removeAll();
        seatButtons.clear();

        char rowChar = 'A';
        for (int row = 0; row < ROWS; row++) {
            for (int col = 1; col <= COLS; col++) {
                int seatNumber = (row * COLS) + col;
                String seatLabel = String.valueOf((char)(rowChar + row)) + col;
                JToggleButton seatButton = new JToggleButton(seatLabel);
                seatButton.setPreferredSize(new Dimension(50, 40));

                // Set warna berdasarkan status kursi
                if (schedule.isSeatBooked(seatNumber)) {
                    seatButton.setBackground(Color.RED);
                    seatButton.setEnabled(false);
                } else {
                    seatButton.setBackground(Color.GREEN);
                }

                seatButton.addActionListener(e -> onSeatClicked(seatButton, seatNumber));
                seatButton.putClientProperty("seatNumber", seatNumber);

                seatGridPanel.add(seatButton);
                seatButtons.add(seatButton);
            }
        }

        seatGridPanel.revalidate();
        seatGridPanel.repaint();
    }

    private void onSeatClicked(JToggleButton seatButton, int seatNumber) {
        if (seatButton.isSelected()) {
            // Pilih kursi
            if (!schedule.isSeatBooked(seatNumber)) {
                selectedSeats.add(seatNumber);
                seatButton.setBackground(Color.BLUE);
            } else {
                seatButton.setSelected(false);
                JOptionPane.showMessageDialog(this, "Kursi sudah dipesan!", "Info", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            // Batalkan pilihan
            selectedSeats.remove((Integer) seatNumber);
            seatButton.setBackground(Color.GREEN);
        }

        // Update total
        double total = selectedSeats.size() * schedule.getMovie().getTicketPrice();
        totalLabel.setText(String.format("Total: Rp %,.0f", total));
        btnConfirm.setEnabled(!selectedSeats.isEmpty());
    }

    private void confirmSeats() {
        if (selectedSeats.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih minimal 1 kursi!", "Info", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Anda memilih %d kursi dengan total Rp %,.0f\nLanjutkan ke pembayaran?",
                        selectedSeats.size(), selectedSeats.size() * schedule.getMovie().getTicketPrice()),
                "Konfirmasi Kursi", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Buat booking dan lanjut ke pembayaran
            controller.createBookingAndProceedToPayment(schedule, selectedSeats, this);
        }
    }

    public List<Integer> getSelectedSeats() {
        return selectedSeats;
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}