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
import com.io7m.junreachable.UnreachableCodeException;
import javaslang.collection.Seq;
import javaslang.collection.Vector;

public final class RoomPolygons
{
  private RoomPolygons()
  {
    throw new UnreachableCodeException();
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
    final Seq<Vector2I> points)
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
}
