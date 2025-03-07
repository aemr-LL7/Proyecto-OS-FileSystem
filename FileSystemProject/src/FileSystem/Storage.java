package FileSystem;

public class Storage {

    private int storageSize;        // Dimension de la matriz
    private Data[][] storage;       // El tamaño se pasa para hacer una matriz nxn
    private static Storage instance;

    // Constructor para tamaño por defecto
    private Storage() {
        this.storageSize = 25;
        this.storage = new Data[25][25];
    }

    public static synchronized Storage getInstance() {
        if (instance == null) {
            instance = new Storage();
        }
        return instance;
    }
    
    

}
