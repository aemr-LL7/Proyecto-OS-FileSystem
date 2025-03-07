/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Main.GUI;

import FileSystem.Directory;
import FileSystem.FileAllocationTable;
import FileSystem.OurFile;
import FileSystem.Storage;
import Managers.FileSystemManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
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
    private String rootPath = "/root";
    private String currentPath = "";

    private JPopupMenu contextMenu;

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
        // Iniciar componentes
        this.setupContextMenu();
        this.setupListeners();

        // Iniciar el filesystem manager
        fsManager = new FileSystemManager(fileSystemTree);
        fsManager.updateTree();
    }

    public static synchronized FileSystemUI getInstance() {
        if (fileSystemUiInstance == null) {
            setFileSystemUiInstance(new FileSystemUI());
        }
        return fileSystemUiInstance;
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
            System.out.println(selectionPath);
            if (selectionPath != null) {
                StringBuilder pathBuilder = new StringBuilder("/root");
                // Para evitar tocar la raiz se inicia desde 1
                for (int i = 1; i < selectionPath.getPathCount(); i++) {
                    pathBuilder.append("/").append(selectionPath.getPathComponent(i).toString().split(" ")[0]);
                }
                this.currentPath = pathBuilder.toString();
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

                Directory newDir = new Directory(directoryName, parentDir);
                parentDir.addSubdirectory(newDir);

                // Añadir nuevo directorio al MODELO del jtree
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newDir);
                treeModel.insertNodeInto(newNode, selectedParentNode, selectedParentNode.getChildCount());

                this.fileSystemTree.expandPath(new TreePath(selectedParentNode.getPath()));

                System.out.println("WE MADE IT DIRECTORY NOW");
                // Se actualiza automaticamente la UI
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Directorios solo puedenser creados sobre otros directorios.",
                    "No se puede crear Directorio",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to handle file creation
    private void createFileAction() {
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

        Object nodeObject = selectedParentNode.getUserObject();

        if (nodeObject instanceof Directory) {

            String fileName = JOptionPane.showInputDialog(this, "Ingrese el nombre del archivo::", "Crear Archivo", JOptionPane.QUESTION_MESSAGE);

            if (fileName != null && !fileName.trim().isEmpty()) {

                String sizeStr = JOptionPane.showInputDialog(this, "Añadir el tamaño del archivo en (blocks):", "Tamaño Archivo", JOptionPane.QUESTION_MESSAGE);
                try {
                    int size = Integer.parseInt(sizeStr);
                    OurFile newFile = new OurFile(size, fileName);

                    boolean allocated = Storage.getInstance().allocateBlocks(newFile);

                    if (allocated) {
                        Directory parentDir = (Directory) nodeObject;

                        parentDir.addFile(newFile);

                        FileAllocationTable fat = new FileAllocationTable();
                        fat.addEntry(newFile);

                        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newFile);
                        treeModel.insertNodeInto(newNode, selectedParentNode, selectedParentNode.getChildCount());

                        this.fileSystemTree.expandPath(new TreePath(selectedParentNode.getPath()));

                        System.out.println("WE MADE IT NEW FILES NOW");

                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Not enough storage space for this file size.",
                                "Allocation Failed",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this,
                            "Please enter a valid number for file size.",
                            "Invalid Size",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (nodeObject instanceof OurFile) {
            // Show an error if trying to create a file inside another file
            JOptionPane.showMessageDialog(this,
                    "Files cannot contain other files. Please select a directory.",
                    "Cannot Create File",
                    JOptionPane.ERROR_MESSAGE);
        } else {
            // For any other case, show generic error
            JOptionPane.showMessageDialog(this,
                    "Files can only be created inside directories.",
                    "Cannot Create File",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void renameAction() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void deleteAction() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
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
        mainMenuBar = new javax.swing.JMenuBar();
        fileMenuItem = new javax.swing.JMenu();
        saveOptionMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        mainPanel.setBackground(new java.awt.Color(204, 255, 255));

        viewTreePanel.setBackground(new java.awt.Color(255, 204, 204));

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
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 608, Short.MAX_VALUE)
                .addGap(131, 131, 131))
        );

        controlPanel.setBackground(new java.awt.Color(204, 153, 255));
        controlPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Controles"));

        javax.swing.GroupLayout controlPanelLayout = new javax.swing.GroupLayout(controlPanel);
        controlPanel.setLayout(controlPanelLayout);
        controlPanelLayout.setHorizontalGroup(
            controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1114, Short.MAX_VALUE)
        );
        controlPanelLayout.setVerticalGroup(
            controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 231, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(viewTreePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(controlPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(viewTreePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
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
    private javax.swing.JPanel controlPanel;
    private javax.swing.JMenu fileMenuItem;
    private javax.swing.JTree fileSystemTree;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JMenuBar mainMenuBar;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuItem saveOptionMenuItem;
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
