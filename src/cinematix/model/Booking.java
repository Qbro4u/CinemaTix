package cinematix.model;

import cinematix.exception.SeatAlreadyBookedException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Booking extends BaseEntity implements Bookable {
    private Customer customer;
    private int customerId;
    private Schedule schedule;
    private int scheduleId;
    private List<Integer> selectedSeats;
    private String bookingCode;
    private String status; // PENDING, CONFIRMED, CANCELLED, EXPIRED
    private double totalPrice;
    private String paymentMethod;
    private LocalDateTime paymentTime;

    public Booking() {
        this.selectedSeats = new ArrayList<>();
        this.status = "PENDING";
        this.bookingCode = generateTicketCode();
    }

    public Booking(Customer customer, Schedule schedule) {
        this();
        this.customer = customer;
        this.customerId = customer.getId();
        this.schedule = schedule;
        this.scheduleId = schedule.getId();
    }

    @Override
    public String generateTicketCode() {
        return "TIX" + System.currentTimeMillis() + String.format("%03d", (int)(Math.random() * 1000));
    }

    @Override
    public boolean reserveSeat(int seatNumber) throws SeatAlreadyBookedException {
        if (schedule.isSeatBooked(seatNumber)) {
            throw new SeatAlreadyBookedException("Kursi nomor " + seatNumber + " sudah dipesan untuk jadwal ini!");
        }
        selectedSeats.add(seatNumber);
        schedule.bookSeat(seatNumber);
        return true;
    }

    @Override
    public double calculateTotal() {
        totalPrice = selectedSeats.size() * schedule.getMovie().getTicketPrice();
        return totalPrice;
    }

    @Override
    public boolean cancelBooking(String bookingCode) {
        if (this.bookingCode.equals(bookingCode) && (status.equals("PENDING") || status.equals("CONFIRMED"))) {
            for (int seat : selectedSeats) {
                schedule.freeSeat(seat);
            }
            this.status = "CANCELLED";
            updateBookingInDatabase();
            return true;
        }
        return false;
    }

    public boolean saveToDatabase() {
        String sql = "INSERT INTO bookings (booking_code, customer_id, schedule_id, total_price, status, booking_time) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, this.bookingCode);
            pstmt.setInt(2, this.customerId);
            pstmt.setInt(3, this.scheduleId);
            pstmt.setDouble(4, this.totalPrice);
            pstmt.setString(5, this.status);
            pstmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    this.id = rs.getInt(1);
                }
                saveSelectedSeats();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void saveSelectedSeats() {
        String sql = "INSERT INTO booked_seats (booking_id, seat_number) VALUES (?, ?)";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            for (int seat : selectedSeats) {
                pstmt.setInt(1, this.id);
                pstmt.setInt(2, seat);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // PERBAIKAN: Method untuk update booking ke database
    private void updateBookingInDatabase() {
        String sql = "UPDATE bookings SET status = ?, payment_method = ?, payment_time = ? WHERE id = ?";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, this.status);
            pstmt.setString(2, this.paymentMethod);
            pstmt.setTimestamp(3, this.paymentTime != null ? Timestamp.valueOf(this.paymentTime) : null);
            pstmt.setInt(4, this.id);
            pstmt.executeUpdate();
            System.out.println("Booking updated: status=" + this.status + ", payment_method=" + this.paymentMethod);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // PERBAIKAN: Method confirmPayment
    public void confirmPayment(String paymentMethod, double amount) {
        System.out.println("=== CONFIRM PAYMENT CALLED ===");
        System.out.println("Payment Method: " + paymentMethod);
        System.out.println("Amount: " + amount);
        System.out.println("Booking ID: " + this.id);

        this.paymentMethod = paymentMethod;
        this.paymentTime = LocalDateTime.now();
        this.status = "CONFIRMED";

        updateBookingInDatabase();

        // ... rest of code
    }

    // ============================================================
    // METHOD UNTUK RIWAYAT
    // ============================================================

    public static List<Booking> getBookingsByCustomer(int customerId) {
        List<Booking> bookings = new ArrayList<>();
        // PASTIKAN SQL mengambil semua kolom termasuk payment_method dan payment_time
        String sql = "SELECT id, booking_code, customer_id, schedule_id, total_price, status, payment_method, payment_time, booking_time FROM bookings WHERE customer_id = ? ORDER BY booking_time DESC";

        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Booking booking = new Booking();
                booking.id = rs.getInt("id");
                booking.bookingCode = rs.getString("booking_code");
                booking.customerId = rs.getInt("customer_id");
                booking.scheduleId = rs.getInt("schedule_id");
                booking.totalPrice = rs.getDouble("total_price");
                booking.status = rs.getString("status");

                // PERBAIKAN: Ambil payment_method dan payment_time dari database
                booking.paymentMethod = rs.getString("payment_method");

                if (rs.getTimestamp("payment_time") != null) {
                    booking.paymentTime = rs.getTimestamp("payment_time").toLocalDateTime();
                }

                if (rs.getTimestamp("booking_time") != null) {
                    booking.createdAt = rs.getTimestamp("booking_time").toLocalDateTime();
                }

                // Load schedule
                booking.schedule = Schedule.findById(booking.scheduleId);

                // Load selected seats
                booking.loadSelectedSeatsForHistory();

                // Debug: cetak ke console
                System.out.println("Loaded booking: " + booking.bookingCode +
                        ", paymentMethod: " + booking.paymentMethod +
                        ", paymentTime: " + booking.paymentTime);

                bookings.add(booking);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }
    private void loadSelectedSeatsForHistory() {
        selectedSeats.clear();
        String sql = "SELECT seat_number FROM booked_seats WHERE booking_id = ?";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, this.id);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                selectedSeats.add(rs.getInt("seat_number"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ============================================================
    // GETTERS & SETTERS
    // ============================================================

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; this.customerId = customer.getId(); }
    public int getCustomerId() { return customerId; }
    public Schedule getSchedule() { return schedule; }
    public void setSchedule(Schedule schedule) { this.schedule = schedule; this.scheduleId = schedule.getId(); }
    public int getScheduleId() { return scheduleId; }
    public List<Integer> getSelectedSeats() { return selectedSeats; }
    public String getBookingCode() { return bookingCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getTotalPrice() { return totalPrice; }
    public String getPaymentMethod() { return paymentMethod; }
    public LocalDateTime getPaymentTime() { return paymentTime; }

    public String getSeatsDisplay() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < selectedSeats.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(selectedSeats.get(i));
        }
        return sb.toString();
    }

    public String getFormattedCreatedAt() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return createdAt.format(formatter);
    }
}