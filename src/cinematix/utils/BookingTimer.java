package cinematix.utils;

import cinematix.exception.SessionTimeoutException;
import cinematix.model.Booking;
import javax.swing.*;

public class BookingTimer {
    private Timer timer;
    private int timeRemaining;
    private Booking booking;
    private JLabel timerLabel;
    private Runnable onTimeout;

    public BookingTimer(Booking booking, JLabel timerLabel, int durationSeconds) {
        this.booking = booking;
        this.timerLabel = timerLabel;
        this.timeRemaining = durationSeconds;
    }

    public void setOnTimeout(Runnable onTimeout) {
        this.onTimeout = onTimeout;
    }

    public void start() {
        timer = new Timer(1000, e -> {
            timeRemaining--;

            if (timerLabel != null) {
                int minutes = timeRemaining / 60;
                int seconds = timeRemaining % 60;
                timerLabel.setText(String.format("Sisa waktu: %02d:%02d", minutes, seconds));

                // Warning when time less than 1 minute
                if (timeRemaining <= 60) {
                    timerLabel.setForeground(java.awt.Color.RED);
                }
            }

            if (timeRemaining <= 0) {
                stop();
                try {
                    throw new SessionTimeoutException("Waktu pemesanan habis! Booking dibatalkan.");
                } catch (SessionTimeoutException ex) {
                    booking.cancelBooking(booking.getBookingCode());
                    if (onTimeout != null) {
                        onTimeout.run();
                    }
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Timeout",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        timer.start();
    }

    public void stop() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
    }

    public void reset(int newDuration) {
        stop();
        this.timeRemaining = newDuration;
        start();
    }

    public int getTimeRemaining() {
        return timeRemaining;
    }
}