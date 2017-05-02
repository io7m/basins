package com.io7m.basins.world0;

import com.io7m.jtensors.core.unparameterized.vectors.Vector2I;
import javaslang.collection.Vector;

import java.util.Objects;
import java.util.Optional;

import static com.io7m.basins.world0.RoomModelOpBind.bind;
import static com.io7m.basins.world0.RoomModelOpIdentity.identity;
import static com.io7m.basins.world0.RoomModelOpIterate.iterate;
import static com.io7m.basins.world0.RoomModelOpPointCreate.createPoint;
import static com.io7m.jnull.NullCheck.notNull;

public final class RoomEditingModel
{
  private final RoomModel model;

  public RoomEditingModel(
    final RoomModel in_model)
  {
    this.model = notNull(in_model, "Model");
  }

  public RoomEditingPolygonCreatorType polygonCreate()
  {
    return new PolygonCreator(this.model);
  }

  private static final class PolygonCreator
    implements RoomEditingPolygonCreatorType
  {
    private final RoomModel model;
    private Vector<RoomModelOpType<RoomPointID>> ops;
    private Vector<Vector2I> positions;

    PolygonCreator(
      final RoomModel in_model)
    {
      this.model = notNull(in_model, "Model");
      this.ops = Vector.empty();
      this.positions = Vector.empty();
    }

    @Override
    public boolean addPoint(
      final Vector2I position)
    {
      notNull(position, "Position");

      if (!this.positions.isEmpty()) {
        if (Objects.equals(this.positions.head(), position)) {
          return true;
        }
      }

      final Optional<RoomPoint> exist_opt =
        this.model.state().pointLookup(position);
      if (exist_opt.isPresent()) {
        this.ops = this.ops.append(identity(exist_opt.get().id()));
      } else {
        this.ops = this.ops.append(createPoint(position));
      }
      this.positions = this.positions.append(position);
      return false;
    }

    @Override
    public RoomPolygonID create()
    {
      try {
        return this.model.evaluate(
          bind(iterate(this.ops), RoomModelOpPolygonCreate::createPolygon));
      } finally {
        this.positions = Vector.empty();
        this.ops = Vector.empty();
      }
    }

    @Override
    public Vector<Vector2I> points()
    {
      return this.positions;
    }
  }
}
