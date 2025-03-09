/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package FileSystem;

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
    private SimpleList<OurFile> fileList;

    private Storage() {
        this.storageSize = 6;
        this.storageMatrix = new OurData[6][6];
        this.availableStorage = storageSize * storageSize;
    }

    public static synchronized Storage getInstance() {
        if (instance == null) {
            instance = new Storage();
        }
        return instance;
    }

    // Asignar bloques a un archivo usando asignacion encadenada
    public void allocateBlocks(OurFile file) {
        int blocksNeeded = file.getSize();

        // Verificar si hay suficientes bloques disponibles
        if (this.availableStorage < blocksNeeded) {
            System.out.println("Epa no tengo espacio papa");
            return;
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

        this.fileList.addAtTheEnd(file);
        this.availableStorage -= blocksNeeded;
    }

    // Liberar bloques de un archivo (Probablemente haya que buscar otra forma de referenciar el objeto archivo aca desde la UI)
    public void deleteFile(OurFile file) {

        SimpleNode<OurData> auxNode = file.getDataNodes().getpFirst();
        while(auxNode != null){
            int row = auxNode.getData().getIndexRow();
            int col = auxNode.getData().getIndexCol();
            this.storageMatrix[row][col] = null;
            
            //Reseteamos las posiciones desde los datanodes porque ya no estan en almacenamiento
            auxNode.getData().setStorageMatrixIndex(0, 0);
            
            auxNode = auxNode.getpNext();
            this.availableStorage++;
        }
        
        this.fileList.delete(file);
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
                if (!this.isIndexOccupied(i, j)) {
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
        return freePositions; // Devolver las posiciones encontradas
    }
    
    //Codigo para "Defragmentar" el almacenamiento
    public void defragmentStorage(){
        this.storageMatrix = new OurData[this.storageSize][this.storageSize];
        this.availableStorage = this.storageSize;
        
        SimpleNode<OurFile> auxNode = this.getFileList().getpFirst();
        while(auxNode != null){
            OurFile file = auxNode.getData();
            this.allocateBlocks(file);
            auxNode = auxNode.getpNext();
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

    public SimpleList<OurFile> getFileList() {
        return fileList;
    }

    public void setFileList(SimpleList<OurFile> fileList) {
        this.fileList = fileList;
    }
    
    

}
