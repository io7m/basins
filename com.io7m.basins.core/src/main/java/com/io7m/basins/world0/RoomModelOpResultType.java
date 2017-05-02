package com.io7m.basins.world0;

import com.io7m.basins.core.BasinsImmutableStyleType;
import org.immutables.javaslang.encodings.JavaslangEncodingEnabled;
import org.immutables.value.Value;

@BasinsImmutableStyleType
@Value.Immutable
@JavaslangEncodingEnabled
public interface RoomModelOpResultType<T>
{
  @Value.Parameter
  T result();

  @Value.Parameter
  RoomModelState state();
}
