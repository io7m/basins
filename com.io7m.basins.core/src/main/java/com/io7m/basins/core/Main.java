/*
 * Copyright © 2017 <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.basins.core;

import com.io7m.jtensors.core.unparameterized.vectors.Vector2D;
import com.io7m.jtensors.core.unparameterized.vectors.Vector2I;
import com.io7m.jtensors.core.unparameterized.vectors.Vectors2D;
import com.io7m.jtensors.core.unparameterized.vectors.Vectors2I;
import org.jgrapht.graph.SimpleGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class Main
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(Main.class);
  }

  private Main()
  {

  }

  private static final class PolygonCanvas extends JPanel
  {
    private final SimpleGraph<ConvexPolygon, ConvexPolygonConnection> polygon_graph;
    private Vector2I mouse;
    private Vector2I mouse_snap;
    private List<Vector2I> points;
    private String status;

    PolygonCanvas()
    {
      this.mouse = Vectors2I.zero();
      this.mouse_snap = Vectors2I.zero();
      this.points = new ArrayList<>();
      this.polygon_graph = new SimpleGraph<>(ConvexPolygonConnection::of);

      final MouseAdapter listener = new MouseAdapter()
      {
        @Override
        public void mouseReleased(final MouseEvent e)
        {
          PolygonCanvas.this.onMouseReleased(e.getX(), e.getY(), e.getButton());
        }

        @Override
        public void mouseDragged(final MouseEvent e)
        {
          PolygonCanvas.this.onMouseMoved(e.getX(), e.getY());
        }

        @Override
        public void mouseMoved(final MouseEvent e)
        {
          PolygonCanvas.this.onMouseMoved(e.getX(), e.getY());
        }
      };

      this.addMouseMotionListener(listener);
      this.addMouseListener(listener);
    }

    private void onMouseReleased(
      final int x,
      final int y,
      final int button)
    {
      try {
        LOG.trace(
          "onMouseReleased: {} {} {}",
          Integer.valueOf(x),
          Integer.valueOf(y),
          Integer.valueOf(button));

        if (button == 1) {
          if (this.points.contains(this.mouse_snap)) {
            this.addPolygon(this.points);
            return;
          }

          this.points.add(this.mouse_snap);
        }
      } finally {
        this.repaint();
      }
    }

    private void addPolygon(
      final List<Vector2I> points)
    {
      try {
        final ConvexPolygon poly = ConvexPolygon.of(points);

        LOG.debug("addPolygon: {}", poly);

        for (final ConvexPolygon existing : this.polygon_graph.vertexSet()) {
          final boolean intersects = ConvexPolygons.intersects(poly, existing);

          LOG.debug(
            "intersects: {} {} -> {}",
            poly,
            existing,
            Boolean.valueOf(intersects));

          if (intersects) {
            this.status = "Polygon overlaps!";
            return;
          }
        }

        this.polygon_graph.addVertex(poly);

        for (final ConvexPolygon existing : this.polygon_graph.vertexSet()) {
          if (Objects.equals(existing, poly)) {
            continue;
          }

          if (isTouching(poly, existing)) {
            this.polygon_graph.addEdge(poly, existing);
          }
        }

        this.status = null;
      } catch (final IllegalArgumentException e) {
        LOG.error("error adding polygon: ", e);
        this.status = e.getMessage();
      } finally {
        this.points.clear();
      }
    }

    private static boolean isTouching(
      final ConvexPolygon poly,
      final ConvexPolygon existing)
    {
      int count = 0;
      for (final Vector2I v : poly.vertices()) {
        if (existing.vertices().contains(v)) {
          ++count;
        }
      }
      return count >= 2;
    }

    private static int snap(
      final int v,
      final int r)
    {
      return (int) (Math.floor((double) v / (double) r) * (double) r);
    }

    private static int GRID_SNAP = 32;

    private void onMouseMoved(
      final int x,
      final int y)
    {
      LOG.trace("onMouseMoved: {} {}", Integer.valueOf(x), Integer.valueOf(y));
      this.mouse = Vector2I.of(x, y);
      this.mouse_snap = Vector2I.of(
        snap(x + (GRID_SNAP / 2), GRID_SNAP),
        snap(y + (GRID_SNAP / 2), GRID_SNAP));
      this.repaint();
    }

    @Override
    public void paint(
      final Graphics g)
    {
      super.paint(g);

      final Graphics2D gg = (Graphics2D) g;
      gg.setPaint(Color.WHITE);
      gg.fillRect(0, 0, this.getWidth(), this.getHeight());

      drawPoints(gg, this.points, Color.GRAY);

      if (!this.points.isEmpty()) {
        gg.setPaint(Color.MAGENTA);
        gg.fillOval(
          this.points.get(0).x() - 4,
          this.points.get(0).y() - 4,
          8,
          8);
      }

      for (final ConvexPolygon p : this.polygon_graph.vertexSet()) {
        this.drawPolygon(gg, p);
      }

      if (this.status != null) {
        gg.setPaint(Color.BLACK);
        gg.drawString(this.status, 16, 16);
      }

      gg.setPaint(Color.GREEN);
      gg.fillOval(this.mouse.x() - 4, this.mouse.y() - 4, 8, 8);

      gg.setPaint(Color.RED);
      gg.fillOval(this.mouse_snap.x() - 4, this.mouse_snap.y() - 4, 8, 8);
    }

    private void drawPolygon(
      final Graphics2D gg,
      final ConvexPolygon p)
    {
      final List<Vector2I> vertices = p.vertices();
      drawPoints(gg, vertices, Color.BLUE);

      for (int pi0 = 0; pi0 < vertices.size(); ++pi0) {
        final int pi1 = pi0 + 1 == vertices.size() ? 0 : pi0 + 1;

        final Vector2I p0 = vertices.get(pi0);
        final Vector2D axis = Vectors2D.scale(p.normal(pi0, pi1), 16.0);
        gg.setPaint(Color.BLUE);
        final int x0 = p0.x();
        final int y0 = p0.y();
        final int x1 = (int) ((double) x0 + axis.x());
        final int y1 = (int) ((double) y0 + axis.y());
        gg.drawLine(x0, y0, x1, y1);
      }

      final Stroke s = gg.getStroke();
      try {
        final float[] dash = {3.0f};
        gg.setPaint(Color.PINK);
        gg.setStroke(new BasicStroke(
          1.0f,
          BasicStroke.CAP_BUTT,
          BasicStroke.JOIN_MITER,
          10.0f,
          dash,
          0.0f));

        for (final Vector2I v : vertices) {
          gg.drawLine(-1000, v.y(), 1000, v.y());
          gg.drawLine(v.x(), -1000, v.x(), 1000);
        }
      } finally {
        gg.setStroke(s);
      }

      final Set<ConvexPolygonConnection> outgoing =
        this.polygon_graph.edgesOf(p);

      for (final ConvexPolygonConnection e : outgoing) {
        final Vector2D c0 = e.polygon0().barycenter();
        final Vector2D c1 = e.polygon1().barycenter();
        gg.setPaint(Color.GREEN);
        gg.drawLine((int) c0.x(), (int) c0.y(), (int) c1.x(), (int) c1.y());
      }
    }

    private static void drawPoints(
      final Graphics2D gg,
      final List<Vector2I> pl,
      final Color c)
    {
      for (int index = 0; index < pl.size(); ++index) {
        final Vector2I pc = pl.get(index);

        gg.setPaint(c);
        gg.fillOval(pc.x() - 4, pc.y() - 4, 8, 8);

        if (index + 1 < pl.size()) {
          final Vector2I pn = pl.get(index + 1);
          gg.drawLine(pc.x(), pc.y(), pn.x(), pn.y());
        }
      }

      if (pl.size() > 1) {
        final Vector2I first = pl.get(0);
        final Vector2I last = pl.get(pl.size() - 1);
        gg.drawLine(first.x(), first.y(), last.x(), last.y());
      }
    }
  }

  private static final class PolygonWindow extends JFrame
  {
    private final PolygonCanvas canvas;

    private PolygonWindow()
    {
      super("Polygons");

      this.canvas = new PolygonCanvas();
      this.canvas.setFocusable(true);

      this.setPreferredSize(new Dimension(800, 600));
      this.getContentPane().add(this.canvas);
      this.addWindowListener(new WindowAdapter()
      {
        @Override
        public void windowOpened(final WindowEvent e)
        {
          PolygonWindow.this.canvas.requestFocusInWindow();
        }
      });
    }
  }

  public static void main(final String[] args)
  {
    SwingUtilities.invokeLater(() -> {
      final PolygonWindow window = new PolygonWindow();
      window.pack();
      window.setVisible(true);
      window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    });
  }
}
