package cp3.ass03;

import java.awt.Dimension;
import java.awt.Point;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author lewi0146
 */
public class LifeProcessor extends Thread {


    public enum ComputeMode {JAVA_SINGLE, JAVA_MULTI}

    private boolean[][] gameBoard;


    final ReentrantLock lock = new ReentrantLock();

    private boolean toStop = false;
    private Dimension gameBoardSize = null;
    private ArrayList<Point> point = new ArrayList<Point>(0);

    private boolean keepLiving;
    int[] birth;
    int[] survives;
    int blockSize;

    private ArrayList<LifeListener> listeners;

    /**
     * "B3/S23"
     *
     * @param birth
     * @param survives
     */
    public LifeProcessor(int[] birth, int[] survives, ArrayList<Point> point, Dimension gameBoardSize, int blockSize) {
        this.birth = birth;
        this.survives = survives;
        this.point = point;
        this.gameBoardSize = gameBoardSize;
        this.blockSize = blockSize;

        this.listeners = new ArrayList<>();
    }

    public void stopLife() {
        this.keepLiving = false;
    }

    public void processLife(int generations, ComputeMode m) {

        switch (m) {
            case JAVA_SINGLE:
                compute_java_single(generations);
                break;
            case JAVA_MULTI:
                compute_java_multi(generations);
                break;
        }
    }

    public synchronized boolean[][] getPartialGrid(int iTop, int jLeft, int iBottom, int jRight) {
        //iBottom and jRight are included
        boolean[][] partialGrid = new boolean[iBottom - iTop + 1][jRight - jLeft + 1];
        for (int iOut = 0, iGrid = iTop; iGrid <= iBottom; iOut++, iGrid++) {
            System.arraycopy(gameBoard[iGrid], jLeft, partialGrid[iOut], 0, partialGrid[iOut].length);
        }
        return partialGrid;
    }

    private void compute_java_single(int generations) {

        keepLiving = true;
        int ilive = 0;
        int movesPerSecond = 0;
        if (generations < 0) {
            movesPerSecond = -generations;
            ilive = generations - 1; // ignore the ilive (go until keepLiving is false)
        }

        while (keepLiving && ilive < generations) {

            // create a new gameBoard for asessing life
            gameBoard = new boolean[((gameBoardSize.width) / blockSize)][((gameBoardSize.height) / blockSize)];

            // Initialise the game board with the surviving life forms (Point objects)
            for (int i = 0; i < point.size(); i++) {
                Point current = point.get(i);
                gameBoard[current.x + 1][current.y + 1] = true;
            }
            ArrayList<Point> survivingCells = new ArrayList<Point>(0);


            for (int i = 1; i < gameBoard.length - 1; i++) {
                for (int j = 1; j < gameBoard[0].length - 1; j++) {
                    // count up the number of surrounding cells that are alive
                    int surrounding = 0;
                    if (gameBoard[i - 1][j - 1]) {
                        surrounding++;
                    }
                    if (gameBoard[i - 1][j]) {
                        surrounding++;
                    }
                    if (gameBoard[i - 1][j + 1]) {
                        surrounding++;
                    }
                    if (gameBoard[i][j - 1]) {
                        surrounding++;
                    }
                    if (gameBoard[i][j + 1]) {
                        surrounding++;
                    }
                    if (gameBoard[i + 1][j - 1]) {
                        surrounding++;
                    }
                    if (gameBoard[i + 1][j]) {
                        surrounding++;
                    }
                    if (gameBoard[i + 1][j + 1]) {
                        surrounding++;
                    }

                    // Check for survival
                    if (gameBoard[i][j]) {
                        // Cell is alive, Can the cell live? (Conway, 2-3)
                        boolean survive = true;
                        for (int si = 0; si < this.survives.length; si++) {
                            if (this.survives[si] == surrounding) {
                                // survival!!
                                survivingCells.add(new Point(i - 1, j - 1));
                                break;
                            }
                        }

                    } else // Cell is dead, will the cell be given birth? (Conway, 3)
                    {
                        for (int bi = 0; bi < this.birth.length; bi++) {
                            if (this.birth[bi] == surrounding) {
                                // survival!!
                                survivingCells.add(new Point(i - 1, j - 1));
                                break;
                            }
                        }
                    }
                }
            }
            // Iterate through the gameBoard array, following the game of life rules


            // update the points
            point.clear();
            point.addAll(survivingCells);

            // notify listeners
            for (LifeListener l : listeners) {
                l.lifeUpdated();
            }

            if (generations > 0) {
                ilive++;
            } else {
                try {
                    Thread.sleep(1000 / movesPerSecond);
                } catch (InterruptedException ex) {
                    break;
                }
            }

        }
    }

    public class IterateGameboard extends RecursiveAction {

        private ArrayList<Point> cells;

        private int start;
        private int end;
        private int THRESHOLD = 0;
        private int yStart;
        private int yEnd;
        public IterateGameboard (ArrayList<Point> survivingCells, int iStart, int iEnd, int yStartIte, int yEndIte) {
            cells = survivingCells;
            start = iStart;
            end = iEnd;
            yStart = yStartIte;
            yEnd = yEndIte;
            THRESHOLD = gameBoard.length / Runtime.getRuntime().availableProcessors();
        }

        @Override
        protected void compute() {

            if (end - start <= 30) {
                computeDirectly();
                return;
            }

            int iMiddle = (start + end) >>> 1;
            int yMiddle = (yStart + yEnd) >>> 1;
            invokeAll(new IterateGameboard(cells, start, iMiddle, yStart, yMiddle),
                    new IterateGameboard(cells, start, iMiddle, yMiddle, yEnd),
                    new IterateGameboard(cells,  iMiddle, end, yStart, yMiddle),
                    new IterateGameboard(cells, iMiddle, end, yMiddle, yEnd));
        }

        protected void computeDirectly() {

            for (int i = start + 1; i < end - 1; i++) {
                for (int j = yStart + 1; j < yEnd - 1; j++) {
                    // count up the number of surrounding cells that are alive
                    int surrounding = 0;
                    if (gameBoard[i - 1][j - 1]) {
                        surrounding++;
                    }
                    if (gameBoard[i - 1][j]) {
                        surrounding++;
                    }
                    if (gameBoard[i - 1][j + 1]) {
                        surrounding++;
                    }
                    if (gameBoard[i][j - 1]) {
                        surrounding++;
                    }
                    if (gameBoard[i][j + 1]) {
                        surrounding++;
                    }
                    if (gameBoard[i + 1][j - 1]) {
                        surrounding++;
                    }
                    if (gameBoard[i + 1][j]) {
                        surrounding++;
                    }
                    if (gameBoard[i + 1][j + 1]) {
                        surrounding++;
                    }
//
                    // Check for survival
                    if (gameBoard[i][j]) {
                        // Cell is alive, Can the cell live? (Conway, 2-3)
                        boolean survive = true;
                        for (int si = 0; si < survives.length; si++) {
                            if (survives[si] == surrounding) {
                                // survival!!
                                cells.add(new Point(i - 1, j - 1));
                                break;
                            }
                        }

                    } else // Cell is dead, will the cell be given birth? (Conway, 3)
                    {
                        for (int bi = 0; bi < birth.length; bi++) {
                            if (birth[bi] == surrounding) {
                                // survival!!
                                cells.add(new Point(i - 1, j - 1));
                                break;
                            }
                        }
                    }

                }
            }

        }
    }





    public synchronized boolean getValue(int i, int j) {
        return gameBoard[i][j];
    }

    public synchronized void replaceGrid(boolean[][] newGrid) {
        gameBoard = newGrid;
    }

    public synchronized void updateGrid(int i, int j, boolean value) {
        gameBoard[i][j] = value;
    }

    public boolean[][] getGrid() {
        return gameBoard;
    }

    public int getRowNum() {
        return gameBoard.length;
    }

    private class SubUpdateThread extends Thread {
        private int iStart, iEnd;


        private ArrayList<Point> cells;

        private int start;
        private int end;

        public SubUpdateThread(ArrayList<Point> survivingCells, int iStart, int iEnd) {

            cells = survivingCells;

            start = iStart;
            end = iEnd;

        }

        @Override
        public void run() {
            // iEnd is excluded
            for (int i = start + 1; i < end - 1; i++) {
                for (int j = 1; j < gameBoard[0].length - 1; j++) {
                    // count up the number of surrounding cells that are alive
                    int surrounding = 0;
                    if (gameBoard[i - 1][j - 1]) {
                        surrounding++;
                    }
                    if (gameBoard[i - 1][j]) {
                        surrounding++;
                    }
                    if (gameBoard[i - 1][j + 1]) {
                        surrounding++;
                    }
                    if (gameBoard[i][j - 1]) {
                        surrounding++;
                    }
                    if (gameBoard[i][j + 1]) {
                        surrounding++;
                    }
                    if (gameBoard[i + 1][j - 1]) {
                        surrounding++;
                    }
                    if (gameBoard[i + 1][j]) {
                        surrounding++;
                    }
                    if (gameBoard[i + 1][j + 1]) {
                        surrounding++;
                    }

                    // Check for survival
                    if (gameBoard[i][j]) {
                        // Cell is alive, Can the cell live? (Conway, 2-3)
                        boolean survive = true;
                        for (int si = 0; si < survives.length; si++) {
                            if (survives[si] == surrounding) {
                                // survival!!
                                cells.add(new Point(i - 1, j - 1));
                                break;
                            }
                        }

                    } else // Cell is dead, will the cell be given birth? (Conway, 3)
                    {
                        for (int bi = 0; bi < birth.length; bi++) {
                            if (birth[bi] == surrounding) {
                                // survival!!
                                cells.add(new Point(i - 1, j - 1));
                                break;
                            }
                        }
                    }

                }
            }
        }
    }


    private  void compute_java_multi(int generations) {
        keepLiving = true;
        int ilive = 0;
        int movesPerSecond = 0;
        if (generations < 0) {
            movesPerSecond = -generations;
            ilive = generations - 1; // ignore the ilive (go until keepLiving is false)
        }

        ForkJoinPool pool = new ForkJoinPool();
        while (keepLiving && ilive < generations) {

            // create a new gameBoard for asessing life
            gameBoard = new boolean[((gameBoardSize.width) / blockSize)][((gameBoardSize.height) / blockSize)];


            //Splitting into 2


            // Initialise the game board with the surviving life forms (Point objects)
            for (int i = 0; i < point.size(); i++) {
                Point current = point.get(i);
                gameBoard[current.x + 1][current.y + 1] = true;
            }
            ArrayList<Point> survivingCells = new ArrayList<Point>(0);

//            SubUpdateThread[] subUpdaters = new SubUpdateThread[20];
//
//           int sizeToCheck = gameBoard.length / 20;
//
//            for (int i = 0; i < 20; i++) {
//               int iEnd = (i == 20 - 1) ? gameBoard.length : (i + 1) * sizeToCheck;
//               subUpdaters[i] = new SubUpdateThread(survivingCells, i * sizeToCheck, gameBoard.length);
//              subUpdaters[i].start();
//           }
////
//            for (int i = 0; i < subUpdaters.length; i++) {
//                try {
//                    subUpdaters[i].join();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
            long endTime = System.currentTimeMillis();
            //Fork - Join attempt
//            long startTime = System.currentTimeMillis();
            IterateGameboard task = new IterateGameboard(survivingCells, 0, gameBoard.length, 0, gameBoard[0].length);
           pool.invoke(task);
//
//            long endTime = System.currentTimeMillis();

            synchronized(this) {
                point.clear();
                point.addAll(survivingCells);
            }

            // update the points


            // notify listeners
            for (LifeListener l : listeners) {
                l.lifeUpdated();
            }

            if (generations > 0) {
                ilive++;
            } else {
                try {
                    Thread.sleep(100);
                    pool.shutdown();
                } catch (InterruptedException ex) {
                    break;
                }
            }

        }
    }

    public void addLifeListener(LifeListener l) {
        this.listeners.add(l);
    }
}
