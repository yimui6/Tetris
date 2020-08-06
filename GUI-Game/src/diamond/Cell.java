package diamond;

import java.awt.Image;

/**
 * 1�����ӣ����������У��У� �У� ��ͼƬ
 */
public class Cell {
	private int row;
	private int col;
	private Image image;//���ӵ���ͼ����������7�����͵��ĸ񷽿飩 
	
	public Cell() {
	}

	public Cell(int row, int col, Image image) {
		super();
		this.row = row;
		this.col = col;
		this.image = image;
	}

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
	
	
	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public void moveRight(){
		col++;
		//System.out.println("Cell moveRight()" + col); 
	}
	
	public void moveLeft(){
		col--;
	}
	
	public void moveDown(){
		row++;
	}
	
	@Override
	public String toString() {
		return "["+row+","+col+"]";
	}
}
