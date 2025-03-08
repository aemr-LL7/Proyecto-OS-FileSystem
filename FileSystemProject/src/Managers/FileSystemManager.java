/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Managers;

import EDD.SimpleList;
import FileSystem.BlockPosition;
import FileSystem.Directory;
import FileSystem.FileAllocationTable;
import FileSystem.OurFile;
import FileSystem.Storage;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author Windows 11
 */
public class FileSystemManager {

    private Directory rootDirectory;
    private JTree fileSystemTree;
    private FileAllocationTable fat;

    public FileSystemManager(JTree fileSystemTree) {
        this.fileSystemTree = fileSystemTree;
        this.rootDirectory = new Directory("/root", null);
        this.fat = new FileAllocationTable();
    }

    public void updateTree() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(getRootDirectory()); // Usar el objeto Directory
        this.buildTreeNode(rootNode, getRootDirectory());

        DefaultTreeModel model = (DefaultTreeModel) fileSystemTree.getModel();
        model.setRoot(rootNode);
        model.reload();
    }

// Recursively build the tree structure from the directory structure
    private void buildTreeNode(DefaultMutableTreeNode parentNode, Directory directory) {
        SimpleList<OurFile> files = directory.getFiles();
        for (int i = 0; i < files.getSize(); i++) {
            OurFile file = files.getValueByIndex(i);
            DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(file); // Guardar el objeto OurFile
            parentNode.add(fileNode);
        }

        // Add subdirectories recursively
        SimpleList<Directory> subdirs = directory.getSubdirectories();
        for (int i = 0; i < subdirs.getSize(); i++) {
            Directory subdir = subdirs.getValueByIndex(i);
            DefaultMutableTreeNode subdirNode = new DefaultMutableTreeNode(subdir); // Guardar el objeto Directory
            parentNode.add(subdirNode);
            this.buildTreeNode(subdirNode, subdir);
        }
    }

    /**
     * CRUD Operations
     */
    // Crear un directorio y verificar si existe
    public boolean createDirectory(String path, String name) {

        Directory parent = this.findDirectoryByPath(path);
        if (parent == null) {
            return false;
        }

        // Revisar si un directorio existe con el mismo nombre
        SimpleList<Directory> subdirs = parent.getSubdirectories();
        for (int i = 0; i < subdirs.getSize(); i++) {
            if (subdirs.getValueByIndex(i).getName().equals(name)) {
                return false;
            }
        }

        // Crear el nuevo directorio
        Directory newDir = new Directory(name, parent);
        parent.addSubdirectory(newDir);

        // Update tree
        this.updateTree();
        return true;
    }

    // Crear un archivo en un directorio especifico
    public boolean createFile(String dirPath, String fileName, int fileSize) {
        Directory parent = findDirectoryByPath(dirPath);
        if (parent == null) {
            return false;
        }

        // Revisa si ya existe
        SimpleList<OurFile> files = parent.getFiles();
        for (int i = 0; i < files.getSize(); i++) {
            if (files.getValueByIndex(i).getName().equals(fileName)) {
                return false;
            }
        }

        OurFile newFile = new OurFile(fileSize, fileName);

        // Asigna los bloques
        Storage storage = Storage.getInstance();
        boolean allocated = storage.allocateBlocks(newFile);

        if (!allocated) {
            return false;
        }

        // Añadir al directorio y a la entrada de FAT
        parent.addFile(newFile);
        getFat().addEntry(newFile);

        // Update tree
        this.updateTree();
        return true;
    }

    // Elimina un archivo, busca si existe en la estructura
    public boolean deleteFile(String path) {

        int lastSlash = path.lastIndexOf('/');
        if (lastSlash == -1) {
            return false;
        }

        String dirPath = path.substring(0, lastSlash);
        String fileName = path.substring(lastSlash + 1);

        Directory parent = this.findDirectoryByPath(dirPath);
        if (parent == null) {
            return false;
        }

        // Buscar el archivo
        SimpleList<OurFile> files = parent.getFiles();
        OurFile fileToDelete = null;

        for (int i = 0; i < files.getSize(); i++) {
            OurFile file = files.getValueByIndex(i);
            if (file.getName().equals(fileName)) {
                fileToDelete = file;
                break;
            }
        }

        if (fileToDelete == null) {
            return false;
        }

        // Vacia los bloques, elimina de FAT
        Storage.getInstance().freeBlocks(fileToDelete);
        getFat().removeEntry(fileToDelete);

        // Lo saca del directorio
        parent.removeFile(fileToDelete);

        // Update tree
        this.updateTree();
        return true;
    }

    // Elimina un directorio y sus archivos que contiene
    public boolean deleteDirectory(String path) {

        // No se elimina la raiz
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash == -1 || path.equals("/root")) {
            return false;
        }

        String parentPath = path.substring(0, lastSlash);
        String dirName = path.substring(lastSlash + 1);

        Directory parent = this.findDirectoryByPath(parentPath);
        if (parent == null) {
            return false;
        }

        // Find the directory
        SimpleList<Directory> subdirs = parent.getSubdirectories();
        Directory dirToDelete = null;

        for (int i = 0; i < subdirs.getSize(); i++) {
            Directory dir = subdirs.getValueByIndex(i);
            if (dir.getName().equals(dirName)) {
                dirToDelete = dir;
                break;
            }
        }

        if (dirToDelete == null) {
            return false;
        }

        // Delete recursively
        dirToDelete.deleteRecursive();
        parent.removeSubdirectory(dirToDelete);

        // Update tree
        this.updateTree();
        return true;
    }

    // Cambiar nombre a un archivo
    public boolean renameFile(String path, String newName) {

        int lastSlash = path.lastIndexOf('/');
        if (lastSlash == -1) {
            return false;
        }

        String dirPath = path.substring(0, lastSlash);
        String fileName = path.substring(lastSlash + 1);

        Directory parent = this.findDirectoryByPath(dirPath);
        if (parent == null) {
            return false;
        }

        SimpleList<OurFile> files = parent.getFiles();
        OurFile fileToRename = null;

        for (int i = 0; i < files.getSize(); i++) {
            OurFile file = files.getValueByIndex(i);
            if (file.getName().equals(fileName)) {
                fileToRename = file;
                break;
            }
        }

        if (fileToRename == null) {
            return false;
        }

        // Actualiza FAT
        getFat().removeEntry(fileToRename);
        fileToRename.setName(newName);
        getFat().addEntry(fileToRename);

        // Update tree
        this.updateTree();
        return true;
    }

    // Cambiar el nombre de un directorio
    public boolean renameDirectory(String path, String newName) {

        int lastSlash = path.lastIndexOf('/');
        if (lastSlash == -1 || path.equals("/root")) {
            return false; // No se debe renombrar la raiz
        }

        String parentPath = path.substring(0, lastSlash);
        String dirName = path.substring(lastSlash + 1);

        Directory parent = findDirectoryByPath(parentPath);
        if (parent == null) {
            return false;
        }

        SimpleList<Directory> subdirs = parent.getSubdirectories();
        Directory dirToRename = null;

        for (int i = 0; i < subdirs.getSize(); i++) {
            Directory dir = subdirs.getValueByIndex(i);
            if (dir.getName().equals(dirName)) {
                dirToRename = dir;
                break;
            }
        }

        if (dirToRename == null) {
            return false;
        }

        dirToRename.setName(newName);

        // Update tree
        this.updateTree();
        return true;
    }

    // Retorna informacion del archivo y su Blockchain
    public String getFileInfo(String path) {

        int lastSlash = path.lastIndexOf('/');
        if (lastSlash == -1) {
            return null;
        }

        String dirPath = path.substring(0, lastSlash);
        String fileName = path.substring(lastSlash + 1);

        Directory parent = findDirectoryByPath(dirPath);
        if (parent == null) {
            return null;
        }

        SimpleList<OurFile> files = parent.getFiles();
        OurFile file = null;

        for (int i = 0; i < files.getSize(); i++) {
            OurFile fileAux = files.getValueByIndex(i);
            if (fileAux.getName().equals(fileName)) {
                file = fileAux;
                break;
            }
        }

        if (file == null) {
            return null;
        }

        // Build info string
        StringBuilder info = new StringBuilder();
        info.append("File Name: ").append(file.getName()).append("\n");
        info.append("Size: ").append(file.getSize()).append(" blocks\n");
        info.append("Block Chain: ").append(file.getBlockChain()).append("\n");

        BlockPosition firstBlock = file.getFirstBlock();
        if (firstBlock != null) {
            info.append("First Block: (").append(firstBlock.getRow()).append(",")
                    .append(firstBlock.getCol()).append(")\n");
        }

        return info.toString();
    }

    // Mover archivo a otro directorio
    public boolean moveFile(String sourcePath, String destPath) {

        int lastSlash = sourcePath.lastIndexOf('/');
        if (lastSlash == -1) {
            return false;
        }

        String sourceDirPath = sourcePath.substring(0, lastSlash);
        String fileName = sourcePath.substring(lastSlash + 1);

        Directory sourceDir = this.findDirectoryByPath(sourceDirPath);
        Directory destDir = this.findDirectoryByPath(destPath);

        if (sourceDir == null || destDir == null) {
            return false;
        }

        SimpleList<OurFile> sourceFiles = sourceDir.getFiles();
        OurFile fileToMove = null;

        for (int i = 0; i < sourceFiles.getSize(); i++) {
            OurFile file = sourceFiles.getValueByIndex(i);
            if (file.getName().equals(fileName)) {
                fileToMove = file;
                break;
            }
        }

        if (fileToMove == null) {
            return false; // File not found
        }

        // Verificar si existe algun archivo en el directorio de destino con el mismo nombre
        SimpleList<OurFile> destFiles = destDir.getFiles();
        for (int i = 0; i < destFiles.getSize(); i++) {
            if (destFiles.getValueByIndex(i).getName().equals(fileName)) {
                return false;
            }
        }

        // Finalmente mueve el archivo
        sourceDir.removeFile(fileToMove);
        destDir.addFile(fileToMove);

        // Update tree
        this.updateTree();
        return true;
    }

    // Mover directorio hacia otro dir
    public boolean moveDirectory(String sourcePath, String destPath) {
        // No tocar raiz, caca-toche
        if (sourcePath.equals("/root")) {
            return false;
        }

        int lastSlash = sourcePath.lastIndexOf('/');
        if (lastSlash == -1) {
            return false;
        }

        String sourceParentPath = sourcePath.substring(0, lastSlash);
        String dirName = sourcePath.substring(lastSlash + 1);

        Directory sourceParent = findDirectoryByPath(sourceParentPath);
        Directory destDir = findDirectoryByPath(destPath);

        if (sourceParent == null || destDir == null) {
            return false;
        }

        // Asegurarse de que el destino no sea un subdirectorio del origen
        Directory tempDir = destDir;
        while (tempDir != null) {
            if (tempDir.getParent() != null && getFullPath(tempDir.getParent()).equals(sourcePath)) {
                return false;
            }
            tempDir = tempDir.getParent();
        }

        SimpleList<Directory> sourceSubdirs = sourceParent.getSubdirectories();
        Directory dirToMove = null;

        for (int i = 0; i < sourceSubdirs.getSize(); i++) {
            Directory dir = sourceSubdirs.getValueByIndex(i);
            if (dir.getName().equals(dirName)) {
                dirToMove = dir;
                break;
            }
        }

        if (dirToMove == null) {
            return false;
        }

        SimpleList<Directory> destSubdirs = destDir.getSubdirectories();
        for (int i = 0; i < destSubdirs.getSize(); i++) {
            if (destSubdirs.getValueByIndex(i).getName().equals(dirName)) {
                return false;
            }
        }

        // Move directory
        sourceParent.removeSubdirectory(dirToMove);
        dirToMove.setParent(destDir);
        destDir.addSubdirectory(dirToMove);

        // Update tree
        this.updateTree();
        return true;
    }

    /**
     * Get storage usage statistics
     *
     * @return String with storage statistics
     */
    public String printStorageStats() {
        Storage storage = Storage.getInstance();
        int totalBlocks = storage.getStorageSize() * storage.getStorageSize();
        int availableBlocks = storage.getAvailableBlocks();
        int usedBlocks = totalBlocks - availableBlocks;

        double usedPercentage = (double) (usedBlocks / totalBlocks) * 100;

        StringBuilder finalStats = new StringBuilder();
        finalStats.append("Estado del Almacenamiento:\n");
        finalStats.append("Bloques TOTALES: ").append(totalBlocks).append("\n");
        finalStats.append("Bloques usados: ").append(usedBlocks).append(" (").append(String.format("%.2f", usedPercentage)).append("%)\n");
        finalStats.append("Bloques disponibles: ").append(availableBlocks).append(" (").append(String.format("%.2f", 100 - usedPercentage)).append("%)\n");

        return finalStats.toString();
    }

    // Retorna la "tabla" FAT como string
    public String printFATContent() {
        SimpleList<FileAllocationTable.FileTableEntry> entries = getFat().getEntries();

        if (entries.getSize() == 0) {
            return "La Tabla de Asignación de Archivos (F.A.T) esta vacia.";
        }

        StringBuilder content = new StringBuilder();

        content.append("File Allocation Table:\n");
        content.append(String.format("%-20s %-15s %-15s\n", "Nombre de archivo", "Bloques", "Primer Bloque"));
        content.append("-".repeat(50)).append("\n");

        for (int i = 0; i < entries.getSize(); i++) {
            FileAllocationTable.FileTableEntry entry = entries.getValueByIndex(i);
            content.append(String.format("%-20s %-15d %-15s\n",
                    entry.getFileName(),
                    entry.getBlocksAllocated(),
                    entry.getFirstBlockAddress()));
        }

        return content.toString();
    }

    private Directory findDirectoryByPath(String path) {
        if (path.equals("/root") || path.equals("/")) {
            return getRootDirectory();
        }

        // Split path into components
        String[] components = path.split("/");

        // Start from root
        Directory current = getRootDirectory();

        // Skip the first component (empty due to leading slash) and the second (root)
        for (int i = 2; i < components.length; i++) {
            String dirName = components[i];
            boolean found = false;

            SimpleList<Directory> subdirs = current.getSubdirectories();
            for (int j = 0; j < subdirs.getSize(); j++) {
                Directory dir = subdirs.getValueByIndex(j);
                if (dir.getName().equals(dirName)) {
                    current = dir;
                    found = true;
                    break;
                }
            }

            if (!found) {
                return null; // Directory not found
            }
        }

        return current;
    }

    // Obtener la ruta completa de un directorio
    private String getFullPath(Directory dir) {

        if (dir.getParent() == null) {
            return "/root";
        }

        return getFullPath(dir.getParent()) + "/" + dir.getName();
    }

    /**
     * @return the rootDirectory
     */
    public Directory getRootDirectory() {
        return rootDirectory;
    }

    /**
     * @return the fat
     */
    public FileAllocationTable getFat() {
        return fat;
    }
}
