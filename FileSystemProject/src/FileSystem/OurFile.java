/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package FileSystem;

import EDD.OurQueue;
import EDD.SimpleList;

/**
 *
 * @author B-St
 */
public class OurFile {

    private int size;                       //Vamos a trabajar los tamayos como ints para poder meterlos en bloques
    private SimpleList<OurData> dataNodes;     //Lista de nodos que tienen la data
    private String name;

    public OurFile(int size, String name) {
        this.size = size;
        this.name = name;
        this.dataNodes = this.generateDataNodes(size);

    }

    private SimpleList<OurData> generateDataNodes(int size) {

        SimpleList<OurData> dataNodes = new SimpleList<>();
        for (int i = 0; i < size; i++) {
            OurData newNode = new OurData(i);
            newNode.setFather(this);
            dataNodes.addAtTheEnd(newNode);
        }

        return dataNodes;
    }

    public String getFirstBlockAddress() {
        if (dataNodes.isEmpty()) {
            return "N/A";  // No hay bloques asignados
        }

        // Obtener el primer nodo de datos
        OurData firstDataNode = dataNodes.getValueByIndex(0);

        // Devolver la posición del primer bloque
        return "row, col: [" + firstDataNode.getIndexRow() + ", " + firstDataNode.getIndexCol() + "]";
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public SimpleList<OurData> getDataNodes() {
        return dataNodes;
    }

    public void setDataNodes(SimpleList<OurData> dataNodes) {
        this.dataNodes = dataNodes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
