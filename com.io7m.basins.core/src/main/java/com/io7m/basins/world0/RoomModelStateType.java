package com.io7m.basins.world0;

import com.io7m.basins.core.BasinsImmutableStyleType;
import com.io7m.jtensors.core.unparameterized.vectors.Vector2I;
import javaslang.collection.Multimap;
import javaslang.collection.TreeMap;
import javaslang.collection.Vector;
import org.immutables.javaslang.encodings.JavaslangEncodingEnabled;
import org.immutables.value.Value;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import static com.io7m.jnull.NullCheck.notNull;
import static java.lang.Math.addExact;

@BasinsImmutableStyleType
@Value.Immutable
@JavaslangEncodingEnabled
public interface RoomModelStateType
{
  @Value.Parameter
  TreeMap<RoomPointID, RoomPoint> points();

  @Value.Parameter
  TreeMap<RoomPolygonID, RoomPolygon> polygons();

  @Value.Parameter
  Multimap<RoomPointID, RoomPolygonID> pointsPolygons();

  default RoomPointID pointFreshID()
  {
    return this.points().max()
      .map(p -> RoomPointID.of(addExact(p._1.value(), 1)))
      .getOrElse(RoomPointID.of(0));
  }

  default RoomPolygonID polygonFreshID()
  {
    return this.polygons().max()
      .map(p -> RoomPolygonID.of(addExact(p._1.value(), 1)))
      .getOrElse(RoomPolygonID.of(0));
  }

  default Optional<RoomPoint> pointLookup(
    final Vector2I position)
  {
    notNull(position, "Position");

    return this.points()
      .find(p -> Objects.equals(p._2.position(), position))
      .map(p -> p._2)
      .toJavaOptional();
  }

  default RoomPoint pointGet(
    final RoomPointID id)
  {
    notNull(id, "ID");

    final TreeMap<RoomPointID, RoomPoint> p = this.points();
    if (!p.containsKey(id)) {
      throw new NoSuchElementException("No such point: " + id.value());
    }
    return p.get(id).get();
  }

  default RoomPolygon polygonGet(
    final RoomPolygonID id)
  {
    notNull(id, "ID");

    final TreeMap<RoomPolygonID, RoomPolygon> p = this.polygons();
    if (!p.containsKey(id)) {
      throw new NoSuchElementException("No such polygon: " + id.value());
    }
    return p.get(id).get();
  }

  default Vector<RoomPolygon> polygonsForPoint(
    final RoomPointID id)
  {
    notNull(id, "ID");

    final TreeMap<RoomPointID, RoomPoint> o = this.points();
    final TreeMap<RoomPolygonID, RoomPolygon> polys = this.polygons();
    final Multimap<RoomPointID, RoomPolygonID> pp = this.pointsPolygons();
    if (o.containsKey(id)) {
      return pp.apply(id).map(poly_id -> polys.get(poly_id).get()).toVector();
    }
    throw new NoSuchElementException("No such point: " + id.value());
  }
}