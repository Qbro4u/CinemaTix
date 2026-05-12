package cinematix.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ManageCustomerPanel extends JPanel {
    private JFrame parent;
    private JTable customerTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public ManageCustomerPanel(JFrame parent) {
        this.parent = parent;
        initComponents();
        loadCustomers();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel titleLabel = new JLabel("Manajemen Customer");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Cari:"));
        searchField = new JTextField(20);
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                searchCustomers();
            }
        });
        searchPanel.add(searchField);

        JButton btnSearch = new JButton("Cari");
        btnSearch.addActionListener(e -> searchCustomers());
        searchPanel.add(btnSearch);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadCustomers());
        buttonPanel.add(btnRefresh);

        // Table
        String[] columns = {"ID", "Username", "Nama Lengkap", "Email", "No. HP", "Member Sejak"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        customerTable = new JTable(tableModel);
        customerTable.setRowHeight(25);
        customerTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(customerTable);

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadCustomers() {
        tableModel.setRowCount(0);

        String sql = "SELECT id, username, full_name, email, phone, created_at FROM users WHERE role = 'customer' ORDER BY created_at DESC";

        try (Statement stmt = cinematix.model.DatabaseConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("full_name"),
                        rs.getString("email") != null ? rs.getString("email") : "-",
                        rs.getString("phone") != null ? rs.getString("phone") : "-",
                        rs.getTimestamp("created_at") != null ?
                                rs.getTimestamp("created_at").toLocalDateTime().format(
                                        java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "-"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data customer: " + e.getMessage());
        }
    }

    private void searchCustomers() {
        String keyword = searchField.getText().toLowerCase();
        tableModel.setRowCount(0);

        String sql = "SELECT id, username, full_name, email, phone, created_at FROM users WHERE role = 'customer' AND (username LIKE ? OR full_name LIKE ? OR email LIKE ?) ORDER BY created_at DESC";

        try (PreparedStatement pstmt = cinematix.model.DatabaseConnection.getConnection().prepareStatement(sql)) {
            String searchParam = "%" + keyword + "%";
            pstmt.setString(1, searchParam);
            pstmt.setString(2, searchParam);
            pstmt.setString(3, searchParam);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("full_name"),
                        rs.getString("email") != null ? rs.getString("email") : "-",
                        rs.getString("phone") != null ? rs.getString("phone") : "-",
                        rs.getTimestamp("created_at") != null ?
                                rs.getTimestamp("created_at").toLocalDateTime().format(
                                        java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "-"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}