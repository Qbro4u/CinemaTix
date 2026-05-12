package cinematix.controller;

import cinematix.exception.InvalidPaymentException;
import cinematix.model.Booking;
import javax.swing.SwingWorker;
import java.util.List;

public class PaymentController {

    public interface PaymentCallback {
        void onProgress(int percent);
        void onSuccess(String bookingCode);
        void onError(String message);
    }

    public void processPayment(Booking booking, double amountPaid, String paymentMethod, PaymentCallback callback) {
        // Gunakan SwingWorker untuk multithreading
        SwingWorker<Boolean, Integer> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // Simulasi proses pembayaran
                for (int i = 0; i <= 100; i += 10) {
                    Thread.sleep(200); // Simulasi koneksi ke payment gateway
                    publish(i);

                    // Cek pembatalan
                    if (isCancelled()) {
                        return false;
                    }
                }

                // Validasi pembayaran - LANGSUNG LEMPAR EXCEPTION
                if (amountPaid < booking.getTotalPrice()) {
                    throw new InvalidPaymentException(
                            String.format("Pembayaran kurang: Rp %,.0f", booking.getTotalPrice() - amountPaid));
                }

                // Jika kelebihan bayar, hitung kembalian
                double change = amountPaid - booking.getTotalPrice();
                if (change > 0) {
                    System.out.println("Kembalian: Rp " + change);
                }

                return true;
            }

            @Override
            protected void process(List<Integer> chunks) {
                int progress = chunks.get(chunks.size() - 1);
                callback.onProgress(progress);
            }

            @Override
            protected void done() {
                try {
                    // Ambil hasil dari doInBackground()
                    // Jika ada exception, get() akan melempar ExecutionException
                    boolean success = get();

                    if (success) {
                        // Konfirmasi pembayaran
                        booking.confirmPayment(paymentMethod, amountPaid);
                        callback.onSuccess(booking.getBookingCode());
                    } else {
                        callback.onError("Pembayaran dibatalkan.");
                    }

                } catch (Exception e) {
                    // Tangkap semua exception termasuk yang dilempar dari doInBackground()
                    Throwable cause = e.getCause();
                    if (cause instanceof InvalidPaymentException) {
                        callback.onError(cause.getMessage());
                    } else {
                        callback.onError("Terjadi kesalahan: " + e.getMessage());
                    }
                }
            }
        };

        worker.execute();
    }

    // Method overloading untuk pembayaran tanpa callback (default)
    public void processPayment(Booking booking, double amountPaid, String paymentMethod) {
        processPayment(booking, amountPaid, paymentMethod, new PaymentCallback() {
            @Override
            public void onProgress(int percent) {
                System.out.println("Progress: " + percent + "%");
            }

            @Override
            public void onSuccess(String bookingCode) {
                System.out.println("Pembayaran berhasil! Kode Booking: " + bookingCode);
            }

            @Override
            public void onError(String message) {
                System.err.println("Error: " + message);
            }
        });
    }
}