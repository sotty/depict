/**
 *
 */
package http.it.unibo.deis.lia.depict;

import http.it.unibo.deis.lia.depict.eventview.EventMessage;
import http.it.unibo.deis.lia.depict.eventview.FluentMessage;
import http.it.unibo.deis.lia.depict.eventview.Interval;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JPanel;


/**
 * @author stefano, fabio, marco
 *
 */
public class JRECPanel extends JPanel {


    /**
     * A snapshot is a collection of features (values over entries) that have
     * changed from previous snapshot, together with the width in pixels of the
     * screen area required to draw it.
     *
     * @author stefano
     */
    private class Snapshot {
        private Map<String, Interval> features;
        private int width;
    }

    /**
     * The dash for dashed lines.
     */
    private static final float _LINE_DASH[] = { 5.0f, 3.0f };

    /**
     * The dot for dotted lines.
     */
    private static final float _LINE_DOT[] = { 1.0f };

    /**
     * The default margin in pixels for the elements of the graph.
     */
    private static final int DEFAULT_MARGIN = 4;

    /**
     * The default scale for adjusting the height of the graph.
     */
    private static final double DEFAULT_SCALE = 3.0;

    /**
     * The default spanning in pixels between the rows of the graph.
     */
    private static final int DEFAULT_SPANNING = 2 * DEFAULT_MARGIN;

    /**
     * The default width in pixels of a unit of the graph.
     */
    private static final int DEFAULT_UNIT_WIDTH = 25;

    /**
     * The default width in pixels of the right gutter of the graph.
     */
    // private static final int DEFAULT_GUTTER_WIDTH = 1 + DEFAULT_UNIT_WIDTH /
    // 2;

    /**
     * The generated serial version UID for this class.
     */
    private static final long serialVersionUID = -1381331321743314389L;

    /**
     * A stroke for a thick solid line.
     */
    private static final BasicStroke THICK_SOLID_LINE = new BasicStroke(3.0f);

    /**
     * The line color to draw events.
     */
    private static final Color EVENT_LINE_COLOR = new Color(0, 200, 255);

    /**
     * The line color to draw fluents.
     */
    private static final Color FLUENT_LINE_COLOR = new Color(255, 200, 0);

    /**
     * The fill color to draw fluents.
     */
    private static final Color FLUENT_FILL_COLOR = new Color(255, 240, 0, 128);

    /**
     * A stroke for a thin dashed line.
     */
    private static final BasicStroke THIN_DASHED_LINE = new BasicStroke(1.0f,
            BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, _LINE_DASH,
            0.0f);

    /**
     * A stroke for a thin dotted line.
     */
    private static final BasicStroke THIN_DOTTED_LINE = new BasicStroke(1.0f,
            BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, _LINE_DOT,
            0.0f);

    /**
     * A stroke for a thin solid line.
     */
    private static final BasicStroke THIN_SOLID_LINE = new BasicStroke(1.0f);

    /**
     * The font used to render the captions.
     */
    private Font captionFont;

    /**
     * The metrics used to render the captions' font.
     */
    private FontMetrics captionMetrics;

    /**
     * The width in pixels of the right gutter of the graph.
     */
    private int gutterWidth;

    /**
     * The font used to render the labels.
     */
    private Font labelFont;

    /**
     * The metrics used to render the labels' font.
     */
    private FontMetrics labelMetrics;

    /**
     * The events' section of this graph's legend.
     */
    private TreeSet<String> events;

    /**
     * The fluents' section of this graph's legend.
     */
    private TreeSet<String> fluents;

    /**
     * The height in pixels of the legend of the graph.
     */
    private int legendHeight;

    /**
     * The width in pixels of the legend of the graph.
     */
    private int legendWidth;

    /**
     * The margin in pixels for the elements of the graph.
     */
    private int margin;


    /**
     * The height in pixels of each row of the graph.
     */
    private int rowHeight;

    /**
     * The scale for adjusting the height of the graph.
     */
    private double scale;

    /**
     * The spanning in pixels between the rows of the graph.
     */
    private int spanning;

    /**
     * The height in pixels of a unit of the graph.
     */
    private int unitHeight;

    /**
     * The width in pixels of a unit of the graph.
     */
    private int unitWidth;

    /**
     * The sliding window for this graph.
     */
    private TreeMap<Long, Snapshot> window;

    /**
     * The time of the snapshot of the domain's status just before the window's
     * history.
     */
    private long windowBound;

    /**
     * A snapshot of the domain's status just before the window's history.
     */
    private Snapshot windowFrame;

    /**
     * The width in pixels of the cached history.
     */
    private int windowWidth;

    /**
     * Component listener: notifies a change in the panel's dimension.
     */
    private ComponentListener componentListener = new ComponentListener() {

        public void componentResized(ComponentEvent paramComponentEvent) {
            modified = true;
        }


        public void componentMoved(ComponentEvent paramComponentEvent) {
        }


        public void componentShown(ComponentEvent paramComponentEvent) {
        }


        public void componentHidden(ComponentEvent paramComponentEvent) {
        }
    };

    /**
     *
     */
    private boolean modified;

    /**
     * @param layout
     * @param isDoubleBuffered
     */
    public JRECPanel( LayoutManager layout, boolean isDoubleBuffered ) {
        super(layout, isDoubleBuffered);

        // this.legend = new HashMap<String, Datatype>();
        this.events = new TreeSet<String>();
        this.fluents = new TreeSet<String>();
        this.window = new TreeMap<Long, Snapshot>();
        this.windowBound = 0;
        this.windowFrame = new Snapshot();
        this.windowFrame.features = new HashMap<String, Interval>();
        this.windowWidth = 0;

        this.captionFont = new Font(Font.SANS_SERIF, Font.ITALIC, 10);
        this.labelFont = new Font(Font.SANS_SERIF, Font.BOLD, 12);
        this.margin = DEFAULT_MARGIN;
        this.scale = DEFAULT_SCALE;
        this.spanning = DEFAULT_SPANNING;

        this.modified = true;
        this.addComponentListener(componentListener);
    }


    /**
     * @param message
     */
    public void add(EventMessage message) {
        if (message == null)
            throw new IllegalArgumentException(
                    "Illegal 'message' argument in JRECPanel.add(EventMessage): "
                            + message);
        String label = message.getLabel();
        if (!label.contains("Initially")&&!label.contains("null")) {
            if (!events.contains(label)) {
                events.add(label);
                modified = true;
            }
            Snapshot snapshot = addTime(message.getStart());
            snapshot.features.put(label, Interval.TRUE);
            repaint();
        }
    }

    /**
     * @param message
     */
    public void add(FluentMessage message) {
        if (message == null)
            throw new IllegalArgumentException(
                    "Illegal 'message' argument in JRECPanel.add(FluentMessage): "
                            + message);
        String label = message.getLabel();
        long time = message.getStart();
        Interval value = message.getValue();
        if (time == windowBound)
            windowFrame.features.put(label, value);
        else {
            if (!fluents.contains(label)) {
                fluents.add(label);
                modified = true;
            }
            Snapshot snapshot = addTime(time);
            snapshot.features.put(label, value);
        }
        repaint();
    }

    /**
     * @param time
     * @return
     */
    private Snapshot addTime(long time) {
        Long tick;
        Snapshot result = window.get(time);
        if (result == null) {
            result = new Snapshot();
            result.features = new HashMap<String, Interval>();
            if ((tick = window.lowerKey(time)) == null)
                tick = windowBound;
            result.width = (int) (unitWidth * Math.log1p(time - tick));
            this.windowWidth += result.width;
            if ((tick = window.higherKey(time)) != null) {
                Snapshot next = window.get(tick);
                this.windowWidth -= next.width;
                next.width = (int) (unitWidth * Math.log1p(tick - time));
                this.windowWidth += next.width;
            }
            window.put(time, result);
            if (!modified) {
                this.unitWidth = Math.max(unitWidth,
                        captionMetrics.stringWidth(Long.toString(time)));
                this.gutterWidth = 1 + unitWidth / 2;
                slideWindow();
            }
        }
        return result;
    }

    /*
      * (non-Javadoc)
      * 
      * @see java.awt.Component#getFont()
      */

    public Font getFont() {
        return labelFont;
    }

    /**
     * Returns the margin in pixels for the elements of the graph.
     *
     * @return the margin in pixels for the elements of the graph
     */
    public int getMargin() {
        return margin;
    }

    /**
     * Returns the scale for adjusting the height of the graph.
     *
     * @return the scale for adjusting the height of the graph
     */
    public double getScale() {
        return scale;
    }

    /**
     * Returns the spanning in pixels between the rows of the graph.
     *
     * @return the spanning in pixels between the rows of the graph
     */
    public int getSpanning() {
        return spanning;
    }



    /**
     * The background rectangle.
     */
    private Rectangle bkgArea = getBounds();

    /**
     * The working rectangle.
     */
    private Rectangle wrkArea = new Rectangle(bkgArea.x + margin, bkgArea.y
            + margin, bkgArea.width - 2 * margin, legendHeight + 2 * margin);

    /**
     * The plotting rectangle.
     */
    private Rectangle pltArea = new Rectangle(wrkArea.x + legendWidth
            + spanning, wrkArea.y, wrkArea.width - legendWidth - spanning,
            wrkArea.height);

    /**
     * The graph area.
     */
    private Rectangle grfArea = new Rectangle(pltArea.x + margin, pltArea.y
            + margin, pltArea.width - 2 * margin, legendHeight);

    /*
      * (non-Javadoc)
      * 
      * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
      */

    protected void paintComponent(Graphics g) {
        if (events.size() + fluents.size() > 0) {
            if (modified)
                updateWindow();
            Graphics2D g2 = (Graphics2D) g;
            // Fill the background area with the background color
            g2.setPaint(getBackground());
            g2.fillRect(bkgArea.x, bkgArea.y, bkgArea.width, bkgArea.height);
            // Fill the plotting area with white
            g2.setPaint(Color.WHITE);
            g2.fillRect(pltArea.x, pltArea.y, pltArea.width, pltArea.height);
            // Plot two vertical gray thin lines around the graph
            g2.setPaint(Color.LIGHT_GRAY);
            g2.setStroke(THIN_SOLID_LINE);
            g2.drawLine(grfArea.x, grfArea.y, grfArea.x, grfArea.y
                    + grfArea.height);
            g2.drawLine(grfArea.x + grfArea.width, grfArea.y, grfArea.x
                    + grfArea.width, grfArea.y + grfArea.height);
            // Plot the vertical gray dashed lines of the graph
            g2.setStroke(THIN_DASHED_LINE);
            Rectangle fullCol = new Rectangle(grfArea.x, grfArea.y, 0,
                    grfArea.height - margin - captionMetrics.getHeight());
            g2.setFont(captionFont);
            for (long time : window.keySet()) {
                fullCol.width = window.get(time).width;
                String caption = Long.toString(time);
                g2.drawLine(fullCol.x + fullCol.width, fullCol.y, fullCol.x
                        + fullCol.width, fullCol.y + fullCol.height);
                g2.drawString(caption, fullCol.x + fullCol.width
                        - captionMetrics.stringWidth(caption) / 2, fullCol.y
                        + fullCol.height + captionMetrics.getHeight());
                fullCol.x += fullCol.width;
            }
            // Define the first label row
            Rectangle lblRow = new Rectangle(wrkArea.x, wrkArea.y + 2 * margin,
                    wrkArea.width - margin, unitHeight);
            g2.setFont(labelFont);
            g2.setPaint(Color.BLACK);
            g2.setStroke(THIN_SOLID_LINE);
            for (String label : events) {
                // Draw the label for this entry
                g2.drawString(label, lblRow.x, lblRow.y + lblRow.height - 1);
                // Plot an horizontal black thin line for this entry
                g2.drawLine(lblRow.x, lblRow.y + lblRow.height, lblRow.x
                        + lblRow.width, lblRow.y + lblRow.height);
                lblRow.y += rowHeight;
            }
            for (String label : fluents) {
                // Draw the label for this entry
                g2.drawString(label, lblRow.x, lblRow.y + lblRow.height - 1);
                // Plot an horizontal black thin line for this entry
                g2.drawLine(lblRow.x, lblRow.y + lblRow.height, lblRow.x
                        + lblRow.width, lblRow.y + lblRow.height);
                lblRow.y += rowHeight;
            }

            // Define the first graph row
            Rectangle grfRow = new Rectangle(grfArea.x, grfArea.y + margin,
                    grfArea.width, unitHeight);
            g2.setPaint(Color.LIGHT_GRAY);
            g2.setStroke(THIN_DOTTED_LINE);
            for (int i = 0; i < events.size() + fluents.size(); i++) {
                // Plot an horizontal light gray dotted line for this entry
                g2.drawLine(grfRow.x, grfRow.y, grfRow.x + grfRow.width,
                        grfRow.y);
                grfRow.y += rowHeight;
            }

            g2.setStroke(THICK_SOLID_LINE);
            g2.setPaint(EVENT_LINE_COLOR);
            Interval previous, value;
            Snapshot snapshot;
            Rectangle cell, col, row = new Rectangle(grfArea.x, grfArea.y
                    + margin, grfArea.width, unitHeight);
            for (String label : events) {
                col = new Rectangle(grfArea.x, grfArea.y, 0, grfArea.height
                        - margin - captionMetrics.getHeight());
                for (long time : window.keySet()) {
                    snapshot = window.get(time);
                    col.width = snapshot.width;
                    cell = col.intersection(row);
                    value = snapshot.features.get(label);
                    g2.drawLine(cell.x, cell.y + cell.height, cell.x
                            + cell.width, cell.y + cell.height);
                    if (value != null && value.equals(Interval.TRUE))
                        g2.drawLine(cell.x + cell.width, cell.y, cell.x
                                + cell.width, cell.y + cell.height);
                    col.x += col.width;
                }
                row.y += rowHeight;
            }

            for (String label : fluents) {
                previous = windowFrame.features.get(label);
                //Inserita per evitare initially
                if (previous==null){
                    previous=Interval.FALSE;
                }
                int v_hi, p_hi = (int) (unitHeight * (1.0 - previous
                        .getUpperBound()));
                int v_lo, p_lo = (int) (unitHeight * (1.0 - previous
                        .getLowerBound()));
                col = new Rectangle(grfArea.x, grfArea.y, 0, grfArea.height
                        - margin - captionMetrics.getHeight());
                cell = col.intersection(row);
                for (long time : window.keySet()) {
                    // count -= 1;
                    snapshot = window.get(time);

                    value = snapshot.features.get(label);
                    if (value == null)
                        value = previous;
                    v_hi = (int) (unitHeight * (1.0 - value.getUpperBound()));
                    v_lo = (int) (unitHeight * (1.0 - value.getLowerBound()));

                    col.width = snapshot.width;
                    cell = col.intersection(row);

                    g2.setPaint(FLUENT_FILL_COLOR);
                    g2.fillRect(cell.x, cell.y + p_hi, cell.width, p_lo - p_hi);

                    g2.setPaint(FLUENT_LINE_COLOR);
                    // Horizontal higher line
                    if (!previous.equals(Interval.FALSE))
                        g2.drawLine(cell.x, cell.y + p_hi, cell.x + cell.width,
                                cell.y + p_hi);
                    // Horizontal lower line
                    if (!previous.equals(Interval.TRUE))
                        g2.drawLine(cell.x, cell.y + p_lo, cell.x + cell.width,
                                cell.y + p_lo);
                    // Vertical lines if we have some sort of transition
                    if (!value.equals(previous)) {
                        g2.drawLine(cell.x + cell.width, cell.y + v_hi, cell.x
                                + cell.width, cell.y + p_hi);
                        g2.drawLine(cell.x + cell.width, cell.y + v_lo, cell.x
                                + cell.width, cell.y + p_lo);
                    }
                    previous = value;
                    p_hi = v_hi;
                    p_lo = v_lo;
                    col.x += col.width;
                }
                cell.x = col.x;
                cell.width = bkgArea.width - cell.x - 2 * margin;

                g2.setPaint(FLUENT_FILL_COLOR);
                g2.fillRect(cell.x, cell.y + p_hi, cell.width, p_lo - p_hi);

                g2.setPaint(FLUENT_LINE_COLOR);
                // Horizontal higher line
                if (!previous.equals(Interval.FALSE))
                    g2.drawLine(cell.x, cell.y + p_hi, cell.x + cell.width,
                            cell.y + p_hi);
                // Horizontal lower line
                if (!previous.equals(Interval.TRUE))
                    g2.drawLine(cell.x, cell.y + p_lo, cell.x + cell.width,
                            cell.y + p_lo);
                row.y += rowHeight;
            }
        }
    }

    /*
      * (non-Javadoc)
      * 
      * @see javax.swing.JComponent#setFont(java.awt.Font)
      */

    public void setFont(Font font) {
        if (font == null)
            throw new IllegalArgumentException(
                    "Illegal 'font' argument in JRECPanel.setFont(Font): "
                            + font);
        String fontName = font.getFontName();
        int fontSize = font.getSize();
        font = new Font(fontName, Font.BOLD, fontSize);
        if (font != this.labelFont) {
            this.labelFont = font;
            this.captionFont = new Font(fontName, Font.ITALIC, fontSize - 2);
            modified = true;
            repaint();
        }
    }

    /**
     * Sets the margin in pixels for the elements of the graph and, if need be,
     * repaints the panel.
     *
     * @param margin
     *            the margin in pixels for the elements of the graph to set
     */
    public void setMargin(int margin) {
        if (margin <= 0)
            throw new IllegalArgumentException(
                    "Illegal 'margin' argument in JRECPanel.setMargin(int): "
                            + margin);
        if (margin != this.margin) {
            this.margin = margin;
            modified = true;
            repaint();
        }
    }

    /**
     * Sets the scale for adjusting the height of the graph and, if need be,
     * repaints the panel.
     *
     * @param scale
     *            the scale for adjusting the height of the graph to set
     */
    public void setScale(double scale) {
        if (scale <= 0.0)
            throw new IllegalArgumentException(
                    "Illegal 'scale' argument in JRECPanel.setScale(double): "
                            + scale);
        if (scale != this.scale) {
            this.scale = scale;
            modified = true;
            repaint();
        }
    }

    /**
     * Sets the spanning in pixels between the rows of the graph and, if need
     * be, repaints the panel.
     *
     * @param spanning
     *            the spanning in pixels between the rows of the graph to set
     */
    public void setSpanning(int spanning) {
        if (spanning <= 0)
            throw new IllegalArgumentException(
                    "Illegal 'spanning' argument in JRECPanel.setSpanning(int): "
                            + spanning);
        if (spanning != this.spanning) {
            this.spanning = spanning;
            modified = true;
            repaint();
        }
    }

    /**
     * Adjust the data on current window.
     */
    private void adjustWindow() {
        Snapshot snapshot;
        long current = windowBound;
        this.windowWidth = 0;
        for (long time : window.keySet()) {
            snapshot = window.get(time);
            snapshot.width = (int) (unitWidth * Math.log1p(time - current));
            windowWidth += snapshot.width;
            current = time;
        }
    }

    /**
     * Slides out the data on current window.
     */
    private void slideWindow() {
        Long time;
        Snapshot snapshot;
        while (windowWidth + gutterWidth >= grfArea.width) {
            time = window.firstKey();
            windowWidth -= window.get(time).width;
            snapshot = window.remove(time);
            windowFrame.width = snapshot.width;
            if (windowFrame.features == null)
                windowFrame.features = new HashMap<String, Interval>();
            windowFrame.features.putAll(snapshot.features);
            windowBound = time;
        }
    }

    /**
     * Updates the values used to render the window.
     */
    private void updateWindow() {
        this.captionMetrics = getFontMetrics(captionFont);
        this.labelMetrics = getFontMetrics(labelFont);
        this.unitHeight = (int) (scale * labelMetrics.getHeight());
        this.unitWidth = DEFAULT_UNIT_WIDTH;
        if (window.size() > 0)
            this.unitWidth = Math.max(unitWidth,
                    captionMetrics.stringWidth(window.lastKey().toString()));
        this.gutterWidth = 1 + unitWidth / 2;
        this.rowHeight = margin + unitHeight + spanning;
        this.legendHeight = (events.size() + fluents.size()) * rowHeight
                + captionMetrics.getHeight() + margin;
        this.legendWidth = DEFAULT_UNIT_WIDTH;
        for (String label : events)
            this.legendWidth = Math.max(legendWidth,
                    labelMetrics.stringWidth(label));
        for (String label : fluents)
            this.legendWidth = Math.max(legendWidth,
                    labelMetrics.stringWidth(label));
        this.legendWidth += spanning;
        this.bkgArea = getBounds();
        this.wrkArea = new Rectangle(bkgArea.x + margin, bkgArea.y + margin,
                bkgArea.width - 2 * margin, legendHeight + 2 * margin);
        this.pltArea = new Rectangle(wrkArea.x + legendWidth + spanning,
                wrkArea.y, wrkArea.width - legendWidth - spanning,
                wrkArea.height);
        this.grfArea = new Rectangle(pltArea.x + margin, pltArea.y + margin,
                pltArea.width - 2 * margin, legendHeight);
        adjustWindow();
        slideWindow();
        modified = false;
    }





}
