package com.io7m.basins.world0;

import static com.io7m.jnull.NullCheck.notNull;

public final class RoomModelOpIdentity<T> implements RoomModelOpType<T>
{
  private final T value;

  public RoomModelOpIdentity(
    final T value)
  {
    this.value = notNull(value, "Value");
  }

  public static <T> RoomModelOpIdentity<T> identity(
    final T value)
  {
    return new RoomModelOpIdentity<>(value);
  }

  @Override
  public RoomModelOpResult<T> evaluate(
    final RoomModelState state)
  {
    return RoomModelOpResult.of(value, notNull(state, "State"));
  }

  @Override
  public RoomModelState undo(
    final RoomModelState state)
  {
    return notNull(state, "State");
  }
}
