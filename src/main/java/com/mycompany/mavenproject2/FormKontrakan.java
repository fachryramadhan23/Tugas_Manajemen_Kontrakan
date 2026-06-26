package com.mycompany.mavenproject2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

public class FormKontrakan extends JFrame {
    private JTextField txtNoKamar, txtPenghuni, txtHarga, txtCari;
    private JComboBox<String> cbTipe, cbStatus;
    private JTable tabelKamar;
    private DefaultTableModel tableModel;
    private JButton btnSimpan, btnUbah, btnHapus, btnCetak;
    private JLabel lblTotalPendapatan, lblKamarTerisi;

    public FormKontrakan() {
        setTitle("Sistem Informasi Manajemen Kontrakan");
        setSize(950, 550);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- PANEL INPUT (Kiri) ---
        JPanel panelInput = new JPanel(new GridLayout(6, 2, 10, 10));
        panelInput.setBorder(BorderFactory.createTitledBorder("Input Data Kamar"));
        panelInput.setPreferredSize(new Dimension(300, 400));

        panelInput.add(new JLabel("No. Kamar:"));
        txtNoKamar = new JTextField();
        panelInput.add(txtNoKamar);

        panelInput.add(new JLabel("Nama Penghuni:"));
        txtPenghuni = new JTextField();
        panelInput.add(txtPenghuni);

        panelInput.add(new JLabel("Tipe Kamar:"));
        cbTipe = new JComboBox<>(new String[]{"Standard", "VIP", "Family"});
        panelInput.add(cbTipe);

        panelInput.add(new JLabel("Harga Sewa /Bulan:"));
        txtHarga = new JTextField();
        panelInput.add(txtHarga);

        panelInput.add(new JLabel("Status:"));
        cbStatus = new JComboBox<>(new String[]{"Tersedia", "Terisi"});
        panelInput.add(cbStatus);

        add(panelInput, BorderLayout.WEST);

        // --- PANEL UTAMA & TABEL (Kanan) ---
        JPanel panelKanan = new JPanel(new BorderLayout(5, 5));
        
        // Panel Pencarian (Atas Tabel)
        JPanel panelCari = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelCari.add(new JLabel("Cari Data (No Kamar / Nama): "));
        txtCari = new JTextField(20);
        panelCari.add(txtCari);
        panelKanan.add(panelCari, BorderLayout.NORTH);
        
        String[] kolom = {"No Kamar", "Nama Penghuni", "Tipe", "Harga", "Status"};
        tableModel = new DefaultTableModel(kolom, 0);
        tabelKamar = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tabelKamar);
        panelKanan.add(scrollPane, BorderLayout.CENTER);
// --- PANEL TOMBOL AKSI & STATISTIK (Bawah) ---
        JPanel panelBawah = new JPanel(new GridLayout(2, 1));
        
        // Diubah menggunakan GridLayout agar semua tombol rapi, kelihatan semua, dan ukurannya sama
        JPanel panelTombol = new JPanel(new GridLayout(1, 5, 5, 5)); 
        panelTombol.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        btnSimpan = new JButton("Simpan (Create)");
        btnUbah = new JButton("Ubah (Update)");
        btnHapus = new JButton("Hapus (Delete)");
        
        
        // Tombol Cetak Laporan
        btnCetak = new JButton("Cetak Laporan (Excel/CSV)");
        btnCetak.setBackground(new Color(0, 153, 76));
        btnCetak.setForeground(Color.BLACK);

        

        panelTombol.add(btnCetak);
        panelTombol.add(btnSimpan);
        panelTombol.add(btnUbah);
        panelTombol.add(btnHapus);
        
        
        // Panel Informasi Ringkasan Finansial
        JPanel panelStatus = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        panelStatus.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        lblKamarTerisi = new JLabel("Kamar Terisi: 0");
        lblKamarTerisi.setFont(new Font("Arial", Font.BOLD, 12));
        lblTotalPendapatan = new JLabel("Total Pendapatan: Rp 0");
        lblTotalPendapatan.setFont(new Font("Arial", Font.BOLD, 12));
        lblTotalPendapatan.setForeground(new Color(0, 102, 0));
        panelStatus.add(lblKamarTerisi);
        panelStatus.add(lblTotalPendapatan);

        panelBawah.add(panelTombol);
        panelBawah.add(panelStatus);
        panelKanan.add(panelBawah, BorderLayout.SOUTH);

        add(panelKanan, BorderLayout.CENTER);

        // --- EVENT HANDLING & LOGIC ---
        tampilkanData(""); 

        // Event Pencarian Real-time
        txtCari.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                tampilkanData(txtCari.getText());
            }
        });

        // Event klik baris tabel
        tabelKamar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int baris = tabelKamar.getSelectedRow();
                if(baris != -1) {
                    txtNoKamar.setText(tableModel.getValueAt(baris, 0).toString());
                    txtNoKamar.setEditable(false);
                    txtPenghuni.setText(tableModel.getValueAt(baris, 1).toString());
                    cbTipe.setSelectedItem(tableModel.getValueAt(baris, 2).toString());
                    txtHarga.setText(tableModel.getValueAt(baris, 3).toString());
                    cbStatus.setSelectedItem(tableModel.getValueAt(baris, 4).toString());
                }
            }
        });

        btnSimpan.addActionListener(e -> simpanData());
        btnUbah.addActionListener(e -> ubahData());
        btnHapus.addActionListener(e -> hapusData());
        
        
        // Event klik tombol cetak laporan
        btnCetak.addActionListener(e -> cetakLaporanCSV());
    }

    private void tampilkanData(String keyword) {
        tableModel.setRowCount(0);
        int totalKamarTerisi = 0;
        int totalPendapatan = 0;
        
        try {
            Connection conn = Koneksi.getKoneksi();
            String sql = "SELECT * FROM tbl_kamar WHERE no_kamar LIKE ? OR nama_penghuni LIKE ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, "%" + keyword + "%");
            pst.setString(2, "%" + keyword + "%");
            
            ResultSet res = pst.executeQuery();
            while (res.next()) {
                String noKamar = res.getString("no_kamar");
                String nama = res.getString("nama_penghuni");
                String tipe = res.getString("tipe_kamar");
                int harga = res.getInt("harga_sewa");
                String status = res.getString("status");
                
                tableModel.addRow(new Object[]{noKamar, nama, tipe, harga, status});
                
                if (status.equalsIgnoreCase("Terisi")) {
                    totalKamarTerisi++;
                    totalPendapatan += harga;
                }
            }
            
            NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            lblKamarTerisi.setText("Kamar Terisi: " + totalKamarTerisi + " Kamar");
            lblTotalPendapatan.setText("Total Potensi Pendapatan: " + formatRupiah.format(totalPendapatan) + " /Bulan");
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data: " + e.getMessage());
        }
    }

    // FITUR BARU: Metode Ekspor Data JTable ke file CSV (Bisa dibuka langsung di Microsoft Excel)
    private void cetakLaporanCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan Laporan Kontrakan");
        int userSelection = fileChooser.showSaveDialog(this);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            
            // Memastikan ekstensi file adalah .csv
            if (!filePath.toLowerCase().endsWith(".csv")) {
                filePath += ".csv";
            }
            
            try (FileWriter fw = new FileWriter(filePath)) {
                // Menulis Header Kolom
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    fw.write(tableModel.getColumnName(i) + ",");
                }
                fw.write("\n");
                
                // Menulis Data Baris Tabel
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        fw.write(tableModel.getValueAt(i, j).toString() + ",");
                    }
                    fw.write("\n");
                }
                
                JOptionPane.showMessageDialog(this, "Laporan berhasil disimpan di:\n" + filePath, "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Gagal mengekspor laporan: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void simpanData() {
        if(txtNoKamar.getText().isEmpty() || txtPenghuni.getText().isEmpty() || txtHarga.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua data inputan wajib diisi!");
            return;
        }
        try {
            String sql = "INSERT INTO tbl_kamar VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pst = Koneksi.getKoneksi().prepareStatement(sql);
            pst.setString(1, txtNoKamar.getText());
            pst.setString(2, txtPenghuni.getText());
            pst.setString(3, cbTipe.getSelectedItem().toString());
            pst.setInt(4, Integer.parseInt(txtHarga.getText()));
            pst.setString(5, cbStatus.getSelectedItem().toString());
            
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data Berhasil Disimpan!");
            tampilkanData("");
            resetForm();
        } catch (SQLException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error saat menyimpan: " + ex.getMessage());
        }
    }

    private void ubahData() {
        try {
            String sql = "UPDATE tbl_kamar SET nama_penghuni=?, tipe_kamar=?, harga_sewa=?, status=? WHERE no_kamar=?";
            PreparedStatement pst = Koneksi.getKoneksi().prepareStatement(sql);
            pst.setString(1, txtPenghuni.getText());
            pst.setString(2, cbTipe.getSelectedItem().toString());
            pst.setInt(3, Integer.parseInt(txtHarga.getText()));
            pst.setString(4, cbStatus.getSelectedItem().toString());
            pst.setString(5, txtNoKamar.getText());

            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data Berhasil Diperbarui!");
            tampilkanData("");
            resetForm();
        } catch (SQLException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error saat memperbarui: " + ex.getMessage());
        }
    }

    private void hapusData() {
        int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus data ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String sql = "DELETE FROM tbl_kamar WHERE no_kamar=?";
                PreparedStatement pst = Koneksi.getKoneksi().prepareStatement(sql);
                pst.setString(1, txtNoKamar.getText());
                
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Data Berhasil Dihapus!");
                tampilkanData("");
                resetForm();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error saat menghapus: " + ex.getMessage());
            }
        }
    }

    private void resetForm() {
        txtNoKamar.setText("");
        txtNoKamar.setEditable(true);
        txtPenghuni.setText("");
        txtHarga.setText("");
        txtCari.setText("");
        cbTipe.setSelectedIndex(0);
        cbStatus.setSelectedIndex(0);
        tabelKamar.clearSelection();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new FormKontrakan().setVisible(true);
        });
    }
}