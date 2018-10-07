package com.adam.ipuploader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@SuppressWarnings({"squid:S1659","squid:S1068" /*unused*/})
public class IpUploader {
    final static private String DATABASE_URL  = "jdbc:mysql://sql7.freemysqlhosting.net:3306/sql7260160",
                                DATABASE_USER = "sql7260160",
                                DATABASE_PASSWORD_LOCATION = "./db_password";
    final static private String NO_PASS_FILE_MSG = "Cannot find file 'db_password'!";

    final static private int REFRESH_INTERVAL = 1; // IN MINUTES

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver"); // Load JDBC driver
        } catch (ClassNotFoundException e) {
            fatalError(e);
        }
    }



    ///////////////////////
    // MAIN
    ///////////////////////
    public static void main(String[] args) throws SQLException {
        if (!Files.exists(Paths.get(DATABASE_PASSWORD_LOCATION)))
            throw new RuntimeException(NO_PASS_FILE_MSG);

        String currentIP = getIPFromDB(),
               newIP;
        int sleepFor;

        try {
            while(true) {
                newIP = getCurrentIP();
                if (!currentIP.equals(newIP)) {
                    System.out.println("Updating with: " + newIP);
                    currentIP = newIP;
                    postIP(newIP);
                } else {
                    System.out.println("Skipping update! IP: " + newIP + " is up-to-date.");
                }

                sleepFor = REFRESH_INTERVAL * 1000 * 60;
                System.out.println("Sleeping for " + REFRESH_INTERVAL + " mins...");
                Thread.sleep(sleepFor);
            }

        } catch (Exception e) {
            fatalError(e);
        }
    }

    private static String getCurrentIP() {
        
        URL whatIsMyIP = null;

        try {
            whatIsMyIP = new URL("http://checkip.amazonaws.com");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try (BufferedReader in = new BufferedReader(new InputStreamReader(whatIsMyIP.openStream()))) {
            return in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    private static String getPasswordFromFile() {
        try {
            return new String(Files.readAllBytes(Paths.get(DATABASE_PASSWORD_LOCATION)), StandardCharsets.US_ASCII);
        } catch (IOException e) {
            System.err.println(NO_PASS_FILE_MSG);
            throw new RuntimeException(e);
        }
    }

    private static String getIPFromDB() {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, getPasswordFromFile())) {
            Statement stmt = connection.createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT ip_address FROM ip_uploader WHERE entry_id=1;");
            resultSet.next();
            return resultSet.getString("ip_address");
        } catch (SQLException e) {
            throw new RuntimeException("Cannot post IP!", e);
        }
    }

    private static void postIP(String ip) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, getPasswordFromFile())) {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("UPDATE ip_uploader SET ip_address='" + ip + "' WHERE entry_id=1;");
        } catch (SQLException e) {
            throw new RuntimeException("Cannot post IP!", e);
        }
    }

    private static void fatalError(Exception e) {
        System.err.println(e.getMessage());
        System.exit(1);
    }
}
