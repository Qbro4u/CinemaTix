package cinematix.model;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Schedule extends BaseEntity {
    private Movie movie;
    private int movieId;
    private String studio;
    private LocalDateTime showTime;
    private int totalSeats;
    private List<Integer> bookedSeats;

    public Schedule() {
        this.totalSeats = 80;
        this.bookedSeats = new ArrayList<>();
    }

    // Tambahkan method ini di Schedule.java
    public Schedule getSchedule() {
        return this;
    }

    public boolean save() {
        String sql = "INSERT INTO schedules (movie_id, studio, show_time, total_seats) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, this.movieId);
            pstmt.setString(2, this.studio);
            pstmt.setTimestamp(3, Timestamp.valueOf(this.showTime));
            pstmt.setInt(4, this.totalSeats);

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    this.id = rs.getInt(1);
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static List<Schedule> getSchedulesByMovie(int movieId) {
        List<Schedule> schedules = new ArrayList<>();
        String sql = "SELECT * FROM schedules WHERE movie_id = ? AND show_time > NOW() ORDER BY show_time";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, movieId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Schedule schedule = new Schedule();
                schedule.id = rs.getInt("id");
                schedule.movieId = rs.getInt("movie_id");
                schedule.movie = Movie.findById(schedule.movieId);
                schedule.studio = rs.getString("studio");
                schedule.showTime = rs.getTimestamp("show_time").toLocalDateTime();
                schedule.totalSeats = rs.getInt("total_seats");
                schedule.loadBookedSeats();
                schedules.add(schedule);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schedules;
    }

    private void loadBookedSeats() {
        bookedSeats.clear();
        String sql = "SELECT DISTINCT seat_number FROM booked_seats bs " +
                "JOIN bookings b ON bs.booking_id = b.id " +
                "WHERE b.schedule_id = ? AND b.status IN ('PENDING', 'CONFIRMED')";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, this.id);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                bookedSeats.add(rs.getInt("seat_number"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isSeatBooked(int seatNumber) {
        return bookedSeats.contains(seatNumber);
    }

    public void bookSeat(int seatNumber) {
        if (!bookedSeats.contains(seatNumber)) {
            bookedSeats.add(seatNumber);
        }
    }

    public void freeSeat(int seatNumber) {
        bookedSeats.remove((Integer) seatNumber);
    }

    public int getAvailableSeatsCount() {
        return totalSeats - bookedSeats.size();
    }

    // Tambahkan method ini di dalam class Schedule.java

    public static Schedule findById(int id) {
        String sql = "SELECT * FROM schedules WHERE id = ?";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Schedule schedule = new Schedule();
                schedule.id = rs.getInt("id");
                schedule.movieId = rs.getInt("movie_id");
                schedule.movie = Movie.findById(schedule.movieId);
                schedule.studio = rs.getString("studio");
                schedule.showTime = rs.getTimestamp("show_time").toLocalDateTime();
                schedule.totalSeats = rs.getInt("total_seats");
                schedule.loadBookedSeats();
                return schedule;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Getters & Setters
    public Movie getMovie() { return movie; }
    public void setMovie(Movie movie) { this.movie = movie; }
    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }
    public String getStudio() { return studio; }
    public void setStudio(String studio) { this.studio = studio; }
    public LocalDateTime getShowTime() { return showTime; }
    public void setShowTime(LocalDateTime showTime) { this.showTime = showTime; }
    public String getFormattedShowTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy - HH:mm");
        return showTime.format(formatter);
    }
    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }
    public List<Integer> getBookedSeats() { return bookedSeats; }
}