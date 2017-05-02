package com.io7m.basins.world0;

import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.core.unparameterized.vectors.Vector2I;

public final class RoomEditingModel
{
  private final RoomModel model;

  public RoomEditingModel(
    final RoomModel in_model)
  {
    this.model = NullCheck.notNull(in_model, "Model");
  }

  public RoomEditingPolygonCreatorType polygonCreate()
  {
    return new PolygonCreator();
  }

  private final class PolygonCreator implements RoomEditingPolygonCreatorType
  {

    @Override
    public void addPoint(final Vector2I position)
    {

    }
  }
}
