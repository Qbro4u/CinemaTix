package cinematix.view;

import cinematix.model.Movie;
import cinematix.model.Schedule;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ScheduleSelectionDialog extends JDialog {
    private Movie movie;
    private JList<String> scheduleList;
    private DefaultListModel<String> listModel;
    private Schedule selectedSchedule;
    private boolean selected = false;

    public ScheduleSelectionDialog(JFrame parent, Movie movie) {
        super(parent, "Pilih Jadwal - " + movie.getTitle(), true);
        this.movie = movie;
        initComponents();
        loadSchedules();
        setSize(700, 500);  // Diperbesar untuk menampung cast & sutradara
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // ============================================================
        // INFO PANEL - Diperbesar dengan Cast & Sutradara
        // ============================================================
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Info Film"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Baris 1: Judul
        gbc.gridx = 0; gbc.gridy = 0;
        infoPanel.add(new JLabel("Judul:"), gbc);
        gbc.gridx = 1;
        JLabel titleLabel = new JLabel(movie.getTitle());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        infoPanel.add(titleLabel, gbc);

        // Baris 2: Genre
        gbc.gridx = 0; gbc.gridy = 1;
        infoPanel.add(new JLabel("Genre:"), gbc);
        gbc.gridx = 1;
        infoPanel.add(new JLabel(movie.getGenre()), gbc);

        // Baris 3: Durasi
        gbc.gridx = 0; gbc.gridy = 2;
        infoPanel.add(new JLabel("Durasi:"), gbc);
        gbc.gridx = 1;
        infoPanel.add(new JLabel(movie.getDuration() + " menit"), gbc);

        // Baris 4: Harga Tiket
        gbc.gridx = 0; gbc.gridy = 3;
        infoPanel.add(new JLabel("Harga Tiket:"), gbc);
        gbc.gridx = 1;
        infoPanel.add(new JLabel("Rp " + String.format("%,.0f", movie.getTicketPrice())), gbc);

        // ============================================================
        // TAMBAHAN: Cast dan Sutradara
        // ============================================================

        // Baris 5: Sutradara
        gbc.gridx = 0; gbc.gridy = 4;
        infoPanel.add(new JLabel("Sutradara:"), gbc);
        gbc.gridx = 1;
        String director = (movie.getDirector() != null && !movie.getDirector().isEmpty())
                ? movie.getDirector() : "-";
        infoPanel.add(new JLabel(director), gbc);

        // Baris 6: Cast/Pemain (membutuhkan lebih banyak ruang)
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        infoPanel.add(new JLabel("Pemain:"), gbc);
        gbc.gridx = 1;

        String castText = (movie.getCast() != null && !movie.getCast().isEmpty())
                ? movie.getCast() : "-";

        // Gunakan JTextArea untuk cast yang panjang
        JTextArea castArea = new JTextArea(castText);
        castArea.setEditable(false);
        castArea.setLineWrap(true);
        castArea.setWrapStyleWord(true);
        castArea.setBackground(infoPanel.getBackground());
        castArea.setFont(new Font("Arial", Font.PLAIN, 11));

        JScrollPane castScroll = new JScrollPane(castArea);
        castScroll.setPreferredSize(new Dimension(300, 60));
        castScroll.setBorder(BorderFactory.createEmptyBorder());
        infoPanel.add(castScroll, gbc);

        // ============================================================
        // JADWAL PANEL
        // ============================================================
        listModel = new DefaultListModel<>();
        scheduleList = new JList<>(listModel);
        scheduleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scheduleList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(scheduleList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Jadwal Tayang"));
        scrollPane.setPreferredSize(new Dimension(450, 200));

        // ============================================================
        // BUTTON PANEL
        // ============================================================
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnSelect = new JButton("Pilih Jadwal Ini");
        btnSelect.setBackground(new Color(70, 130, 200));
        btnSelect.setForeground(Color.WHITE);
        btnSelect.setFont(new Font("Arial", Font.BOLD, 12));
        btnSelect.addActionListener(e -> selectSchedule());

        JButton btnCancel = new JButton("Batal");
        btnCancel.addActionListener(e -> dispose());

        buttonPanel.add(btnSelect);
        buttonPanel.add(btnCancel);

        // ============================================================
        // LAYOUT ASSEMBLY
        // ============================================================
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(infoPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadSchedules() {
        listModel.clear();
        List<Schedule> schedules = Schedule.getSchedulesByMovie(movie.getId());

        if (schedules.isEmpty()) {
            listModel.addElement("Tidak ada jadwal tayang untuk film ini");
            return;
        }

        for (Schedule schedule : schedules) {
            String info = String.format("%s | Studio: %s | Kursi tersedia: %d/%d",
                    schedule.getFormattedShowTime(),
                    schedule.getStudio(),
                    schedule.getAvailableSeatsCount(),
                    schedule.getTotalSeats()
            );
            listModel.addElement(info);
        }
    }

    private void selectSchedule() {
        int selectedIndex = scheduleList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Pilih jadwal terlebih dahulu!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<Schedule> schedules = Schedule.getSchedulesByMovie(movie.getId());
        if (selectedIndex < schedules.size()) {
            selectedSchedule = schedules.get(selectedIndex);
            selected = true;
            dispose();
        }
    }

    public boolean isSelected() {
        return selected;
    }

    public Schedule getSelectedSchedule() {
        return selectedSchedule;
    }
}