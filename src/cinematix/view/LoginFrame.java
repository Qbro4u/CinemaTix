package cinematix.view;

import cinematix.model.Admin;
import cinematix.model.Customer;
import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton btnLogin, btnRegister;

    public LoginFrame() {
        setTitle("CinemaTix - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(25, 25, 112));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("CinemaTix");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(Color.WHITE);
        mainPanel.add(userLabel, gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(15);
        mainPanel.add(usernameField, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(Color.WHITE);
        mainPanel.add(passLabel, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        mainPanel.add(passwordField, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        btnLogin = new JButton("Login");
        btnLogin.setBackground(new Color(70, 130, 200));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.addActionListener(e -> doLogin());
        mainPanel.add(btnLogin, gbc);

        gbc.gridx = 1;
        btnRegister = new JButton("Register");
        btnRegister.addActionListener(e -> openRegisterDialog());
        mainPanel.add(btnRegister, gbc);

        add(mainPanel);
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Harap isi username dan password!");
            return;
        }

        Customer customer = new Customer();
        if (customer.login(username, password)) {
            JOptionPane.showMessageDialog(this, "Login berhasil! Selamat datang, " + customer.getFullName());
            new CustomerDashboard(customer).setVisible(true);
            dispose();
            return;
        }

        Admin admin = new Admin();
        if (admin.login(username, password)) {
            JOptionPane.showMessageDialog(this, "Login berhasil! Selamat datang, Admin " + admin.getFullName());
            new AdminDashboard(admin).setVisible(true);
            dispose();
            return;
        }

        JOptionPane.showMessageDialog(this, "Username atau password salah!");
    }

    private void openRegisterDialog() {
        JDialog registerDialog = new JDialog(this, "Register Customer", true);
        registerDialog.setSize(400, 450);
        registerDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField fullNameField = new JTextField(15);
        JTextField usernameFieldReg = new JTextField(15);
        JPasswordField passwordFieldReg = new JPasswordField(15);
        JTextField emailField = new JTextField(15);
        JTextField phoneField = new JTextField(15);

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Nama Lengkap:"), gbc);
        gbc.gridx = 1; panel.add(fullNameField, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; panel.add(usernameFieldReg, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; panel.add(passwordFieldReg, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; panel.add(emailField, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("No. HP:"), gbc);
        gbc.gridx = 1; panel.add(phoneField, gbc);

        y++; JButton btnSubmit = new JButton("Daftar");
        btnSubmit.addActionListener(e -> {
            Customer newCustomer = new Customer();
            newCustomer.setFullName(fullNameField.getText());
            newCustomer.setUsername(usernameFieldReg.getText());
            newCustomer.setPassword(new String(passwordFieldReg.getPassword()));
            newCustomer.setEmail(emailField.getText());
            newCustomer.setPhone(phoneField.getText());

            if (newCustomer.register()) {
                JOptionPane.showMessageDialog(registerDialog, "Registrasi berhasil! Silakan login.");
                registerDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(registerDialog, "Registrasi gagal! Username mungkin sudah terdaftar.");
            }
        });

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        panel.add(btnSubmit, gbc);

        registerDialog.add(panel);
        registerDialog.setVisible(true);
    }
}