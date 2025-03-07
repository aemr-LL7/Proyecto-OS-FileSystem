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
public class File {

    private int size;                   //Vamos a trabajar los tamayos como ints para poder meterlos en bloques
    private SimpleList<Data> dataNodes; //Lista de nodos que tienen la data
    private String name;

    public File(int size, String name) {
        this.size = size;
        this.name = name;
        this.dataNodes = this.generateDataNodes(size);
        
    }

    private SimpleList<Data> generateDataNodes(int size){
        
        SimpleList<Data> dataNodes = new SimpleList<>();
        for (int i=0; i < size; i++){
            Data newNode = new Data(i);
            newNode.setFather(this);
            dataNodes.addAtTheEnd(newNode);
        }
        
        return dataNodes;
        
    }
    
    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public SimpleList<Data> getDataNodes() {
        return dataNodes;
    }

    public void setDataNodes(SimpleList<Data> dataNodes) {
        this.dataNodes = dataNodes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    

}
