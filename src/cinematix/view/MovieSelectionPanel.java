package cinematix.view;

import cinematix.controller.BookingController;
import cinematix.model.Customer;
import cinematix.model.Movie;
import cinematix.model.Schedule;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MovieSelectionPanel extends JPanel {
    private Customer customer;
    private BookingController bookingController;
    private JTable movieTable;
    private JComboBox<String> genreFilter;
    private DefaultTableModel tableModel;
    private JButton btnSelectMovie;

    public MovieSelectionPanel(Customer customer, BookingController bookingController) {
        this.customer = customer;
        this.bookingController = bookingController;
        initComponents();
        loadMovies();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter Genre:"));
        genreFilter = new JComboBox<>(new String[]{"Semua", "Action", "Drama", "Comedy", "Horror"});
        genreFilter.addActionListener(e -> loadMovies());
        filterPanel.add(genreFilter);

        // Refresh Button
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadMovies());
        filterPanel.add(btnRefresh);

        // Table
        tableModel = new DefaultTableModel(new String[]{"ID", "Judul Film", "Genre", "Durasi", "Harga", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        movieTable = new JTable(tableModel);
        movieTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        movieTable.getColumnModel().getColumn(0).setMaxWidth(50);
        movieTable.getColumnModel().getColumn(4).setMaxWidth(100);

        JScrollPane scrollPane = new JScrollPane(movieTable);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        btnSelectMovie = new JButton("Pilih Film & Jadwal");
        btnSelectMovie.setBackground(new Color(70, 130, 200));
        btnSelectMovie.setForeground(Color.WHITE);
        btnSelectMovie.setFont(new Font("Arial", Font.BOLD, 14));
        btnSelectMovie.setPreferredSize(new Dimension(200, 40));
        btnSelectMovie.addActionListener(e -> selectMovie());
        buttonPanel.add(btnSelectMovie);

        add(filterPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadMovies() {
        tableModel.setRowCount(0);
        List<Movie> movies = Movie.getAll();
        String selectedGenre = (String) genreFilter.getSelectedItem();

        for (Movie movie : movies) {
            if (!selectedGenre.equals("Semua") && !movie.getGenre().equals(selectedGenre)) {
                continue;
            }
            tableModel.addRow(new Object[]{
                    movie.getId(),
                    movie.getTitle(),
                    movie.getGenre(),
                    movie.getDuration() + " menit",
                    "Rp " + movie.getTicketPrice(),
                    movie.isActive() ? "Tayang" : "Tidak Tayang"
            });
        }
    }

    private void selectMovie() {
        int selectedRow = movieTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih film terlebih dahulu!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int movieId = (int) tableModel.getValueAt(selectedRow, 0);
        Movie selectedMovie = Movie.findById(movieId);

        // Buka dialog pilih jadwal
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        ScheduleSelectionDialog dialog = new ScheduleSelectionDialog(parentFrame, selectedMovie);
        dialog.setVisible(true);

        if (dialog.isSelected()) {
            Schedule selectedSchedule = dialog.getSelectedSchedule();
            bookingController.startBooking(customer, selectedMovie, selectedSchedule, this);
        }
    }
    private JFrame getParentFrame() {
        return (JFrame) SwingUtilities.getWindowAncestor(this);
    }
}