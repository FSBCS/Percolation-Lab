import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

/**
 * Percolatron: an interactive visualizer for the Percolation assignment.
 *
 * Usage:
 *   Percolatron.show(20);        // from your own main method
 *   java Percolatron 50          // or run this class directly with a grid size
 *
 * Click or drag to open sites. Run opens random sites until the system
 * percolates, at the rate set by the Speed slider. Step opens one random
 * site, and Reset starts over.
 *
 * Colors: black = blocked, white = open, blue = full,
 *         magenta = isOpen() or isFull() threw an exception for that site.
 *
 * Requires a Percolation class with the standard API, using 0-based rows and columns.
 */
public class Percolatron extends JPanel {
    private static final Color GRID_COLOR    = new Color(60, 60, 60);
    private static final Color BLOCKED_COLOR = Color.BLACK;
    private static final Color OPEN_COLOR    = Color.WHITE;
    private static final Color FULL_COLOR    = new Color(30, 144, 255);
    private static final Color ERROR_COLOR   = Color.MAGENTA;
    private static final Color WARN_TEXT     = new Color(200, 0, 0);
    private static final int GAP = 1;
    private static final int TICK_MS = 25;         // animation frame interval (40 per second)
    private static final int SLIDER_MAX = 100;
    private static final Random RNG = new Random();

    private final int n;
    private final double minRate;     // slowest speed, in sites per second
    private final double maxRate;     // fastest speed, in sites per second
    private double sitesPerSecond;    // current speed, set by the slider
    private double pending;           // fractional sites owed to the animation
    private Percolation perc;
    private int[] order;              // all sites (row * n + col) in random order
    private int next;                 // index of the next site to try in order
    private String error;             // first error from Percolation code, or null
    private boolean paintErrorLogged; // so a failing isFull() doesn't flood the console

    private final GridPanel grid = new GridPanel();
    private final JLabel status = new JLabel(" ");
    private final Color normalText = status.getForeground();
    private final JButton runButton  = new JButton("Run");
    private final JButton stepButton = new JButton("Step");
    private final JSlider speedSlider = new JSlider(0, SLIDER_MAX);
    private final JLabel speedLabel = new JLabel();
    private final Timer timer = new Timer(TICK_MS, e -> tick());

    // ===================================================================
    // Public entry points
    // ===================================================================

    /** Opens a Percolatron window for an n-by-n grid. Safe to call from any thread. */
    public static void show(int n) {
        SwingUtilities.invokeLater(() -> {
            final Percolatron view;
            try {
                view = new Percolatron(n);
            } catch (RuntimeException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Could not start Percolatron:\n" + ex,
                        "Percolatron", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JFrame frame = new JFrame("Percolatron (n = " + n + ")");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    view.pause();   // stop the timer when the window goes away
                }
            });
            frame.add(view);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    public static void main(String[] args) {
        int n = (args.length > 0) ? Integer.parseInt(args[0]) : 20;
        show(n);
    }

    /** Creates the visualizer panel. Most callers should use show(n) instead. */
    public Percolatron(int n) {
        super(new BorderLayout());
        if (n <= 0) throw new IllegalArgumentException("n must be positive, got " + n);
        this.n = n;
        this.minRate = 1;
        this.maxRate = Math.max(10, n * n);   // fastest: the whole grid in about a second
        this.perc = new Percolation(n);   // failures propagate to show()
        shuffleOrder();

        // Press "s" to save the grid as a PNG (handy for documentation)
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('s'), "saveImage");
        getActionMap().put("saveImage", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveImage();
            }
        });

        JButton resetButton = new JButton("Reset");
        runButton.addActionListener(e -> toggleRun());
        stepButton.addActionListener(e -> step());
        resetButton.addActionListener(e -> reset());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        for (JButton b : new JButton[] { runButton, stepButton, resetButton }) {
            b.setFocusable(false);
            buttons.add(b);
        }

        // Speed: default is about 10 seconds to percolate (~0.6 n^2 sites at n^2/16 per second)
        double defaultRate = Math.max(minRate, Math.min(maxRate, n * n / 16.0));
        speedSlider.setValue(sliderValueFor(defaultRate));
        speedSlider.setPreferredSize(new Dimension(160, speedSlider.getPreferredSize().height));
        speedSlider.addChangeListener(e -> setSpeed(rateFor(speedSlider.getValue())));

        // Size the rate label for its widest text so the layout doesn't shift as it changes
        speedLabel.setText(formatRate(maxRate));
        speedLabel.setPreferredSize(speedLabel.getPreferredSize());
        setSpeed(rateFor(speedSlider.getValue()));

        JPanel speed = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        speed.add(new JLabel("Speed"));
        speed.add(speedSlider);
        speed.add(speedLabel);

        JPanel controls = new JPanel(new BorderLayout());
        controls.add(speed, BorderLayout.WEST);
        controls.add(buttons, BorderLayout.EAST);

        JPanel bar = new JPanel(new BorderLayout(0, 6));   // 6 px between the two rows
        bar.setBorder(BorderFactory.createEmptyBorder(6, 10, 8, 10));
        bar.add(status, BorderLayout.NORTH);
        bar.add(controls, BorderLayout.SOUTH);

        add(grid, BorderLayout.CENTER);
        add(bar, BorderLayout.SOUTH);

        stateChanged();
    }

    // ===================================================================
    // State changes: everything that alters the model ends in stateChanged()
    // ===================================================================

    /** Opens a site, then refreshes the display. */
    public void open(int row, int col) {
        safeOpen(row, col);
        stateChanged();
    }

    /** Discards the current Percolation and starts over with a fresh one. */
    public void reset() {
        pause();
        error = null;
        paintErrorLogged = false;
        try {
            perc = new Percolation(n);
        } catch (RuntimeException ex) {
            fail("new Percolation(" + n + ")", ex);
        }
        shuffleOrder();
        stateChanged();
    }

    private void stateChanged() {
        boolean done = (error == null) && safePercolates();
        boolean canRun = !done && error == null;
        if (!canRun) pause();
        runButton.setEnabled(canRun);
        stepButton.setEnabled(canRun);
        updateStatus(done);
        grid.repaint();
    }

    private void updateStatus(boolean done) {
        String text = null;
        String warning = null;
        if (error == null) {
            try {
                int opened = perc.numberOfOpenSites();
                double p = (double) opened / ((double) n * n);
                text = String.format("Open: %d / %d (%.1f%%)", opened, n * n, 100 * p);
                if (done) text += String.format("   PERCOLATES at p = %.3f", p);
                warning = consistencyWarning(done);
            } catch (RuntimeException ex) {
                fail("a Percolation method", ex);
            }
        }

        String message;
        if (error != null) {
            message = "Error: " + error + "  (details in console; Reset to clear)";
        } else if (warning != null) {
            message = text + "   WARNING: " + warning;
        } else {
            message = text;
        }
        boolean problem = (error != null || warning != null);
        status.setForeground(problem ? WARN_TEXT : normalText);
        status.setText(message);
        status.setToolTipText(problem ? message : null);   // full text on hover if truncated
    }

    /**
     * Cross-checks the student's methods against each other: the system
     * percolates exactly when some bottom-row site is full.
     */
    private String consistencyWarning(boolean percolates) {
        boolean bottomFull = false;
        for (int col = 0; col < n && !bottomFull; col++) {
            bottomFull = perc.isFull(n - 1, col);
        }
        if (percolates && !bottomFull) {
            return "percolates() is true, but no bottom-row site isFull()";
        }
        if (!percolates && bottomFull) {
            return "a bottom-row site isFull(), but percolates() is false";
        }
        return null;
    }

    // ===================================================================
    // Calls into Percolation code, guarded so bugs show up in the window
    // ===================================================================

    private void fail(String call, RuntimeException ex) {
        if (error == null) {
            String detail = (ex.getMessage() != null) ? ": " + ex.getMessage() : "";
            error = call + " threw " + ex.getClass().getSimpleName() + detail;
            System.err.println("Percolatron: " + error);
            ex.printStackTrace();
        }
        pause();
    }

    private void safeOpen(int row, int col) {
        try {
            perc.open(row, col);
        } catch (RuntimeException ex) {
            fail("open(" + row + ", " + col + ")", ex);
        }
    }

    /** Returns null if isOpen threw. */
    private Boolean safeIsOpen(int row, int col) {
        try {
            return perc.isOpen(row, col);
        } catch (RuntimeException ex) {
            fail("isOpen(" + row + ", " + col + ")", ex);
            return null;
        }
    }

    private boolean safePercolates() {
        try {
            return perc.percolates();
        } catch (RuntimeException ex) {
            fail("percolates()", ex);
            return false;
        }
    }

    // ===================================================================
    // Animation
    // ===================================================================

    private void toggleRun() {
        if (timer.isRunning()) pause();
        else play();
    }

    // ----- speed: the slider maps logarithmically from minRate to maxRate -----
    private double rateFor(int sliderValue) {
        return minRate * Math.pow(maxRate / minRate, (double) sliderValue / SLIDER_MAX);
    }

    private int sliderValueFor(double rate) {
        return (int) Math.round(SLIDER_MAX * Math.log(rate / minRate) / Math.log(maxRate / minRate));
    }

    private void setSpeed(double rate) {
        sitesPerSecond = rate;
        speedLabel.setText(formatRate(rate));
    }

    private static String formatRate(double rate) {
        return (rate < 10) ? String.format("%.1f sites/s", rate)
                           : String.format("%,.0f sites/s", rate);
    }

    private void play() {
        pending = 1.0;   // open the first site immediately, even at slow speeds
        timer.start();
        runButton.setText("Pause");
    }

    private void pause() {
        timer.stop();
        runButton.setText("Run");
    }

    /** One timer tick: opens however many sites the speed calls for, stopping if it percolates. */
    private void tick() {
        if (error != null || safePercolates()) {   // defensive: nothing left to do
            stateChanged();
            return;
        }
        // Accumulate fractional sites so slow speeds (under 1 site per tick) work too
        pending += sitesPerSecond * TICK_MS / 1000.0;
        int count = (int) pending;
        pending -= count;

        for (int k = 0; k < count; k++) {
            if (!openNextRandom()) break;
            if (error != null || safePercolates()) break;
        }
        if (count > 0) stateChanged();
    }

    /** The Step button: opens exactly one random site. */
    private void step() {
        openNextRandom();
        stateChanged();
    }

    /** Opens the next closed site in the shuffled order. Returns false if none was opened. */
    private boolean openNextRandom() {
        while (next < order.length) {
            int site = order[next++];
            int row = site / n;
            int col = site % n;
            Boolean open = safeIsOpen(row, col);
            if (open == null) return false;
            if (!open) {
                safeOpen(row, col);
                return error == null;
            }
        }
        return false;
    }

    /** Fisher-Yates shuffle of all n*n site indices. */
    private void shuffleOrder() {
        order = new int[n * n];
        for (int i = 0; i < order.length; i++) order[i] = i;
        for (int i = order.length - 1; i > 0; i--) {
            int j = RNG.nextInt(i + 1);
            int tmp = order[i];
            order[i] = order[j];
            order[j] = tmp;
        }
        next = 0;
    }

    // ===================================================================
    // The grid: drawing and mouse input
    // ===================================================================

    /** Writes the grid to percolatron-n-k.png in the working directory (first unused k). */
    private void saveImage() {
        try {
            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
                    grid.getWidth(), grid.getHeight(), java.awt.image.BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            grid.paint(g);
            g.dispose();
            java.io.File f;
            int k = 1;
            do {
                f = new java.io.File("percolatron-" + n + "-" + k++ + ".png");
            } while (f.exists());
            javax.imageio.ImageIO.write(img, "png", f);
            System.out.println("Saved " + f.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private class GridPanel extends JPanel {

        GridPanel() {
            setPreferredSize(new Dimension(600, 600));
            setBackground(GRID_COLOR);

            MouseAdapter handler = new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) {
                    lastRow = -1;   // a new press starts a new line
                    dragTo(e.getX(), e.getY());
                }
                @Override public void mouseDragged(MouseEvent e) {
                    dragTo(e.getX(), e.getY());
                }
            };
            addMouseListener(handler);
            addMouseMotionListener(handler);
        }

        private int lastRow = -1;   // last cell visited in the current drag (-1 = none)
        private int lastCol = -1;

        /** Opens the cell under the mouse and every cell on the way from the previous one. */
        private void dragTo(int x, int y) {
            int row = rowAt(y);
            int col = colAt(x);
            if (row < 0 || col < 0) {   // off the grid: break the line
                lastRow = -1;
                return;
            }
            if (lastRow < 0) openCell(row, col);
            else openLine(lastRow, lastCol, row, col);
            lastRow = row;
            lastCol = col;
            stateChanged();
        }

        /**
         * Opens a 4-connected path of cells from (r0, c0) to (r1, c1).
         * Steps along the longer axis, rounding the shorter one, and fills in
         * a corner cell wherever the line moves diagonally, since cells that
         * touch only at a corner aren't connected in percolation.
         */
        private void openLine(int r0, int c0, int r1, int c1) {
            int dr = r1 - r0;
            int dc = c1 - c0;
            int steps = Math.max(Math.abs(dr), Math.abs(dc));
            int prevR = r0;
            int prevC = c0;
            for (int i = 1; i <= steps; i++) {
                int r = r0 + (int) Math.round((double) i * dr / steps);
                int c = c0 + (int) Math.round((double) i * dc / steps);
                if (r != prevR && c != prevC) openCell(prevR, c);   // fill the corner
                openCell(r, c);
                prevR = r;
                prevC = c;
            }
        }

        /** Opens one cell if it's closed. The caller refreshes the display. */
        private void openCell(int row, int col) {
            if (error != null) return;
            Boolean open = safeIsOpen(row, col);
            if (open != null && !open) safeOpen(row, col);
        }

        // geometry: getWidth()/getHeight() here are the GRID's size
        private int cellSize() { return Math.min(getWidth(), getHeight()) / n; }
        private int originX()  { return (getWidth()  - cellSize() * n) / 2; }
        private int originY()  { return (getHeight() - cellSize() * n) / 2; }

        private int colAt(int x) {
            int size = cellSize();
            if (size == 0) return -1;
            int col = Math.floorDiv(x - originX(), size);
            return (col >= 0 && col < n) ? col : -1;
        }

        private int rowAt(int y) {
            int size = cellSize();
            if (size == 0) return -1;
            int row = Math.floorDiv(y - originY(), size);
            return (row >= 0 && row < n) ? row : -1;
        }

        private Color colorOf(int row, int col) {
            try {
                if (perc.isFull(row, col)) return FULL_COLOR;
                if (perc.isOpen(row, col)) return OPEN_COLOR;
                return BLOCKED_COLOR;
            } catch (RuntimeException ex) {
                if (!paintErrorLogged) {
                    paintErrorLogged = true;
                    System.err.println("Percolatron: isFull/isOpen(" + row + ", " + col
                            + ") threw while painting (cells drawn in magenta):");
                    ex.printStackTrace();
                }
                return ERROR_COLOR;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int size = cellSize();
            int gap = (size >= 4) ? GAP : 0;   // drop grid lines when cells are tiny
            int x0 = originX();
            int y0 = originY();
            for (int row = 0; row < n; row++) {
                for (int col = 0; col < n; col++) {
                    g.setColor(colorOf(row, col));
                    g.fillRect(x0 + col * size + gap, y0 + row * size + gap,
                               size - gap, size - gap);
                }
            }
        }
    }
}