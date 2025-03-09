/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package FileSystem;

import EDD.SimpleList;

/**
 *
 * @author Windows 11
 */
public class Directory {

    private String name;
    private Directory parent;
    private SimpleList<Directory> subdirectories;
    private SimpleList<OurFile> files;

    public Directory(String name, Directory parent) {
        this.name = name;
        this.parent = parent;
        this.subdirectories = new SimpleList<>();
        this.files = new SimpleList<>();
    }

    public void addSubdirectory(Directory dir) {
        this.subdirectories.addAtTheEnd(dir);
    }

    public void removeSubdirectory(Directory dir) {
        this.subdirectories.delete(dir);
    }

    // Métodos para gestionar archivos
    public void addFile(OurFile file) {
        this.files.addAtTheEnd(file);
    }

    public void removeFile(OurFile file) {
        this.files.delete(file);
    }

    // Metodo recursivo para eliminar todo un directorio
    public void deleteRecursive() {
        // Eliminar todos los archivos
        while (!files.isEmpty()) {
            OurFile file = this.files.getValueByIndex(0);
            Storage.getInstance().deleteFile(file);
            this.files.deleteByIndex(0);
        }

        // Eliminar todos los subdirectorios recursivamente
        while (!subdirectories.isEmpty()) {
            Directory dir = this.subdirectories.getValueByIndex(0);
            dir.deleteRecursive();
            this.subdirectories.deleteByIndex(0);
        }
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the parent
     */
    public Directory getParent() {
        return parent;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(Directory parent) {
        this.parent = parent;
    }

    /**
     * @return the subdirectories
     */
    public SimpleList<Directory> getSubdirectories() {
        return subdirectories;
    }

    /**
     * @param subdirectories the subdirectories to set
     */
    public void setSubdirectories(SimpleList<Directory> subdirectories) {
        this.subdirectories = subdirectories;
    }

    /**
     * @return the files
     */
    public SimpleList<OurFile> getFiles() {
        return files;
    }

    /**
     * @param files the files to set
     */
    public void setFiles(SimpleList<OurFile> files) {
        this.files = files;
    }
    
}
