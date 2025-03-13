/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Managers;

import EDD.OurHashTable;
import EDD.SimpleList;
import FileSystem.Directory;
import FileSystem.OurFile;
import FileSystem.Storage;
import Main.GUI.FileSystemUI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JOptionPane;

/**
 *
 * @author andre
 */
public class FileManager {

    private final String SAVED_DIRECTORY = ".saved";
    private File logFile;
    private File fileSystemDataFile;
    private final Gson gson;
    private Logger logger;

    public FileManager() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        this.initializeDirectory();
        this.logger = new Logger(this.logFile);
    }

    private void initializeDirectory() {
        try {
            Path savedDirectoryPath = Paths.get(this.SAVED_DIRECTORY);

            if (!Files.exists(savedDirectoryPath)) {
                Files.createDirectory(savedDirectoryPath);
                System.out.println("Carpeta .saved creada en el directorio raiz");
            }

            this.logFile = new File(savedDirectoryPath.toFile(), "system_logs.txt");
            this.fileSystemDataFile = new File(savedDirectoryPath.toFile(), "filesystem_data.json");

            if (!logFile.exists()) {
                logFile.createNewFile();
                System.out.println("Archivo system_logs.txt creado");
            }

            if (!fileSystemDataFile.exists()) {
                fileSystemDataFile.createNewFile();
                System.out.println("Archivo filesystem_data.json creado");
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al crear la carpeta o archivos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Verificar si hay subdirectorios o archivos en el directorio raiz
    private boolean hasExistingData(Directory rootDirectory) {
        return rootDirectory.getSubdirectories().getSize() > 0 || rootDirectory.getFiles().getSize() > 0;
    }

    private boolean confirmLoadOperation() {
        int option = JOptionPane.showConfirmDialog(null, "El sistema ya tiene datos. ¿Desea cargar nueva información? (Se perderán los datos actuales)", "Datos Existentes",
                JOptionPane.YES_NO_OPTION);

        return option == JOptionPane.YES_OPTION;
    }

    public Directory loadFileSystem(Directory rootDirectory, Storage actualStorage, OurHashTable<OurFile> fileTable) {

        if (this.hasExistingData(rootDirectory)) {

            if (!confirmLoadOperation()) {
                return null;
            }

            // Limpiar los datos existentes
            this.clearExistingData(rootDirectory, fileTable);
        }

        try {
            String content = this.readFile(this.fileSystemDataFile);
            if (content.isEmpty()) {
                JOptionPane.showMessageDialog(null, "El archivo de sistema de archivos está vacío.", "Error", JOptionPane.WARNING_MESSAGE);
                return null;
            }

            // Convertir el string a un objeto JSON
            JsonParser jsonParser = new JsonParser();
            JsonObject rootJson = jsonParser.parse(content).getAsJsonObject();

            // Deserializar la estructura jerarquica
            this.deserializeDirectory(rootJson.get("/root").getAsJsonObject(), rootDirectory, actualStorage, fileTable);

            // Registrar la accion en el log
            this.logger.log("Estructura del sistema de archivos leida desde: " + this.logFile.getPath());

            return rootDirectory;

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al cargar la estructura del sistema de archivos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    // Limpiar subdirectorios y archivos del directorio raiz
    private void clearExistingData(Directory rootDirectory, OurHashTable<OurFile> fileTable) {

        rootDirectory.getSubdirectories().wipeList();
        rootDirectory.getFiles().wipeList();

        // Limpiar la tabla de archivos
        fileTable.clear();
    }

    public void saveFileSystem(Directory rootDirectory) {
        try (Writer writer = new FileWriter(this.fileSystemDataFile)) {
            // Crear un objeto JSON para representar la estructura jerárquica
            JsonObject rootJson = new JsonObject();
            serializeDirectory(rootDirectory, rootJson);

            // Escribir el JSON en el archivo
            gson.toJson(rootJson, writer);
            System.out.println("Estructura del sistema de archivos guardada en " + this.fileSystemDataFile.getPath());
            // Registrar la accion en el log
            this.logger.log("Estructura del sistema de archivos guardada en " + this.fileSystemDataFile.getPath());

            JOptionPane.showMessageDialog(null, "Estructura del sistema de archivos guardada en " + this.fileSystemDataFile.getPath(), "Filemanager", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al guardar la estructura del sistema de archivos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void serializeDirectory(Directory directory, JsonObject parentJson) {
        JsonObject dirJson = new JsonObject();

        // Serializar subdirectorios
        JsonArray subdirsArray = new JsonArray();
        SimpleList<Directory> subdirectories = directory.getSubdirectories();
        for (int i = 0; i < subdirectories.getSize(); i++) {
            Directory subdir = subdirectories.getValueByIndex(i);
            JsonObject subdirJson = new JsonObject();
            this.serializeDirectory(subdir, subdirJson);
            subdirsArray.add(subdirJson);
        }
        dirJson.add("subdirectories", subdirsArray);

        // Serializar archivos
        JsonArray filesArray = new JsonArray();
        SimpleList<OurFile> files = directory.getFiles();
        for (int i = 0; i < files.getSize(); i++) {
            OurFile file = files.getValueByIndex(i);
            JsonObject fileJson = new JsonObject();
            fileJson.addProperty("name", file.getName());
            fileJson.addProperty("size", file.getSize());
            filesArray.add(fileJson);
        }
        dirJson.add("files", filesArray);

        // Añadir el directorio al JSON padre
        parentJson.add(directory.getName(), dirJson);
    }

    private void deserializeDirectory(JsonObject dirJson, Directory parentDirectory, Storage storage, OurHashTable<OurFile> fileTable) {
        // Deserializar subdirectorios
        JsonArray subdirsArray = dirJson.getAsJsonArray("subdirectories");
        for (JsonElement subdirElement : subdirsArray) {
            JsonObject subdirJson = subdirElement.getAsJsonObject();
            String subdirName = subdirJson.keySet().iterator().next();
            Directory subdir = new Directory(subdirName, parentDirectory);
            parentDirectory.addSubdirectory(subdir);
            this.deserializeDirectory(subdirJson.getAsJsonObject(subdirName), subdir, storage, fileTable);
        }

        // Deserializar archivos
        JsonArray filesArray = dirJson.getAsJsonArray("files");
        for (JsonElement fileElement : filesArray) {

            JsonObject fileJson = fileElement.getAsJsonObject();
            String fileName = fileJson.get("name").getAsString();
            int fileSize = fileJson.get("size").getAsInt();

            // Crear un nuevo archivo (genera los dataNodes segun el tamaño del archivo)
            OurFile file = new OurFile(fileSize, fileName);

            // Añadir el archivo al directorio y a la tabla de archivos
            parentDirectory.addFile(file);
            storage.allocateBlocks(file);
            fileTable.put(file.getName(), file);

        }
    }

    private String readFile(File file) {
        StringBuilder data = new StringBuilder();
        String line;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    data.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al leer el archivo.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return data.toString();
    }

    public String loadLogsInGUI(File logFile) {
        try {
            // Leer el contenido del archivo de log
            String logContent = new String(Files.readAllBytes(Paths.get(logFile.getAbsolutePath())));
            
            return logContent;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error al leer el archivo de log: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return "";
    }

    /**
     * @return the logger
     */
    public Logger getLogger() {
        return logger;
    }

}
