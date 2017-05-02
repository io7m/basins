package com.io7m.basins.world0;

import java.util.function.Function;

import static com.io7m.jnull.NullCheck.notNull;

public final class RoomModelOpBind<A, B> implements RoomModelOpType<B>
{
  private final RoomModelOpType<A> op;
  private final Function<A, RoomModelOpType<B>> supplier;
  private RoomModelOpType<B> inter_op;

  public RoomModelOpBind(
    final RoomModelOpType<A> op,
    final Function<A, RoomModelOpType<B>> supplier)
  {
    this.op = notNull(op, "Op");
    this.supplier = notNull(supplier, "Supplier");
  }

  public static <A, B> RoomModelOpBind<A, B> bind(
    final RoomModelOpType<A> op,
    final Function<A, RoomModelOpType<B>> f)
  {
    return new RoomModelOpBind<>(op, f);
  }

  @Override
  public RoomModelOpResult<B> evaluate(
    final RoomModelState state)
  {
    notNull(state, "State");

    final RoomModelOpResult<A> r = this.op.evaluate(state);
    this.inter_op = this.supplier.apply(r.result());
    return this.inter_op.evaluate(r.state());
  }

  @Override
  public RoomModelState undo(
    final RoomModelState state)
  {
    notNull(state, "State");

    final RoomModelState r_state = this.inter_op.undo(state);
    return this.op.undo(r_state);
  }
}
