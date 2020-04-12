import java.util.*;

public class PathFinder {
    private MazeGenerator mazeGenerator;
    private int[][][] maze;
    private int[] start, end;
    private int n;
    private boolean[][] isVisited;
    private int[] dr, dc;
    private Stack<int[]> path;
    private List<int[]> pathList;
    private boolean pathFound;

    public PathFinder(MazeGenerator mazeGenerator) {
        this.mazeGenerator = mazeGenerator;
        maze = mazeGenerator.getMaze();
        start = new int[]{-1, -1};
        end = new int[]{-1, -1};
        n = mazeGenerator.getN();
        isVisited = new boolean[n][n];
        dr = mazeGenerator.getDr();
        dc = mazeGenerator.getDc();
        path = new Stack<>();
        pathList = new ArrayList<>();
        pathFound = false;
    }

    public void setStart(int x, int y) {
        start[0] = x;
        start[1] = y;
    }

    public void setEnd(int x, int y) {
        end[0] = x;
        end[1] = y;
    }

    public int[] getStart() {
        return start;
    }

    public int[] getEnd() {
        return end;
    }

    private boolean isValid(int x, int y) {
        return x >= 0 && x < n && y >= 0 && y < n;
    }

    private int[][][] getParent() {
        int[][][] parent = new int[n][n][2];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                parent[i][j][0] = -1;
                parent[i][j][1] = -1;
            }
        }
        return parent;
    }

    private int[][] getDist() {
        int[][] arr = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                arr[i][j] = Integer.MAX_VALUE;  //initialize all distances as max value
            }
        }
        return arr;
    }

    private boolean isStart(int[] node) {
        return node[0] == start[0] && node[1] == start[1];
    }

    private boolean isEnd(int[] node) {
        return node[0] == end[0] && node[1] == end[1];
    }

    private boolean isDirectionUnblocked(int x, int y, int direction) {
        return maze[x][y][direction] == 1;
    }

    public boolean isPathFound() {
        return pathFound;
    }

    public List<int[]> getPath() {
        return pathList;
    }

    public void solveMaze() {
        isVisited = new boolean[n][n];
        int[][][] parent = getParent(); //stores parent of each element
        int[][] dist = getDist(); //stores distance of each node from start node
        PriorityQueue<int[]> queue = new PriorityQueue<>(new Comparator<int[]>() {
            @Override
            public int compare(int[] t1, int[] t2) {
                return dist[t1[0]][t1[1]] - dist[t2[0]][t2[1]];
            }
        }); // stores elements in ascending order of their distance from start
        queue.offer(start);
        dist[start[0]][start[1]] = 0;

        while(!queue.isEmpty()) {
            int[] curr = queue.poll();
            int x = curr[0];
            int y = curr[1];
            System.out.println(x + " " + y);
            isVisited[x][y] = true;
            if (isEnd(curr)) {
                System.out.println("Path Found");
            }
            for (int i = 0; i < 4; i++) {
                if (isDirectionUnblocked(x, y, i)) {
                    int new_x = curr[0] + dr[i];
                    int new_y = curr[1] + dc[i];
                    if (isValid(new_x, new_y) && !isVisited[new_x][new_y] && dist[x][y] + 1 < dist[new_x][new_y]) {
                        parent[new_x][new_y][0] = x;
                        parent[new_x][new_y][1] = y;
                        dist[new_x][new_y] = dist[x][y] + 1;
                        queue.offer(new int[]{new_x, new_y});
                    }
                }
            }
        }
        System.out.println("Over");
    }

    public void bfs() {
        isVisited = new boolean[n][n];
        int[][][] parent = getParent();
        Queue<int[]> queue = new LinkedList<>();
        queue.offer(start);
        isVisited[start[0]][start[1]] = true;
        while(!queue.isEmpty()) {
            int[] curr = queue.poll();
            int x = curr[0];
            int y = curr[1];
            for (int i = 0; i < 4; i++) {
                if (isDirectionUnblocked(x, y, i)) { //check if the direction has a wall
                    int new_x = x + dr[i];
                    int new_y = y + dc[i];
                    if (isValid(new_x, new_y) && !isVisited[new_x][new_y]) {
                        isVisited[new_x][new_y] = true;
                        parent[new_x][new_y][0] = x;
                        parent[new_x][new_y][1] = y;
                        int[] newNode = new int[]{new_x, new_y};
                        if (isEnd(newNode)) {
                            formPath(parent);
                            System.out.println("Path found");
                        } else {
                            queue.offer(newNode);
                        }
                    }
                }
            }
        }
        System.out.println("Over");
    }

    private void formPath(int[][][] parent) {
        path = new Stack<>(); // path stack
        int[] curr = new int[2];
        curr[0] = end[0];
        curr[1] = end[1];
        while (true) {
            path.push(new int[]{curr[0], curr[1]});
            if (isStart(curr)) break;
            int x = parent[curr[0]][curr[1]][0];
            int y = parent[curr[0]][curr[1]][1];
            curr[0] = x;
            curr[1] = y;
        }
        pathFound = true;
        while (!path.empty()) {
            pathList.add(path.pop());
        }
        mazeGenerator.repaint();
    }
}
