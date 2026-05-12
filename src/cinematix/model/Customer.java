package cinematix.model;

import java.sql.*;

public class Customer extends User {

    public Customer() {}

    public Customer(int id, String username, String fullName, String email) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.role = "customer";
    }

    @Override
    public boolean login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND role = 'customer'";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                this.id = rs.getInt("id");
                this.username = rs.getString("username");
                this.fullName = rs.getString("full_name");
                this.email = rs.getString("email");
                this.phone = rs.getString("phone");
                this.role = rs.getString("role");

                // Debug: cetak ke console
                System.out.println("Login berhasil! ID: " + this.id + ", Nama: " + this.fullName);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean register() {
        String sql = "INSERT INTO users (username, password, full_name, email, phone, role) VALUES (?, ?, ?, ?, ?, 'customer')";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, this.username);
            pstmt.setString(2, this.password);
            pstmt.setString(3, this.fullName);
            pstmt.setString(4, this.email);
            pstmt.setString(5, this.phone);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Ambil ID yang di-generate oleh database
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    this.id = generatedKeys.getInt(1);
                    System.out.println("Registrasi berhasil! ID: " + this.id);
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateProfile() {
        String sql = "UPDATE users SET full_name = ?, email = ?, phone = ? WHERE id = ?";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, this.fullName);
            pstmt.setString(2, this.email);
            pstmt.setString(3, this.phone);
            pstmt.setInt(4, this.id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Getter tambahan untuk debugging
    public boolean isLoggedIn() {
        return this.id > 0;
    }
}