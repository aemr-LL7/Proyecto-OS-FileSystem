package FileSystem;

import EDD.OurHashTable;

public class Storage {

    private int storageSize;              // Dimension de la matriz
    private int availableStorage;         //Almacenamiento que se puede usar
    private Data[][] storage;             // El tamaño se pasa para hacer una matriz nxn
    private static Storage instance; 
    private OurHashTable<File> fileTable; //Tabla para hashear archivos

    // Constructor para tamaño por defecto
    private Storage() {
        this.storageSize = 6;         
        this.availableStorage = 6*6; //Tamanyo default del almacenamiento
        this.storage = new Data[6][6];
        this.fileTable = new OurHashTable<>();
    }

    public static synchronized Storage getInstance() {
        if (instance == null) {
            instance = new Storage();
        }
        return instance;
    }
    
    public void addFile(File file){
        
    }
    
    public void deleteFile(File file){
        
    }
    
    public boolean isFileAlreadyIn(File file){
        return false;
    }
    
    public void clearMatrix(){
        this.storage = new Data[storageSize][storageSize];
    }

    public int getStorageSize() {
        return storageSize;
    }

    public void setStorageSize(int storageSize) {
        this.storageSize = storageSize;
    }

    public Data[][] getStorage() {
        return storage;
    }

    public void setStorage(Data[][] storage) {
        this.storage = storage;
    }

    public OurHashTable<File> getFileTable() {
        return fileTable;
    }

    public void setFileTable(OurHashTable<File> fileTable) {
        this.fileTable = fileTable;
    }

}
