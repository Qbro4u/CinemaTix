package cinematix.view;

import cinematix.controller.BookingController;
import cinematix.model.Customer;
import javax.swing.*;
import java.awt.*;

public class CustomerDashboard extends JFrame {
    private Customer customer;
    private BookingController bookingController;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public CustomerDashboard(Customer customer) {
        this.customer = customer;
        this.bookingController = new BookingController();

        setTitle("CinemaTix - Customer Dashboard");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(25, 25, 112));
        headerPanel.setPreferredSize(new Dimension(900, 70));

        JLabel titleLabel = new JLabel("CinemaTix - Customer Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        // ============================================================
        // 🔽 MEMBUAT MENU BAR (DI SINI TEMPATNYA) 🔽
        // ============================================================

        JMenuBar menuBar = new JMenuBar();

        // ===== MENU FILE =====
        JMenu menuFile = new JMenu("File");
        JMenuItem menuLogout = new JMenuItem("Logout");
        menuLogout.addActionListener(e -> logout());
        menuFile.add(menuLogout);

        // ===== MENU BOOKING =====
        JMenu menuBooking = new JMenu("Booking");

        JMenuItem menuDashboard = new JMenuItem("Dashboard");
        menuDashboard.addActionListener(e -> showDashboard());
        menuBooking.add(menuDashboard);

        JMenuItem menuNewBooking = new JMenuItem("Pesan Tiket Baru");
        menuNewBooking.addActionListener(e -> showMovieSelection());
        menuBooking.add(menuNewBooking);

        JMenuItem menuHistory = new JMenuItem("Riwayat Pemesanan");
        menuHistory.addActionListener(e -> showHistory());
        menuBooking.add(menuHistory);

        // ===== MENU HELP =====
        JMenu menuHelp = new JMenu("Help");
        JMenuItem menuAbout = new JMenuItem("About");
        menuAbout.addActionListener(e -> showAbout());
        menuHelp.add(menuAbout);

        // Add menus to menuBar
        menuBar.add(menuFile);
        menuBar.add(menuBooking);
        menuBar.add(menuHelp);

        // ============================================================
        // 🔼 SAMPAI SINI PEMBUATAN MENU BAR 🔼
        // ============================================================

        setJMenuBar(menuBar);

        // Main Panel with CardLayout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Welcome Panel
        mainPanel.add(createWelcomePanel(), "WELCOME");

        // Movie Selection Panel
        MovieSelectionPanel moviePanel = new MovieSelectionPanel(customer, bookingController);
        mainPanel.add(moviePanel, "MOVIE_SELECTION");

        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        // Show welcome panel initially
        cardLayout.show(mainPanel, "WELCOME");
    }

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        JLabel welcomeLabel = new JLabel("Selamat Datang, " + customer.getFullName() + "!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Pesan tiket bioskop dengan mudah dan cepat");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(welcomeLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(subtitleLabel);
        centerPanel.add(Box.createVerticalStrut(30));

        // Info Panel
        JPanel infoPanel = new JPanel(new GridLayout(4, 2, 15, 10));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Informasi Akun"));
        infoPanel.setMaximumSize(new Dimension(400, 150));

        infoPanel.add(new JLabel("Username:"));
        infoPanel.add(new JLabel(customer.getUsername()));
        infoPanel.add(new JLabel("Email:"));
        infoPanel.add(new JLabel(customer.getEmail() != null ? customer.getEmail() : "-"));
        infoPanel.add(new JLabel("No. HP:"));
        infoPanel.add(new JLabel(customer.getPhone() != null ? customer.getPhone() : "-"));
        infoPanel.add(new JLabel("Member Sejak:"));
        infoPanel.add(new JLabel(customer.getFormattedCreatedAt()));

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton btnBooking = new JButton("Pesan Tiket Sekarang");
        btnBooking.setBackground(new Color(70, 130, 200));
        btnBooking.setForeground(Color.WHITE);
        btnBooking.setFont(new Font("Arial", Font.BOLD, 14));
        btnBooking.setPreferredSize(new Dimension(200, 45));
        btnBooking.addActionListener(e -> showMovieSelection());

        JButton btnHistory = new JButton("Riwayat Pesanan");
        btnHistory.setPreferredSize(new Dimension(150, 45));
        btnHistory.addActionListener(e -> showHistory());

        buttonPanel.add(btnBooking);
        buttonPanel.add(btnHistory);

        centerPanel.add(infoPanel);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(buttonPanel);

        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    // ============================================================
    // 🔽 METHOD NAVIGASI 🔽
    // ============================================================

    private void showDashboard() {
        mainPanel.removeAll();
        mainPanel.add(createWelcomePanel(), "WELCOME");
        cardLayout.show(mainPanel, "WELCOME");
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void showMovieSelection() {
        mainPanel.removeAll();

        MovieSelectionPanel moviePanel = new MovieSelectionPanel(customer, bookingController);
        mainPanel.add(moviePanel, "MOVIE_SELECTION");
        cardLayout.show(mainPanel, "MOVIE_SELECTION");

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void showHistory() {
        mainPanel.removeAll();

        BookingHistoryPanel historyPanel = new BookingHistoryPanel(customer);
        mainPanel.add(historyPanel, "HISTORY");
        cardLayout.show(mainPanel, "HISTORY");

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // ============================================================
    // 🔼 SAMPAI SINI METHOD NAVIGASI 🔼
    // ============================================================

    private void showAbout() {
        JOptionPane.showMessageDialog(this,
                "CinemaTix - Sistem Pemesanan Tiket Bioskop\n\n" +
                        "Version 1.0\n\n" +
                        "Aplikasi ini dibuat untuk memenuhi:\n" +
                        "- Praktikum Pemrograman Berorientasi Objek\n" +
                        "- Implementasi MVC, OOP, Database, Multithreading\n\n" +
                        "© 2024 CinemaTix",
                "About", JOptionPane.INFORMATION_MESSAGE);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin logout?",
                "Konfirmasi Logout",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}