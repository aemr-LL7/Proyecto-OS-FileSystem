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
public class OurFile {

    private int size;                   //Vamos a trabajar los tamayos como ints para poder meterlos en bloques
    private SimpleList<Data> dataNodes; //Lista de nodos que tienen la data
    private String name;

    public OurFile(int size, String name) {
        this.size = size;
        this.name = name;
        this.dataNodes = this.generateDataNodes(size);

    }

    public SimpleList<Data> generateDataNodes(int size) {

        SimpleList<Data> dataNodes = new SimpleList<>();
        for (int i = 0; i < size; i++) {
            Data newNode = new Data(i);
            newNode.setFather(this);
            dataNodes.addAtTheEnd(newNode);
        }

        return dataNodes;

    }

    public String getBlockChain() {
        Storage storage = Storage.getInstance();
        SimpleList<BlockPosition> positions = storage.getAllFileBlockPositions(this);

        StringBuilder chain = new StringBuilder();
        for (int i = 0; i < positions.getSize(); i++) {
            BlockPosition pos = positions.getValueByIndex(i);
            chain.append("(").append(pos.getRow()).append(",").append(pos.getCol()).append(")");
            if (i < positions.getSize() - 1) {
                chain.append(" → ");
            }
        }

        return chain.toString();
    }

    // Saber la posicion del primer bloque
    public BlockPosition getFirstBlock() {
        Storage storage = Storage.getInstance();
        return storage.getFirstBlockPosition(this);
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