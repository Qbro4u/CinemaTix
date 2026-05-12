package cinematix.view;

import cinematix.model.Movie;
import cinematix.model.Schedule;
import cinematix.model.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public class ManageSchedulePanel extends JPanel {
    private JFrame parent;
    private JTable scheduleTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> movieFilter;

    public ManageSchedulePanel(JFrame parent) {
        this.parent = parent;
        initComponents();
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
        filterPanel.add(new JLabel("Filter Film:"));
        movieFilter = new JComboBox<>();
        movieFilter.addItem("Semua Film");
        loadMovieFilter();
        movieFilter.addActionListener(e -> loadSchedules());
        filterPanel.add(movieFilter);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnAdd = new JButton("+ Tambah Jadwal");
        btnAdd.setBackground(new Color(70, 130, 200));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.addActionListener(e -> openScheduleDialog(null));

        JButton btnDelete = new JButton("🗑 Hapus");
        btnDelete.setBackground(Color.RED);
        btnDelete.setForeground(Color.WHITE);
        btnDelete.addActionListener(e -> deleteSchedule());

        JButton btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadSchedules());

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnRefresh);

        // Table
        String[] columns = {"ID", "Film", "Studio", "Tanggal & Jam", "Total Kursi", "Tersedia"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        scheduleTable = new JTable(tableModel);
        scheduleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scheduleTable.setRowHeight(25);

        // Set column widths
        scheduleTable.getColumnModel().getColumn(0).setMaxWidth(50);
        scheduleTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        scheduleTable.getColumnModel().getColumn(5).setPreferredWidth(80);

        JScrollPane scrollPane = new JScrollPane(scheduleTable);

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(filterPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadMovieFilter() {
        List<Movie> movies = Movie.getAll();
        for (Movie movie : movies) {
            movieFilter.addItem(movie.getTitle());
        }
    }

    private void loadSchedules() {
        tableModel.setRowCount(0);
        String selectedMovie = (String) movieFilter.getSelectedItem();

        List<Movie> movies = Movie.getAll();
        for (Movie movie : movies) {
            if (!selectedMovie.equals("Semua Film") && !movie.getTitle().equals(selectedMovie)) {
                continue;
            }

            List<Schedule> schedules = Schedule.getSchedulesByMovie(movie.getId());
            for (Schedule schedule : schedules) {
                tableModel.addRow(new Object[]{
                        schedule.getId(),
                        movie.getTitle(),
                        schedule.getStudio(),
                        schedule.getFormattedShowTime(),
                        schedule.getTotalSeats(),
                        schedule.getAvailableSeatsCount() + "/" + schedule.getTotalSeats()
                });
            }
        }
    }

    private void openScheduleDialog(Schedule existingSchedule) {
        JDialog dialog = new JDialog(parent, existingSchedule == null ? "Tambah Jadwal" : "Edit Jadwal", true);
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Form fields
        JComboBox<String> movieCombo = new JComboBox<>();
        List<Movie> movies = Movie.getAll();
        for (Movie movie : movies) {
            movieCombo.addItem(movie.getTitle());
        }

        JComboBox<String> studioCombo = new JComboBox<>(new String[]{"Studio A", "Studio B", "Studio C", "Studio D"});

        // Date and Time Picker
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy HH:mm");
        dateSpinner.setEditor(dateEditor);

        JSpinner totalSeatsSpinner = new JSpinner(new SpinnerNumberModel(80, 30, 200, 10));

        if (existingSchedule != null) {
            movieCombo.setSelectedItem(existingSchedule.getMovie().getTitle());
            movieCombo.setEnabled(false); // Tidak bisa edit film jika sudah ada jadwal
            studioCombo.setSelectedItem(existingSchedule.getStudio());
            dateSpinner.setValue(java.util.Date.from(existingSchedule.getShowTime().atZone(ZoneId.systemDefault()).toInstant()));
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

        // Button Panel
        JPanel buttonPanel = new JPanel();
        JButton btnSave = new JButton("Simpan");
        btnSave.setBackground(new Color(70, 130, 200));
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> {
            String selectedMovie = (String) movieCombo.getSelectedItem();
            Movie movie = findMovieByTitle(selectedMovie);

            if (movie == null) {
                JOptionPane.showMessageDialog(dialog, "Film tidak ditemukan!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
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
        btnCancel.addActionListener(e -> dialog.dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private Movie findMovieByTitle(String title) {
        List<Movie> movies = Movie.getAll();
        for (Movie movie : movies) {
            if (movie.getTitle().equals(title)) {
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

    // ============================================================
    // PERBAIKAN: Method DELETE Jadwal
    // ============================================================
    private void deleteSchedule() {
        int selectedRow = scheduleTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Pilih jadwal yang akan dihapus terlebih dahulu!",
                    "Peringatan",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Ambil ID jadwal dari tabel
        int scheduleId = (int) tableModel.getValueAt(selectedRow, 0);
        String movieTitle = (String) tableModel.getValueAt(selectedRow, 1);
        String studio = (String) tableModel.getValueAt(selectedRow, 2);
        String showTime = (String) tableModel.getValueAt(selectedRow, 3);

        // Konfirmasi penghapusan
        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin menghapus jadwal berikut?\n\n" +
                        "Film: " + movieTitle + "\n" +
                        "Studio: " + studio + "\n" +
                        "Waktu: " + showTime + "\n\n" +
                        "PERINGATAN: Data booking yang terkait dengan jadwal ini juga akan terhapus!",
                "Konfirmasi Hapus Jadwal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // Cek apakah ada booking yang terkait dengan jadwal ini
            if (hasBookings(scheduleId)) {
                int confirmBooking = JOptionPane.showConfirmDialog(this,
                        "Jadwal ini memiliki booking tiket yang sudah terdaftar.\n" +
                                "Data booking tersebut juga akan ikut terhapus.\n\n" +
                                "Apakah Anda tetap ingin melanjutkan?",
                        "Peringatan - Ada Data Booking",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (confirmBooking != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            // Lakukan penghapusan
            boolean success = deleteScheduleFromDatabase(scheduleId);

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Jadwal berhasil dihapus!",
                        "Sukses",
                        JOptionPane.INFORMATION_MESSAGE);
                loadSchedules(); // Refresh tabel
            } else {
                JOptionPane.showMessageDialog(this,
                        "Gagal menghapus jadwal!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
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

    private boolean deleteScheduleFromDatabase(int scheduleId) {
        Connection conn = null;
        PreparedStatement pstmtBooking = null;
        PreparedStatement pstmtSchedule = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Mulai transaksi

            // Hapus booking yang terkait terlebih dahulu (karena foreign key)
            String sqlBooking = "DELETE FROM bookings WHERE schedule_id = ?";
            pstmtBooking = conn.prepareStatement(sqlBooking);
            pstmtBooking.setInt(1, scheduleId);
            int deletedBookings = pstmtBooking.executeUpdate();

            if (deletedBookings > 0) {
                System.out.println("Menghapus " + deletedBookings + " booking yang terkait");
            }

            // Hapus jadwal
            String sqlSchedule = "DELETE FROM schedules WHERE id = ?";
            pstmtSchedule = conn.prepareStatement(sqlSchedule);
            pstmtSchedule.setInt(1, scheduleId);
            int result = pstmtSchedule.executeUpdate();

            conn.commit(); // Commit transaksi
            return result > 0;

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback(); // Rollback jika ada error
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmtBooking != null) pstmtBooking.close();
                if (pstmtSchedule != null) pstmtSchedule.close();
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}