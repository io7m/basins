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

package com.io7m.basins.world0;

import com.io7m.jtensors.core.unparameterized.vectors.Vector2I;
import com.io7m.jtensors.core.unparameterized.vectors.Vectors2I;
import io.reactivex.subjects.PublishSubject;
import javaslang.collection.Seq;
import javaslang.collection.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static java.awt.event.InputEvent.CTRL_DOWN_MASK;

public final class RoomDemo
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(RoomDemo.class);
  }

  private RoomDemo()
  {

  }

  private static final class PolygonCanvas extends JPanel
  {
    private final RoomModel model;
    private final RoomEditingModel model_edit;
    private final RoomEditingPolygonCreatorType poly_create;
    private final PublishSubject<UndoAvailable> undo_status;
    private Vector2I mouse;
    private Vector2I mouse_snap;
    private String status;

    PolygonCanvas()
    {
      this.mouse = Vectors2I.zero();
      this.mouse_snap = Vectors2I.zero();
      this.model = new RoomModel(32);
      this.model_edit = new RoomEditingModel(this.model);
      this.poly_create = this.model_edit.polygonCreate();
      this.undo_status = PublishSubject.create();
      this.undo_status.onNext(new UndoAvailable(false));

      final MouseAdapter listener = new MouseAdapter()
      {
        @Override
        public void mouseReleased(final MouseEvent e)
        {
          RoomDemo.PolygonCanvas.this.onMouseReleased(
            e.getX(),
            e.getY(),
            e.getButton());
        }

        @Override
        public void mouseDragged(final MouseEvent e)
        {
          RoomDemo.PolygonCanvas.this.onMouseMoved(e.getX(), e.getY());
        }

        @Override
        public void mouseMoved(final MouseEvent e)
        {
          RoomDemo.PolygonCanvas.this.onMouseMoved(e.getX(), e.getY());
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
          if (this.poly_create.addPoint(this.mouse_snap)) {
            final RoomPolygonID pid = this.poly_create.create();
            this.status = "Created " + pid.value();
          }
        }

      } catch (final Exception e) {
        this.status = e.getMessage();
      } finally {
        this.undo_status.onNext(new UndoAvailable(this.model.undoAvailable()));
        this.repaint();
      }
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

      if (this.status != null) {
        gg.setPaint(Color.BLACK);
        gg.drawString(this.status, 16, 16);
      }

      {
        final Vector<Vector2I> in_progress = this.poly_create.points();
        if (!in_progress.isEmpty()) {
          drawPoints(gg, in_progress, Color.GRAY);
        }
      }

      {
        final RoomModelState state = this.model.state();
        state.polygons().forEach(
          (poly_id, poly) ->
            drawPoints(
              gg,
              poly.points().map(p -> state.pointGet(p).position()),
              Color.BLUE));
      }

      gg.setPaint(Color.GREEN);
      gg.fillOval(this.mouse.x() - 4, this.mouse.y() - 4, 8, 8);

      gg.setPaint(Color.RED);
      gg.fillOval(this.mouse_snap.x() - 4, this.mouse_snap.y() - 4, 8, 8);
    }

    private static void drawPoints(
      final Graphics2D gg,
      final Seq<Vector2I> pl,
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

    public void undo()
    {
      this.model.undo();
      this.undo_status.onNext(new UndoAvailable(this.model.undoAvailable()));
    }
  }

  private static final class UndoAvailable
  {
    private final boolean available;

    UndoAvailable(
      final boolean in_available)
    {
      this.available = in_available;
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

      final JMenuItem file_exit = new JMenuItem("Exit");
      file_exit.addActionListener(e -> this.dispose());
      file_exit.setMnemonic('x');
      final JMenu file = new JMenu("File");
      file.add(file_exit);

      final JMenuItem edit_undo = new JMenuItem("Undo");
      edit_undo.setEnabled(false);
      edit_undo.setMnemonic('U');
      edit_undo.setAccelerator(KeyStroke.getKeyStroke('Z', CTRL_DOWN_MASK));
      edit_undo.addActionListener(e -> canvas.undo());
      this.canvas.undo_status.subscribe(u -> edit_undo.setEnabled(u.available));

      final JMenu edit = new JMenu("Edit");
      edit.add(edit_undo);

      final JMenuBar menu = new JMenuBar();
      menu.add(file);
      menu.add(edit);
      this.setJMenuBar(menu);

      this.setPreferredSize(new Dimension(800, 600));
      this.getContentPane().add(this.canvas);
      this.addWindowListener(new WindowAdapter()
      {
        @Override
        public void windowOpened(final WindowEvent e)
        {
          RoomDemo.PolygonWindow.this.canvas.requestFocusInWindow();
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
