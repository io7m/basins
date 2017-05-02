package com.io7m.basins.world0;

public interface RoomModelOpType<T>
{
  RoomModelOpResult<T> evaluate(
    RoomModelState state);

  RoomModelState undo(
    RoomModelState state);
}
