package com.io7m.basins.world0;

import com.io7m.junreachable.UnreachableCodeException;

import java.util.Optional;

public final class RoomModelStateErrors
{
  private RoomModelStateErrors()
  {
    throw new UnreachableCodeException();
  }

  public static RoomModelStateError ofMessage(
    final String message)
  {
    return RoomModelStateError.of(message, Optional.empty());
  }

  public static RoomModelStateError ofException(
    final Exception e)
  {
    return RoomModelStateError.of(e.getMessage(), Optional.of(e));
  }
}
