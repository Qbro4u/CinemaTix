package cinematix.view;

import cinematix.model.Movie;
import cinematix.model.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.*;
import java.util.List;

public class ManageMoviePanel extends JPanel {
    private JFrame parent;
    private JTable movieTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JCheckBox showInactiveCheckbox;

    public ManageMoviePanel(JFrame parent) {
        this.parent = parent;
        initComponents();
        loadMovies();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title Panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Manajemen Film");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titlePanel.add(titleLabel);

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Cari:"));
        searchField = new JTextField(20);
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                searchMovies();
            }
        });
        searchPanel.add(searchField);

        // Checkbox untuk menampilkan film tidak aktif
        showInactiveCheckbox = new JCheckBox("Tampilkan film tidak aktif");
        showInactiveCheckbox.addActionListener(e -> loadMovies());
        searchPanel.add(showInactiveCheckbox);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnAdd = new JButton("+ Tambah Film");
        btnAdd.setBackground(new Color(70, 130, 200));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdd.addActionListener(e -> openMovieDialog(null));

        JButton btnEdit = new JButton("✏ Edit");
        btnEdit.setBackground(new Color(100, 150, 100));
        btnEdit.setForeground(Color.WHITE);
        btnEdit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEdit.addActionListener(e -> editMovie());

        JButton btnToggleStatus = new JButton("🔄 Aktif/Nonaktifkan");
        btnToggleStatus.setBackground(new Color(255, 140, 0));
        btnToggleStatus.setForeground(Color.WHITE);
        btnToggleStatus.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnToggleStatus.addActionListener(e -> toggleMovieStatus());

        JButton btnDelete = new JButton("🗑 Hapus Permanen");
        btnDelete.setBackground(Color.RED);
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDelete.addActionListener(e -> deleteMoviePermanently());

        JButton btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.setBackground(new Color(70, 130, 200));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> loadMovies());

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnToggleStatus);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnRefresh);

        // Table
        String[] columns = {"ID", "Judul Film", "Genre", "Durasi", "Harga", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        movieTable = new JTable(tableModel);
        movieTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        movieTable.setRowHeight(25);
        movieTable.getTableHeader().setReorderingAllowed(false);

        // Custom renderer untuk warna status
        movieTable.setDefaultRenderer(Object.class, new StatusCellRenderer());

        // Set column widths
        movieTable.getColumnModel().getColumn(0).setMaxWidth(50);
        movieTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        movieTable.getColumnModel().getColumn(5).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(movieTable);

        // Info Panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel infoLabel = new JLabel("💡 Informasi: Film yang dinonaktifkan tidak akan muncul di aplikasi customer, tetapi data tetap tersimpan untuk riwayat.");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        infoLabel.setForeground(Color.GRAY);
        infoPanel.add(infoLabel);

        // Layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(titlePanel, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);
    }

    private void loadMovies() {
        tableModel.setRowCount(0);
        boolean showInactive = showInactiveCheckbox.isSelected();

        String sql;
        if (showInactive) {
            sql = "SELECT * FROM movies ORDER BY title";
        } else {
            sql = "SELECT * FROM movies WHERE is_active = TRUE ORDER BY title";
        }

        try (Statement stmt = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                boolean isActive = rs.getBoolean("is_active");
                String status = isActive ? "🟢 Aktif" : "🔴 Tidak Aktif";
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("genre"),
                        rs.getInt("duration") + " menit",
                        "Rp " + String.format("%,.0f", rs.getDouble("ticket_price")),
                        status
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data film: " + e.getMessage());
        }

        if (tableModel.getRowCount() == 0) {
            tableModel.addRow(new Object[]{"-", "Tidak ada data", "-", "-", "-", "-"});
        }
    }

    private void searchMovies() {
        String keyword = searchField.getText().toLowerCase();
        tableModel.setRowCount(0);
        boolean showInactive = showInactiveCheckbox.isSelected();

        String sql = "SELECT * FROM movies WHERE (title LIKE ? OR genre LIKE ?)";
        if (!showInactive) {
            sql += " AND is_active = TRUE";
        }
        sql += " ORDER BY title";

        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            String searchParam = "%" + keyword + "%";
            pstmt.setString(1, searchParam);
            pstmt.setString(2, searchParam);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                boolean isActive = rs.getBoolean("is_active");
                String status = isActive ? "🟢 Aktif" : "🔴 Tidak Aktif";
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("genre"),
                        rs.getInt("duration") + " menit",
                        "Rp " + String.format("%,.0f", rs.getDouble("ticket_price")),
                        status
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (tableModel.getRowCount() == 0) {
            tableModel.addRow(new Object[]{"-", "Tidak ada data", "-", "-", "-", "-"});
        }
    }

    // ============================================================
    // PERBAIKAN: Method toggleMovieStatus (Aktif/Nonaktifkan)
    // ============================================================
    private void toggleMovieStatus() {
        int selectedRow = movieTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Pilih film yang akan diubah statusnya!",
                    "Peringatan",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int movieId = (int) tableModel.getValueAt(selectedRow, 0);
        String movieTitle = (String) tableModel.getValueAt(selectedRow, 1);
        String currentStatus = (String) tableModel.getValueAt(selectedRow, 5);

        // PERBAIKAN: Deteksi status berdasarkan icon
        boolean isCurrentlyActive;
        if (currentStatus.contains("🟢")) {
            isCurrentlyActive = true;
        } else if (currentStatus.contains("🔴")) {
            isCurrentlyActive = false;
        } else {
            // Fallback: periksa teks dengan regex
            isCurrentlyActive = !currentStatus.contains("Tidak Aktif");
        }

        String action = isCurrentlyActive ? "NONAKTIFKAN" : "AKTIFKAN";
        String newStatusText = isCurrentlyActive ? "nonaktif" : "aktif";
        String fromStatus = isCurrentlyActive ? "Aktif" : "Tidak Aktif";

        String message = String.format(
                "Apakah Anda yakin ingin %s film berikut?\n\n" +
                        "Film: %s\n" +
                        "Status saat ini: %s\n\n" +
                        "Setelah di%s:\n" +
                        "- Film %s akan %s dari daftar film customer\n" +
                        "- Data film tetap tersimpan di database\n" +
                        "- Film bisa di%s kembali kapan saja",
                action,
                movieTitle,
                fromStatus,
                newStatusText,
                movieTitle,
                isCurrentlyActive ? "hilang" : "muncul",
                isCurrentlyActive ? "aktifkan" : "nonaktifkan"
        );

        int confirm = JOptionPane.showConfirmDialog(this,
                message,
                "Konfirmasi " + action + " Film",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = updateMovieStatus(movieId, !isCurrentlyActive);

            if (success) {
                String successMessage = isCurrentlyActive ?
                        "Film berhasil dinonaktifkan!" :
                        "Film berhasil diaktifkan kembali!";
                JOptionPane.showMessageDialog(this, successMessage, "Sukses", JOptionPane.INFORMATION_MESSAGE);
                loadMovies(); // Refresh tabel
            } else {
                JOptionPane.showMessageDialog(this,
                        "Gagal mengubah status film! Periksa koneksi database.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Method khusus untuk update status film di database
    private boolean updateMovieStatus(int movieId, boolean newStatus) {
        String sql = "UPDATE movies SET is_active = ? WHERE id = ?";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setBoolean(1, newStatus);
            pstmt.setInt(2, movieId);
            int rowsAffected = pstmt.executeUpdate();

            System.out.println("Updating movie ID: " + movieId + " to active=" + newStatus);
            System.out.println("Rows affected: " + rowsAffected);

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void deleteMoviePermanently() {
        int selectedRow = movieTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Pilih film yang akan dihapus permanen!",
                    "Peringatan",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int movieId = (int) tableModel.getValueAt(selectedRow, 0);
        String movieTitle = (String) tableModel.getValueAt(selectedRow, 1);

        // Cek apakah film memiliki jadwal atau booking
        boolean hasSchedules = checkIfMovieHasSchedules(movieId);
        boolean hasBookings = checkIfMovieHasBookings(movieId);

        StringBuilder warning = new StringBuilder();
        warning.append("⚠️ PERINGATAN! ⚠️\n\n");
        warning.append("Anda akan MENGHAPUS PERMANEN film:\n");
        warning.append("'").append(movieTitle).append("'\n\n");

        if (hasSchedules || hasBookings) {
            warning.append("Data yang akan hilang:\n");
            if (hasSchedules) {
                warning.append("- Jadwal tayang film ini\n");
            }
            if (hasBookings) {
                warning.append("- Riwayat booking tiket customer\n");
            }
            warning.append("\n");
        }

        warning.append("TINDAKAN INI TIDAK DAPAT DIBATALKAN!\n\n");
        warning.append("Lanjutkan?");

        int confirm = JOptionPane.showConfirmDialog(this,
                warning.toString(),
                "Hapus Permanen Film",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            Connection conn = null;
            try {
                conn = DatabaseConnection.getConnection();
                conn.setAutoCommit(false);

                // Hapus booking terkait
                String sqlBooking = "DELETE b FROM bookings b " +
                        "JOIN schedules s ON b.schedule_id = s.id " +
                        "WHERE s.movie_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlBooking)) {
                    pstmt.setInt(1, movieId);
                    pstmt.executeUpdate();
                }

                // Hapus schedule terkait
                String sqlSchedule = "DELETE FROM schedules WHERE movie_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlSchedule)) {
                    pstmt.setInt(1, movieId);
                    pstmt.executeUpdate();
                }

                // Hapus film
                String sqlMovie = "DELETE FROM movies WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlMovie)) {
                    pstmt.setInt(1, movieId);
                    int result = pstmt.executeUpdate();

                    if (result > 0) {
                        conn.commit();
                        JOptionPane.showMessageDialog(this, "Film berhasil dihapus permanen!");
                        loadMovies();
                    } else {
                        conn.rollback();
                        JOptionPane.showMessageDialog(this, "Gagal menghapus film!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (SQLException e) {
                try {
                    if (conn != null) conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                try {
                    if (conn != null) conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean checkIfMovieHasSchedules(int movieId) {
        String sql = "SELECT COUNT(*) as total FROM schedules WHERE movie_id = ?";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, movieId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total") > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean checkIfMovieHasBookings(int movieId) {
        String sql = "SELECT COUNT(*) as total FROM bookings b " +
                "JOIN schedules s ON b.schedule_id = s.id " +
                "WHERE s.movie_id = ?";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, movieId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total") > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void openMovieDialog(Movie existingMovie) {
        JDialog dialog = new JDialog(parent, existingMovie == null ? "Tambah Film" : "Edit Film", true);
        dialog.setSize(450, 520);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField titleField = new JTextField(20);
        JComboBox<String> genreCombo = new JComboBox<>(new String[]{"Action", "Drama", "Comedy", "Horror", "Sci-Fi", "Romance", "Thriller"});
        JSpinner durationSpinner = new JSpinner(new SpinnerNumberModel(120, 30, 300, 5));
        JTextField directorField = new JTextField(20);
        JTextArea castArea = new JTextArea(3, 20);
        JSpinner priceSpinner = new JSpinner(new SpinnerNumberModel(50000, 10000, 200000, 5000));
        JCheckBox activeCheckBox = new JCheckBox("Aktif (film akan muncul di customer)", true);

        if (existingMovie != null) {
            titleField.setText(existingMovie.getTitle());
            genreCombo.setSelectedItem(existingMovie.getGenre());
            durationSpinner.setValue(existingMovie.getDuration());
            directorField.setText(existingMovie.getDirector());
            castArea.setText(existingMovie.getCast());
            priceSpinner.setValue((int) existingMovie.getTicketPrice());
            activeCheckBox.setSelected(existingMovie.isActive());
        }

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Judul Film:"), gbc);
        gbc.gridx = 1; formPanel.add(titleField, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Genre:"), gbc);
        gbc.gridx = 1; formPanel.add(genreCombo, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Durasi (menit):"), gbc);
        gbc.gridx = 1; formPanel.add(durationSpinner, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Sutradara:"), gbc);
        gbc.gridx = 1; formPanel.add(directorField, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Pemain:"), gbc);
        gbc.gridx = 1;
        JScrollPane castScroll = new JScrollPane(castArea);
        castScroll.setPreferredSize(new Dimension(200, 60));
        formPanel.add(castScroll, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Harga Tiket:"), gbc);
        gbc.gridx = 1; formPanel.add(priceSpinner, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1; formPanel.add(activeCheckBox, gbc);

        JPanel buttonPanel = new JPanel();
        JButton btnSave = new JButton("Simpan");
        btnSave.setBackground(new Color(70, 130, 200));
        btnSave.setForeground(Color.WHITE);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> {
            if (titleField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Judul film harus diisi!");
                return;
            }

            Movie movie = existingMovie != null ? existingMovie : new Movie();
            movie.setTitle(titleField.getText().trim());
            movie.setGenre((String) genreCombo.getSelectedItem());
            movie.setDuration((int) durationSpinner.getValue());
            movie.setDirector(directorField.getText().trim());
            movie.setCast(castArea.getText().trim());
            movie.setTicketPrice((int) priceSpinner.getValue());
            movie.setActive(activeCheckBox.isSelected());

            boolean success;
            if (existingMovie != null) {
                success = updateMovie(movie);
            } else {
                success = saveMovie(movie);
            }

            if (success) {
                JOptionPane.showMessageDialog(dialog, "Data film berhasil disimpan!");
                dialog.dispose();
                loadMovies();
            } else {
                JOptionPane.showMessageDialog(dialog, "Gagal menyimpan data film!");
            }
        });

        JButton btnCancel = new JButton("Batal");
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dialog.dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private boolean saveMovie(Movie movie) {
        String sql = "INSERT INTO movies (title, genre, duration, director, cast, ticket_price, is_active) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, movie.getTitle());
            pstmt.setString(2, movie.getGenre());
            pstmt.setInt(3, movie.getDuration());
            pstmt.setString(4, movie.getDirector());
            pstmt.setString(5, movie.getCast());
            pstmt.setDouble(6, movie.getTicketPrice());
            pstmt.setBoolean(7, movie.isActive());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean updateMovie(Movie movie) {
        String sql = "UPDATE movies SET title = ?, genre = ?, duration = ?, director = ?, cast = ?, ticket_price = ?, is_active = ? WHERE id = ?";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, movie.getTitle());
            pstmt.setString(2, movie.getGenre());
            pstmt.setInt(3, movie.getDuration());
            pstmt.setString(4, movie.getDirector());
            pstmt.setString(5, movie.getCast());
            pstmt.setDouble(6, movie.getTicketPrice());
            pstmt.setBoolean(7, movie.isActive());
            pstmt.setInt(8, movie.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void editMovie() {
        int selectedRow = movieTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih film yang akan diedit!");
            return;
        }

        int movieId = (int) tableModel.getValueAt(selectedRow, 0);
        Movie movie = findMovieById(movieId);

        if (movie != null) {
            openMovieDialog(movie);
        }
    }

    private Movie findMovieById(int movieId) {
        String sql = "SELECT * FROM movies WHERE id = ?";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, movieId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Movie movie = new Movie();
                movie.setId(rs.getInt("id"));
                movie.setTitle(rs.getString("title"));
                movie.setGenre(rs.getString("genre"));
                movie.setDuration(rs.getInt("duration"));
                movie.setDirector(rs.getString("director"));
                movie.setCast(rs.getString("cast"));
                movie.setTicketPrice(rs.getDouble("ticket_price"));
                movie.setActive(rs.getBoolean("is_active"));
                return movie;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ============================================================
    // PERBAIKAN: Custom cell renderer untuk warna status
    // ============================================================
    class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected && column == 5 && value != null) {
                String status = value.toString();
                // Deteksi berdasarkan icon
                if (status.contains("🟢")) {
                    c.setForeground(new Color(0, 150, 0));
                } else if (status.contains("🔴")) {
                    c.setForeground(Color.RED);
                } else {
                    // Fallback: deteksi berdasarkan teks
                    if (status.contains("Aktif") && !status.contains("Tidak")) {
                        c.setForeground(new Color(0, 150, 0));
                    } else {
                        c.setForeground(Color.RED);
                    }
                }
            } else if (!isSelected) {
                c.setForeground(Color.BLACK);
            }

            return c;
        }
    }
}