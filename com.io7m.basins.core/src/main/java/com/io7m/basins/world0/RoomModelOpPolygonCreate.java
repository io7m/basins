package com.io7m.basins.world0;

import javaslang.collection.Multimap;
import javaslang.collection.TreeMap;
import javaslang.collection.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.io7m.jnull.NullCheck.notNull;

public final class RoomModelOpPolygonCreate implements RoomModelOpType<RoomPolygonID>
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(RoomModelOpPolygonCreate.class);
  }

  private final Vector<RoomPointID> points;
  private RoomPolygonID id;

  public RoomModelOpPolygonCreate(
    final Vector<RoomPointID> in_point_ids)
  {
    this.points = notNull(in_point_ids, "Points");
  }

  public static RoomModelOpPolygonCreate createPolygon(
    final Vector<RoomPointID> in_point_ids)
  {
    return new RoomModelOpPolygonCreate(in_point_ids);
  }

  @Override
  public RoomModelOpResult<RoomPolygonID> evaluate(
    final RoomModelState state)
  {
    notNull(state, "State");

    LOG.debug("createPolygon: {}", this.points);

    final Vector<RoomPoint> poly_points = this.points.map(state::pointGet);

    if (!RoomPolygons.isConvex(poly_points.map(RoomPoint::position))) {
      throw new IllegalArgumentException("Polygon is not convex");
    }

    final RoomPolygonID polygon_id = state.polygonFreshID();
    final RoomPolygon poly = RoomPolygon.of(polygon_id, this.points);
    final TreeMap<RoomPolygonID, RoomPolygon> next_polygons =
      state.polygons().put(polygon_id, poly);

    Multimap<RoomPointID, RoomPolygonID> next_points_polygons =
      state.pointsPolygons();
    for (int index = 0; index < this.points.size(); ++index) {
      next_points_polygons = next_points_polygons.put(
        this.points.get(index),
        polygon_id);
    }

    this.id = polygon_id;
    return RoomModelOpResult.of(
      polygon_id,
      RoomModelState.builder().from(state)
        .setPolygons(next_polygons)
        .setPointsPolygons(next_points_polygons)
        .build());
  }

  @Override
  public RoomModelState undo(
    final RoomModelState state)
  {
    notNull(state, "State");

    LOG.debug("createPolygon: undo {}", this.id);

    final RoomPolygon poly = state.polygonGet(this.id);

    Multimap<RoomPointID, RoomPolygonID> next_points_polygons =
      state.pointsPolygons();

    for (final RoomPointID point : poly.points()) {
      next_points_polygons = next_points_polygons.remove(point, poly.id());
    }

    final TreeMap<RoomPolygonID, RoomPolygon> next_polygons =
      state.polygons().remove(poly.id());

    return RoomModelState.builder().from(state)
      .setPolygons(next_polygons)
      .setPointsPolygons(next_points_polygons)
      .build();
  }
}
