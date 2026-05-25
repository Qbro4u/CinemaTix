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
    private int totalSeats;

    public SeatSelectionPanel(Schedule schedule, BookingController controller) {
        this.schedule = schedule;
        this.controller = controller;
        this.selectedSeats = new ArrayList<>();
        this.seatButtons = new ArrayList<>();
        this.totalSeats = schedule.getTotalSeats();
        initComponents();
        loadSeatLayout();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Info Panel
        JPanel infoPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Informasi Studio"));
        infoPanel.add(new JLabel("Studio: " + schedule.getStudio()));
        infoPanel.add(new JLabel("Film: " + schedule.getMovie().getTitle()));
        infoPanel.add(new JLabel("Jam Tayang: " + schedule.getFormattedShowTime()));
        infoPanel.add(new JLabel("Total Kursi: " + totalSeats + " kursi"));
        infoPanel.add(new JLabel(" "));

        // Screen Panel
        JPanel screenPanel = new JPanel(new BorderLayout());
        JLabel screenLabel = new JLabel("LAYAR", SwingConstants.CENTER);
        screenLabel.setFont(new Font("Arial", Font.BOLD, 16));
        screenLabel.setOpaque(true);
        screenLabel.setBackground(Color.LIGHT_GRAY);
        screenLabel.setPreferredSize(new Dimension(800, 40));  // Diperbesar
        screenPanel.add(screenLabel, BorderLayout.CENTER);

        // Seat Grid Panel - dengan ScrollPane agar bisa scroll jika terlalu besar
        seatGridPanel = new JPanel();
        seatGridPanel.setBorder(BorderFactory.createTitledBorder("Pilih Kursi Anda"));

        JScrollPane scrollPane = new JScrollPane(seatGridPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(900, 500));  // Ukuran panel dengan scroll

        // Legend Panel
        JPanel legendPanel = new JPanel(new FlowLayout());
        legendPanel.add(createLegend("Tersedia", Color.GREEN));
        legendPanel.add(createLegend("Terpilih", Color.BLUE));
        legendPanel.add(createLegend("Terbooking", Color.RED));

        // Bottom Panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        totalLabel = new JLabel("Total: Rp 0", SwingConstants.CENTER);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        totalLabel.setForeground(new Color(70, 130, 200));

        btnConfirm = new JButton("Lanjutkan ke Pembayaran");
        btnConfirm.setBackground(new Color(70, 130, 200));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFont(new Font("Arial", Font.BOLD, 14));
        btnConfirm.setPreferredSize(new Dimension(200, 40));
        btnConfirm.setEnabled(false);
        btnConfirm.addActionListener(e -> confirmSeats());

        bottomPanel.add(totalLabel, BorderLayout.CENTER);
        bottomPanel.add(btnConfirm, BorderLayout.SOUTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(screenPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(infoPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(legendPanel, BorderLayout.WEST);
        add(bottomPanel, BorderLayout.SOUTH);
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

        // Tentukan jumlah baris (8 baris: A-H) dan kolom (10 kolom untuk 80 kursi)
        int rows = 8;
        int cols = 10;

        // Set layout dengan ukuran tombol yang lebih kecil agar muat
        seatGridPanel.setLayout(new GridLayout(rows, cols, 8, 8));

        String[] rowLetters = {"A", "B", "C", "D", "E", "F", "G", "H"};
        int seatNumber = 1;

        // Atur preferred size seatGridPanel agar cukup lebar
        int buttonWidth = 65;
        int buttonHeight = 45;
        seatGridPanel.setPreferredSize(new Dimension(cols * (buttonWidth + 8), rows * (buttonHeight + 8)));

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (seatNumber > totalSeats) {
                    // Kursi tidak ada
                    JPanel emptyPanel = new JPanel();
                    emptyPanel.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
                    emptyPanel.setBackground(Color.LIGHT_GRAY);
                    emptyPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                    seatGridPanel.add(emptyPanel);
                } else {
                    // Format label: A1, A2, ..., B1, B2, ...
                    String seatLabel = rowLetters[row] + (col + 1);

                    JToggleButton seatButton = new JToggleButton(seatLabel);
                    seatButton.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
                    seatButton.setFont(new Font("Arial", Font.BOLD, 11));

                    // Cek status kursi
                    if (schedule.isSeatBooked(seatNumber)) {
                        seatButton.setBackground(Color.RED);
                        seatButton.setEnabled(false);
                        seatButton.setSelected(false);
                    } else {
                        seatButton.setBackground(Color.GREEN);
                        seatButton.setEnabled(true);
                    }

                    final int currentSeatNumber = seatNumber;
                    final JToggleButton currentButton = seatButton;
                    final String currentLabel = seatLabel;

                    seatButton.addActionListener(e -> {
                        if (currentButton.isSelected()) {
                            if (!schedule.isSeatBooked(currentSeatNumber)) {
                                selectedSeats.add(currentSeatNumber);
                                currentButton.setBackground(Color.BLUE);
                                System.out.println("Kursi " + currentLabel + " dipilih");
                            } else {
                                currentButton.setSelected(false);
                                JOptionPane.showMessageDialog(SeatSelectionPanel.this,
                                        "Kursi " + currentLabel + " sudah dipesan!",
                                        "Info", JOptionPane.WARNING_MESSAGE);
                            }
                        } else {
                            selectedSeats.remove((Integer) currentSeatNumber);
                            currentButton.setBackground(Color.GREEN);
                            System.out.println("Kursi " + currentLabel + " dibatalkan");
                        }

                        double total = selectedSeats.size() * schedule.getMovie().getTicketPrice();
                        totalLabel.setText(String.format("Total: Rp %,.0f", total));
                        btnConfirm.setEnabled(!selectedSeats.isEmpty());
                    });

                    seatGridPanel.add(seatButton);
                    seatButtons.add(seatButton);
                }
                seatNumber++;
            }
        }

        seatGridPanel.revalidate();
        seatGridPanel.repaint();
    }

    private void confirmSeats() {
        if (selectedSeats.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih minimal 1 kursi!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder seatsDisplay = new StringBuilder();
        String[] rowLetters = {"A", "B", "C", "D", "E", "F", "G", "H"};

        for (int i = 0; i < selectedSeats.size(); i++) {
            int seat = selectedSeats.get(i);
            int rowIndex = (seat - 1) / 10;
            int colIndex = (seat - 1) % 10;
            String seatLabel = rowLetters[rowIndex] + (colIndex + 1);
            if (i > 0) seatsDisplay.append(", ");
            seatsDisplay.append(seatLabel);
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Anda memilih kursi: %s\nJumlah: %d kursi\nTotal: Rp %,.0f\n\nLanjutkan ke pembayaran?",
                        seatsDisplay.toString(),
                        selectedSeats.size(),
                        selectedSeats.size() * schedule.getMovie().getTicketPrice()),
                "Konfirmasi Kursi", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
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