/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Managers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author HP-Probook
 */
public class Logger {

    private File logFile;

    public Logger(File logFile) {
        this.logFile = logFile;
    }

    public void log(String message) {
        // Obtener la marca de tiempo actual
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timestamp = now.format(formatter);

        // Formatear el mensaje con la marca de tiempo
        String logMessage = "[" + timestamp + "] " + message + "\n";

        // Escribir en LOG
        try (FileWriter writer = new FileWriter(getLogFile(), true)) { // true para append
            writer.write(logMessage);
        } catch (IOException e) {
            System.err.println("Error al escribir en el archivo de log: " + e.getMessage());
        }
    }

    /**
     * @return the logFile
     */
    public File getLogFile() {
        return logFile;
    }
}
