package com.io7m.basins.world0;

import com.io7m.basins.core.BasinsImmutableStyleType;
import org.immutables.javaslang.encodings.JavaslangEncodingEnabled;
import org.immutables.value.Value;

import java.util.Optional;

@BasinsImmutableStyleType
@Value.Immutable
@JavaslangEncodingEnabled
public interface RoomModelStateErrorType
{
  @Value.Parameter
  String message();

  @Value.Parameter
  Optional<Exception> exeception();
}
