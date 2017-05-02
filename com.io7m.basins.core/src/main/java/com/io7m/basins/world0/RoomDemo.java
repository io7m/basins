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
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import javaslang.collection.Seq;
import javaslang.collection.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static com.io7m.jnull.NullCheck.notNull;
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

  private static void drawPoints(
    final Graphics2D gg,
    final Seq<Vector2I> pl,
    final Color color_first,
    final Color color_rest)
  {
    for (int index = 0; index < pl.size(); ++index) {
      final Vector2I pc = pl.get(index);

      gg.setPaint(color_rest);
      if (index + 1 < pl.size()) {
        final Vector2I pn = pl.get(index + 1);
        gg.drawLine(pc.x(), pc.y(), pn.x(), pn.y());
      }

      gg.setPaint(index == 0 ? color_first : color_rest);
      gg.fillOval(pc.x() - 4, pc.y() - 4, 8, 8);
    }

    if (pl.size() > 1) {
      final Vector2I first = pl.get(0);
      final Vector2I last = pl.get(pl.size() - 1);
      gg.drawLine(first.x(), first.y(), last.x(), last.y());
    }
  }

  private static int GRID_SNAP = 32;

  private static int snap(
    final int v,
    final int r)
  {
    return (int) (Math.floor((double) v / (double) r) * (double) r);
  }

  private static final class PolygonCanvas extends JPanel
  {
    private final RoomModel model;
    private final RoomEditingModel model_edit;
    private Vector2I mouse;
    private Vector2I mouse_snap;
    private EditingOperationType edit_op;

    PolygonCanvas(
      final PublishSubject<String> messages)
    {
      this.mouse = Vectors2I.zero();
      this.mouse_snap = Vectors2I.zero();
      this.model = new RoomModel(32);
      this.model_edit = new RoomEditingModel(this.model);

      this.addMouseMotionListener(new MouseAdapter()
      {
        @Override
        public void mouseMoved(final MouseEvent e)
        {
          PolygonCanvas.this.onMouseMoved(e.getX(), e.getY());
        }
      });

      this.model.observable().subscribe(e -> this.repaint());
    }

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

      {
        final RoomModelState state = this.model.state();
        state.polygons().forEach(
          (poly_id, poly) ->
            drawPoints(
              gg,
              poly.points().map(p -> state.pointGet(p).position()),
              Color.BLUE,
              Color.BLUE));
      }

      {
        final EditingOperationType op = this.edit_op;
        if (op != null) {
          op.paint(gg);
        }
      }

      gg.setPaint(Color.GREEN);
      gg.fillOval(this.mouse.x() - 4, this.mouse.y() - 4, 8, 8);

      gg.setPaint(Color.RED);
      gg.fillOval(this.mouse_snap.x() - 4, this.mouse_snap.y() - 4, 8, 8);
    }

    public void undo()
    {
      this.model.undo();
    }

    public void startEditingOperation(
      final EditingOperationType op)
    {
      this.edit_op = notNull(op, "op");
      this.addMouseMotionListener(this.edit_op);
      this.addMouseListener(this.edit_op);
    }

    public void stopEditingOperation(
      final EditingOperationType op)
    {
      this.removeMouseMotionListener(op);
      this.removeMouseListener(op);
      this.edit_op = null;
    }
  }

  private interface EditingOperationType extends MouseListener,
    MouseMotionListener
  {
    void paint(Graphics2D g);
  }

  private static final class PolygonCreatorListener extends MouseAdapter implements
    EditingOperationType
  {
    private final RoomEditingPolygonCreatorType poly_create;
    private final PublishSubject<String> messages;
    private final PolygonCanvas canvas;
    private Vector2I mouse_snap;

    PolygonCreatorListener(
      final PolygonCanvas in_canvas,
      final PublishSubject<String> in_messages,
      final RoomEditingModel in_editing)
    {
      this.canvas = notNull(in_canvas, "Canvas");
      this.messages = notNull(in_messages, "Messages");
      this.poly_create = notNull(in_editing, "Editing").polygonCreate();
    }

    @Override
    public void mouseReleased(
      final MouseEvent e)
    {
      final int x = e.getX();
      final int y = e.getY();
      final int button = e.getButton();

      try {
        LOG.trace(
          "onMouseReleased: {} {} {}",
          Integer.valueOf(x),
          Integer.valueOf(y),
          Integer.valueOf(button));

        if (button == 1) {
          if (this.poly_create.addPoint(this.mouse_snap)) {
            final RoomPolygonID pid = this.poly_create.create();
            this.messages.onNext("Created " + pid.value());
            this.canvas.stopEditingOperation(this);
          }
        }
      } catch (final Exception ex) {
        this.messages.onNext(ex.getMessage());
        LOG.error("error: ", ex);
      }
    }

    @Override
    public void mouseMoved(
      final MouseEvent e)
    {
      final int x = e.getX();
      final int y = e.getY();
      LOG.trace("onMouseMoved: {} {}", Integer.valueOf(x), Integer.valueOf(y));
      this.mouse_snap = Vector2I.of(
        snap(x + (GRID_SNAP / 2), GRID_SNAP),
        snap(y + (GRID_SNAP / 2), GRID_SNAP));
    }

    @Override
    public void paint(final Graphics2D g)
    {
      final Vector<Vector2I> in_progress = this.poly_create.points();
      if (!in_progress.isEmpty()) {
        drawPoints(g, in_progress, Color.RED, Color.GRAY);
      }
    }
  }

  private static final class StatusBar extends JPanel
  {
    private final JLabel text_field;

    StatusBar(final Observable<String> messages)
    {
      this.text_field = new JLabel(" ");
      this.setLayout(new FlowLayout(FlowLayout.LEFT));
      this.add(this.text_field);
      messages.subscribe(this.text_field::setText);
    }
  }

  private static final class ToolBar extends JPanel
  {
    private final JButton create_polygon;

    ToolBar(
      final PolygonCanvas canvas,
      final PublishSubject<String> messages)
    {
      this.create_polygon =
        new JButton(new ImageIcon(RoomDemo.class.getResource(
          "/com/io7m/basins/world0/polygon_create.png")));
      this.create_polygon.setToolTipText("Create polygons");
      this.create_polygon.addActionListener(
        e -> {
          messages.onNext(
            "Create points by clicking. Click the starting point to create a polygon.");
          canvas.startEditingOperation(
            new PolygonCreatorListener(canvas, messages, canvas.model_edit));
        });

      this.setLayout(new FlowLayout(FlowLayout.LEFT));
      this.add(this.create_polygon);
    }
  }

  private static final class PolygonWindow extends JFrame
  {
    private final PolygonCanvas canvas;
    private final ToolBar toolbar;
    private final StatusBar status;
    private final PublishSubject<String> messages;

    private PolygonWindow()
    {
      super("Polygons");

      this.messages = PublishSubject.create();
      this.canvas = new PolygonCanvas(this.messages);
      this.toolbar = new ToolBar(this.canvas, this.messages);
      this.status = new StatusBar(this.messages);
      this.canvas.setFocusable(true);
      this.canvas.requestFocusInWindow();

      final JMenuItem file_exit = new JMenuItem("Exit");
      file_exit.addActionListener(e -> this.dispose());
      file_exit.setMnemonic('x');
      final JMenu file = new JMenu("File");
      file.add(file_exit);

      final JMenuItem edit_undo = new JMenuItem("Undo");
      edit_undo.setEnabled(false);
      edit_undo.setMnemonic('U');
      edit_undo.setAccelerator(KeyStroke.getKeyStroke('Z', CTRL_DOWN_MASK));
      edit_undo.addActionListener(e -> this.canvas.undo());
      this.canvas.model.observable().subscribe(
        u -> edit_undo.setEnabled(u.available()));

      final JMenu edit = new JMenu("Edit");
      edit.add(edit_undo);

      final JMenuBar menu = new JMenuBar();
      menu.add(file);
      menu.add(edit);
      this.setJMenuBar(menu);

      this.setPreferredSize(new Dimension(800, 600));
      final Container content = this.getContentPane();
      content.setLayout(new BorderLayout());
      content.add(this.toolbar, BorderLayout.PAGE_START);
      content.add(this.canvas, BorderLayout.CENTER);
      content.add(this.status, BorderLayout.PAGE_END);
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
