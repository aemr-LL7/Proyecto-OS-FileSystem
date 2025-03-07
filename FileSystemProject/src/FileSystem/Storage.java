/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package FileSystem;

import EDD.SimpleList;

/**
 *
 * @author B-St
 */
public class Storage {

    private int storageSize;       // Dimensión de la matriz
    private Data[][] blocks;       // Matriz que representa los bloques del disco
    private boolean[][] occupied;  // Matriz para rastrear bloques ocupados
    private static Storage instance;

    private Storage() {
        this.storageSize = 25;
        this.blocks = new Data[25][25];
        this.occupied = new boolean[25][25];
        // Inicializar todos los bloques como libres
        for (int i = 0; i < 25; i++) {
            for (int j = 0; j < 25; j++) {
                this.occupied[i][j] = false;
            }
        }
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
        SimpleList<BlockPosition> positions = new SimpleList<>();

        // Verificar si hay suficientes bloques disponibles
        if (getAvailableBlocks() < blocksNeeded) {
            return false;
        }

        // Buscar bloques libres y asignarlos
        for (int i = 0; i < this.storageSize && blocksNeeded > 0; i++) {
            for (int j = 0; j < this.storageSize && blocksNeeded > 0; j++) {
                if (!this.occupied[i][j]) {
                    this.occupied[i][j] = true;
                    positions.addAtTheEnd(new BlockPosition(i, j));
                    blocksNeeded--;
                }
            }
        }

        // Asignar los bloques al archivo (vincular los nodos de datos)
        SimpleList<Data> dataNodes = file.getDataNodes();

        for (int i = 0; i < positions.getSize(); i++) {
            BlockPosition pos = positions.getValueByIndex(i);
            Data dataNode = dataNodes.getValueByIndex(i);
            this.blocks[pos.getRow()][pos.getCol()] = dataNode;
        }

        return true;
    }

    // Liberar bloques de un archivo
    public void freeBlocks(OurFile file) {
        SimpleList<Data> dataNodes = file.getDataNodes();

        // Buscar los bloques del archivo y liberarlos
        for (int i = 0; i < getStorageSize(); i++) {
            for (int j = 0; j < getStorageSize(); j++) {
                if (this.blocks[i][j] != null && this.belongsToFile(this.blocks[i][j], file)) {
                    this.blocks[i][j] = null;
                    this.occupied[i][j] = false;
                }
            }
        }
    }

    // Verifica si un nodo de datos pertenece a un archivo especifico
    private boolean belongsToFile(Data data, OurFile file) {
        return data.getFather() == file;
    }

    // Obtener la cantidad de bloques disponibles
    public int getAvailableBlocks() {
        int count = 0;
        for (int i = 0; i < this.storageSize; i++) {
            for (int j = 0; j < this.storageSize; j++) {
                if (!this.occupied[i][j]) {
                    count++;
                }
            }
        }
        return count;
    }

    // Busca la ubicacion del 1er bloque de un archivo
    public BlockPosition getFirstBlockPosition(OurFile file) {

        for (int i = 0; i < this.storageSize; i++) {
            for (int j = 0; j < this.storageSize; j++) {
                if (this.blocks[i][j] != null && getBlocks()[i][j].getFather() == file && this.blocks[i][j].getDataNumber() == 0) {
                    return new BlockPosition(i, j);
                }
            }
        }
        return null;
    }

    // Obtener todos los bloques asignados a un archivo
    public SimpleList<BlockPosition> getAllFileBlockPositions(OurFile file) {
        
        SimpleList<BlockPosition> positions = new SimpleList<>();

        for (int i = 0; i < this.storageSize; i++) {
            for (int j = 0; j < this.storageSize; j++) {
                if (this.blocks[i][j] != null && this.blocks[i][j].getFather() == file) {
                    positions.addAtTheEnd(new BlockPosition(i, j, this.blocks[i][j].getDataNumber()));
                }
            }
        }

        // Ordenar por número de datos para mantener el orden lógico
        // Esto es una simplificación - necesitarías implementar el ordenamiento
        return positions;
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
     * @return the blocks
     */
    public Data[][] getBlocks() {
        return blocks;
    }

    /**
     * @param blocks the blocks to set
     */
    public void setBlocks(Data[][] blocks) {
        this.blocks = blocks;
    }

    /**
     * @return the occupied
     */
    public boolean[][] getOccupied() {
        return occupied;
    }

    /**
     * @param occupied the occupied to set
     */
    public void setOccupied(boolean[][] occupied) {
        this.occupied = occupied;
    }

}
