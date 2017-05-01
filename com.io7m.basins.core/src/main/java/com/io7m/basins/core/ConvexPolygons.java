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

import java.util.List;

public final class ConvexPolygons
{
  private ConvexPolygons()
  {

  }

  private static double crossProductLength(
    final double Ax,
    final double Ay,
    final double Bx,
    final double By,
    final double Cx,
    final double Cy)
  {
    final double BAx = Ax - Bx;
    final double BAy = Ay - By;
    final double BCx = Cx - Bx;
    final double BCy = Cy - By;

    return (BAx * BCy - BAy * BCx);
  }

  static boolean isConvex(
    final List<Vector2I> points)
  {
    boolean got_negative = false;
    boolean got_positive = false;
    final int num_points = points.size();

    for (int point_a = 0; point_a < num_points; point_a++) {
      final int point_b = (point_a + 1) % num_points;
      final int point_c = (point_b + 1) % num_points;

      final double cross_product =
        crossProductLength(
          (double) points.get(point_a).x(), (double) points.get(point_a).y(),
          (double) points.get(point_b).x(), (double) points.get(point_b).y(),
          (double) points.get(point_c).x(), (double) points.get(point_c).y());
      if (cross_product < 0.0) {
        got_negative = true;
      } else if (cross_product > 0.0) {
        got_positive = true;
      }
      if (got_negative && got_positive) {
        return false;
      }
    }

    return true;
  }

  static Vector2D normal(
    final Vector2D vv0,
    final Vector2D vv1)
  {
    final Vector2D edge = Vectors2D.subtract(vv0, vv1);
    final Vector2D perp = Vector2D.of(edge.y(), -edge.x());
    return Vectors2D.normalize(perp);
  }

  static Vector2D normal(
    final Vector2I vv0,
    final Vector2I vv1)
  {
    return normal(toVector2D(vv0), toVector2D(vv1));
  }

  private static Vector2D toVector2D(
    final Vector2I v)
  {
    return Vector2D.of((double) v.x(), (double) v.y());
  }

  static Projection project(
    final List<Vector2I> points,
    final Vector2D axis)
  {
    double min = Vectors2D.dotProduct(axis, toVector2D(points.get(0)));
    double max = min;

    for (int index = 1; index < points.size(); ++index) {
      final double p = Vectors2D.dotProduct(axis, toVector2D(points.get(index)));
      min = Math.min(p, min);
      max = Math.max(p, max);
    }

    return Projection.of(min, max);
  }

  static boolean intersects(
    final ConvexPolygon p0,
    final ConvexPolygon p1)
  {
    final List<Vector2D> p0_axes = p0.axes();
    final List<Vector2D> p1_axes = p1.axes();

    for (int index = 0; index < p0_axes.size(); ++index) {
      final Vector2D axis = p0_axes.get(index);
      final Projection proj0 = project(p0.vertices(), axis);
      final Projection proj1 = project(p1.vertices(), axis);
      if (!overlaps(proj0, proj1)) {
        return false;
      }
    }

    for (int index = 0; index < p1_axes.size(); ++index) {
      final Vector2D axis = p1_axes.get(index);
      final Projection proj0 = project(p0.vertices(), axis);
      final Projection proj1 = project(p1.vertices(), axis);
      if (!overlaps(proj0, proj1)) {
        return false;
      }
    }

    return true;
  }

  private static boolean overlaps(
    final Projection proj0,
    final Projection proj1)
  {
    final double x0 = proj0.minimum();
    final double x1 = proj0.maximum();
    final double y0 = proj1.minimum();
    final double y1 = proj1.maximum();
    return x1 - 1 > y0 && y1 - 1 > x0;
  }

  static boolean containsPoint(
    final List<Vector2I> points,
    final Vector2I point)
  {
    boolean result = false;
    int i;
    int j;
    for (i = 0, j = points.size() - 1; i < points.size(); j = i++) {
      final Vector2I p0 = points.get(i);
      final Vector2I p1 = points.get(j);
      if (((p0.y() > point.y()) != (p1.y() > point.y()))) {
        final int p1x_p0x_delta = p1.x() - p0.x();
        final int p1y_p0y_delta = p1.y() - p0.y();
        final int py_p0y_delta = point.y() - p0.y();
        if ((point.x() < (((p1x_p0x_delta * py_p0y_delta) / p1y_p0y_delta) + p0.x()))) {
          result = !result;
        }
      }
    }
    return result;
  }

  static Vector2D barycenter(
    final List<Vector2I> points)
  {
    double x = 0.0;
    double y = 0.0;
    final int size = points.size();
    for (int index = 0; index < size; ++index) {
      final Vector2I p = points.get(index);
      x = x + (double) p.x();
      y = y + (double) p.y();
    }
    return Vector2D.of(x / (double) size, y / (double) size);
  }
}
