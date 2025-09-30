package reservation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public LoginFrame() {
        setTitle("Online Reservation - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(360, 200);
        setLocationRelativeTo(null);
        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        JPanel p = new JPanel(new GridLayout(4,1,5,5));
        txtUsername = new JTextField();
        txtPassword = new JPasswordField();
        btnLogin = new JButton("Login");

        p.add(new JLabel("Username:"));
        p.add(txtUsername);
        p.add(new JLabel("Password:"));
        p.add(txtPassword);

        Container c = getContentPane();
        c.setLayout(new BorderLayout(5,5));
        c.add(p, BorderLayout.CENTER);
        c.add(btnLogin, BorderLayout.SOUTH);

        btnLogin.addActionListener(e -> login());
    }

    private void login() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter both username and password.");
            return;
        }

        String sql = "SELECT id, username FROM users WHERE username=? AND password=SHA2(?,256)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user);
            ps.setString(2, pass);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("id");
                    String username = rs.getString("username");
                    JOptionPane.showMessageDialog(this, "Login successful. Welcome " + username + "!");
                    this.dispose();
                    new MainMenuFrame(userId, username);
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid credentials.");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB error: " + ex.getMessage());
        }
    }
}
