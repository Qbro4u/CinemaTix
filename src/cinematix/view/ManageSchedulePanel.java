package cinematix.view;

import cinematix.model.Movie;
import cinematix.model.Schedule;
import cinematix.model.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ManageSchedulePanel extends JPanel {
    private JFrame parent;
    private JTable scheduleTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> movieFilter;
    private JComboBox<String> dateFilter;  // TAMBAHAN: Filter tanggal
    private JCheckBox showInactiveMoviesCheckbox;

    public ManageSchedulePanel(JFrame parent) {
        this.parent = parent;
        initComponents();
        loadMovieFilter();
        loadSchedules();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel titleLabel = new JLabel("Manajemen Jadwal Tayang");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Filter Film
        filterPanel.add(new JLabel("Film:"));
        movieFilter = new JComboBox<>();
        movieFilter.addItem("Semua Film");
        movieFilter.addActionListener(e -> loadSchedules());
        filterPanel.add(movieFilter);

        // ============================================================
        // TAMBAHAN: Filter Tanggal (Hari Ini, Besok, Pilih Tanggal)
        // ============================================================
        filterPanel.add(new JLabel("  Tanggal:"));

        dateFilter = new JComboBox<>();
        dateFilter.addItem("📅 Semua Tanggal");
        dateFilter.addItem("🟢 Hari Ini - " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        dateFilter.addItem("🔜 Besok - " + LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        dateFilter.addItem("📆 Pilih Tanggal Lain...");
        dateFilter.addActionListener(e -> {
            if (dateFilter.getSelectedItem() == "📆 Pilih Tanggal Lain...") {
                showDatePickerDialog();
            } else {
                loadSchedules();
            }
        });
        filterPanel.add(dateFilter);

        // Checkbox film tidak aktif
        showInactiveMoviesCheckbox = new JCheckBox("Tampilkan film tidak aktif");
        showInactiveMoviesCheckbox.addActionListener(e -> loadMovieFilter());
        filterPanel.add(showInactiveMoviesCheckbox);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnAdd = new JButton("+ Tambah Jadwal");
        btnAdd.setBackground(new Color(70, 130, 200));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdd.addActionListener(e -> openScheduleDialog(null));

        JButton btnDelete = new JButton("🗑 Hapus");
        btnDelete.setBackground(Color.RED);
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDelete.addActionListener(e -> deleteSchedule());

        JButton btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.setBackground(new Color(100, 150, 100));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> refreshData());

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnRefresh);

        // Table
        String[] columns = {"ID", "Film", "Studio", "Tanggal & Jam", "Total Kursi", "Tersedia", "Status Film"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        scheduleTable = new JTable(tableModel);
        scheduleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scheduleTable.setRowHeight(25);
        scheduleTable.getTableHeader().setReorderingAllowed(false);

        // Custom renderer untuk status film
        scheduleTable.setDefaultRenderer(Object.class, new StatusCellRenderer());

        // Set column widths
        scheduleTable.getColumnModel().getColumn(0).setMaxWidth(50);
        scheduleTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        scheduleTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        scheduleTable.getColumnModel().getColumn(6).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(scheduleTable);

        // Info Panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel infoLabel = new JLabel("💡 Informasi: Film yang tidak aktif tetap bisa diatur jadwalnya. Gunakan filter tanggal untuk melihat jadwal hari ini/besok.");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        infoLabel.setForeground(Color.GRAY);
        infoPanel.add(infoLabel);

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(titleLabel, BorderLayout.WEST);

        JPanel filterAndButtonPanel = new JPanel(new BorderLayout());
        filterAndButtonPanel.add(filterPanel, BorderLayout.WEST);
        filterAndButtonPanel.add(buttonPanel, BorderLayout.EAST);
        topPanel.add(filterAndButtonPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);
    }

    // ============================================================
    // TAMBAHAN: Dialog Pilih Tanggal
    // ============================================================
    private void showDatePickerDialog() {
        JDialog dateDialog = new JDialog(parent, "Pilih Tanggal", true);
        dateDialog.setSize(300, 200);
        dateDialog.setLocationRelativeTo(parent);
        dateDialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Date picker menggunakan JSpinner
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy");
        dateSpinner.setEditor(dateEditor);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Pilih Tanggal:"), gbc);
        gbc.gridx = 1;
        panel.add(dateSpinner, gbc);

        JPanel buttonPanel = new JPanel();
        JButton btnOk = new JButton("Tampilkan");
        btnOk.addActionListener(e -> {
            java.util.Date selectedDate = (java.util.Date) dateSpinner.getValue();
            LocalDate localDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            // Update combo box dengan tanggal yang dipilih
            String customDate = "📅 " + localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            dateFilter.addItem(customDate);
            dateFilter.setSelectedItem(customDate);

            dateDialog.dispose();
            loadSchedules();
        });

        JButton btnCancel = new JButton("Batal");
        btnCancel.addActionListener(e -> {
            dateFilter.setSelectedIndex(0);
            dateDialog.dispose();
        });

        buttonPanel.add(btnOk);
        buttonPanel.add(btnCancel);

        dateDialog.add(panel, BorderLayout.CENTER);
        dateDialog.add(buttonPanel, BorderLayout.SOUTH);
        dateDialog.setVisible(true);
    }

    // ============================================================
    // METHOD UNTUK MENDAPATKAN FILTER TANGGAL
    // ============================================================
    private String getDateFilterCondition() {
        String selectedDate = (String) dateFilter.getSelectedItem();
        if (selectedDate == null || selectedDate.equals("📅 Semua Tanggal")) {
            return "";
        }

        LocalDate targetDate = null;

        if (selectedDate.contains("Hari Ini")) {
            targetDate = LocalDate.now();
        } else if (selectedDate.contains("Besok")) {
            targetDate = LocalDate.now().plusDays(1);
        } else if (selectedDate.contains("📅") && !selectedDate.equals("📆 Pilih Tanggal Lain...")) {
            // Parse tanggal dari format "📅 dd/MM/yyyy"
            String dateStr = selectedDate.substring(3);
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                targetDate = LocalDate.parse(dateStr, formatter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (targetDate != null) {
            return " AND DATE(s.show_time) = '" + targetDate.toString() + "'";
        }

        return "";
    }

    private void loadMovieFilter() {
        movieFilter.removeAllItems();
        movieFilter.addItem("Semua Film");

        boolean showInactive = showInactiveMoviesCheckbox.isSelected();

        List<Movie> movies;
        if (showInactive) {
            movies = getAllMovies();
        } else {
            movies = getActiveMovies();
        }

        for (Movie movie : movies) {
            String displayName = movie.getTitle();
            if (!movie.isActive()) {
                displayName = "🔴 " + movie.getTitle() + " (Tidak Aktif)";
            }
            movieFilter.addItem(displayName);
        }
    }

    private List<Movie> getAllMovies() {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM movies ORDER BY title";

        try (Statement stmt = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Movie movie = new Movie();
                movie.setId(rs.getInt("id"));
                movie.setTitle(rs.getString("title"));
                movie.setGenre(rs.getString("genre"));
                movie.setDuration(rs.getInt("duration"));
                movie.setDirector(rs.getString("director"));
                movie.setCast(rs.getString("cast"));
                movie.setTicketPrice(rs.getDouble("ticket_price"));
                movie.setActive(rs.getBoolean("is_active"));
                movies.add(movie);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movies;
    }

    private List<Movie> getActiveMovies() {
        List<Movie> activeMovies = new ArrayList<>();
        String sql = "SELECT * FROM movies WHERE is_active = TRUE ORDER BY title";

        try (Statement stmt = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Movie movie = new Movie();
                movie.setId(rs.getInt("id"));
                movie.setTitle(rs.getString("title"));
                movie.setGenre(rs.getString("genre"));
                movie.setDuration(rs.getInt("duration"));
                movie.setDirector(rs.getString("director"));
                movie.setCast(rs.getString("cast"));
                movie.setTicketPrice(rs.getDouble("ticket_price"));
                movie.setActive(rs.getBoolean("is_active"));
                activeMovies.add(movie);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return activeMovies;
    }

    private void loadSchedules() {
        tableModel.setRowCount(0);

        try {
            String selectedItem = (String) movieFilter.getSelectedItem();
            if (selectedItem == null) {
                selectedItem = "Semua Film";
            }

            String selectedMovieTitle = selectedItem;
            if (selectedItem.startsWith("🔴 ")) {
                selectedMovieTitle = selectedItem.substring(3).replace(" (Tidak Aktif)", "");
            }

            // Bangun query dengan filter tanggal
            String dateFilterCondition = getDateFilterCondition();

            String sql = "SELECT s.id, s.movie_id, s.studio, s.show_time, s.total_seats, " +
                    "m.title, m.is_active " +
                    "FROM schedules s " +
                    "JOIN movies m ON s.movie_id = m.id " +
                    "WHERE 1=1 " + dateFilterCondition +
                    " ORDER BY s.show_time";

            try (Statement stmt = DatabaseConnection.getConnection().createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    int movieId = rs.getInt("movie_id");
                    String movieTitle = rs.getString("title");
                    boolean isMovieActive = rs.getBoolean("is_active");

                    // Filter berdasarkan film yang dipilih
                    if (!selectedItem.equals("Semua Film") && !movieTitle.equals(selectedMovieTitle)) {
                        continue;
                    }

                    int scheduleId = rs.getInt("id");
                    String studio = rs.getString("studio");
                    Timestamp showTimeStamp = rs.getTimestamp("show_time");
                    LocalDateTime showTime = showTimeStamp != null ? showTimeStamp.toLocalDateTime() : null;
                    int totalSeats = rs.getInt("total_seats");

                    // Hitung kursi terpakai
                    int bookedSeats = getBookedSeatsCount(scheduleId);
                    int availableSeats = totalSeats - bookedSeats;

                    String movieStatus = isMovieActive ? "🟢 Aktif" : "🔴 Tidak Aktif";
                    String formattedShowTime = showTime != null ?
                            showTime.format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy - HH:mm")) : "-";

                    tableModel.addRow(new Object[]{
                            scheduleId,
                            movieTitle,
                            studio,
                            formattedShowTime,
                            totalSeats,
                            availableSeats + "/" + totalSeats,
                            movieStatus
                    });
                }
            }

            if (tableModel.getRowCount() == 0) {
                String message = "Tidak ada data";
                String dateSelected = (String) dateFilter.getSelectedItem();
                if (dateSelected != null && !dateSelected.equals("📅 Semua Tanggal")) {
                    message = "Tidak ada jadwal untuk " + dateSelected;
                }
                tableModel.addRow(new Object[]{"-", message, "-", "-", "-", "-", "-"});
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saat memuat jadwal: " + e.getMessage());
        }
    }

    private int getBookedSeatsCount(int scheduleId) {
        String sql = "SELECT COUNT(DISTINCT seat_number) as total FROM booked_seats bs " +
                "JOIN bookings b ON bs.booking_id = b.id " +
                "WHERE b.schedule_id = ? AND b.status IN ('PENDING', 'CONFIRMED')";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, scheduleId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void refreshData() {
        JButton refreshButton = null;
        Component topComponent = ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.NORTH);
        if (topComponent instanceof JPanel) {
            JPanel topPanel = (JPanel) topComponent;
            Component centerComponent = ((BorderLayout) topPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
            if (centerComponent instanceof JPanel) {
                JPanel filterAndButtonPanel = (JPanel) centerComponent;
                Component eastComponent = ((BorderLayout) filterAndButtonPanel.getLayout()).getLayoutComponent(BorderLayout.EAST);
                if (eastComponent instanceof JPanel) {
                    JPanel buttonPanel = (JPanel) eastComponent;
                    for (Component comp : buttonPanel.getComponents()) {
                        if (comp instanceof JButton && ((JButton) comp).getText().contains("Refresh")) {
                            refreshButton = (JButton) comp;
                            break;
                        }
                    }
                }
            }
        }

        final JButton finalRefreshButton = refreshButton;

        if (finalRefreshButton != null) {
            finalRefreshButton.setEnabled(false);
            finalRefreshButton.setText("Memuat...");
        }

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                loadMovieFilter();
                loadSchedules();
                return null;
            }

            @Override
            protected void done() {
                if (finalRefreshButton != null) {
                    finalRefreshButton.setEnabled(true);
                    finalRefreshButton.setText("🔄 Refresh");
                }
                JOptionPane.showMessageDialog(ManageSchedulePanel.this,
                        "Data berhasil direfresh!",
                        "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        };
        worker.execute();
    }

    private void openScheduleDialog(Schedule existingSchedule) {
        // ... (kode tetap sama seperti sebelumnya)
        JDialog dialog = new JDialog(parent, existingSchedule == null ? "Tambah Jadwal" : "Edit Jadwal", true);
        dialog.setSize(450, 480);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> movieCombo = new JComboBox<>();
        List<Movie> movies = getAllMovies();
        for (Movie movie : movies) {
            String displayName = movie.getTitle();
            if (!movie.isActive()) {
                displayName = "🔴 " + movie.getTitle() + " (Tidak Aktif)";
            }
            movieCombo.addItem(displayName);
        }

        JComboBox<String> studioCombo = new JComboBox<>(new String[]{"Studio A", "Studio B", "Studio C", "Studio D", "Studio E"});

        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy HH:mm");
        dateSpinner.setEditor(dateEditor);

        JSpinner totalSeatsSpinner = new JSpinner(new SpinnerNumberModel(80, 30, 200, 10));

        JLabel warningLabel = new JLabel("⚠️ Film yang tidak aktif tidak akan terlihat oleh customer");
        warningLabel.setForeground(Color.ORANGE);
        warningLabel.setFont(new Font("Arial", Font.ITALIC, 11));

        if (existingSchedule != null) {
            String displayName = existingSchedule.getMovie().getTitle();
            if (!existingSchedule.getMovie().isActive()) {
                displayName = "🔴 " + displayName + " (Tidak Aktif)";
            }
            movieCombo.setSelectedItem(displayName);
            studioCombo.setSelectedItem(existingSchedule.getStudio());
            if (existingSchedule.getShowTime() != null) {
                dateSpinner.setValue(java.util.Date.from(existingSchedule.getShowTime().atZone(ZoneId.systemDefault()).toInstant()));
            }
            totalSeatsSpinner.setValue(existingSchedule.getTotalSeats());
        }

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Film:"), gbc);
        gbc.gridx = 1; formPanel.add(movieCombo, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Studio:"), gbc);
        gbc.gridx = 1; formPanel.add(studioCombo, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Waktu Tayang:"), gbc);
        gbc.gridx = 1; formPanel.add(dateSpinner, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Total Kursi:"), gbc);
        gbc.gridx = 1; formPanel.add(totalSeatsSpinner, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2; formPanel.add(warningLabel, gbc);

        JPanel buttonPanel = new JPanel();
        JButton btnSave = new JButton("Simpan");
        btnSave.setBackground(new Color(70, 130, 200));
        btnSave.setForeground(Color.WHITE);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> {
            String selectedMovie = (String) movieCombo.getSelectedItem();
            if (selectedMovie.startsWith("🔴 ")) {
                selectedMovie = selectedMovie.substring(3).replace(" (Tidak Aktif)", "");
            }
            Movie movie = findMovieByTitle(selectedMovie);

            if (movie == null) {
                JOptionPane.showMessageDialog(dialog, "Film tidak ditemukan!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!movie.isActive()) {
                int confirm = JOptionPane.showConfirmDialog(dialog,
                        "Film '" + movie.getTitle() + "' sedang dalam status TIDAK AKTIF.\n" +
                                "Jadwal tetap bisa dibuat, tetapi film tidak akan muncul di customer.\n\n" +
                                "Lanjutkan?",
                        "Film Tidak Aktif",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            Schedule schedule = existingSchedule != null ? existingSchedule : new Schedule();
            schedule.setMovieId(movie.getId());
            schedule.setMovie(movie);
            schedule.setStudio((String) studioCombo.getSelectedItem());

            java.util.Date date = (java.util.Date) dateSpinner.getValue();
            LocalDateTime showTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
            schedule.setShowTime(showTime);
            schedule.setTotalSeats((int) totalSeatsSpinner.getValue());

            boolean success;
            if (existingSchedule != null) {
                success = updateSchedule(schedule);
            } else {
                success = schedule.save();
            }

            if (success) {
                JOptionPane.showMessageDialog(dialog, "Jadwal berhasil disimpan!");
                dialog.dispose();
                loadSchedules();
            } else {
                JOptionPane.showMessageDialog(dialog, "Gagal menyimpan jadwal!", "Error", JOptionPane.ERROR_MESSAGE);
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

    private Movie findMovieByTitle(String title) {
        List<Movie> movies = getAllMovies();
        for (Movie movie : movies) {
            if (movie.getTitle().equalsIgnoreCase(title)) {
                return movie;
            }
        }
        return null;
    }

    private boolean updateSchedule(Schedule schedule) {
        String sql = "UPDATE schedules SET studio = ?, show_time = ?, total_seats = ? WHERE id = ?";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, schedule.getStudio());
            pstmt.setTimestamp(2, Timestamp.valueOf(schedule.getShowTime()));
            pstmt.setInt(3, schedule.getTotalSeats());
            pstmt.setInt(4, schedule.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void deleteSchedule() {
        int selectedRow = scheduleTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih jadwal yang akan dihapus terlebih dahulu!");
            return;
        }

        int scheduleId = (int) tableModel.getValueAt(selectedRow, 0);
        String movieTitle = (String) tableModel.getValueAt(selectedRow, 1);
        String studio = (String) tableModel.getValueAt(selectedRow, 2);
        String showTime = (String) tableModel.getValueAt(selectedRow, 3);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Hapus jadwal berikut?\n\n" +
                        "Film: " + movieTitle + "\n" +
                        "Studio: " + studio + "\n" +
                        "Waktu: " + showTime + "\n\n" +
                        "PERINGATAN: Jadwal yang dihapus tidak dapat dikembalikan!",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (hasBookings(scheduleId)) {
                int confirmBooking = JOptionPane.showConfirmDialog(this,
                        "Jadwal ini memiliki data booking tiket.\n" +
                                "Data booking akan ikut terhapus.\n\n" +
                                "Tetap hapus?",
                        "Peringatan - Ada Booking",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (confirmBooking != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            String sql = "DELETE FROM schedules WHERE id = ?";
            try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
                pstmt.setInt(1, scheduleId);
                int result = pstmt.executeUpdate();

                if (result > 0) {
                    JOptionPane.showMessageDialog(this, "Jadwal berhasil dihapus!");
                    loadSchedules();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal menghapus jadwal!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean hasBookings(int scheduleId) {
        String sql = "SELECT COUNT(*) as total FROM bookings WHERE schedule_id = ?";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, scheduleId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total") > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Custom cell renderer untuk status film
    class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected && column == 6 && value != null) {
                String status = value.toString();
                if (status.contains("🟢")) {
                    c.setForeground(new Color(0, 150, 0));
                } else if (status.contains("🔴")) {
                    c.setForeground(Color.RED);
                } else {
                    c.setForeground(Color.BLACK);
                }
            } else if (!isSelected) {
                c.setForeground(Color.BLACK);
            }

            return c;
        }
    }
}