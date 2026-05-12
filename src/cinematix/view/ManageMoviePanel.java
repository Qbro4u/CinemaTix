package cinematix.view;

import cinematix.model.Movie;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ManageMoviePanel extends JPanel {
    private JFrame parent;
    private JTable movieTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

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

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnAdd = new JButton("+ Tambah Film");
        btnAdd.setBackground(new Color(70, 130, 200));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.addActionListener(e -> openMovieDialog(null));

        JButton btnEdit = new JButton("✏ Edit");
        btnEdit.addActionListener(e -> editMovie());

        JButton btnDelete = new JButton("🗑 Hapus");
        btnDelete.setBackground(Color.RED);
        btnDelete.setForeground(Color.WHITE);
        btnDelete.addActionListener(e -> deleteMovie());

        JButton btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadMovies());

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnEdit);
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

        // Set column widths
        movieTable.getColumnModel().getColumn(0).setMaxWidth(50);
        movieTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        movieTable.getColumnModel().getColumn(5).setPreferredWidth(80);

        JScrollPane scrollPane = new JScrollPane(movieTable);

        add(titlePanel, BorderLayout.NORTH);
        add(searchPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadMovies() {
        tableModel.setRowCount(0);
        List<Movie> movies = Movie.getAll();

        for (Movie movie : movies) {
            tableModel.addRow(new Object[]{
                    movie.getId(),
                    movie.getTitle(),
                    movie.getGenre(),
                    movie.getDuration() + " menit",
                    "Rp " + String.format("%,.0f", movie.getTicketPrice()),
                    movie.isActive() ? "Aktif" : "Tidak Aktif"
            });
        }
    }

    private void searchMovies() {
        String keyword = searchField.getText().toLowerCase();
        tableModel.setRowCount(0);

        List<Movie> movies = Movie.getAll();
        for (Movie movie : movies) {
            if (movie.getTitle().toLowerCase().contains(keyword) ||
                    movie.getGenre().toLowerCase().contains(keyword)) {
                tableModel.addRow(new Object[]{
                        movie.getId(),
                        movie.getTitle(),
                        movie.getGenre(),
                        movie.getDuration() + " menit",
                        "Rp " + String.format("%,.0f", movie.getTicketPrice()),
                        movie.isActive() ? "Aktif" : "Tidak Aktif"
                });
            }
        }
    }

    private void openMovieDialog(Movie existingMovie) {
        JDialog dialog = new JDialog(parent, existingMovie == null ? "Tambah Film" : "Edit Film", true);
        dialog.setSize(450, 550);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Form fields
        JTextField titleField = new JTextField(20);
        JComboBox<String> genreCombo = new JComboBox<>(new String[]{"Action", "Drama", "Comedy", "Horror", "Sci-Fi", "Romance", "Thriller"});
        JSpinner durationSpinner = new JSpinner(new SpinnerNumberModel(120, 30, 300, 5));
        JTextField directorField = new JTextField(20);
        JTextArea castArea = new JTextArea(3, 20);
        JSpinner priceSpinner = new JSpinner(new SpinnerNumberModel(50000, 10000, 200000, 5000));
        JCheckBox activeCheckBox = new JCheckBox("Aktif", true);

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

        // Button Panel
        JPanel buttonPanel = new JPanel();
        JButton btnSave = new JButton("Simpan");
        btnSave.setBackground(new Color(70, 130, 200));
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> {
            if (titleField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Judul film harus diisi!", "Error", JOptionPane.ERROR_MESSAGE);
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
                success = movie.update();
            } else {
                success = movie.save();
            }

            if (success) {
                JOptionPane.showMessageDialog(dialog, "Data film berhasil disimpan!");
                dialog.dispose();
                loadMovies();
            } else {
                JOptionPane.showMessageDialog(dialog, "Gagal menyimpan data film!", "Error", JOptionPane.ERROR_MESSAGE);
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

    private void editMovie() {
        int selectedRow = movieTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih film yang akan diedit!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int movieId = (int) tableModel.getValueAt(selectedRow, 0);
        Movie movie = Movie.findById(movieId);

        if (movie != null) {
            openMovieDialog(movie);
        }
    }

    private void deleteMovie() {
        int selectedRow = movieTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih film yang akan dihapus!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin menghapus film ini?\nData jadwal terkait juga akan terhapus!",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int movieId = (int) tableModel.getValueAt(selectedRow, 0);
            Movie movie = Movie.findById(movieId);

            if (movie != null && movie.delete()) {
                JOptionPane.showMessageDialog(this, "Film berhasil dihapus!");
                loadMovies();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menghapus film!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}