package cinematix.model;

import java.sql.*;

public class Admin extends User {

    public Admin() {}

    public Admin(String username, String password, String fullName) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = "admin";
    }

    @Override
    public boolean login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND role = 'admin'";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                this.id = rs.getInt("id");
                this.username = rs.getString("username");
                this.fullName = rs.getString("full_name");
                this.email = rs.getString("email");
                this.role = rs.getString("role");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean register() {
        String sql = "INSERT INTO users (username, password, full_name, role) VALUES (?, ?, ?, 'admin')";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, this.username);
            pstmt.setString(2, this.password);
            pstmt.setString(3, this.fullName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}