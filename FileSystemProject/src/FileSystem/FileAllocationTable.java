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
public class FileAllocationTable {

    private SimpleList<FileTableEntry> entries;

    public FileAllocationTable() {
        this.entries = new SimpleList<>();
    }

    public void addEntry(OurFile file) {
        BlockPosition firstBlock = file.getFirstBlock();
        if (firstBlock != null) {
            FileTableEntry entry = new FileTableEntry(
                    file.getName(),
                    file.getSize(),
                    "col: "+firstBlock.getRow() + ", row: " + firstBlock.getCol()
            );
            entries.addAtTheEnd(entry);
        }
    }

    public void removeEntry(OurFile file) {
        for (int i = 0; i < entries.getSize(); i++) {
            FileTableEntry entry = entries.getValueByIndex(i);
            if (entry.getFileName().equals(file.getName())) {
                entries.deleteByIndex(i);
                break;
            }
        }
    }

    public SimpleList<FileTableEntry> getEntries() {
        return entries;
    }

    /*
        Clase interna que sirve para cada entrada en la tabla
    */
    public class FileTableEntry {

        private String fileName;
        private int blocksAllocated;
        private String firstBlockAddress;

        public FileTableEntry(String fileName, int blocksAllocated, String firstBlockAddress) {
            this.fileName = fileName;
            this.blocksAllocated = blocksAllocated;
            this.firstBlockAddress = firstBlockAddress;
        }

        // Getters
        public String getFileName() {
            return fileName;
        }

        public int getBlocksAllocated() {
            return blocksAllocated;
        }

        public String getFirstBlockAddress() {
            return firstBlockAddress;
        }
    }
}
