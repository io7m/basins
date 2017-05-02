package com.io7m.basins.world0;

import com.io7m.jtensors.core.unparameterized.vectors.Vector2I;
import javaslang.collection.Vector;

public interface RoomEditingPolygonCreatorType
{
  boolean addPoint(Vector2I position);

  RoomPolygonID create();

  Vector<Vector2I> points();
}
