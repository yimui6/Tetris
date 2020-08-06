//主程序
package diamond;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.*;
//使用Jpanel做容器
public class Tetris extends JPanel{
	
	/** 正在下落方块 */
	private Tetromino tetromino;
	/** 下一个下落方块 */
	private Tetromino nextOne;
	/** 行数 */
	public static final int ROWS = 20;
	/** 列数 */
	public static final int COLS = 10;
	/** 墙 */
	private Cell[][] wall = new Cell[ROWS][COLS]; 
	/** 消掉的行数 */
	private int lines;
	/** 分数 */
	private int score;
	
	public static final int CELL_SIZE = 26;
	
	private static Image background;//背景图片
	public static Image I;
	public static Image J;
	public static Image L;
	public static Image S;
	public static Image Z;
	public static Image O;
	public static Image T;
	static{//加载静态资源的，加载图片
		
		try{
			background = ImageIO.read(Tetris.class.getResource("BG1.png"));//导入游戏背景图片
			T=ImageIO.read(Tetris.class.getResource("T.png"));
			I=ImageIO.read(Tetris.class.getResource("I.png"));
			S=ImageIO.read(Tetris.class.getResource("S.png"));
			Z=ImageIO.read(Tetris.class.getResource("Z.png"));
			L=ImageIO.read(Tetris.class.getResource("L.png"));
			J=ImageIO.read(Tetris.class.getResource("J.png"));
			O=ImageIO.read(Tetris.class.getResource("O.png"));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/**键盘事件响应*/
	public void action(){
		//tetromino = Tetromino.randomTetromino();
		//nextOne = Tetromino.randomTetromino();
		//wall[19][2] = new Cell(19,2,Tetris.T);
		startAction();
		repaint();
		KeyAdapter l = new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				if(key == KeyEvent.VK_Q){
					System.exit(0);//退出当前的Java进程
				}
				if(gameOver){
					if(key==KeyEvent.VK_S)   startAction();
					return;
				}
				//如果是暂停状态并且按键是[C]就继续动作
				if(pause){//pause = false
					if(key==KeyEvent.VK_C){	continueAction();	}
					return;
				}
				//否则处理其它按键
				switch(key){
				case KeyEvent.VK_RIGHT: moveRightAction(); break;
				case KeyEvent.VK_LEFT: moveLeftAction(); break;
				case KeyEvent.VK_DOWN: softDropAction() ; break;
				case KeyEvent.VK_UP: rotateRightAction() ; break;
				case KeyEvent.VK_Z: rotateLeftAction() ; break;
				case KeyEvent.VK_SPACE: hardDropAction() ; break;
				case KeyEvent.VK_P: pauseAction() ; break;
				case KeyEvent.VK_E:endAction();break;
				}
				repaint();
			}
		};
		this.requestFocus();
		this.addKeyListener(l);
	}
	/**绘制主界面*/
	public void paint(Graphics g){
		g.drawImage(background, 0, 0, null);//使用this 作为观察者
		g.translate(15, 15);//平移绘图坐标系
		paintTetromino(g);//绘制正在下落的方块
		paintWall(g);//画墙
		paintNextOne(g);
		paintScore(g);
	}
	
	public static final int FONT_COLOR = 0x008899;
	public static final int FONT_SIZE = 0x16;
	
	/**绘制右半部分面板*/
	private void paintScore(Graphics g) {
		Font f = getFont();//获取当前的 面板默认字体
		Font font = new Font(
				f.getName(), Font.BOLD, FONT_SIZE);
		int x = 290;
		int y = 162;
		g.setColor(new Color(FONT_COLOR));
		g.setFont(font);
		String str = "SCORE:"+this.score;
		g.drawString(str, x, y);
		y+=56;
		str = "LINES:"+this.lines;
		g.drawString(str, x, y);
		y+=56;
		str = "[P]暂停 [E]结束";
		if(pause){str = "[C]继续  [E]结束";}
		if(gameOver){	str = "[S]Start!";}
		g.drawString(str, x, y);
	}
	
	/**画正在下落的四格方块 */
	private void paintTetromino(Graphics g) {
		Cell[] cells = tetromino.getCells();
		for(int i=0; i<cells.length; i++){
			Cell c = cells[i];
			int x = c.getCol() * CELL_SIZE-1;
			int y = c.getRow() * CELL_SIZE-1;
			
			g.drawImage(c.getImage(), x, y, null);
		}		
	}
	
	/**绘制下一个四格方块*/
	private void paintNextOne(Graphics g) {
		Cell[] cells = nextOne.getCells();
		for(int i=0; i<cells.length; i++){
			Cell c = cells[i];
			int x = (c.getCol()+10) * CELL_SIZE-1;
			int y = (c.getRow()+1) * CELL_SIZE-1;
			g.drawImage(c.getImage(), x, y, null);
		}	
	}
	
	/**画墙*/
		private void paintWall(Graphics g){        //10*20
			for(int row=0; row<wall.length; row++){
				//迭代每一行, i = 0 1 2 ... 19
				Cell[] line = wall[row];
				//line.length = 10
				for(int col=0; col<line.length; col++){
					Cell cell = line[col]; 
					int x = col*CELL_SIZE; 
					int y = row*CELL_SIZE;
					if(cell==null){
						//g.setColor(new Color(0));   颜色置为0
						//画方形 
						//g.drawRect(x, y, CELL_SIZE, CELL_SIZE);
					}else{
						g.drawImage(cell.getImage(), x-1, y-1, null);
					}
				}
			}
		}	
	
		/**
		  自然下落的动作
		   完成功能：如果能够下落就下落，否则就着陆到墙上，同时新的方块出现并开始落下。
		 */
		public void softDropAction(){
			if(tetrominoCanDrop()){
				tetromino.softDrop();
			}else{
				tetrominoLandToWall();
				destroyLines();//破坏满的行
				checkGameOver();
				tetromino = nextOne;
				nextOne = Tetromino.randomTetromino();
			}
		}
		
		/** 销毁已经满的行，并且计分
		 * 迭代每一行 如果（检查）某行满是格子了 就销毁这行 
		 **/
		public void destroyLines(){
			int lines = 0;
			for(int row = 0; row<wall.length; row++){
				if(fullCells(row)){
					deleteRow(row);
					lines++;
				}
			}
			
			this.lines += lines;//0 1 2 3 4
			this.score += SCORE_TABLE[lines];
		}
		private static final int[] SCORE_TABLE={0,1,10,30,200};
		/**检查行满*/ 	
		public boolean fullCells(int row){
			Cell[] line = wall[row];
			for(int i=0; i<line.length; i++){
				if(line[i]==null){//如果有空格式就不是满行
					return false;
				}
			}
			return true;
		}
		/**消行 ,上面的一行要落下来补掉消失的这行*/
		public void deleteRow(int row){  
			for(int i=row; i>=1; i--){
				//复制 [i-1] -> [i] 
				System.arraycopy(wall[i-1], 0, wall[i], 0, COLS);
			}
			Arrays.fill(wall[0], null);
		}
		
		/** 检查当前的4格方块能否继续下落 */
		public boolean tetrominoCanDrop(){
			Cell[] cells = tetromino.getCells();
			for(int i = 0; i<cells.length; i++){
				Cell cell = cells[i];
				int row = cell.getRow(); int col = cell.getCol();
				if(row == ROWS-1){return false;}//到底就不能下降了
			}
			for(int i = 0; i<cells.length; i++){
				Cell cell = cells[i];
				int row = cell.getRow(); int col = cell.getCol();
				if(wall[row+1][col] != null){
					return false;//下方墙上有方块就不能下降了
				}
			}
			return true;
		}
		/** 4格方块着陆到墙上 */
		public void tetrominoLandToWall(){
			Cell[] cells = tetromino.getCells();
			for(int i=0; i<cells.length; i++){
				Cell cell = cells[i];
				int row = cell.getRow();
				int col = cell.getCol();
				wall[row][col] = cell;
			}
		}
		/** 4格方块右移 */
		public void moveRightAction(){
			tetromino.moveRight();
			if(outOfBound() || coincide()){
				tetromino.moveLeft();
			}
		}
		/** 4格方块左移 */
		public void moveLeftAction(){
			tetromino.moveLeft();
			if(outOfBound() || coincide()){
				tetromino.moveRight();
			}
		}
	
		/** 判是否出界了 */
		private boolean outOfBound(){
			Cell[] cells = tetromino.getCells();
			for(int i=0; i<cells.length; i++){
				Cell cell = cells[i];
				int col = cell.getCol();
				if(col<0 || col>=COLS){
					return true;
				}
			}
			return false;
		}
		/**判重合*/
		private boolean coincide(){
			Cell[] cells = tetromino.getCells();
			//for each 循环、迭代，简化了"数组迭代书写"
			for(Cell cell: cells){//Java 5 以后提供增强版for循环
				int row = cell.getRow();
				int col = cell.getCol();
				if(row<0 || row>=ROWS || col<0 || col>=COLS || 
						wall[row][col]!=null){
					return true; //墙上有格子对象，发生重合
				}
			}
			return false;
		} 
				
		/** 向右旋转规范动作 */
		public void rotateRightAction(){
			tetromino.rotateRight();
			if(outOfBound() || coincide()){//如果旋转过后出界或者重合了要转回去
				tetromino.rotateLeft();
			}
		}
		
		/** 左转规范动作 */
		public void rotateLeftAction(){
			tetromino.rotateLeft();
			if(outOfBound() || coincide()){
				tetromino.rotateRight();
			}
		}
		/**一键完成下落动作，*/
		public void hardDropAction(){
			while(tetrominoCanDrop()){
				tetromino.softDrop();
			}
			tetrominoLandToWall();
			destroyLines();
			checkGameOver();
			tetromino = nextOne;
			nextOne = Tetromino.randomTetromino();
		}
		
		private boolean pause;   //暂停
		private boolean gameOver;
		private Timer timer;
		
		/** 启动游戏 */
		public void startAction(){
			clearWall();
			tetromino = Tetromino.randomTetromino();
			nextOne = Tetromino.randomTetromino();
			lines = 0; score = 0;	pause=false; gameOver=false;
			timer = new Timer();   //添加计时器
			timer.schedule(new TimerTask(){
				public void run() {
					softDropAction();
					repaint();
				}
			}, 700, 700);
		}
		/**清空墙*/
		private void clearWall(){
			//将墙的每一行的每个格子清理为null
			for(int row=0; row<ROWS; row++){
				Arrays.fill(wall[row], null);
			}
		}
		
		/** 暂停动作  */
		public void pauseAction(){
			timer.cancel(); //停止定时器
			pause = true;
			repaint();
		}
		
		/**继续动作*/
		public void continueAction(){
			timer = new Timer();
			timer.schedule(new TimerTask() {
				public void run() {
					softDropAction();
					repaint();
				}
			}, 700, 700);
			pause = false;
			repaint();
		}
		/**结束动作*/
		public void endAction() {
			gameOver = true;
			timer.cancel();
			End();
			repaint();
		}
		/**弹窗告知分数*/
		public void End() {
			JOptionPane.showMessageDialog(null,"游戏结束！,最后得分为"+score);
		}
		/** 检查游戏是否结束 */
		public void checkGameOver(){
			if(wall[0][4]==null){
				return;
			}
			gameOver = true;
			timer.cancel();
			End();
			repaint();
		}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		Tetris tetris = new Tetris();
		frame.add(tetris);
		frame.setSize(525, 590);//窗体大小
		frame.setUndecorated(false);//true隐藏掉窗口框，false保留框
		frame.setTitle("俄罗斯方块");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//设置窗体的相对位置 
		frame.setLocationRelativeTo(null);//使当前窗口居中
		frame.setVisible(true);
		tetris.action();
	}
}

