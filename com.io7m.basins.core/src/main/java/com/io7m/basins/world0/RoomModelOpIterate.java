package com.io7m.basins.world0;

import javaslang.collection.Vector;

import static com.io7m.jnull.NullCheck.notNull;

public final class RoomModelOpIterate<A> implements RoomModelOpType<Vector<A>>
{
  private final Vector<RoomModelOpType<A>> ops;
  private int evaluated;

  public RoomModelOpIterate(
    final Vector<RoomModelOpType<A>> ops)
  {
    this.ops = notNull(ops, "Ops");
    this.evaluated = 0;
  }

  public static <A> RoomModelOpIterate<A> iterate(
    final Vector<RoomModelOpType<A>> ops)
  {
    return new RoomModelOpIterate<>(ops);
  }

  @Override
  public RoomModelOpResult<Vector<A>> evaluate(
    final RoomModelState state)
  {
    notNull(state, "State");

    Vector<A> results = Vector.empty();
    RoomModelState state_current = state;
    for (this.evaluated = 0; this.evaluated < this.ops.size(); ++this.evaluated) {
      final RoomModelOpResult<A> result =
        this.ops.get(this.evaluated).evaluate(state_current);
      results = results.append(result.result());
      state_current = result.state();
    }

    return RoomModelOpResult.of(results, state_current);
  }

  @Override
  public RoomModelState undo(
    final RoomModelState state)
  {
    notNull(state, "State");

    RoomModelState state_current = state;
    --this.evaluated;
    for (; this.evaluated >= 0; --this.evaluated) {
      state_current = this.ops.get(this.evaluated).undo(state_current);
    }

    return state_current;
  }
}
