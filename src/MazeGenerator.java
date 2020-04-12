import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

class MazeGenerator extends JPanel implements KeyListener{
    private final int n;
    private int[][][] maze;
    private boolean[][] visited;
    private final int[] dr, dc;
    private final Random random;
    private boolean generated;
    private final int size, xOff, yOff;
    private PathFinder pathFinder;
    private char currKey;

    public MazeGenerator(int w, int h, int n) {
        setSize(w, h);
        this.n = n;
        addKeyListener(this);
        setFocusable(true);
        requestFocus();
        maze = new int[n][n][4]; //stores places to construct walls for all coordinates
        visited = new boolean[n][n]; //array to store if node has been visited
        dr = new int[]{1, 0, -1, 0};
        dc = new int[]{0, 1, 0, -1};
        random = new Random();
        generated = false;
        size = 20;
        xOff = 20; //x-axis offset
        yOff = 20; //y-axis offset
        currKey = 'a';
        pathFinder = new PathFinder(this);
        generate();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        if (generated) {
            g.setColor(Color.BLACK);
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (maze[i][j][0] == 0) g.drawLine(xOff + (i+1)*size, yOff + j*size,xOff + (i+1)*size,yOff + (j+1)*size); //right wall
                    if (maze[i][j][1] == 0) g.drawLine(xOff + i*size, yOff + (j+1)*size, xOff + (i+1)*size, yOff + (j+1)*size); //bottom wall
                    if (maze[i][j][2] == 0) g.drawLine(xOff + i*size, yOff + j*size, xOff + i*size, yOff + (j+1)*size); //left wall
                    if (maze[i][j][3] == 0) g.drawLine(xOff + i*size, yOff + j*size, xOff + (i+1)*size, yOff + j*size); //top wall
                }
            }
            if (pathFinder.getStart()[0] != -1) { // blank line at the top to present start
                int[] start = pathFinder.getStart();
                g.setColor(Color.WHITE);
                g.drawLine(xOff + start[0]*size, yOff + start[1]*size,
                        xOff + (start[0]+1)*size, yOff + start[1]*size);
            }
            if (pathFinder.getEnd()[0] != -1) { // blank line at the bottom to present end
                int[] end = pathFinder.getEnd();
                g.setColor(Color.WHITE);
                g.drawLine(xOff + end[0]*size, yOff + (end[1]+1)*size,
                        xOff + (end[0]+1)*size, yOff + (end[1]+1)*size);
            }
        }
        if (pathFinder.isPathFound()) {
            List<int[]> path = pathFinder.getPath();
            double red = 50, green = 30, blue = 255; // start rgb values
            double grad = (blue - red) / (double) path.size(); // gradient to update red and blue values
            Color color;
            int half = size / 2;
            int prevDirection = 1; // variable to store direction of prev node
            for (int i = 0; i < path.size()-1; i++) {
                red += grad;
                blue -= grad;
                color = new Color((int)red, (int)green, (int)blue);
                g.setColor(color);
                g.setColor(Color.RED); // comment this line for gradient line
                int[] currNode = path.get(i);
                int[] nextNode = path.get(i+1);
                int direction = getDirection(currNode, nextNode); // direction of next node
                int nodeX = currNode[0]*size + xOff;
                int nodeY = currNode[1]*size + yOff;
                if (direction == 0) { // if direction is right
                    if (prevDirection == 1) { // if prevDirection is down
                        g.drawLine(nodeX + half, nodeY, nodeX + half, nodeY + half);
                        g.drawLine(nodeX + half, nodeY + half, nodeX + size, nodeY + half);
                    }
                    else if (prevDirection == 3) { // if prevDirection is up
                        g.drawLine(nodeX + half, nodeY + size, nodeX + half, nodeY + half);
                        g.drawLine(nodeX + half, nodeY + half, nodeX + size, nodeY + half);
                    }
                    else { // if prevDirection is left or right
                        g.drawLine(nodeX, nodeY + half, nodeX + size, nodeY + half);
                    }
                }
                if (direction == 2) { // if direction is left
                    if (prevDirection == 1) { // if prevDirection is down
                        g.drawLine(nodeX + half, nodeY, nodeX + half, nodeY + half);
                        g.drawLine(nodeX + half, nodeY + half, nodeX, nodeY + half);
                    }
                    else if (prevDirection == 3) { // if prevDirection is up
                        g.drawLine(nodeX + half, nodeY + size, nodeX + half, nodeY + half);
                        g.drawLine(nodeX + half, nodeY + half, nodeX, nodeY + half);
                    }
                    else { // if prevDirection is left or right
                        g.drawLine(nodeX, nodeY + half, nodeX + size, nodeY + half);
                    }
                }
                if (direction == 1) { // if direction is down
                    if (prevDirection == 0) { // if prevDirection is right
                        g.drawLine(nodeX, nodeY + half, nodeX + half, nodeY + half);
                        g.drawLine(nodeX + half, nodeY + half, nodeX + half, nodeY + size);
                    }
                    else if (prevDirection == 2) { // if prevDirection os left
                        g.drawLine(nodeX + size, nodeY + half, nodeX + half, nodeY + half);
                        g.drawLine(nodeX + half, nodeY + half, nodeX + half, nodeY + size);
                    }
                    else { // if prevDirection is up or down
                        g.drawLine(nodeX + half, nodeY, nodeX + half, nodeY + size);
                    }
                }
                if (direction == 3) { // is direction is up
                    if (prevDirection == 0) { // if prevDirection is right
                        g.drawLine(nodeX, nodeY + half, nodeX + half, nodeY + half);
                        g.drawLine(nodeX + half, nodeY + half, nodeX + half, nodeY);
                    }
                    else if (prevDirection == 2) { //if prevDirection is left
                        g.drawLine(nodeX + size, nodeY + half, nodeX + half, nodeY + half);
                        g.drawLine(nodeX + half, nodeY + half, nodeX + half, nodeY);
                    }
                    else { // if prevDirection is up or down
                        g.drawLine(nodeX + half, nodeY, nodeX + half, nodeY + size);
                    }
                }
                prevDirection = direction;
            }
            // printing path in end node
            int endNodeX = pathFinder.getEnd()[0]*size + xOff;
            int endNodeY = pathFinder.getEnd()[1]*size + yOff;
            if (prevDirection == 0) {
                g.drawLine(endNodeX, endNodeY + half, endNodeX + half, endNodeY +half);
                g.drawLine(endNodeX + half, endNodeY + half, endNodeX + half, endNodeY + size);
            }
            if (prevDirection == 2) {
                g.drawLine(endNodeX + size, endNodeY + half, endNodeX + half, endNodeY + half);
                g.drawLine(endNodeX + half, endNodeY + half, endNodeX + half, endNodeY + size);
            }
            else g.drawLine(endNodeX + half, endNodeY, endNodeX + half, endNodeY + size);
        }
    }

    private void generate() {
        Stack<int[]> stack = new Stack<>();
        int[] start = new int[]{n / 2, n / 2};
        stack.push(start);
        visited[start[0]][start[1]] = true;
        while(!stack.empty()) {
            int[] curr = stack.peek();
            if (hasUnvisitedNeighbours(curr)) {
                int[] randomNeighbour = getRandomNeighbour(curr); //Returns random unvisited neighbour
                visited[randomNeighbour[0]][randomNeighbour[1]] = true;
                int direction = getDirection(curr, randomNeighbour); // gets direction of neighbour
                maze[curr[0]][curr[1]][direction] = 1; // 1 == No wall, 0 == Wall
                int oppDirection = getOppositeDirection(direction); // gets direction opposite to 'direction'
                maze[randomNeighbour[0]][randomNeighbour[1]][oppDirection] = 1;
                stack.push(randomNeighbour);
            } else {
                while(!stack.empty() && !hasUnvisitedNeighbours(stack.peek())) stack.pop();
            }
        }
        int randomStart = random.nextInt(n); // random start node
        int randomEnd = random.nextInt(n); // random end node
        pathFinder.setStart(randomStart, 0);
        pathFinder.setEnd(randomEnd, n-1);
        generated = true;
    }

    // method to save screenshot of frame
    private void saveImage() {
        BufferedImage image = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        this.paint(g);
        try {
            ImageIO.write(image, "png", new File("GeneratedMaze.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //used for debugging/ testing purpose
    private void test() {
        for (int i = 0; i < n; i++) {
            String row = "";
            for (int j = 0; j < n; j++) {
                StringBuilder temp = new StringBuilder();
                temp.append("[");
                for (int k = 0; k < 4; k++) {
                    temp.append(String.valueOf(maze[j][i][k]));
                    temp.append(",");
                }
                temp.append("]");
                row += temp.toString();
            }
            System.out.println(row);
        }
    }

    private boolean isValid(int x, int y) {
        return x >= 0 && x < n && y >= 0 && y < n;
    }

    private boolean hasUnvisitedNeighbours(int[] node) {
        int x = node[0];
        int y = node[1];
        return (isValid(x+1, y) && !visited[x+1][y]) ||
                (isValid(x-1, y) && !visited[x-1][y]) ||
                (isValid(x, y+1) && !visited[x][y+1]) ||
                (isValid(x, y-1) && !visited[x][y-1]);
    }

    private int[] getRandomNeighbour(int[] node) {
        int x = node[0];
        int y = node[1];
        List<int[]> unvisited = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (isValid(x+dr[i], y + dc[i]) && !visited[x+dr[i]][y+dc[i]]) {
                unvisited.add(new int[]{x+dr[i], y+dc[i]}); //only add if valid and unvisited
            }
        }
        int randomIdx = random.nextInt(unvisited.size()); //get random index
//        int[] res = new int[2];
//        res[0] = unvisited.get(randomIdx)[0];
//        res[1] = unvisited.get(randomIdx)[1];
        return unvisited.get(randomIdx);
    }

    private int getDirection(int[] curr, int[] neighbour) {
        int xDiff = neighbour[0] - curr[0];
        int yDiff = neighbour[1] - curr[1];
        for (int i = 0; i < 4; i++) {
            if (dr[i] == xDiff && dc[i] == yDiff) return i;
        }
        return -1;
    }

    private int getOppositeDirection(int direction) {
        if (direction == 0) return 2;
        if (direction == 1) return 3;
        if (direction == 2) return 0;
        else return 1;
    }

    public int[][][] getMaze() {
        return maze;
    }

    public int getN() {
        return n;
    }

    public int[] getDr() {
        return dr;
    }

    public int[] getDc() {
        return dc;
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {
        currKey = keyEvent.getKeyChar();
        if (currKey == 's') pathFinder.bfs();
        if (currKey == 'r') reset();
        if (currKey == 'b') saveImage();
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {

    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {

    }

    private void reset() {
        visited = new boolean[n][n];
        maze = new int[n][n][4];
        pathFinder = new PathFinder(this);
        generated = false;
        generate();
        repaint();
    }
}

class Main extends JFrame{

    public Main() {
        int width = 640, height = 670;
        MazeGenerator generator = new MazeGenerator(width, height, 30);
        this.setTitle("Maze generator");
        this.setSize(new Dimension(width, height));
        this.setMinimumSize(new Dimension(width, height));
        this.setResizable(false);
        add(generator);
        pack();
    }

    public static void main(String[] args) {
        new Main().setVisible(true);
    }
}
