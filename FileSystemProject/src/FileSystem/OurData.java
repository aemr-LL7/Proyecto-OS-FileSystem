/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package FileSystem;

/**
 *
 * @author B-St
 */
class OurData {
    private int dataNumber;
    private OurFile father;
    private int[] storageIndex = new int[2];  //Para guardar el indice en la matriz de almacenamiento [Fila, Columna]

    public OurData(int dataNumber) {
        this.dataNumber = dataNumber;
        this.father = null;
    }
    
    public OurData(int dataNumber, int row, int col) {
        this.dataNumber = dataNumber;
        this.father = null;
        this.storageIndex[0] = row;
        this.storageIndex[1] = col;
    }

    public int getDataNumber() {
        return dataNumber;
    }

    public void setDataNumber(int dataNumber) {
        this.dataNumber = dataNumber;
    }

    public OurFile getFather() {
        return father;
    }

    public void setFather(OurFile father) {
        this.father = father;
    }
    
    public void setIndexRow(int row){
        this.storageIndex[0] = row;
    }
    
    public void setIndexCol(int col){
        this.storageIndex[1] = col;
    }
    
    public int getIndexRow(){
        return this.storageIndex[0];
    }
    
    public int getIndexCol() {
        return this.storageIndex[1];
    }
    
    public int[] getStorageIndex(){
        return this.storageIndex;
    }
    
    public void setStorageMatrixIndex(int row, int col){
        this.storageIndex[0] = row;
        this.storageIndex[1] = col;
    }
    
}
