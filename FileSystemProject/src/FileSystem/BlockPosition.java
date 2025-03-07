/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package FileSystem;

/**
 *
 * @author Windows 11
 */
public class BlockPosition {

    private int row;
    private int col;
    private int dataIndex;

    public BlockPosition(int row, int col) {
        this.row = row;
        this.col = col;
        this.dataIndex = 0;
    }

    public BlockPosition(int row, int col, int dataIndex) {
        this.row = row;
        this.col = col;
        this.dataIndex = dataIndex;
    }

    // Getters y setters
    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getDataIndex() {
        return dataIndex;
    }

    public void setDataIndex(int dataIndex) {
        this.dataIndex = dataIndex;
    }
}
