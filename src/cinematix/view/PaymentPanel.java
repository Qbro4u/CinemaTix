package cinematix.view;

import cinematix.controller.PaymentController;
import cinematix.model.Booking;
import javax.swing.*;
import java.awt.*;

public class PaymentPanel extends JPanel {
    private Booking booking;
    private PaymentController paymentController;
    private JTextField amountField;
    private JComboBox<String> paymentMethodCombo;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JButton btnPay;
    private JButton btnCancel;
    private Runnable onSuccess;
    private Runnable onCancel;

    public PaymentPanel(Booking booking, PaymentController paymentController, Runnable onSuccess, Runnable onCancel) {
        this.booking = booking;
        this.paymentController = paymentController;
        this.onSuccess = onSuccess;
        this.onCancel = onCancel;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Order Summary Panel
        JPanel summaryPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Ringkasan Pesanan"));
        summaryPanel.add(new JLabel("Film:"));
        summaryPanel.add(new JLabel(booking.getSchedule().getMovie().getTitle()));
        summaryPanel.add(new JLabel("Studio:"));
        summaryPanel.add(new JLabel(booking.getSchedule().getStudio()));
        summaryPanel.add(new JLabel("Jam Tayang:"));
        summaryPanel.add(new JLabel(booking.getSchedule().getFormattedShowTime()));
        summaryPanel.add(new JLabel("Jumlah Kursi:"));
        summaryPanel.add(new JLabel(String.valueOf(booking.getSelectedSeats().size())));
        summaryPanel.add(new JLabel("Nomor Kursi:"));
        summaryPanel.add(new JLabel(booking.getSeatsDisplay()));  // ← SUDAH MENGGUNAKAN FORMAT BENAR
        summaryPanel.add(new JLabel("Total Harga:"));
        summaryPanel.add(new JLabel(String.format("Rp %,.0f", booking.getTotalPrice())));

        // Payment Form Panel
        JPanel paymentFormPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        paymentFormPanel.setBorder(BorderFactory.createTitledBorder("Metode Pembayaran"));

        paymentFormPanel.add(new JLabel("Metode Pembayaran:"));
        paymentMethodCombo = new JComboBox<>(new String[]{"Cash", "Transfer Bank", "QRIS", "E-Wallet"});
        paymentFormPanel.add(paymentMethodCombo);

        paymentFormPanel.add(new JLabel("Jumlah Bayar:"));
        amountField = new JTextField(String.valueOf((int) booking.getTotalPrice()));
        paymentFormPanel.add(amountField);

        // Progress Panel
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        statusLabel = new JLabel("", SwingConstants.CENTER);

        progressPanel.add(progressBar, BorderLayout.CENTER);
        progressPanel.add(statusLabel, BorderLayout.SOUTH);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        btnPay = new JButton("Bayar Sekarang");
        btnPay.setBackground(new Color(70, 130, 200));
        btnPay.setForeground(Color.WHITE);
        btnPay.setFont(new Font("Arial", Font.BOLD, 14));
        btnPay.setPreferredSize(new Dimension(150, 40));
        btnPay.addActionListener(e -> processPayment());

        btnCancel = new JButton("Batal");
        btnCancel.addActionListener(e -> {
            if (onCancel != null) onCancel.run();
        });

        buttonPanel.add(btnPay);
        buttonPanel.add(btnCancel);

        add(summaryPanel, BorderLayout.NORTH);
        add(paymentFormPanel, BorderLayout.CENTER);
        add(progressPanel, BorderLayout.SOUTH);
        add(buttonPanel, BorderLayout.PAGE_END);
    }

    private void processPayment() {
        double amountPaid;
        try {
            amountPaid = Double.parseDouble(amountField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Masukkan jumlah uang yang valid!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String paymentMethod = (String) paymentMethodCombo.getSelectedItem();

        btnPay.setEnabled(false);
        btnCancel.setEnabled(false);
        progressBar.setVisible(true);

        paymentController.processPayment(booking, amountPaid, paymentMethod,
                new PaymentController.PaymentCallback() {
                    @Override
                    public void onProgress(int percent) {
                        progressBar.setValue(percent);
                        statusLabel.setText("Memproses pembayaran... " + percent + "%");
                    }

                    @Override
                    public void onSuccess(String bookingCode) {
                        progressBar.setValue(100);
                        statusLabel.setText("Pembayaran berhasil!");
                        JOptionPane.showMessageDialog(PaymentPanel.this,
                                "Pembayaran berhasil!\nKode Booking: " + bookingCode,
                                "Sukses", JOptionPane.INFORMATION_MESSAGE);
                        if (onSuccess != null) onSuccess.run();
                    }

                    @Override
                    public void onError(String message) {
                        progressBar.setVisible(false);
                        statusLabel.setText("");
                        JOptionPane.showMessageDialog(PaymentPanel.this, message, "Error", JOptionPane.ERROR_MESSAGE);
                        btnPay.setEnabled(true);
                        btnCancel.setEnabled(true);
                    }
                });
    }
}