package reservation;

import javax.swing.*;
import java.awt.*;

public class MainMenuFrame extends JFrame {
    private int userId;
    private String username;

    public MainMenuFrame(int userId, String username) {
        this.userId = userId;
        this.username = username;
        setTitle("Online Reservation - Main Menu");
        setSize(400,200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        init();
        setVisible(true);
    }

    private void init() {
        JLabel lbl = new JLabel("Welcome, " + username);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        JButton btnReserve = new JButton("Make Reservation");
        JButton btnCancel = new JButton("Cancel Reservation");
        JButton btnLogout = new JButton("Logout");

        btnReserve.addActionListener(e -> new ReservationFrame(userId));
        btnCancel.addActionListener(e -> new CancellationFrame());
        btnLogout.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        JPanel p = new JPanel(new GridLayout(3,1,10,10));
        p.add(btnReserve);
        p.add(btnCancel);
        p.add(btnLogout);

        getContentPane().setLayout(new BorderLayout(10,10));
        getContentPane().add(lbl, BorderLayout.NORTH);
        getContentPane().add(p, BorderLayout.CENTER);
    }
}
