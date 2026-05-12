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
        setSize(500, 350);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Info Film"));
        infoPanel.add(new JLabel("Judul: " + movie.getTitle()));
        infoPanel.add(new JLabel("Genre: " + movie.getGenre()));
        infoPanel.add(new JLabel("Harga Tiket: Rp " + movie.getTicketPrice()));

        listModel = new DefaultListModel<>();
        scheduleList = new JList<>(listModel);
        scheduleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(scheduleList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Jadwal Tayang"));

        JPanel buttonPanel = new JPanel();
        JButton btnSelect = new JButton("Pilih Jadwal Ini");
        btnSelect.setBackground(new Color(70, 130, 200));
        btnSelect.setForeground(Color.WHITE);
        btnSelect.addActionListener(e -> selectSchedule());

        JButton btnCancel = new JButton("Batal");
        btnCancel.addActionListener(e -> dispose());

        buttonPanel.add(btnSelect);
        buttonPanel.add(btnCancel);

        add(infoPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
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