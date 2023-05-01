package tetris;

import java.awt.Color;
import java.awt.Graphics;

public class Shape {

    private Color color;

    private int x, y;

    private long time, lastTime;

    private int normal = 600, fast = 200;

    private int delay;
    // mảng chứa thông tin khối đang di chuyển
    private int[][] coords;

    private int[][] reference;

    private int deltaX;

    private Board board;

    private boolean collision = false, moveX = false;

    private int timePassedFromCollision = -1;

    public Shape(int[][] coords, Board board, Color color) {
        this.coords = coords;
        this.board = board;
        this.color = color;
        deltaX = 0;
        x = 4;
        y = 0;
        delay = normal;
        time = 0;
        lastTime = System.currentTimeMillis();
        reference = new int[coords.length][coords[0].length];

        System.arraycopy(coords, 0, reference, 0, coords.length);

    }

    long deltaTime;

    public void update() {
        moveX = true;
        deltaTime = System.currentTimeMillis() - lastTime;
        time += deltaTime;
        lastTime = System.currentTimeMillis();

        if (collision && timePassedFromCollision > 500) {
            for (int row = 0; row < coords.length; row++) {
                for (int col = 0; col < coords[0].length; col++) {
                    if (coords[row][col] != 0) {
                        board.getBoard()[y + row][x + col] = color;
                    }
                }
            }
            // kiểm tra va chạm
            checkLine();
            board.setCurrentShape();
            timePassedFromCollision = -1;
        }

        //kiểm tra di chuyển ngang, các khối có thể xếp chồng lên nhau..
        if (!(x + deltaX + coords[0].length > 10) && !(x + deltaX < 0)) {

            for (int row = 0; row < coords.length; row++) {
                for (int col = 0; col < coords[row].length; col++) {
                	//kiểm tra nếu ô trên bảng game rỗng thì có thể di chuyển khối nay vào được
                    if (coords[row][col] != 0) {
                        if (board.getBoard()[y + row][x + deltaX + col] != null) {
                            moveX = false;
                        }

                    }
                }
            }

            if (moveX) {
                x += deltaX;
            }

        }

        //Kiểm tra vị trí + chiều cao (số hàng) của hình dạng
        
        if (timePassedFromCollision == -1) {
            if (!(y + 1 + coords.length > 20)) {
            	// nếu vị trí mới mà không nằm trong giới hạn của bảng thì va chạm xảy ra gán collision = true
                for (int row = 0; row < coords.length; row++) {
                    for (int col = 0; col < coords[row].length; col++) {
                        if (coords[row][col] != 0) {

                            if (board.getBoard()[y + 1 + row][x + col] != null) {
                                collision();
                            }
                        }
                    }
                }
                if (time > delay) {
                    y++;
                    time = 0;
                }
            } else {
                collision();
            }
        } else {
            timePassedFromCollision += deltaTime;
        }

        deltaX = 0;
    }

    private void collision() {
        collision = true;
        timePassedFromCollision = 0;
    }

    public void render(Graphics g) {
    	//tạo 1 hình dạng bất kì có màu
        g.setColor(color);
        for (int row = 0; row < coords.length; row++) {
            for (int col = 0; col < coords[0].length; col++) {
                if (coords[row][col] != 0) {
                    g.fillRect(col * 30 + x * 30, row * 30 + y * 30, Board.blockSize, Board.blockSize);
                }
            }
        }

//        for (int row = 0; row < reference.length; row++) {
//            for (int col = 0; col < reference[0].length; col++) {
//                if (reference[row][col] != 0) {
//                    g.fillRect(col * 30 + 320, row * 30 + 160, Board.blockSize, Board.blockSize);
//                }
//
//            }
//
//        }

    }

    //kiểm tra nếu 1 hàng nào đó được tô đầy thì bỏ nó đi
    private void checkLine() {
        int size = board.getBoard().length - 1;
     // duyệt từng ô của bảng từ dưới lên
        for (int i = board.getBoard().length - 1; i > 0; i--) {
            int count = 0;
            for (int j = 0; j < board.getBoard()[0].length; j++) {
            	// ô nào được tô thì tăng biến count++
                if (board.getBoard()[i][j] != null) {
                    count++;
                }
             // hàng dưới lưu giá trị của hàng trên
				// vì là gán nên hàng trên được lưu trong bộ nhớ,
				// chỉ khi 1 hàng bị xóa thì nó mới có thể hiện ra
                board.getBoard()[size][j] = board.getBoard()[i][j];
            }
         // nếu dòng không đầy đủ thì chuyển sang dòng khác
            if (count < board.getBoard()[0].length) {
                size--;
            }
            else {
            	board.addScore();
            	
            }
        }
    }

	// tạo lớp xoay khối
    public void rotateShape() {

        int[][] rotatedShape = null;

        rotatedShape = transposeMatrix(coords);

        rotatedShape = reverseRows(rotatedShape);
        // nếu khối đang di chuyển nằm ngoài khung trò chơi thì thoát
        if ((x + rotatedShape[0].length > 10) || (y + rotatedShape.length > 20)) {
            return;
        }

        for (int row = 0; row < rotatedShape.length; row++) {
            for (int col = 0; col < rotatedShape[row].length; col++) {
                if (rotatedShape[row][col] != 0) {
                    if (board.getBoard()[y + row][x + col] != null) {
                        return;
                    }
                }
            }
        }
        coords = rotatedShape;
    }
    // ma trận chuyển vị
    private int[][] transposeMatrix(int[][] matrix) {
        int[][] temp = new int[matrix[0].length][matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                temp[j][i] = matrix[i][j];
            }
        }
        return temp;
    }
    // đảo ngược các hàng của ma trận
    private int[][] reverseRows(int[][] matrix) {

        int middle = matrix.length / 2;

        for (int i = 0; i < middle; i++) {
            int[] temp = matrix[i];

            matrix[i] = matrix[matrix.length - i - 1];
            matrix[matrix.length - i - 1] = temp;
        }

        return matrix;

    }

    public Color getColor() {
        return color;
    }

    public void setDeltaX(int deltaX) {
        this.deltaX = deltaX;
    }

    public void speedUp() {
        delay = fast;
    }

    public void speedDown() {
        delay = normal;
    }

    public int[][] getCoords() {
        return coords;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}