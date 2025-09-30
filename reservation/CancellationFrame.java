package reservation;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class CancellationFrame extends JFrame {
    private JTextField txtPnr;
    private JTextArea txtDetails;
    private JButton btnSearch, btnCancel;

    public CancellationFrame() {
        setTitle("Cancel Reservation");
        setSize(500,360);
        setLocationRelativeTo(null);
        init();
        setVisible(true);
    }

    private void init() {
        JPanel top = new JPanel(new FlowLayout());
        top.add(new JLabel("Enter PNR (numeric):"));
        txtPnr = new JTextField(12);
        top.add(txtPnr);

        btnSearch = new JButton("Search");
        top.add(btnSearch);

        txtDetails = new JTextArea();
        txtDetails.setEditable(false);
        JScrollPane sp = new JScrollPane(txtDetails);

        btnCancel = new JButton("Cancel Booking");
        btnCancel.setEnabled(false);

        getContentPane().setLayout(new BorderLayout(8,8));
        getContentPane().add(top, BorderLayout.NORTH);
        getContentPane().add(sp, BorderLayout.CENTER);
        getContentPane().add(btnCancel, BorderLayout.SOUTH);

        btnSearch.addActionListener(e -> search());
        btnCancel.addActionListener(e -> doCancel());
    }

    private void search() {
        String pnrStr = txtPnr.getText().trim();
        if (pnrStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter PNR.");
            return;
        }
        long pnr;
        try {
            pnr = Long.parseLong(pnrStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "PNR must be numeric.");
            return;
        }

        String sql = "SELECT * FROM reservations WHERE pnr=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, pnr);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("PNR: ").append(rs.getLong("pnr")).append("\n");
                    sb.append("Passenger: ").append(rs.getString("passenger_name")).append("\n");
                    sb.append("Age/Gender: ").append(rs.getInt("passenger_age")).append("/").append(rs.getString("passenger_gender")).append("\n");
                    sb.append("Train: ").append(rs.getString("train_no")).append(" - ").append(rs.getString("train_name")).append("\n");
                    sb.append("Class: ").append(rs.getString("class_type")).append("\n");
                    sb.append("Journey Date: ").append(rs.getDate("journey_date")).append("\n");
                    sb.append("From: ").append(rs.getString("source")).append(" To: ").append(rs.getString("destination")).append("\n");
                    sb.append("Booked on: ").append(rs.getTimestamp("booking_date")).append("\n");
                    txtDetails.setText(sb.toString());
                    btnCancel.setEnabled(true);
                } else {
                    txtDetails.setText("No reservation found for PNR " + pnr);
                    btnCancel.setEnabled(false);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB error: " + ex.getMessage());
        }
    }

    private void doCancel() {
        String pnrStr = txtPnr.getText().trim();
        long pnr = Long.parseLong(pnrStr);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel PNR " + pnr + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM reservations WHERE pnr=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, pnr);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                JOptionPane.showMessageDialog(this, "Reservation cancelled.");
                txtDetails.setText("");
                btnCancel.setEnabled(false);
            } else {
                JOptionPane.showMessageDialog(this, "Cancellation failed. Reservation may not exist.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB error: " + ex.getMessage());
        }
    }
}
