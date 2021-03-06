package com.io7m.basins.world0;

import com.io7m.jnull.NullCheck;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import javaslang.collection.Vector;

public final class RoomModel
{
  private final int stack_max;
  private final PublishSubject<RoomModelUndoAvailable> observable;
  private Vector<RoomModelOpType<?>> undo_stack;
  private RoomModelState state;

  public RoomModel(
    final int in_stack_max)
  {
    if (in_stack_max < 1) {
      throw new IllegalArgumentException("Stack size must be >= 1");
    }

    this.state = RoomModelState.builder().build();
    this.undo_stack = Vector.empty();
    this.stack_max = in_stack_max;
    this.observable = PublishSubject.create();
  }

  public Observable<RoomModelUndoAvailable> observable()
  {
    return this.observable;
  }

  public <T> T evaluate(
    final RoomModelOpType<T> op)
  {
    NullCheck.notNull(op, "Op");

    final RoomModelOpResultType<T> result = op.evaluate(this.state);
    if (this.undoStackSize() == this.stack_max) {
      this.undo_stack = this.undo_stack.tail();
    }

    this.state = result.state();
    this.undo_stack = this.undo_stack.append(op);
    this.observable.onNext(RoomModelUndoAvailable.of(this.undoAvailable()));
    return result.result();
  }

  public RoomModelState state()
  {
    return this.state;
  }

  public int undoStackSize()
  {
    return this.undo_stack.size();
  }

  public boolean undoAvailable()
  {
    return this.undoStackSize() >= 1;
  }

  public void undo()
  {
    if (this.undoAvailable()) {
      final RoomModelOpType<?> op = this.undo_stack.last();
      final RoomModelState new_state = op.undo(this.state);
      this.state = NullCheck.notNull(new_state, "New state");
      this.undo_stack = this.undo_stack.take(this.undoStackSize() - 1);
      this.observable.onNext(RoomModelUndoAvailable.of(this.undoAvailable()));
    }
  }
}
