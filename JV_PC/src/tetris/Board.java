package tetris;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class Board extends JPanel implements KeyListener, MouseListener, MouseMotionListener {

	// Assets
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private BufferedImage pause, refresh, background;

	// kích thước bảng ( khu vực chơi game )
	private final int boardHeight = 20, boardWidth = 10;

	// kích thước khối
	public static final int blockSize = 30;

	// màu
	private Color[][] board = new Color[boardHeight][boardWidth];

	// mảng với tất cả các hình dạng có thể
	private Shape[] shapes = new Shape[7];

	// con trỏ Shape
	private static Shape currentShape, nextShape, currentnextShape, nextnextShape;

	// kết nối sql
	private DataBaseConnection databaseconnection = new DataBaseConnection();

	// vòng lặp game
	private Timer looper;

	private int FPS = 60;

	private int delay = 1000 / FPS;

	// biến lưu các sự kiện chuột
	private int mouseX, mouseY;

	private boolean leftClick = false;

	private Rectangle stopBounds, refreshBounds;

	private boolean gamePaused = false;

	private boolean gameOver = false;

	private Color[] colors = { Color.decode("#ed1c24"), Color.decode("#ff7f27"), Color.decode("#fff200"),
			Color.decode("#22b14c"), Color.decode("#00a2e8"), Color.decode("#a349a4"), Color.decode("#3f48cc") };
	private Random random = new Random();
	// khoảng thời gian cho mỗi lần nhấn nút
	private Timer buttonLapse = new Timer(300, new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			buttonLapse.stop();
		}
	});

	// Điểm
	private int score = 0, maxScore;

	public Board() {

		pause = ImageLoader.loadImage("/pause.png");
		refresh = ImageLoader.loadImage("/refresh.png");
		background = ImageLoader.loadImage("/backGround.png");

		mouseX = 0;
		mouseY = 0;

		stopBounds = new Rectangle(350, 500, pause.getWidth(), pause.getHeight() + pause.getHeight() / 2);
		refreshBounds = new Rectangle(350, 500 - refresh.getHeight() - 20, refresh.getWidth(),
				refresh.getHeight() + refresh.getHeight() / 2);

		// tạo vòng lặp game
		looper = new Timer(delay, new GameLooper());

		// tạo các khối
		shapes[0] = new Shape(new int[][] { { 1, 1, 1, 1 } // I shape;
		}, this, colors[0]);

		shapes[1] = new Shape(new int[][] { { 1, 1, 1 }, { 0, 1, 0 }, // T shape;
		}, this, colors[1]);

		shapes[2] = new Shape(new int[][] { { 1, 1, 1 }, { 1, 0, 0 }, // L shape;
		}, this, colors[2]);

		shapes[3] = new Shape(new int[][] { { 1, 1, 1 }, { 0, 0, 1 }, // J shape;
		}, this, colors[3]);

		shapes[4] = new Shape(new int[][] { { 0, 1, 1 }, { 1, 1, 0 }, // S shape;
		}, this, colors[4]);

		shapes[5] = new Shape(new int[][] { { 1, 1, 0 }, { 0, 1, 1 }, // Z shape;
		}, this, colors[5]);

		shapes[6] = new Shape(new int[][] { { 1, 1 }, { 1, 1 }, // O shape;
		}, this, colors[6]);

	}

	// cập nhật trạng thái hiện tại
	private void update() {
		isGameOver();
		if (stopBounds.contains(mouseX, mouseY) && leftClick && !buttonLapse.isRunning() && !gameOver) {
			buttonLapse.start();
			gamePaused = !gamePaused;
		}

		if (refreshBounds.contains(mouseX, mouseY) && leftClick) {
			startGame();
		}

		if (gamePaused) {
			return;
//			looper.stop();
		}
		if (gameOver) {
			return;
		}
		// cập nhật hình
		currentShape.update();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());
		// thêm giao diện cho khung trò chơi
		g.drawImage(background, WindowGame.WIDTH / 2 - background.getWidth() / 2, 30 - background.getHeight() / 2 + 150,
				null);
		// tạo hình trên board
		for (int row = 0; row < board.length; row++) {
			for (int col = 0; col < board[row].length; col++) {
				if (board[row][col] != null) {
					g.setColor(board[row][col]);
					g.fillRect(col * blockSize, row * blockSize, blockSize, blockSize);
				}

			}
		}
		// hiển thị khối tiếp theo trên board
		g.setColor(nextShape.getColor());
		for (int row = 0; row < nextShape.getCoords().length; row++) {
			for (int col = 0; col < nextShape.getCoords()[0].length; col++) {
				if (nextShape.getCoords()[row][col] != 0) {
					g.fillRect(col * 30 + 320, row * 30 + 50, Board.blockSize, Board.blockSize);
				}
			}
		}
		currentShape.render(g);
		// **
		
		// hover cho nút pause
		if (stopBounds.contains(mouseX, mouseY)) {
			g.drawImage(
					pause.getScaledInstance(pause.getWidth() + 3, pause.getHeight() + 3, BufferedImage.SCALE_DEFAULT),
					stopBounds.x + 3, stopBounds.y + 3, null);
		} else {
			g.drawImage(pause, stopBounds.x, stopBounds.y, null);
		}

		// hover cho nút refresh
		if (refreshBounds.contains(mouseX, mouseY)) {
			g.drawImage(refresh.getScaledInstance(refresh.getWidth() + 3, refresh.getHeight() + 3,
					BufferedImage.SCALE_DEFAULT), refreshBounds.x + 3, refreshBounds.y + 3, null);
		} else {
			g.drawImage(refresh, refreshBounds.x, refreshBounds.y, null);
		}

		if (gamePaused) {
			String gamePausedString = "GAME PAUSED";
			g.setColor(Color.WHITE);
			g.setFont(new Font("Georgia", Font.BOLD, 30));
			g.drawString(gamePausedString, 35, WindowGame.HEIGHT / 2);
		}
		if (gameOver) {
			String gameOverString = "GAME OVER";
			g.setColor(Color.WHITE);
			g.setFont(new Font("Georgia", Font.BOLD, 30));
			g.drawString(gameOverString, 50, WindowGame.HEIGHT / 2);
			String tenNguoiChoi = "Dung";
			String query = "INSERT INTO Scores (Ten, Diem) VALUES ('" + tenNguoiChoi + "', " + score + ")";
			databaseconnection.executeUpdate(query);
		}
//** bảng gameover
		// điểm số
		g.setColor(Color.WHITE);

		g.setFont(new Font("Georgia", Font.BOLD, 15));

		g.drawString("SCORE", WindowGame.WIDTH - 125, WindowGame.HEIGHT / 2);
		g.drawString(score + "", WindowGame.WIDTH - 125, WindowGame.HEIGHT / 2 + 20);

//		kỷ lục
//		maxScore = databaseconnection.gethighscore();
		g.setColor(Color.WHITE);

		g.setFont(new Font("Georgia", Font.BOLD, 15));

		g.drawString("MAXSCORE", WindowGame.WIDTH - 125, WindowGame.HEIGHT / 2 - 50);
		g.drawString(maxScore + "", WindowGame.WIDTH - 125, WindowGame.HEIGHT / 2 - 30);

		///
		g.setColor(Color.WHITE);

		// vẽ các ô chứa các khối
		for (int i = 0; i <= boardHeight; i++) {
			g.drawLine(0, i * blockSize, boardWidth * blockSize, i * blockSize);
		}
		for (int j = 0; j <= boardWidth; j++) {
			g.drawLine(j * blockSize, 0, j * blockSize, boardHeight * 30);
		}

	}

	// random khối kế tiếp
	public void setNextShape() {
		int index = random.nextInt(shapes.length);
		int colorIndex = random.nextInt(colors.length);
		nextShape = new Shape(shapes[index].getCoords(), this, colors[colorIndex]);
	}

	// ***
	// *****
	public void setCurrentShape() {
		currentShape = nextShape;
		setNextShape();
		if (!gameOver) {
			int highestPoint = getHighestPoint();
			int nextShapeRows = nextShape.getCoords().length;
			if (highestPoint <= 1 && nextShapeRows == 1) {
				gameOver = true;

				// in khối tiếp theo ra
			} else if (highestPoint < nextShapeRows) {
				gameOver = true;
			} else {
				// tiếp tục chơi game
				for (int row = 0; row < currentShape.getCoords().length; row++) {
					for (int col = 0; col < currentShape.getCoords()[0].length; col++) {
						if (currentShape.getCoords()[row][col] != 0) {
							if (board[currentShape.getY() + row][currentShape.getX() + col] != null) {
								gameOver = true;
							}
						}
					}
				}
				// kiểm tra các ô phía dưới của khối hiện tại
				for (int col = 0; col < currentShape.getCoords()[0].length; col++) {
					if (board[currentShape.getY() + currentShape.getCoords().length][currentShape.getX()
							+ col] != null) {
						gameOver = true;
					}
				}
			}
		}
	}
	// **

	public void randomShape() {
		int index = random.nextInt(shapes.length);
		int colorIndex = random.nextInt(colors.length);
		nextnextShape = new Shape(shapes[index].getCoords(), this, colors[colorIndex]);
	}
//	
//	public void setCurrentNextShape() {
//		currentnextShape = nextnextShape;
//		setNextNextShape();
//	}

	public void isGameOver() {
		for (int col = 0; col < WIDTH; col++) {
			if (board[0][col] != null) {
				gameOver = true; // Có ô đã được điền trong hàng đầu tiên, game over
			}
		}
	}

	// lấy tọa độ cao nhất trong bảng game
	public int getHighestPoint() {
		int highestPoint = 0;
		for (int row = 0; row < boardHeight; row++) {
			for (int col = 0; col < boardWidth; col++) {
				if (board[row][col] != null) {
					highestPoint = Math.max(highestPoint, row);
				}
			}
		}
		return highestPoint;
	}

	// trả lại màu cho bảng
	public Color[][] getBoard() {
		return board;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_UP) {
			currentShape.rotateShape();
		}
		if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			currentShape.setDeltaX(1);
		}
		if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			currentShape.setDeltaX(-1);
		}
		if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			currentShape.speedUp();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			currentShape.speedDown();
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	// lớp chạy chương trình (reset)
	public void startGame() {
		stopGame();
		setNextShape();
		setCurrentShape();
		gameOver = false;
		looper.start();

	}

	// ban đầu các khối không có gì cả
	public void stopGame() {
		score = 0;
		// xóa tất cả giá trị của các khối đã được đặt trên bảng
		for (int row = 0; row < board.length; row++) {
			for (int col = 0; col < board[row].length; col++) {
				board[row][col] = null;
			}
		}
		looper.stop();
	}

	// bắt đầu vòng lăp game
	class GameLooper implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			update();
			repaint();
			if (gameOver)
				looper.stop();
		}

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			leftClick = true;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			leftClick = false;
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	public void addScore() {
		score++;
	}

}