package cp3.ass03;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import javax.swing.JPanel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GameBoard extends JPanel implements ComponentListener, MouseListener, MouseMotionListener {

    private int blockSize = 5;
    private boolean drawGrid = true;

    private Dimension gameBoardSize = null;
    private ArrayList<Point> point = new ArrayList<Point>(0);

    ArrayList<GameBoardListener> gbListener = new ArrayList<>();

    public GameBoard() {
        // Add resizing listener
        addComponentListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);

        this.setBackground(new Color(255,255,255));
    }

    private void updateArraySize() {
        ArrayList<Point> removeList = new ArrayList<Point>(0);
        for (Point current : point) {
            if ((current.x > gameBoardSize.width - 1) || (current.y > gameBoardSize.height - 1)) {
                removeList.add(current);
            }
        }
        point.removeAll(removeList);
        repaint();
    }

    public void addPoint(int x, int y) {
        if (!point.contains(new Point(x, y))) {
            point.add(new Point(x, y));
        }
        repaint();
    }

    public void addPoint(MouseEvent me) {
        int x = me.getPoint().x / blockSize - 1;
        int y = me.getPoint().y / blockSize - 1;
        if ((x >= 0) && (x < gameBoardSize.width) && (y >= 0) && (y < gameBoardSize.height)) {
            addPoint(x, y);
        }
    }

    public void removePoint(int x, int y) {
        point.remove(new Point(x, y));
    }

    public void resetBoard() {
        point.clear();
    }

    public void randomlyFillBoard(int percent) {
        resetBoard();
        for (int i = 0; i < gameBoardSize.width; i++) {
            for (int j = 0; j < gameBoardSize.height; j++) {
                if (Math.random() * 100 < percent) {
                    addPoint(i, j);
                }
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        try {
            for (Point newPoint : point) {
                // Draw new point
                g.setColor(Color.blue);
                g.fillRect(blockSize + (blockSize * newPoint.x), blockSize + (blockSize * newPoint.y), blockSize, blockSize);
            }
        } catch (ConcurrentModificationException cme) {
            System.err.println("ConcurrentModificationException!  Naughty, naughty, you code is not thread-safe!");
        }
        // Setup grid
        if (gameBoardSize != null && drawGrid) {
            g.setColor(Color.BLACK);
            for (int i = 0; i <= gameBoardSize.width; i++) {
                g.drawLine(((i * blockSize) + blockSize), blockSize, (i * blockSize) + blockSize, blockSize + (blockSize * gameBoardSize.height));
            }
            for (int i = 0; i <= gameBoardSize.height; i++) {
                g.drawLine(blockSize, ((i * blockSize) + blockSize), blockSize * (gameBoardSize.width + 1), ((i * blockSize) + blockSize));
            }
        }

    }

    public void updateBlockSize(int blockSize) {
        this.blockSize = blockSize;
        componentResized(null);
    }

    public void updateDrawGrid(boolean drawGrid) {
        this.drawGrid = drawGrid;
        repaint();

    }

    @Override
    public void componentResized(ComponentEvent e) {
        // Setup the game board size with proper boundries
        gameBoardSize = new Dimension(getWidth() / blockSize - 2, getHeight() / blockSize - 2);
        updateArraySize();
        for (GameBoardListener l : gbListener) {
            l.gameBoardDimensionUpdated(gameBoardSize);
        }
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Mouse was released (user clicked)
        addPoint(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Mouse is being dragged, user wants multiple selections
        addPoint(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    ArrayList<Point> getPoints() {
        return this.point;
    }

    public void addGameBoardListener(GameBoardListener l) {
        this.gbListener.add(l);
    }

    public void saveGameBoard(String filename) throws IOException {
        JSONObject gameboard = new JSONObject();

        gameboard.put("BlockSize", new Integer(this.blockSize));
        gameboard.put("DrawGrid", new Boolean(this.drawGrid));
        gameboard.put("Width", new Double(this.gameBoardSize.getWidth()));
        gameboard.put("Height", new Double(this.gameBoardSize.getHeight()));

        JSONArray points = new JSONArray();

        for (Point current : point) {
            JSONObject p = new JSONObject();
            p.put("x", new Integer(current.x));
            p.put("y", new Integer(current.y));
            points.add(p);
        }

        gameboard.put("points", points);

        FileWriter file = new FileWriter(filename);
        file.write(gameboard.toJSONString());
        file.flush();
        file.close();

    }

    public void loadGameBoard(String filename) throws IOException, ParseException {

        JSONParser parser = new JSONParser();

        Object obj = parser.parse(new FileReader(filename));

        JSONObject jsonObject = (JSONObject) obj;

        JSONArray points = (JSONArray) jsonObject.get("points");

        this.point.clear();
        for (Object ptemp : points)
        {
            JSONObject p = (JSONObject)ptemp;
            int x = ((Long)p.get("x")).intValue();
            int y = ((Long)p.get("y")).intValue();
            point.add(new Point(x,y));

        }

        this.repaint();

    }

}
