package cinematix.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Movie extends BaseEntity {
    private String title;
    private String genre;
    private int duration;
    private String director;
    private String cast;
    private double ticketPrice;
    private boolean isActive;
    private String posterPath;

    // CRUD Operations
    public boolean save() {
        String sql = "INSERT INTO movies (title, genre, duration, director, cast, ticket_price, is_active, poster_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, this.title);
            pstmt.setString(2, this.genre);
            pstmt.setInt(3, this.duration);
            pstmt.setString(4, this.director);
            pstmt.setString(5, this.cast);
            pstmt.setDouble(6, this.ticketPrice);
            pstmt.setBoolean(7, this.isActive);
            pstmt.setString(8, this.posterPath);

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

    public static Movie findById(int id) {
        String sql = "SELECT * FROM movies WHERE id = ?";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToMovie(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Movie> getAll() {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM movies WHERE is_active = TRUE ORDER BY title";
        try (Statement stmt = DatabaseConnection.getConnection().createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                movies.add(mapResultSetToMovie(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movies;
    }

    public boolean update() {
        String sql = "UPDATE movies SET title = ?, genre = ?, duration = ?, director = ?, cast = ?, ticket_price = ?, is_active = ?, poster_path = ? WHERE id = ?";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, this.title);
            pstmt.setString(2, this.genre);
            pstmt.setInt(3, this.duration);
            pstmt.setString(4, this.director);
            pstmt.setString(5, this.cast);
            pstmt.setDouble(6, this.ticketPrice);
            pstmt.setBoolean(7, this.isActive);
            pstmt.setString(8, this.posterPath);
            pstmt.setInt(9, this.id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete() {
        String sql = "DELETE FROM movies WHERE id = ?";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, this.id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static Movie mapResultSetToMovie(ResultSet rs) throws SQLException {
        Movie movie = new Movie();
        movie.id = rs.getInt("id");
        movie.title = rs.getString("title");
        movie.genre = rs.getString("genre");
        movie.duration = rs.getInt("duration");
        movie.director = rs.getString("director");
        movie.cast = rs.getString("cast");
        movie.ticketPrice = rs.getDouble("ticket_price");
        movie.isActive = rs.getBoolean("is_active");
        movie.posterPath = rs.getString("poster_path");
        if (rs.getTimestamp("created_at") != null) {
            movie.createdAt = rs.getTimestamp("created_at").toLocalDateTime();
        }
        return movie;
    }

    // Getters & Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }
    public String getCast() { return cast; }
    public void setCast(String cast) { this.cast = cast; }
    public double getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(double ticketPrice) { this.ticketPrice = ticketPrice; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public String getPosterPath() { return posterPath; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }

    @Override
    public String toString() {
        return title + " - " + genre + " (" + duration + " menit) - Rp " + ticketPrice;
    }
}