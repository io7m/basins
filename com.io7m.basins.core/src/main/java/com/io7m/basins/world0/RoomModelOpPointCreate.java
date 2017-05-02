package com.io7m.basins.world0;

import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.core.unparameterized.vectors.Vector2I;

public final class RoomModelOpPointCreate implements RoomModelOpType<RoomPointID>
{
  private final Vector2I position;
  private RoomPointID id;

  public RoomModelOpPointCreate(
    final Vector2I in_position)
  {
    this.position = NullCheck.notNull(in_position, "Position");
  }

  @Override
  public RoomModelOpResult<RoomPointID> evaluate(
    final RoomModelState state)
  {
    this.id = state.pointFreshID();
    final RoomPoint point = RoomPoint.of(this.id, this.position);
    return RoomModelOpResult.of(
      this.id, state.withPoints(state.points().put(this.id, point)));
  }

  @Override
  public RoomModelState undo(
    final RoomModelState state)
  {
    return state.withPoints(state.points().remove(this.id));
  }
}
