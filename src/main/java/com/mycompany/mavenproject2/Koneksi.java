package com.mycompany.mavenproject2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class Koneksi {
    private static Connection koneksi;
    
    public static Connection getKoneksi() {
        if (koneksi == null) {
            try {
                // Nama database diubah menjadi db_manajemenkontrakan
                // Port menggunakan 8889 dan password menggunakan "root" sesuai MAMP
                String url = "jdbc:mysql://localhost:8889/db_manajemenkontrakan"; 
                String user = "root"; 
                String password = "root"; 
                
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                koneksi = DriverManager.getConnection(url, user, password);
                System.out.println("Koneksi ke Database Berhasil!");
            } catch (SQLException | ClassNotFoundException e) {
                JOptionPane.showMessageDialog(null, "Koneksi Database Gagal: " + e.getMessage());
            }
        }
        return koneksi;
    }
}