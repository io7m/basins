package com.io7m.basins.world0;

import com.io7m.jtensors.core.unparameterized.vectors.Vector2I;
import com.io7m.junreachable.UnreachableCodeException;
import javaslang.collection.Seq;
import javaslang.collection.Vector;
import javaslang.control.Validation;

import java.util.ArrayList;
import java.util.List;

import static com.io7m.basins.world0.RoomModelStateErrors.ofException;
import static com.io7m.basins.world0.RoomModelStateErrors.ofMessage;
import static javaslang.control.Validation.invalid;
import static javaslang.control.Validation.valid;

public final class RoomModelStateVerification
{
  private RoomModelStateVerification()
  {
    throw new UnreachableCodeException();
  }

  public static Validation<Seq<RoomModelStateError>, RoomModelState> verify(
    final RoomModelState state)
  {
    final List<RoomModelStateError> errors = new ArrayList<>();
    checkPolygonsExist(state, errors);
    checkPolygonsAreConvex(state, errors);
    return errors.isEmpty() ? valid(state) : invalid(Vector.ofAll(errors));
  }

  private static void checkPolygonsAreConvex(
    final RoomModelState state,
    final List<RoomModelStateError> errors)
  {
    /*
     * Check that all polygons are convex.
     */

    state.polygons().forEach(
      (poly_id, poly) -> {
        try {
          final Vector<Vector2I> points =
            poly.points().map(id -> state.pointGet(id).position());

          if (!RoomPolygons.isConvex(points)) {
            errors.add(ofMessage(
              String.format(
                "Polygon %d is not convex",
                Integer.valueOf(poly_id.value()))));
          }
        } catch (final Exception e) {
          errors.add(ofException(e));
        }
      });
  }

  private static void checkPolygonsExist(
    final RoomModelState state,
    final List<RoomModelStateError> errors)
  {
    /*
     * Check that all polygons referenced by points actually exist.
     */

    state.pointsPolygons().forEach((point_id, poly_id) -> {
      if (!state.polygons().containsKey(poly_id)) {
        errors.add(ofMessage(
          String.format(
            "No such polygon %d for point %d",
            Integer.valueOf(poly_id.value()),
            Integer.valueOf(point_id.value()))));
      }
    });
  }
}
