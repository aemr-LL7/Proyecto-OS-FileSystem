/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package FileSystem;

/**
 *
 * @author B-St
 */
class Data {
    private int dataNumber;
    private OurFile father;

    public Data(int dataNumber) {
        this.dataNumber = dataNumber;
        this.father = null;
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
    
}
