/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Main.GUI;

import EDD.OurHashTable;
import FileSystem.Directory;
import FileSystem.OurFile;
import Managers.FileSystemManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
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
    private final FileSystemManager fsManager;
    //private String rootPath = "/root";
    private String currentPath = "";

    private JPopupMenu contextMenu;
    private final DefaultTableModel tableFilesModel;

    // parametros a considerar
    /**
     * Creates new form FileSystemUI
     */
    public FileSystemUI() {
        initComponents();
        // gui properties
        this.setTitle("Simulador Sistema de Archivos");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1320, 768);
        this.setLocationRelativeTo(null);
        //this.setResizable(false);

        // configuraciones
        // Iniciar el filesystem manager
        fsManager = new FileSystemManager(fileSystemTree);
        fsManager.updateTree();

        // Iniciar tabla de archivos
        tableFilesModel = new DefaultTableModel(new String[]{"Nombre de Archivo", "Bloques Asignados", "Primer Espacio de Bloque"}, 0);
        filesJTable.setModel(tableFilesModel);  // Vincular el modelo a la JTable

        // Initial updates
        this.updateFilesTable();
        this.setupContextMenu();
        this.setupListeners();

    }

    public static synchronized FileSystemUI getInstance() {
        if (fileSystemUiInstance == null) {
            setFileSystemUiInstance(new FileSystemUI());
        }
        return fileSystemUiInstance;
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
        JMenuItem createDirItem = new JMenuItem("Crear Directorio");
        JMenuItem createFileItem = new JMenuItem("Crear Archivo");
        JMenuItem renameItem = new JMenuItem("Renombrar");
        JMenuItem deleteItem = new JMenuItem("Eliminar");
        JMenuItem moveItem = new JMenuItem("Mover");
        JMenuItem infoItem = new JMenuItem("Ver Información");

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
            JOptionPane.showMessageDialog(this,
                    "Por favor seleccione un directorio donde quiere crear una carpeta.",
                    "Sin Selección",
                    JOptionPane.WARNING_MESSAGE);
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
                        this.updateFilesTable();

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
            // Actualizar la interfaz grafica
            this.updateFilesTable();
        } else if (nodeParentObject instanceof OurFile) {
            // Renombrar archivo
            boolean success = fsManager.renameFile(currentPath, newName);
            if (!success) {
                JOptionPane.showMessageDialog(this, "No se pudo renombrar el archivo. Verifique que el nombre no esté duplicado.", "Error al Renombrar", JOptionPane.ERROR_MESSAGE);

            }   // Actualizar la interfaz gráfica
            this.updateFilesTable();
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
                // Actualizar la interfaz grafica
                this.updateFilesTable();

            } else if (nodeParentObject instanceof OurFile) {
                // Renombrar archivo
                boolean supressionSuccess = fsManager.deleteFile(currentPath);
                if (!supressionSuccess) {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar el archivo. Verifique la operacion.", "Error al Renombrar", JOptionPane.ERROR_MESSAGE);
                }
                // Actualizar la interfaz gráfica
                this.updateFilesTable();

            } else {
                JOptionPane.showMessageDialog(this, "Solo se pueden modificar archivos y directorios.", "Error al Eliminar", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Operacion cancelada...");
            return;
        }

    }

    private void moveAction() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void showInfoAction() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        viewTreePanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        fileSystemTree = new javax.swing.JTree();
        controlPanel = new javax.swing.JPanel();
        filesPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        filesJTable = new javax.swing.JTable();
        storageViewPanel = new javax.swing.JPanel();
        blockStoragePanel = new javax.swing.JPanel();
        mainMenuBar = new javax.swing.JMenuBar();
        fileMenuItem = new javax.swing.JMenu();
        saveOptionMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        mainPanel.setBackground(new java.awt.Color(204, 255, 255));

        viewTreePanel.setBackground(new java.awt.Color(255, 204, 204));
        viewTreePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Explorador de Archivos"));

        jScrollPane2.setViewportView(fileSystemTree);

        javax.swing.GroupLayout viewTreePanelLayout = new javax.swing.GroupLayout(viewTreePanel);
        viewTreePanel.setLayout(viewTreePanelLayout);
        viewTreePanelLayout.setHorizontalGroup(
            viewTreePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, viewTreePanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        viewTreePanelLayout.setVerticalGroup(
            viewTreePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(viewTreePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 585, Short.MAX_VALUE)
                .addGap(131, 131, 131))
        );

        controlPanel.setBackground(new java.awt.Color(204, 153, 255));
        controlPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Controles"));

        javax.swing.GroupLayout controlPanelLayout = new javax.swing.GroupLayout(controlPanel);
        controlPanel.setLayout(controlPanelLayout);
        controlPanelLayout.setHorizontalGroup(
            controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1104, Short.MAX_VALUE)
        );
        controlPanelLayout.setVerticalGroup(
            controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 231, Short.MAX_VALUE)
        );

        filesPanel.setBackground(new java.awt.Color(153, 255, 153));
        filesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Tabla de Asignación"));

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

        javax.swing.GroupLayout filesPanelLayout = new javax.swing.GroupLayout(filesPanel);
        filesPanel.setLayout(filesPanelLayout);
        filesPanelLayout.setHorizontalGroup(
            filesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 446, Short.MAX_VALUE)
                .addContainerGap())
        );
        filesPanelLayout.setVerticalGroup(
            filesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(94, Short.MAX_VALUE))
        );

        storageViewPanel.setBackground(new java.awt.Color(204, 204, 255));
        storageViewPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Almacenamiento"));

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

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(viewTreePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(controlPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(filesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(storageViewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(viewTreePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(filesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(storageViewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(controlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        fileMenuItem.setText("Archivo");

        saveOptionMenuItem.setText("Guardar");
        fileMenuItem.add(saveOptionMenuItem);

        mainMenuBar.add(fileMenuItem);

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
    private javax.swing.JPanel blockStoragePanel;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JMenu fileMenuItem;
    private javax.swing.JTree fileSystemTree;
    private javax.swing.JTable filesJTable;
    private javax.swing.JPanel filesPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JMenuBar mainMenuBar;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuItem saveOptionMenuItem;
    private javax.swing.JPanel storageViewPanel;
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
