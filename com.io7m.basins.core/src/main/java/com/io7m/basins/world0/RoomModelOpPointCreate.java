package com.io7m.basins.world0;

import com.io7m.jtensors.core.unparameterized.vectors.Vector2I;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.io7m.jnull.NullCheck.notNull;

public final class RoomModelOpPointCreate implements RoomModelOpType<RoomPointID>
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(RoomModelOpPointCreate.class);
  }

  private final Vector2I position;
  private RoomPointID id;

  public RoomModelOpPointCreate(
    final Vector2I in_position)
  {
    this.position = notNull(in_position, "Position");
  }

  public static RoomModelOpPointCreate createPoint(
    final Vector2I in_position)
  {
    return new RoomModelOpPointCreate(in_position);
  }

  @Override
  public RoomModelOpResult<RoomPointID> evaluate(
    final RoomModelState state)
  {
    notNull(state, "State");

    LOG.debug("createPoint: {}", this.position);

    this.id = state.pointFreshID();
    final RoomPoint point = RoomPoint.of(this.id, this.position);
    return RoomModelOpResult.of(
      this.id, state.withPoints(state.points().put(this.id, point)));
  }

  @Override
  public RoomModelState undo(
    final RoomModelState state)
  {
    notNull(state, "State");

    LOG.debug("createPoint: undo {}", this.id);
    return state.withPoints(state.points().remove(this.id));
  }
}
