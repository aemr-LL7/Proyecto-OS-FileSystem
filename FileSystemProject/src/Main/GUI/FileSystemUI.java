/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Main.GUI;

import EDD.OurHashTable;
import EDD.SimpleList;
import EDD.SimpleNode;
import FileSystem.Directory;
import FileSystem.OurFile;
import FileSystem.Storage;
import Managers.FileManager;
import Managers.FileSystemManager;
import Managers.Logger;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author Windows 11
 */
public class FileSystemUI extends javax.swing.JFrame {

    private static FileSystemUI fileSystemUiInstance;
    // Creacion de managers
    private final FileSystemManager fsManager;
    private final FileManager fileManager;
    private String currentPath = "";
    private boolean isAdminMode = false;
    private final String ADMIN_PASSWORD = "1236";

    // Menu items for contextual menu
    private JPopupMenu contextMenu;
    private JMenuItem createDirItem;
    private JMenuItem createFileItem;
    private JMenuItem renameItem;
    private JMenuItem deleteItem;
    private JMenuItem moveItem;
    private JMenuItem infoItem;
    // table file model
    private final DefaultTableModel tableFilesModel;

    // Instancia del Logger
    private final Logger logger;

    public FileSystemUI() {
        initComponents();
        // gui properties
        this.setTitle("Simulador Sistema de Archivos - Usuario Regular");

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1075, 650);
        this.setLocationRelativeTo(null);
        //this.setResizable(false);

        // configuraciones
        // Añadir separadores en la tabla y sd
        this.setupSplitPane();
        // Iniciar el filesystem manager
        this.fsManager = new FileSystemManager(fileSystemTree);
        this.fileManager = new FileManager();
        this.fsManager.updateTree();
        this.logger = fileManager.getLogger();

        // Iniciar tabla de archivos
        this.tableFilesModel = new DefaultTableModel(new String[]{"Nombre de Archivo", "Bloques Asignados", "Primer Espacio de Bloque"}, 0);
        this.filesJTable.setModel(tableFilesModel);  // Vincular el modelo a la JTable

        // Initial updates
        this.updateFilesTable();
        this.setupContextMenu();
        this.setupListeners();
        this.updateMoreInfoPanel();

        // Registrar la accion en el log
        this.logger.log("==========> INICIALIZANDO SISTEMA DE ARCHIVOS... <==========");
    }

    public static synchronized FileSystemUI getInstance() {
        if (fileSystemUiInstance == null) {
            setFileSystemUiInstance(new FileSystemUI());
        }
        return fileSystemUiInstance;
    }

    private void setupSplitPane() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.filesPanel, this.storageViewPanel);

        // Configurar el JSplitPane
        splitPane.setDividerLocation(0.5); // Divisor en el centro
        splitPane.setResizeWeight(0.5); // Redimensionar proporcionalmente
        splitPane.setOneTouchExpandable(true); // Botones para expandir/colapsar
        splitPane.setContinuousLayout(true); // Actualizar en tiempo real

        // Añadir el JSplitPane al contenedor principal
        auxMainPanel.add(splitPane, BorderLayout.CENTER);
    }

    private void showLogRegisterView(Frame parent) {
        this.logRegisterView.setTitle("Registro de Operaciones");
        this.logRegisterView.setSize(650, 350);
        this.logRegisterView.setLocationRelativeTo(parent);
        this.logRegisterView.setResizable(false);
        this.logTextArea.setEditable(false); // Hacer el texto no editable
        // Mostrar el registro en el jtextarea
        String logContent = this.fileManager.loadLogsInGUI(this.logger.getLogFile());
        this.logTextArea.setText(logContent);
        this.logRegisterView.setVisible(true);
    }

    private void updateFilesTable() {
        tableFilesModel.setRowCount(0); // Limpiar la tabla

        OurHashTable<OurFile> fileTable = this.fsManager.getStorage().getFileTable();

        for (int i = 0; i < fileTable.getEntriesList().getSize(); i++) {
            OurFile file = fileTable.getEntriesList().getValueByIndex(i);
            // Verificar si el archivo es nulo
            if (file == null) {
                System.out.println("Archivo nulo encontrado en la tabla de archivos.");
                continue;
            }
            // Obtener información del archivo
            String fileName = file.getName();
            int blocksAllocated = file.getSize();
            String firstBlockAddress = file.getFirstBlockAddress();

            tableFilesModel.addRow(new String[]{fileName, String.valueOf(blocksAllocated), firstBlockAddress});
        }

        // Aplicar el renderizado personalizado para la columna de nombres de archivo
        filesJTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    String fileName = (String) value;
                    Color fileColor = generateFileColor(fileName);
                    cell.setForeground(fileColor);
                }

                return cell;
            }
        });

        // Refrescar la tabla
        filesJTable.revalidate();
        filesJTable.repaint();
    }

    private Color generateFileColor(String fileName) {

        if (fileName != null) {

            int hash = fileName.hashCode();

            int r = Math.abs(hash) % 200;          // Componente rojo ente 0-199
            int g = Math.abs(hash / 256) % 200;    // Componente verde ente 0-199
            int b = Math.abs(hash / 65536) % 200;  // Componente azul ente 0-199
            r += 55;
            g += 55;
            b += 55;
            return new Color(r, g, b);

        }
        return null;
    }

    private void setupContextMenu() {
        this.contextMenu = new JPopupMenu();

        // Menu items
        createDirItem = new JMenuItem("Crear Directorio");
        createFileItem = new JMenuItem("Crear Archivo");
        renameItem = new JMenuItem("Renombrar");
        deleteItem = new JMenuItem("Eliminar");
        moveItem = new JMenuItem("Mover");
        infoItem = new JMenuItem("Ver Información");

        // Add to menu
        this.contextMenu.add(createDirItem);
        this.contextMenu.add(createFileItem);
        this.contextMenu.addSeparator();
        this.contextMenu.add(renameItem);
        this.contextMenu.add(deleteItem);
        this.contextMenu.add(moveItem);
        this.contextMenu.addSeparator();
        this.contextMenu.add(infoItem);

        // Add action listeners
        createDirItem.addActionListener(e -> this.createDirectoryAction());
        createFileItem.addActionListener(e -> this.createFileAction());
        renameItem.addActionListener(e -> this.renameAction());
        deleteItem.addActionListener(e -> this.deleteAction());
        moveItem.addActionListener(e -> this.moveAction());
        infoItem.addActionListener(e -> this.showInfoAction());

        // Configuracion inicial de usuario regular
        this.disableContextMenuEditOptions();
    }

    private void setupListeners() {
        fileSystemTree.addTreeSelectionListener(e -> {
            TreePath selectionPath = fileSystemTree.getSelectionPath();
            if (selectionPath != null) {

                StringBuilder pathBuilder = new StringBuilder("/root");

                // Para evitar tocar la raíz, se inicia desde 1
                for (int i = 1; i < selectionPath.getPathCount(); i++) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath.getPathComponent(i);
                    Object userObject = node.getUserObject();

                    if (userObject instanceof Directory) {
                        Directory dir = (Directory) userObject;
                        pathBuilder.append("/").append(dir.getName());
                    } else if (userObject instanceof OurFile) {
                        OurFile file = (OurFile) userObject;
                        pathBuilder.append("/").append(file.getName());
                    }
                }
                this.currentPath = pathBuilder.toString();
                System.out.println("Current Path: " + this.currentPath); // Para depuración
            }
        });

        // Mouse listener para click derecho
        fileSystemTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }
        });
    }

    private void showContextMenu(MouseEvent e) {
        TreePath path = fileSystemTree.getPathForLocation(e.getX(), e.getY());
        if (path != null) {
            fileSystemTree.setSelectionPath(path);

//            Object selectedNode = path.getLastPathComponent();
//            if (selectedNode.toString().equals("root") || selectedNode.toString().equals("/root")) {
//                return;
//            }
            contextMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    private void createDirectoryAction() {
        // Obtener el nodo seleccionado como padre
        DefaultTreeModel treeModel = (DefaultTreeModel) this.fileSystemTree.getModel();
        DefaultMutableTreeNode selectedParentNode = (DefaultMutableTreeNode) this.fileSystemTree.getLastSelectedPathComponent();

        if (selectedParentNode == null) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione un directorio donde quiere crear una carpeta.", "Sin Selección", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Object nodeParentObject = selectedParentNode.getUserObject();

        if (nodeParentObject instanceof Directory) {
            String directoryName = JOptionPane.showInputDialog(this, "Ingrese el nombre del directorio:", "Crear Directorio", JOptionPane.QUESTION_MESSAGE);

            if (directoryName != null && !directoryName.trim().isEmpty()) {
                Directory parentDir = (Directory) nodeParentObject;

                // Crear el nuevo directorio
                Directory newDir = new Directory(directoryName, parentDir);

                // Añadir el nuevo directorio al directorio padre
                parentDir.addSubdirectory(newDir);

                // Añadir el nuevo directorio al JTree
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newDir);
                treeModel.insertNodeInto(newNode, selectedParentNode, selectedParentNode.getChildCount());

                // Expandir el nodo padre en el JTree
                this.fileSystemTree.expandPath(new TreePath(selectedParentNode.getPath()));

                // Registrar la acción en el log
                this.logger.log("Directorio creado '" + directoryName + "' en: " + this.currentPath);

                // Actualizar estado panel de info
                this.updateMoreInfoPanel();

                System.out.println("Directorio creado exitosamente: " + directoryName);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Directorios solo pueden ser creados sobre otros directorios.",
                    "No se puede crear Directorio",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Metodo refactorizado de creacion de archivos
    private void createFileAction() {

        System.out.println("current path: " + currentPath);
        // Obtener el nodo seleccionado como padre
        DefaultTreeModel treeModel = (DefaultTreeModel) this.fileSystemTree.getModel();
        DefaultMutableTreeNode selectedParentNode = (DefaultMutableTreeNode) this.fileSystemTree.getLastSelectedPathComponent();

        if (selectedParentNode == null) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione un directorio donde quiere crear una carpeta.", "Sin Selección", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Object nodeParentObject = selectedParentNode.getUserObject();

        if (nodeParentObject instanceof Directory) {

            String fileName = JOptionPane.showInputDialog(this, "Ingrese el nombre del archivo::", "Crear Archivo", JOptionPane.QUESTION_MESSAGE);

            if (fileName != null && !fileName.trim().isEmpty()) {

                String sizeStr = JOptionPane.showInputDialog(this, "Añadir el tamaño del archivo en (blocks):", "Tamaño Archivo", JOptionPane.QUESTION_MESSAGE);
                try {
                    int size = Integer.parseInt(sizeStr);
                    OurFile newFile = new OurFile(size, fileName);

                    boolean allocationSuccess = this.fsManager.getStorage().allocateBlocks(newFile);

                    if (allocationSuccess) {
                        this.fsManager.getStorage().printStorageMatrix();
                        // Añadir el archivo al directorio padre
                        Directory parentDir = (Directory) nodeParentObject;
                        parentDir.addFile(newFile);

                        // Añadir aal JTREE
                        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newFile);
                        treeModel.insertNodeInto(newNode, selectedParentNode, selectedParentNode.getChildCount());
                        this.fileSystemTree.expandPath(new TreePath(selectedParentNode.getPath()));

                        // Registrar la accion en el log
                        this.logger.log("Nuevo archivo creado '" + newFile.getName() + "', Tamaño: " + newFile.getSize() + ", Direccion Primer Bloque: " + newFile.getFirstBlockAddress() + ", en " + this.currentPath);

                        // Actualizar componentes de UI
                        this.updateFilesTable();
                        this.updateBlockStoragePanel();
                        this.updateMoreInfoPanel();

                        System.out.println("Archivo creado exitosamente: " + fileName);

                    } else {
                        JOptionPane.showMessageDialog(this,
                                "No hay espacio suficiente para este tamaño de archivo.",
                                "Localizacion fallida",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this,
                            "Por favor ingrese un numero valido para el tamaño de archivo.",
                            "Tamaño invalido",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (nodeParentObject instanceof OurFile) {
            JOptionPane.showMessageDialog(this, "Archivos no pueden contener otros archivos. Por favor selecciones un directorio.", "Creacion de Archivos fallida", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Los archivos solo pueden ser creados dentro de directorios.", "Error al crear Archivo", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void renameAction() {
        // Obtener el nodo seleccionado como padre
        // DefaultTreeModel treeModel = (DefaultTreeModel) this.fileSystemTree.getModel();
        DefaultMutableTreeNode selectedParentNode = (DefaultMutableTreeNode) this.fileSystemTree.getLastSelectedPathComponent();

        if (selectedParentNode == null) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione un directorio donde quiere crear una carpeta.", "Sin Selección", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Object nodeParentObject = selectedParentNode.getUserObject();

        // Solicitar el nuevo nombre
        String newName = JOptionPane.showInputDialog(this, "Ingrese el nuevo nombre:", "Renombrar", JOptionPane.QUESTION_MESSAGE);

        if (newName == null || newName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre no puede estar vacío.", "Nombre Inválido", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Verificar si el nodo seleccionado es un directorio o un archivo
        if (nodeParentObject instanceof Directory) {
            // Renombrar directorio
            boolean success = fsManager.renameDirectory(currentPath, newName);
            if (!success) {
                JOptionPane.showMessageDialog(this, "No se pudo renombrar el directorio. Verifique que el nombre no esté duplicado.", "Error al Renombrar", JOptionPane.ERROR_MESSAGE);
            }

            // Registrar la accion en el log
            this.logger.log("El directorio '" + ((Directory) nodeParentObject).getName() + "' ha sido renombrado como: '" + newName + "'");

            // Actualizar componentes de UI
            this.updateFilesTable();
            this.updateBlockStoragePanel();

        } else if (nodeParentObject instanceof OurFile) {
            // Renombrar archivo
            boolean success = fsManager.renameFile(currentPath, newName);
            if (!success) {
                JOptionPane.showMessageDialog(this, "No se pudo renombrar el archivo. Verifique que el nombre no esté duplicado.", "Error al Renombrar", JOptionPane.ERROR_MESSAGE);

            }

            // Registrar la accion en el log
            this.logger.log("El archivo '" + ((OurFile) nodeParentObject).getName() + "' ha sido renombrado como: '" + newName + "'");

            // Actualizar componentes de UI
            this.updateFilesTable();
            this.updateBlockStoragePanel();

        } else {
            JOptionPane.showMessageDialog(this, "Solo se pueden renombrar archivos y directorios.", "Error al Renombrar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteAction() {
        System.out.println("current path: " + currentPath);
        // Obtener el nodo seleccionado como padre
        // DefaultTreeModel treeModel = (DefaultTreeModel) this.fileSystemTree.getModel();
        DefaultMutableTreeNode selectedParentNode = (DefaultMutableTreeNode) this.fileSystemTree.getLastSelectedPathComponent();

        if (selectedParentNode == null) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione un directorio valido para ser eliminado.", "Sin Selección", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Object nodeParentObject = selectedParentNode.getUserObject();

        int reply = JOptionPane.showConfirmDialog(this, "¿Estas seguro que quieres eliminar el elemento seleccionado?", "Eliminar un Archivo", JOptionPane.YES_NO_OPTION);
        if (reply == JOptionPane.YES_OPTION) {
            // Verificar si el nodo seleccionado es un directorio o un archivo
            if (nodeParentObject instanceof Directory) {
                // Eliminar el directorio (se debe verificar si recursivo o directorio unico)
                boolean supressionSuccess = fsManager.deleteDirectory(currentPath);
                if (!supressionSuccess) {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar el directorio. Verifique la operacion.", "Error al Renombrar", JOptionPane.ERROR_MESSAGE);
                }

                // Registrar la accion en el log
                this.logger.log("El documento '" + ((Directory) nodeParentObject).getName() + "' ha sido eliminado de la ruta: " + this.currentPath);

                // Actualizar componentes de UI
                this.updateFilesTable();
                this.updateBlockStoragePanel();
                this.updateMoreInfoPanel();

            } else if (nodeParentObject instanceof OurFile) {
                // Eliminar archivo
                boolean supressionSuccess = fsManager.deleteFile(currentPath);
                if (!supressionSuccess) {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar el archivo. Verifique la operacion.", "Error al Renombrar", JOptionPane.ERROR_MESSAGE);
                }

                // Registrar la accion en el log
                this.logger.log("El archivo '" + ((OurFile) nodeParentObject).getName() + "' ha sido eliminado de la ruta: " + this.currentPath);

                // Actualizar componentes de UI
                this.updateFilesTable();
                this.updateBlockStoragePanel();
                this.updateMoreInfoPanel();

            } else {
                JOptionPane.showMessageDialog(this, "Solo se pueden modificar archivos y directorios.", "Error al Eliminar", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Operacion cancelada...");
            return;
        }

    }

    private void moveAction() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) fileSystemTree.getLastSelectedPathComponent();
        if (selectedNode == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un archivo o directorio para mover.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Object nodeObject = selectedNode.getUserObject();
        if (!(nodeObject instanceof Directory || nodeObject instanceof OurFile)) {
            JOptionPane.showMessageDialog(this, "Selección invalida.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sourcePath = this.currentPath;

        if (sourcePath.equals("/root")) {
            JOptionPane.showMessageDialog(this, "No se puede mover la carpeta raíz.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Obtener lista de directorios disponibles
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) fileSystemTree.getModel().getRoot();
        String[] directories = this.collectDirectories(rootNode, "/root");

        // Mostrar cuadro de dialogo para seleccionar destino
        String destPath = (String) JOptionPane.showInputDialog(this, "Seleccione el destino:", "Mover Archivo/Carpeta",
                JOptionPane.QUESTION_MESSAGE, null, directories, directories.length > 0 ? directories[0] : null);

        if (destPath == null || destPath.isEmpty() || destPath.equals(sourcePath)) {
            return;
        }

        boolean success;
        if (nodeObject instanceof OurFile) {
            // Ya se actualiza el arbol
            success = fsManager.moveFile(sourcePath, destPath);
            // Registrar la accion en el log
            this.logger.log("El archivo '" + ((OurFile) nodeObject).getName() + "' se ha movido. Origen: " + sourcePath + ", Destino: " + destPath);

        } else {
            // Ya se actualiza el arbol
            success = fsManager.moveDirectory(sourcePath, destPath);
            // Registrar la accion en el log
            this.logger.log("El directorio '" + ((Directory) nodeObject).getName() + "' se ha movido. Origen: " + sourcePath + ", Destino: " + destPath);

        }

        if (!success) {
            JOptionPane.showMessageDialog(this, "Error al mover el elemento. Verifique la operación.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        JOptionPane.showMessageDialog(this, "El traslado se ha completado exitosamente.", "Operacion Completada", JOptionPane.INFORMATION_MESSAGE);
    }

    private String[] collectDirectories(DefaultMutableTreeNode node, String path) {
        String[] tempDirectories = new String[10];

        // Para obtener los sub directorios de la carpeta seleccionada
        int count = this.collectDirectoriesRecursive(node, tempDirectories, path, 0);
        String[] directories = new String[count];

        // JAVA: Copies an array from the specified source array, beginning at the specified position, to the specified position of the destination array.
        System.arraycopy(tempDirectories, 0, directories, 0, count);
        return directories;
    }

    private int collectDirectoriesRecursive(DefaultMutableTreeNode node, String[] directories, String path, int index) {

        Object userObject = node.getUserObject();
        if (userObject instanceof Directory && !path.equals(this.currentPath)) {
            directories[index++] = path;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            Object childObject = child.getUserObject();
            if (childObject instanceof Directory) {
                index = this.collectDirectoriesRecursive(child, directories, path + "/" + ((Directory) childObject).getName(), index);
            }
        }
        return index;
    }

    private void showInfoAction() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) fileSystemTree.getLastSelectedPathComponent();
        if (selectedNode == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un archivo o directorio para ver su información.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Object nodeObject = selectedNode.getUserObject();
        fileViewInfoPanel.removeAll();
        fileViewInfoPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel();
        titleLabel.setFont(new Font("Yu Gothic UI Semibold", Font.BOLD, 14));
        fileViewInfoPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel infoPanelToReplace = new JPanel();
        infoPanelToReplace.setLayout(new BoxLayout(infoPanelToReplace, BoxLayout.Y_AXIS));

        if (nodeObject instanceof OurFile) {
            OurFile file = (OurFile) nodeObject;
            titleLabel.setText(file.getName());

            JLabel parentLabel = new JLabel("Ubicación: " + currentPath.substring(0, currentPath.lastIndexOf('/')));
            JLabel sizeLabel = new JLabel("Tamaño: " + file.getSize() + " bloques");

            infoPanelToReplace.add(parentLabel);
            infoPanelToReplace.add(sizeLabel);
        } else if (nodeObject instanceof Directory) {
            Directory dir = (Directory) nodeObject;
            titleLabel.setText(dir.getName());

            JLabel filesLabel = new JLabel("Archivos en la carpeta:");
            infoPanelToReplace.add(filesLabel);

            SimpleList<OurFile> files = dir.getFiles();
            JList<String> fileList = new JList<>(convertFileListToArray(files));
            JScrollPane scrollPane = new JScrollPane(fileList);

            infoPanelToReplace.add(scrollPane);
        }

        fileViewInfoPanel.add(infoPanelToReplace, BorderLayout.CENTER);
        fileViewInfoPanel.revalidate();
        fileViewInfoPanel.repaint();
    }

    private String[] convertFileListToArray(SimpleList<OurFile> files) {

        String[] fileArray = new String[files.getSize()];

        for (int i = 0; i < files.getSize(); i++) {
            fileArray[i] = files.getValueByIndex(i).getName();
        }
        return fileArray;
    }

    private void updateBlockStoragePanel() {
        blockStoragePanel.removeAll(); // Limpiar el panel
        blockStoragePanel.revalidate();
        blockStoragePanel.repaint();

        // Obtener la matriz de almacenamiento
        String[][] guiMatrix = fsManager.getStorage().getStorageMatrixForGUI();
        int storageSize = fsManager.getStorage().getStorageSize();

        for (int row = 0; row < storageSize; row++) {
            for (int col = 0; col < storageSize; col++) {
                JLabel blockLabel = new JLabel();

                if (!guiMatrix[row][col].isEmpty()) {
                    // Bloque ocupado
                    blockLabel.setText(guiMatrix[row][col]);  // Mostrar el nombre del archivo
                    blockLabel.setOpaque(true);
                    blockLabel.setBackground(this.generateFileColor(guiMatrix[row][col]));  // Color basado en el nombre
                    blockLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    blockLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                } else {
                    // Bloque libre
                    blockLabel.setOpaque(true);
                    blockLabel.setBackground(Color.LIGHT_GRAY);
                    blockLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                }

                blockStoragePanel.add(blockLabel);
            }
        }

        // Configurar el layout del panel
        blockStoragePanel.setLayout(new GridLayout(storageSize, storageSize, 2, 2));

        // Actualizar el panel
        blockStoragePanel.revalidate();
        blockStoragePanel.repaint();
    }

    private void updateMoreInfoPanel() {
        // Limpiar el panel
        moreInfoPanel.removeAll();
        moreInfoPanel.setLayout(new BoxLayout(moreInfoPanel, BoxLayout.Y_AXIS));
        moreInfoPanel.revalidate();
        moreInfoPanel.repaint();

        // Obtener el almacenamiento
        Storage storage = fsManager.getStorage();

        // Calcular los bloques totales, usados y disponibles
        int totalBlocks = storage.getStorageSize() * storage.getStorageSize();
        int usedBlocks = totalBlocks - storage.getAvailableStorage();
        double usedPercentage = (double) usedBlocks / totalBlocks * 100;

        // Crear el texto con las estadísticas
        StringBuilder stats = new StringBuilder();
        stats.append("Bloques TOTALES: ").append(totalBlocks).append("\n");
        stats.append("Bloques usados: ").append(usedBlocks).append(" (").append(String.format("%.2f", usedPercentage)).append("%)\n");
        stats.append("Bloques disponibles: ").append(storage.getAvailableStorage()).append(" (").append(String.format("%.2f", 100 - usedPercentage)).append("%)\n");

        // Añadir el título
        JLabel titleLabel = new JLabel("Estado del Almacenamiento");
        titleLabel.setFont(new Font("Yu Gothic UI Semibold", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        moreInfoPanel.add(titleLabel);

        // Añadir el texto de estadísticas
        JTextArea statsTextArea = new JTextArea(stats.toString());
        statsTextArea.setFont(new Font("Arial", Font.PLAIN, 14));
        statsTextArea.setEditable(false);
        statsTextArea.setBackground(moreInfoPanel.getBackground());
        statsTextArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        moreInfoPanel.add(Box.createVerticalStrut(7));
        moreInfoPanel.add(statsTextArea);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue((int) usedPercentage);
        progressBar.setStringPainted(true);
        progressBar.setString("Uso del Almacenamiento: " + String.format("%.2f", usedPercentage) + "%");
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        moreInfoPanel.add(Box.createVerticalStrut(7));
        moreInfoPanel.add(progressBar);

        // Actualizar el panel
        moreInfoPanel.revalidate();
        moreInfoPanel.repaint();
    }


    /* OPCIONES DE TIPOS DE USUARIO */
    private void requestAdminPassword() {
        String password = JOptionPane.showInputDialog(this, "Ingrese la contraseña de administrador:", "Autenticación de Administrador", JOptionPane.QUESTION_MESSAGE);

        if (password != null && password.equals(ADMIN_PASSWORD)) {
            this.setAdminMode();
        } else if (password != null) {
            JOptionPane.showMessageDialog(this, "Contraseña incorrecta", "Error de Autenticación", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setRegularUserMode() {
        isAdminMode = false;
        regularUserItem.setEnabled(false);
        adminUserItem.setEnabled(true);

        // desactivar las opciones de edicion en el menu contextual
        this.disableContextMenuEditOptions();
        this.viewLogButton.setEnabled(false);
        JOptionPane.showMessageDialog(this, "Ha cambiado a Usuario Regular", "Cambio de Usuario", JOptionPane.INFORMATION_MESSAGE);

        // Registrar la acción en el log
        this.logger.log("* Modo de usuario cambiado exitosamente! - Tipo: Regular");

        this.setTitle("Simulador Sistema de Archivos - Modo Usuario Regular");
    }

    private void setAdminMode() {
        isAdminMode = true;
        regularUserItem.setEnabled(true);
        adminUserItem.setEnabled(false);

        // activar opciones de edicion
        this.enableContextMenuEditOptions();
        this.viewLogButton.setEnabled(true);
        JOptionPane.showMessageDialog(this, "Has accedido al Modo Administrador", "Cambio de Usuario", JOptionPane.INFORMATION_MESSAGE);

        // Registrar la acción en el log
        this.logger.log("* Modo de usuario cambiado exitosamente! - Tipo: ADMIN");

        this.setTitle("Simulador Sistema de Archivos - Modo Administrador");
    }

    private void disableContextMenuEditOptions() {

        if (this.createDirItem != null) {
            this.createDirItem.setEnabled(false);
        }
        if (this.createFileItem != null) {
            this.createFileItem.setEnabled(false);
        }
        if (this.renameItem != null) {
            this.renameItem.setEnabled(false);
        }

        if (this.deleteItem != null) {
            this.deleteItem.setEnabled(false);
        }
        if (this.moveItem != null) {
            this.moveItem.setEnabled(false);
        }

        // Solo habilitar la opción de "Ver informacion"
        if (this.infoItem != null) {
            this.infoItem.setEnabled(true);
        }
    }

    private void enableContextMenuEditOptions() {
        // Activamos todas las opciones
        if (this.createDirItem != null) {
            this.createDirItem.setEnabled(true);
        }
        if (this.createFileItem != null) {
            this.createFileItem.setEnabled(true);
        }
        if (this.renameItem != null) {
            this.renameItem.setEnabled(true);
        }

        if (this.deleteItem != null) {
            this.deleteItem.setEnabled(true);
        }
        if (this.moveItem != null) {
            this.moveItem.setEnabled(true);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        logRegisterView = new javax.swing.JDialog();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        logCloseButton = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        logTextArea = new javax.swing.JTextArea();
        mainPanel = new javax.swing.JPanel();
        viewTreePanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        fileSystemTree = new javax.swing.JTree();
        fileViewInfoPanel = new javax.swing.JPanel();
        moreInfoPanel = new javax.swing.JPanel();
        auxMainPanel = new javax.swing.JPanel();
        filesPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        filesJTable = new javax.swing.JTable();
        viewLogButton = new javax.swing.JButton();
        storageViewPanel = new javax.swing.JPanel();
        blockStoragePanel = new javax.swing.JPanel();
        mainMenuBar = new javax.swing.JMenuBar();
        fileMenuItem = new javax.swing.JMenu();
        saveOptionMenuItem = new javax.swing.JMenuItem();
        loadOptionMenuItem = new javax.swing.JMenuItem();
        userModeMenuItem = new javax.swing.JMenu();
        regularUserItem = new javax.swing.JMenuItem();
        adminUserItem = new javax.swing.JMenuItem();

        logRegisterView.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Yu Gothic UI Semibold", 1, 14)); // NOI18N
        jLabel1.setText("> REGISTRO DE OPERACIONES");

        logCloseButton.setText("Cerrar");
        logCloseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logCloseButtonActionPerformed(evt);
            }
        });

        logTextArea.setColumns(20);
        logTextArea.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 12)); // NOI18N
        logTextArea.setRows(5);
        jScrollPane4.setViewportView(logTextArea);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(198, 198, 198)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 638, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(logCloseButton)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(logCloseButton)
                .addGap(19, 19, 19))
        );

        javax.swing.GroupLayout logRegisterViewLayout = new javax.swing.GroupLayout(logRegisterView.getContentPane());
        logRegisterView.getContentPane().setLayout(logRegisterViewLayout);
        logRegisterViewLayout.setHorizontalGroup(
            logRegisterViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        logRegisterViewLayout.setVerticalGroup(
            logRegisterViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        viewTreePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Explorador de Archivos", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Yu Gothic UI Semibold", 0, 12))); // NOI18N
        viewTreePanel.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 12)); // NOI18N

        jScrollPane2.setViewportView(fileSystemTree);

        fileViewInfoPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Detalles", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Yu Gothic UI Semibold", 0, 12))); // NOI18N
        fileViewInfoPanel.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 12)); // NOI18N

        javax.swing.GroupLayout fileViewInfoPanelLayout = new javax.swing.GroupLayout(fileViewInfoPanel);
        fileViewInfoPanel.setLayout(fileViewInfoPanelLayout);
        fileViewInfoPanelLayout.setHorizontalGroup(
            fileViewInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        fileViewInfoPanelLayout.setVerticalGroup(
            fileViewInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 180, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout viewTreePanelLayout = new javax.swing.GroupLayout(viewTreePanel);
        viewTreePanel.setLayout(viewTreePanelLayout);
        viewTreePanelLayout.setHorizontalGroup(
            viewTreePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(viewTreePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(viewTreePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE)
                    .addComponent(fileViewInfoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        viewTreePanelLayout.setVerticalGroup(
            viewTreePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(viewTreePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(fileViewInfoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        moreInfoPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Información", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Yu Gothic UI Semibold", 0, 12))); // NOI18N

        javax.swing.GroupLayout moreInfoPanelLayout = new javax.swing.GroupLayout(moreInfoPanel);
        moreInfoPanel.setLayout(moreInfoPanelLayout);
        moreInfoPanelLayout.setHorizontalGroup(
            moreInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        moreInfoPanelLayout.setVerticalGroup(
            moreInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        auxMainPanel.setLayout(new java.awt.GridLayout(1, 0));

        filesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Tabla de Asignación", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Yu Gothic UI Semibold", 0, 12))); // NOI18N

        filesJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Archivo", "Bloques Asignados", "Dirección Primer Bloque"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        filesJTable.setEnabled(false);
        jScrollPane1.setViewportView(filesJTable);

        viewLogButton.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 12)); // NOI18N
        viewLogButton.setText("Ver Registro");
        viewLogButton.setEnabled(false);
        viewLogButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewLogButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout filesPanelLayout = new javax.swing.GroupLayout(filesPanel);
        filesPanel.setLayout(filesPanelLayout);
        filesPanelLayout.setHorizontalGroup(
            filesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(filesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(filesPanelLayout.createSequentialGroup()
                        .addComponent(viewLogButton)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        filesPanelLayout.setVerticalGroup(
            filesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(viewLogButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        auxMainPanel.add(filesPanel);

        storageViewPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Almacenamiento", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Yu Gothic UI Semibold", 0, 12))); // NOI18N

        javax.swing.GroupLayout blockStoragePanelLayout = new javax.swing.GroupLayout(blockStoragePanel);
        blockStoragePanel.setLayout(blockStoragePanelLayout);
        blockStoragePanelLayout.setHorizontalGroup(
            blockStoragePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        blockStoragePanelLayout.setVerticalGroup(
            blockStoragePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout storageViewPanelLayout = new javax.swing.GroupLayout(storageViewPanel);
        storageViewPanel.setLayout(storageViewPanelLayout);
        storageViewPanelLayout.setHorizontalGroup(
            storageViewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(storageViewPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(blockStoragePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        storageViewPanelLayout.setVerticalGroup(
            storageViewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(storageViewPanelLayout.createSequentialGroup()
                .addComponent(blockStoragePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        auxMainPanel.add(storageViewPanel);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(viewTreePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(auxMainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 863, Short.MAX_VALUE)
                    .addComponent(moreInfoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(auxMainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 421, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(moreInfoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(viewTreePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        fileMenuItem.setText("Archivo");

        saveOptionMenuItem.setText("Guardar estado");
        saveOptionMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveOptionMenuItemActionPerformed(evt);
            }
        });
        fileMenuItem.add(saveOptionMenuItem);

        loadOptionMenuItem.setText("Cargar estado");
        loadOptionMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadOptionMenuItemActionPerformed(evt);
            }
        });
        fileMenuItem.add(loadOptionMenuItem);

        mainMenuBar.add(fileMenuItem);

        userModeMenuItem.setText("Modo de Usuario");

        regularUserItem.setText("Usuario Regular");
        regularUserItem.setEnabled(false);
        regularUserItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                regularUserItemActionPerformed(evt);
            }
        });
        userModeMenuItem.add(regularUserItem);

        adminUserItem.setText("Administrador");
        adminUserItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adminUserItemActionPerformed(evt);
            }
        });
        userModeMenuItem.add(adminUserItem);

        mainMenuBar.add(userModeMenuItem);

        setJMenuBar(mainMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void regularUserItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_regularUserItemActionPerformed
        // TODO add your handling code here:
        if (isAdminMode) {
            this.setRegularUserMode();
        }
    }//GEN-LAST:event_regularUserItemActionPerformed

    private void adminUserItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adminUserItemActionPerformed
        // TODO add your handling code here:
        // Registrar la accion en el log
        this.logger.log("Se ha solicitado acceso de administrador... Estado de usuario actual: " + this.isAdminMode);

        if (!isAdminMode) {
            this.requestAdminPassword();
        }
    }//GEN-LAST:event_adminUserItemActionPerformed

    private void saveOptionMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveOptionMenuItemActionPerformed
        // TODO add your handling code here:
        this.fileManager.saveFileSystem(this.fsManager.getRootDirectory());

    }//GEN-LAST:event_saveOptionMenuItemActionPerformed

    private void loadOptionMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadOptionMenuItemActionPerformed
        // TODO add your handling code here:
        Directory newRootDir = this.fileManager.loadFileSystem(this.fsManager.getRootDirectory(), this.fsManager.getStorage(), this.fsManager.getStorage().getFileTable());
        this.fsManager.setRootDirectory(newRootDir);

        // ACTUALIZAR UI
        this.fsManager.updateTree();
        this.updateFilesTable();
        this.updateBlockStoragePanel();
        this.updateMoreInfoPanel();

        JOptionPane.showMessageDialog(this, "Estructura del sistema de archivos ha sido cargada correctamente!", "Filemanager", JOptionPane.INFORMATION_MESSAGE);

    }//GEN-LAST:event_loadOptionMenuItemActionPerformed

    private void logCloseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logCloseButtonActionPerformed
        // TODO add your handling code here:
        this.logRegisterView.dispose();
    }//GEN-LAST:event_logCloseButtonActionPerformed

    private void viewLogButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewLogButtonActionPerformed
        // TODO add your handling code here:
        this.showLogRegisterView(this);
    }//GEN-LAST:event_viewLogButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                System.out.println(info.getName());
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FileSystemUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FileSystemUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FileSystemUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FileSystemUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FileSystemUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem adminUserItem;
    private javax.swing.JPanel auxMainPanel;
    private javax.swing.JPanel blockStoragePanel;
    private javax.swing.JMenu fileMenuItem;
    private javax.swing.JTree fileSystemTree;
    private javax.swing.JPanel fileViewInfoPanel;
    private javax.swing.JTable filesJTable;
    private javax.swing.JPanel filesPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JMenuItem loadOptionMenuItem;
    private javax.swing.JButton logCloseButton;
    private javax.swing.JDialog logRegisterView;
    private javax.swing.JTextArea logTextArea;
    private javax.swing.JMenuBar mainMenuBar;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel moreInfoPanel;
    private javax.swing.JMenuItem regularUserItem;
    private javax.swing.JMenuItem saveOptionMenuItem;
    private javax.swing.JPanel storageViewPanel;
    private javax.swing.JMenu userModeMenuItem;
    private javax.swing.JButton viewLogButton;
    private javax.swing.JPanel viewTreePanel;
    // End of variables declaration//GEN-END:variables

    /**
     * @param aFileSystemUiInstance the fileSystemUiInstance to set
     */
    public static void setFileSystemUiInstance(FileSystemUI aFileSystemUiInstance) {
        fileSystemUiInstance = aFileSystemUiInstance;
    }

    /**
     * @return the fileSystemTree
     */
    public javax.swing.JTree getFileSystemTree() {
        return fileSystemTree;
    }

    /**
     * @param fileSystemTree the fileSystemTree to set
     */
    public void setFileSystemTree(javax.swing.JTree fileSystemTree) {
        this.fileSystemTree = fileSystemTree;
    }
}
