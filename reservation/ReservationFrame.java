package reservation;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ReservationFrame extends JFrame {
    private int userId;
    private JTextField txtPassenger, txtAge, txtJourneyDate, txtSource, txtDestination;
    private JComboBox<String> cmbGender, cmbTrainNo, cmbClass;
    private JLabel lblTrainName;
    private Map<String, String> trainMap = new HashMap<>();

    public ReservationFrame(int userId) {
        this.userId = userId;
        setTitle("Make Reservation");
        setSize(480, 360);
        setLocationRelativeTo(null);
        initComponents();
        setVisible(true);
        loadTrains();
    }

    private void initComponents() {
        JPanel p = new JPanel(new GridLayout(10,2,6,6));
        txtPassenger = new JTextField();
        txtAge = new JTextField();
        cmbGender = new JComboBox<>(new String[]{"Male","Female","Other"});
        cmbTrainNo = new JComboBox<>();
        lblTrainName = new JLabel("Train name will appear here");
        cmbClass = new JComboBox<>(new String[]{"Sleeper","AC3","AC2","AC1"});
        txtJourneyDate = new JTextField("yyyy-mm-dd");
        txtSource = new JTextField();
        txtDestination = new JTextField();

        p.add(new JLabel("Passenger Name:")); p.add(txtPassenger);
        p.add(new JLabel("Age:")); p.add(txtAge);
        p.add(new JLabel("Gender:")); p.add(cmbGender);
        p.add(new JLabel("Train No:")); p.add(cmbTrainNo);
        p.add(new JLabel("Train Name:")); p.add(lblTrainName);
        p.add(new JLabel("Class:")); p.add(cmbClass);
        p.add(new JLabel("Journey Date (YYYY-MM-DD):")); p.add(txtJourneyDate);
        p.add(new JLabel("From (Source):")); p.add(txtSource);
        p.add(new JLabel("To (Destination):")); p.add(txtDestination);

        JButton btnInsert = new JButton("Insert Reservation");
        btnInsert.addActionListener(e -> insertReservation());

        cmbTrainNo.addActionListener(e -> {
            String tn = (String) cmbTrainNo.getSelectedItem();
            if (tn != null) lblTrainName.setText(trainMap.getOrDefault(tn, ""));
        });

        getContentPane().setLayout(new BorderLayout(8,8));
        getContentPane().add(p, BorderLayout.CENTER);
        getContentPane().add(btnInsert, BorderLayout.SOUTH);
    }

    private void loadTrains() {
        String sql = "SELECT train_no, train_name FROM trains ORDER BY train_no";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String no = rs.getString("train_no");
                String name = rs.getString("train_name");
                trainMap.put(no, name);
                cmbTrainNo.addItem(no);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading trains: " + ex.getMessage());
        }
    }

    private void insertReservation() {
        String passenger = txtPassenger.getText().trim();
        String ageStr = txtAge.getText().trim();
        String gender = (String) cmbGender.getSelectedItem();
        String trainNo = (String) cmbTrainNo.getSelectedItem();
        String trainName = lblTrainName.getText();
        String classType = (String) cmbClass.getSelectedItem();
        String journeyStr = txtJourneyDate.getText().trim();
        String source = txtSource.getText().trim();
        String dest = txtDestination.getText().trim();

        if (passenger.isEmpty() || ageStr.isEmpty() || trainNo == null || journeyStr.isEmpty() || source.isEmpty() || dest.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields.");
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid age.");
            return;
        }

        java.sql.Date journeyDate;
        try {
            journeyDate = java.sql.Date.valueOf(LocalDate.parse(journeyStr));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.");
            return;
        }

        String insert = "INSERT INTO reservations (user_id, passenger_name, passenger_age, passenger_gender, train_no, train_name, class_type, journey_date, source, destination) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, userId);
            ps.setString(2, passenger);
            ps.setInt(3, age);
            ps.setString(4, gender);
            ps.setString(5, trainNo);
            ps.setString(6, trainName);
            ps.setString(7, classType);
            ps.setDate(8, journeyDate);
            ps.setString(9, source);
            ps.setString(10, dest);

            int affected = ps.executeUpdate();
            if (affected == 0) {
                JOptionPane.showMessageDialog(this, "Booking failed.");
                return;
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long pnr = keys.getLong(1);
                    JOptionPane.showMessageDialog(this, "Reservation successful! PNR: " + pnr);
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Reservation created but could not fetch PNR.");
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB error: " + ex.getMessage());
        }
    }
}
