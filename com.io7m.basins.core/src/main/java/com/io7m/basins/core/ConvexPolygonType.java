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
import org.immutables.value.Value;

import java.util.ArrayList;
import java.util.List;

@BasinsImmutableStyleType
@Value.Immutable
public interface ConvexPolygonType
{
  @Value.Parameter
  List<Vector2I> vertices();

  default Vector2D normal(
    final int v0,
    final int v1)
  {
    return ConvexPolygons.normal(
      this.vertices().get(v0), this.vertices().get(v1));
  }

  @Value.Auxiliary
  @Value.Derived
  default Vector2D barycenter()
  {
    return ConvexPolygons.barycenter(this.vertices());
  }

  @Value.Auxiliary
  @Value.Derived
  default List<Vector2D> axes()
  {
    final ArrayList<Vector2D> axes = new ArrayList<>();

    for (int i0 = 0; i0 < this.vertices().size(); ++i0) {
      final int i1 = i0 + 1 == this.vertices().size() ? 0 : i0 + 1;
      axes.add(this.normal(i0, i1));
    }

    return axes;
  }

  @Value.Check
  default void checkPreconditions()
  {
    if (this.vertices().size() < 3) {
      throw new IllegalArgumentException("Polygon vertex count must be >= 3");
    }

    if (!ConvexPolygons.isConvex(this.vertices())) {
      throw new IllegalArgumentException("Polygon is not convex");
    }
  }
}
