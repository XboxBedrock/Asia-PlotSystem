package github.BTEPlotSystem.utils;

import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.core.system.plot.Plot;
import github.BTEPlotSystem.core.system.plot.PlotManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FTPManager {

    public static String getFTPURI(Plot plot)
    {
        String address = "address";
        String username = "username";
        String password = "password";
        int port = 21;
        boolean secureFTP = false;
        String defaultPath = "";
        try {
            ResultSet rs = DatabaseConnection
                    .createStatement()
                    .executeQuery("SELECT * FROM servers " +
                                      "WHERE servername = '"
                                    + plot.getCity().getState().toString().toLowerCase() + "'");
            if(rs.next()) {
                address = rs.getString("address");
                username = rs.getString("username");
                password = rs.getString("password");
                port = Integer.parseInt(rs.getString("port"));
                secureFTP = rs.getInt("secureftp") == 1;
                defaultPath = rs.getString("defaultpath");
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return String.format(
                "%sftp://%s:%s@%s:%d/%s",
                secureFTP ? "s" : "",
                username,
                password,
                address,
                port,
                defaultPath
                    + "/finished-plots/"
                    + plot.getCity().getID()
                    + "/"
                    + plot.getID() + ".schematic"
         );
    }

    private static final int BUFFER_SIZE = 4096;

    public static void sendFileFTP(String ftpURL, File schematic)
    {
        try {
            URL url = new URL(ftpURL);
            URLConnection conn = url.openConnection();
            OutputStream outputStream = conn.getOutputStream();
            FileInputStream inputStream = new FileInputStream(schematic);

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = -1;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            System.out.println("File uploaded");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
