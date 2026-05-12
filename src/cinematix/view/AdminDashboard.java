package cinematix.view;

import cinematix.model.Admin;
import cinematix.model.Movie;
import cinematix.model.Booking;
import cinematix.model.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class AdminDashboard extends JFrame {
    private Admin admin;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JPanel sidebarPanel;
    private JLabel totalMoviesLabel;
    private JLabel totalCustomersLabel;
    private JLabel todayRevenueLabel;

    public AdminDashboard(Admin admin) {
        this.admin = admin;

        setTitle("CinemaTix - Admin Dashboard");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        updateStats();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = createHeaderPanel();

        // Sidebar Panel
        sidebarPanel = createSidebarPanel();

        // Main Panel with CardLayout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Add panels to mainPanel
        mainPanel.add(createWelcomePanel(), "WELCOME");
        mainPanel.add(new ManageMoviePanel(this), "MANAGE_MOVIE");
        mainPanel.add(new ManageSchedulePanel(this), "MANAGE_SCHEDULE");
        mainPanel.add(new ReportPanel(this), "REPORT");
        mainPanel.add(new ManageCustomerPanel(this), "MANAGE_CUSTOMER");

        add(headerPanel, BorderLayout.NORTH);
        add(sidebarPanel, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);

        // Show welcome panel initially
        cardLayout.show(mainPanel, "WELCOME");
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(25, 25, 112));
        headerPanel.setPreferredSize(new Dimension(1200, 60));
        headerPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("CinemaTix - Admin Panel");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        JLabel adminLabel = new JLabel("Welcome, " + admin.getFullName() + " (Admin)");
        adminLabel.setForeground(Color.WHITE);
        adminLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(adminLabel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(45, 45, 80));
        sidebar.setPreferredSize(new Dimension(220, 700));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        // Menu items
        String[][] menuItems = {
                {"🏠 Dashboard", "WELCOME"},
                {"🎬 Kelola Film", "MANAGE_MOVIE"},
                {"📅 Kelola Jadwal", "MANAGE_SCHEDULE"},
                {"👥 Kelola Customer", "MANAGE_CUSTOMER"},
                {"📊 Laporan Penjualan", "REPORT"},
                {"🚪 Logout", "LOGOUT"}
        };

        for (String[] item : menuItems) {
            JButton menuButton = createMenuButton(item[0], item[1]);
            sidebar.add(menuButton);
            sidebar.add(Box.createVerticalStrut(5));
        }

        sidebar.add(Box.createVerticalGlue());

        return sidebar;
    }

    private JButton createMenuButton(String text, String panelName) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(45, 45, 80));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (panelName.equals("LOGOUT")) {
            button.addActionListener(e -> logout());
        } else {
            button.addActionListener(e -> {
                cardLayout.show(mainPanel, panelName);
                highlightMenuButton(button);
                if (panelName.equals("WELCOME")) {
                    updateStats();
                }
            });
        }

        return button;
    }

    private void highlightMenuButton(JButton activeButton) {
        for (Component comp : sidebarPanel.getComponents()) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                btn.setBackground(new Color(45, 45, 80));
                btn.setForeground(Color.WHITE);
            }
        }
        activeButton.setBackground(new Color(70, 130, 200));
        activeButton.setForeground(Color.WHITE);
    }

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // Welcome Label
        JLabel welcomeLabel = new JLabel("Selamat Datang di Admin Panel, " + admin.getFullName() + "!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Kelola film, jadwal, dan lihat laporan penjualan di sini");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Stats Panel - 3 Kotak Statistik
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 20));
        statsPanel.setMaximumSize(new Dimension(900, 140));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));

        // Kotak 1: Total Film
        JPanel movieCard = createStatCard("🎬", "Total Film");
        JLabel movieValueLabel = (JLabel) movieCard.getClientProperty("valueLabel");
        totalMoviesLabel = movieValueLabel;

        // Kotak 2: Total Customer
        JPanel customerCard = createStatCard("👥", "Total Customer");
        JLabel customerValueLabel = (JLabel) customerCard.getClientProperty("valueLabel");
        totalCustomersLabel = customerValueLabel;

        // Kotak 3: Pendapatan Hari Ini
        JPanel revenueCard = createStatCard("💰", "Pendapatan Hari Ini");
        JLabel revenueValueLabel = (JLabel) revenueCard.getClientProperty("valueLabel");
        todayRevenueLabel = revenueValueLabel;

        statsPanel.add(movieCard);
        statsPanel.add(customerCard);
        statsPanel.add(revenueCard);

        // Quick Action Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        actionPanel.setBorder(BorderFactory.createTitledBorder("Aksi Cepat"));

        JButton btnAddMovie = new JButton("+ Tambah Film");
        btnAddMovie.setBackground(new Color(70, 130, 200));
        btnAddMovie.setForeground(Color.WHITE);
        btnAddMovie.addActionListener(e -> {
            cardLayout.show(mainPanel, "MANAGE_MOVIE");
        });

        JButton btnAddSchedule = new JButton("📅 Tambah Jadwal");
        btnAddSchedule.setBackground(new Color(100, 150, 100));
        btnAddSchedule.setForeground(Color.WHITE);
        btnAddSchedule.addActionListener(e -> cardLayout.show(mainPanel, "MANAGE_SCHEDULE"));

        JButton btnViewReport = new JButton("📊 Lihat Laporan");
        btnViewReport.setBackground(new Color(200, 120, 50));
        btnViewReport.setForeground(Color.WHITE);
        btnViewReport.addActionListener(e -> cardLayout.show(mainPanel, "REPORT"));

        actionPanel.add(btnAddMovie);
        actionPanel.add(btnAddSchedule);
        actionPanel.add(btnViewReport);

        centerPanel.add(welcomeLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(subtitleLabel);
        centerPanel.add(statsPanel);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(actionPanel);

        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatCard(String icon, String title) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(250, 100));

        // Icon
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 30));

        // Value Panel
        JPanel valuePanel = new JPanel();
        valuePanel.setLayout(new BoxLayout(valuePanel, BoxLayout.Y_AXIS));
        valuePanel.setOpaque(false);

        JLabel valueLabel = new JLabel("0");
        valueLabel.setFont(new Font("Arial", Font.BOLD, 28));
        valueLabel.setForeground(new Color(70, 130, 200));
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        titleLabel.setForeground(Color.GRAY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        valuePanel.add(valueLabel);
        valuePanel.add(Box.createVerticalStrut(5));
        valuePanel.add(titleLabel);

        card.add(iconLabel, BorderLayout.WEST);
        card.add(valuePanel, BorderLayout.CENTER);

        // Simpan reference ke valueLabel
        card.putClientProperty("valueLabel", valueLabel);

        return card;
    }

    private void updateStats() {
        try {
            // Update Total Film
            int totalMovies = Movie.getAll().size();
            if (totalMoviesLabel != null) {
                totalMoviesLabel.setText(String.valueOf(totalMovies));
            }

            // Update Total Customer
            int totalCustomers = getTotalCustomers();
            if (totalCustomersLabel != null) {
                totalCustomersLabel.setText(String.valueOf(totalCustomers));
            }

            // Update Pendapatan Hari Ini
            double todayRevenue = getTodayRevenue();
            if (todayRevenueLabel != null) {
                todayRevenueLabel.setText(String.format("Rp %,.0f", todayRevenue));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getTotalCustomers() {
        String sql = "SELECT COUNT(*) as total FROM users WHERE role = 'customer'";
        try (Statement stmt = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private double getTodayRevenue() {
        LocalDate today = LocalDate.now();
        String sql = "SELECT COALESCE(SUM(total_price), 0) as total FROM bookings WHERE status = 'CONFIRMED' AND DATE(payment_time) = ?";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(today));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
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

    public void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
        if (panelName.equals("WELCOME")) {
            updateStats();
        }
    }
}