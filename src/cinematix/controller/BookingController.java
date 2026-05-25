package cinematix.controller;

import cinematix.model.*;
import cinematix.view.SeatSelectionPanel;
import cinematix.view.PaymentPanel;
import cinematix.view.BookingConfirmationPanel;
import cinematix.utils.BookingTimer;
import cinematix.exception.SeatAlreadyBookedException;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class BookingController {
    private Booking currentBooking;
    private BookingTimer bookingTimer;
    private JDialog currentDialog;
    private PaymentController paymentController;

    public BookingController() {
        this.paymentController = new PaymentController();
    }

    public void startBooking(Customer customer, Movie movie, Schedule schedule, Component parent) {
        // Validasi kursi tersedia
        if (schedule.getAvailableSeatsCount() <= 0) {
            JOptionPane.showMessageDialog(parent, "Maaf, kursi untuk jadwal ini sudah habis!",
                    "Info", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Buat booking baru
        currentBooking = new Booking(customer, schedule);

        // Buka Seat Selection Dialog
        JDialog seatDialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "Pilih Kursi",
                Dialog.ModalityType.APPLICATION_MODAL);
        seatDialog.setSize(1200, 700);
        seatDialog.setLocationRelativeTo(parent);

        SeatSelectionPanel seatPanel = new SeatSelectionPanel(schedule, this);
        seatDialog.add(seatPanel);

        // Start timer untuk timeout booking (5 menit)
        JLabel timerLabel = new JLabel();
        bookingTimer = new BookingTimer(currentBooking, timerLabel, 300);
        bookingTimer.setOnTimeout(() -> {
            if (currentDialog != null) currentDialog.dispose();
            seatDialog.dispose();
            JOptionPane.showMessageDialog(parent, "Waktu pemesanan habis!", "Timeout", JOptionPane.WARNING_MESSAGE);
        });

        seatDialog.setVisible(true);
    }

    public void createBookingAndProceedToPayment(Schedule schedule, List<Integer> selectedSeats, SeatSelectionPanel seatPanel) {
        try {
            for (int seat : selectedSeats) {
                currentBooking.reserveSeat(seat);
            }
            currentBooking.calculateTotal();

            if (currentBooking.saveToDatabase()) {
                SwingUtilities.getWindowAncestor(seatPanel).dispose();
                proceedToPayment();
            } else {
                seatPanel.showError("Gagal menyimpan booking ke database!");
            }
        } catch (SeatAlreadyBookedException e) {
            seatPanel.showError(e.getMessage());
        }
    }
    // Contoh perbaikan di BookingController.java
    private void proceedToPayment() {
        JDialog paymentDialog = new JDialog();
        paymentDialog.setTitle("CinemaTix - Pembayaran");
        paymentDialog.setModal(true);
        paymentDialog.setSize(500, 550);
        paymentDialog.setLocationRelativeTo(null);
        paymentDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Buat final copy untuk digunakan di lambda
        final JDialog finalPaymentDialog = paymentDialog;
        final BookingTimer finalTimer = bookingTimer;

        PaymentPanel paymentPanel = new PaymentPanel(currentBooking, paymentController,
                () -> {
                    // On Success
                    finalPaymentDialog.dispose();
                    if (finalTimer != null) finalTimer.stop();
                    showTicketConfirmation();
                },
                () -> {
                    // On Cancel
                    int confirm = JOptionPane.showConfirmDialog(finalPaymentDialog,
                            "Batalkan pemesanan? Kursi yang sudah dipilih akan dibuka kembali.",
                            "Konfirmasi", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        currentBooking.cancelBooking(currentBooking.getBookingCode());
                        if (finalTimer != null) finalTimer.stop();
                        finalPaymentDialog.dispose();
                    }
                });

        paymentDialog.add(paymentPanel);
        paymentDialog.setVisible(true);
    }
    private void showTicketConfirmation() {
        // PERBAIKAN: Gunakan constructor yang benar
        JDialog ticketDialog = new JDialog((JFrame) null, "CinemaTix - Tiket Anda", true);
        ticketDialog.setSize(500, 600);
        ticketDialog.setLocationRelativeTo(null);
        ticketDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        BookingConfirmationPanel confirmationPanel = new BookingConfirmationPanel(currentBooking,
                () -> {
                    ticketDialog.dispose();
                    JOptionPane.showMessageDialog(null, "Terima kasih telah menggunakan CinemaTix!\nSelamat menonton!");
                });

        ticketDialog.add(confirmationPanel);
        ticketDialog.setVisible(true);
    }

    public void cancelBooking() {
        if (currentBooking != null && bookingTimer != null) {
            currentBooking.cancelBooking(currentBooking.getBookingCode());
            bookingTimer.stop();
        }
    }
}