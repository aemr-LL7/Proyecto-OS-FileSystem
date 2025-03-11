/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Managers;

import EDD.OurHashTable;
import EDD.SimpleList;
import Main.GUI.FileSystemUI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
    private File configFile;
    private File processDataFile;
    private final Gson gson;

    public FileManager() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        this.initializeDirectory();
    }

    private void initializeDirectory() {
        try {
            Path savedDirectoryPath = Paths.get(this.SAVED_DIRECTORY);

            if (!Files.exists(savedDirectoryPath)) {
                Files.createDirectory(savedDirectoryPath);
                System.out.println("Carpeta .saved creada en el directorio raiz");
            }

            this.configFile = new File(savedDirectoryPath.toFile(), "simulation_config.txt");
            this.processDataFile = new File(savedDirectoryPath.toFile(), "process_data.json");

            if (!configFile.exists()) {
                configFile.createNewFile();
                System.out.println("Archivo simulation_config.txt creado");
            }

            if (!processDataFile.exists()) {
                processDataFile.createNewFile();
                System.out.println("Archivo process_data.json creado");
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al crear la carpeta o archivos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadSimulationData(SimulationUI ui, OurHashTable processTable) {
        // Primero verificar si hay datos existentes
        boolean hasExistingData = this.checkExistingData(ui, processTable);

        if (hasExistingData) {
            int option = JOptionPane.showConfirmDialog(null,
                    "Existen datos cargados en el sistema. ¿Desea sobreescribirlos?",
                    "Datos existentes",
                    JOptionPane.YES_NO_OPTION);

            if (option != JOptionPane.YES_OPTION) {
                return;
            }

            // Limpiar datos existentes
            this.clearExistingData(ui, processTable);
        }

        // Cargar configuración del archivo txt
        this.loadConfigFile(ui);

        // Cargar procesos del archivo JSON
        this.loadProcessesFile(processTable, ui.getOperatingSystem().getQueueManager());
    }

    private void loadConfigFile(SimulationUI ui) {
        try {
            String content = this.readFile(configFile);
            if (content.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "El archivo de configuración está vacío.",
                        "Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String[] lines = content.split("\n");
            boolean inGeneralParams = false;

            for (String line : lines) {
                line = line.trim();

                if (line.equals("[General Params]")) {
                    inGeneralParams = true;
                    continue;
                }

                if (inGeneralParams && !line.startsWith("[")) {
                    if (line.startsWith("CycleDuration=")) {
                        int cycleDuration = Integer.parseInt(line.split("=")[1].trim());
                        ui.setCycleDuration(cycleDuration);
                        ui.getOperatingSystem().getSystemClock().setCycleDuration(cycleDuration);

                    } else if (line.startsWith("NumberOfCpus=")) {
                        int numCpus = Integer.parseInt(line.split("=")[1].trim());
                        ui.setNumCPUs(numCpus);
                        ui.getOperatingSystem().getSystemClock().setPermissionsRequired(numCpus);
                        ui.getOperatingSystem().initCpuforUI(numCpus);

                    } else if (line.startsWith("NumberOfProcesses=")) {
                        int numProcesses = Integer.parseInt(line.split("=")[1].trim());
                        ui.setNumProcesses(numProcesses);
                    }
                }
            }

            // Actualizar la UI con los nuevos valores
            ui.updateCPUList();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error al cargar el archivo de configuración: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadProcessesFile(OurHashTable processTable, QueueManager queueManager) {
        try {
            String content = this.readFile(processDataFile);
            if (content.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "El archivo de procesos está vacío.",
                        "Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Convertir el string a un objeto JSON e iterar para crear pcbs y proceso, añadir a la hash
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObj = jsonParser.parse(content).getAsJsonObject();

            // Iterar sobre las entradas
            for (String processName : jsonObj.keySet()) {
                // Obtener el objeto JSON asociado al nombre del proceso
                JsonObject pcbJson = jsonObj.get(processName).getAsJsonObject();

                // Crear un nuevo PCB desde el JSON
                PCB pcb = gson.fromJson(pcbJson, PCB.class);

                // Crear un nuevo proceso con el PCB
                OurProcess process = new OurProcess(pcb);

                // Agregar el proceso a la tabla hash y las colas de listos
                processTable.put(pcb.getId(), process);
                queueManager.addToReadyQueue(pcb);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error al cargar el archivo de procesos: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean checkExistingData(SimulationUI ui, OurHashTable<OurProcess> processTable) {
        return ui.getNumCPUs() > 0 || processTable.getEntriesList().getSize() > 0;
    }

    private void clearExistingData(SimulationUI ui, OurHashTable<OurProcess> processTable) {
        // Limpiar la UI
        ui.clearAllFields();

        // Limpiar la tabla hash
        processTable.clear();
    }

    public void saveSimulationConfig(int cycleDuration, int numberOfCpus, int numberOfProcesses) {
        String configContent = "[General Params]\n"
                + "CycleDuration=" + cycleDuration + "\n"
                + "NumberOfCpus=" + numberOfCpus + "\n"
                + "NumberOfProcesses=" + numberOfProcesses + "\n\n"
                + "[CPUS]\n";

        for (int i = 0; i < numberOfCpus; i++) {
            configContent += "cpuId=" + i + "\n";
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
            writer.write(configContent);
            System.out.println("Configuración guardada en " + configFile.getPath());
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al guardar la configuración.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void saveProcessData(OurHashTable<OurProcess> processHashTable, int numberOfCpus) {
        try (Writer writer = new FileWriter(processDataFile)) {
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{\n");

            SimpleList<OurProcess> processList = processHashTable.getEntriesList();

            for (int i = 0; i < processList.getSize(); i++) {
                OurProcess process = processList.getValueByIndex(i);
                if (process != null) {
                    PCB pcb = process.getPcb();

                    // Agregar el nombre del proceso como clave
                    jsonBuilder.append("  \"").append(pcb.getId()).append("\": ");

                    String pcbJson = gson.toJson(pcb);
                    jsonBuilder.append(pcbJson);

                    // Agregar coma si no es el último elemento
                    if (i < processList.getSize() - 1) {
                        jsonBuilder.append(",");
                    }
                    jsonBuilder.append("\n");
                }
            }

            jsonBuilder.append("}");

            // Escribir el JSON final
            writer.write(jsonBuilder.toString());
            System.out.println("Datos de procesos guardados en " + processDataFile.getPath());

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al guardar los datos de PCBs.", "Error", JOptionPane.ERROR_MESSAGE);
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

    private int[] getGeneralParams(String params) {
        int[] generalParams = new int[3];
        boolean inGeneralParams = false;

        String[] lines = params.split("\n");
        for (String line : lines) {
            line = line.trim();

            if (line.equals("[General Params]")) {
                inGeneralParams = true;
            } else if (line.startsWith("[") && inGeneralParams) {
                break;
            } else if (inGeneralParams) {
                if (line.startsWith("CycleDuration=")) {
                    generalParams[0] = Integer.parseInt(line.split("=")[1].trim());
                } else if (line.startsWith("NumberOfCpus=")) {
                    generalParams[1] = Integer.parseInt(line.split("=")[1].trim());
                } else if (line.startsWith("NumberOfProcesses=")) {
                    generalParams[2] = Integer.parseInt(line.split("=")[1].trim());
                }
            }
        }

        return generalParams;
    }

}
