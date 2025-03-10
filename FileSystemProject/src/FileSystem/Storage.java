/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package FileSystem;

import EDD.OurHashTable;
import EDD.SimpleList;
import EDD.SimpleNode;

/**
 *
 * @author B-St
 */
public class Storage {

    private int storageSize;       // Dimensión de la matriz
    private OurData[][] storageMatrix;       // Matriz que representa los bloques del disco
    private int availableStorage;
    private static Storage instance;
    private OurHashTable<OurFile> fileTable;

    private Storage() {
        this.storageSize = 6;
        this.storageMatrix = new OurData[6][6];
        this.availableStorage = storageSize * storageSize;
        this.fileTable = new OurHashTable<>();
    }

    public static synchronized Storage getInstance() {
        if (instance == null) {
            instance = new Storage();
        }
        return instance;
    }

    // Asignar bloques a un archivo usando asignacion encadenada
    public boolean allocateBlocks(OurFile file) {
        int blocksNeeded = file.getSize();
        String key = file.getName();

        // Verificar si hay suficientes bloques disponibles
        if (this.availableStorage < blocksNeeded) {
            System.out.println("Epa no tengo espacio papa");
            return false;
        }

        if (this.fileTable.isKeyTaken(key)) {
            System.out.println("Epa ese nombre esta tomado");
            return false;
        }

        //Matriz temporal para guardar las posiciones donde se hara la insercion, siempre vuelve como una matriz de nx2
        //donde n es el tamanyo del archivo y en cada posicion hay una tupla del tipo [fila,columna]
        int[][] freePositions = this.getFreePositions(blocksNeeded);

        SimpleNode<OurData> auxNode = file.getDataNodes().getpFirst();
        for (int positionMatrixIndex = 0; positionMatrixIndex < blocksNeeded; positionMatrixIndex++) {
            int row = freePositions[positionMatrixIndex][0];
            int col = freePositions[positionMatrixIndex][1];
            this.storageMatrix[row][col] = auxNode.getData();
            auxNode.getData().setStorageMatrixIndex(row, col);
            auxNode = auxNode.getpNext();
        }

        this.fileTable.put(key, file);
        this.availableStorage -= blocksNeeded;
        return true;
    }

    // Liberar bloques de un archivo (Probablemente haya que buscar otra forma de referenciar el objeto archivo aca desde la UI)
    public void deleteFile(OurFile file) {

        SimpleNode<OurData> auxNode = file.getDataNodes().getpFirst();
        while (auxNode != null) {
            int row = auxNode.getData().getIndexRow();
            int col = auxNode.getData().getIndexCol();
            this.storageMatrix[row][col] = null;

            //Reseteamos las posiciones desde los datanodes porque ya no estan en almacenamiento
            auxNode.getData().setStorageMatrixIndex(0, 0);

            auxNode = auxNode.getpNext();
            this.availableStorage++;
        }

        this.fileTable.delete(file.getName());
    }

    // Verifica si un nodo de datos pertenece a un archivo especifico
    private boolean belongsToFile(OurData data, OurFile file) {
        return data.getFather() == file;
    }

    // Obtener la cantidad de bloques disponibles
    public int countAvailableBlocks() {
        int count = 0;
        for (int i = 0; i < this.storageSize; i++) {
            for (int j = 0; j < this.storageSize; j++) {
                if (this.storageMatrix[i][j] == null) {
                    count++;
                }
            }
        }
        return count;
    }

    private int[][] getFreePositions(int blocksNeeded) {
        int[][] freePositions = new int[blocksNeeded][2];
        int freeCount = 0;

        // Recorrer la matriz para encontrar posiciones vacías
        for (int i = 0; i < storageSize; i++) {
            for (int j = 0; j < storageSize; j++) {
                if (this.storageMatrix[i][j] == null) {
                    freePositions[freeCount][0] = i; // Fila
                    freePositions[freeCount][1] = j; // Columna
                    freeCount++;

                    // Salir del bucle si ya hemos llenado el array
                    if (freeCount == blocksNeeded) {
                        return freePositions;
                    }
                }
            }
        }
        return null; // Si no se encontraron posiciones libres va nullo
    }

    //Codigo para "Defragmentar" el almacenamiento
    public void defragmentStorage() {
        this.storageMatrix = new OurData[this.storageSize][this.storageSize];
        this.availableStorage = this.storageSize;

        SimpleNode<OurFile> auxNode = this.fileTable.getEntriesList().getpFirst();
        while (auxNode != null) {
            OurFile file = auxNode.getData();
            this.allocateBlocks(file);
            auxNode = auxNode.getpNext();
        }

    }

    //Hacer metodo para que funcione el cambiar de nombre. Necesitamos poderle llegar al archivo y cambiarle el nombre y ya xd
    public void modifyFile(OurFile file, String newName) {

    }

    public void printStorageMatrix() {
        System.out.println("Estado actual de la matriz de almacenamiento:");
        for (int i = 0; i < storageMatrix.length; i++) {
            for (int j = 0; j < storageMatrix[i].length; j++) {
                OurData data = storageMatrix[i][j];
                if (data != null) {
                    System.out.print("[" + data.getFather().getName() + "] ");
                } else {
                    System.out.print("[ ] ");
                }
            }
            System.out.println();
        }
    }

    /**
     * @return the storageSize
     */
    public int getStorageSize() {
        return storageSize;
    }

    /**
     * @param storageSize the storageSize to set
     */
    public void setStorageSize(int storageSize) {
        this.storageSize = storageSize;
    }

    /**
     * @return the storageMatrix
     */
    public OurData[][] getBlocks() {
        return storageMatrix;
    }

    /**
     * @param blocks the storageMatrix to set
     */
    public void setBlocks(OurData[][] blocks) {
        this.storageMatrix = blocks;
    }

    private boolean isIndexOccupied(int i, int j) {
        return this.storageMatrix[i][j] == null;
    }

    public OurData[][] getStorageMatrix() {
        return storageMatrix;
    }

    public void setStorageMatrix(OurData[][] storageMatrix) {
        this.storageMatrix = storageMatrix;
    }

    public int getAvailableStorage() {
        return availableStorage;
    }

    public void setAvailableStorage(int availableStorage) {
        this.availableStorage = availableStorage;
    }

    public OurHashTable<OurFile> getFileTable() {
        return fileTable;
    }

    public void setFileTable(OurHashTable<OurFile> fileTable) {
        this.fileTable = fileTable;
    }

}
