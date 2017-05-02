package com.io7m.basins.tests.world0;

import com.io7m.basins.world0.RoomModel;
import com.io7m.basins.world0.RoomModelOpResult;
import com.io7m.basins.world0.RoomModelOpType;
import com.io7m.basins.world0.RoomModelState;
import com.io7m.basins.world0.RoomPoint;
import com.io7m.basins.world0.RoomPointID;
import com.io7m.jtensors.core.unparameterized.vectors.Vector2I;
import org.junit.Assert;
import org.junit.Test;

public final class RoomModelTest
{
  @Test
  public void testUndoEmpty()
  {
    final RoomModel eval = new RoomModel(32);
    final RoomModelState state0 = eval.state();

    Assert.assertEquals(0L, (long) eval.undoStackSize());
    eval.undo();
    Assert.assertEquals(0L, (long) eval.undoStackSize());

    final RoomModelState state1 = eval.state();
    Assert.assertEquals(state0, state1);
  }

  @Test
  public void testIdentityOp()
  {
    final RoomModel eval = new RoomModel(32);
    final RoomModelState state0 = eval.state();

    final Integer r0 = eval.evaluate(new IdentityOp(23));
    Assert.assertEquals(Integer.valueOf(23), r0);

    final RoomModelState state1 = eval.state();
    Assert.assertEquals(state0, state1);
    Assert.assertEquals(1L, (long) eval.undoStackSize());

    eval.undo();
    final RoomModelState state2 = eval.state();
    Assert.assertEquals(state0, state2);
    Assert.assertEquals(0L, (long) eval.undoStackSize());
  }

  @Test
  public void testAddPoint()
  {
    final RoomModel eval = new RoomModel(32);
    final RoomModelState state0 = eval.state();

    final Integer r0 = eval.evaluate(new AddPointOp(0));
    Assert.assertEquals(Integer.valueOf(0), r0);
    Assert.assertFalse(state0.points().containsKey(RoomPointID.of(0)));
    Assert.assertFalse(state0.points().containsKey(RoomPointID.of(1)));

    final RoomModelState state1 = eval.state();
    Assert.assertNotEquals(state0, state1);
    Assert.assertFalse(state0.points().containsKey(RoomPointID.of(0)));
    Assert.assertFalse(state0.points().containsKey(RoomPointID.of(1)));
    Assert.assertTrue(state1.points().containsKey(RoomPointID.of(0)));
    Assert.assertFalse(state1.points().containsKey(RoomPointID.of(1)));
    Assert.assertEquals(1L, (long) eval.undoStackSize());

    final Integer r1 = eval.evaluate(new AddPointOp(1));
    Assert.assertEquals(Integer.valueOf(1), r1);

    final RoomModelState state2 = eval.state();
    Assert.assertNotEquals(state0, state1);
    Assert.assertNotEquals(state0, state2);
    Assert.assertNotEquals(state1, state2);
    Assert.assertFalse(state0.points().containsKey(RoomPointID.of(0)));
    Assert.assertFalse(state0.points().containsKey(RoomPointID.of(1)));
    Assert.assertTrue(state1.points().containsKey(RoomPointID.of(0)));
    Assert.assertFalse(state1.points().containsKey(RoomPointID.of(1)));
    Assert.assertTrue(state2.points().containsKey(RoomPointID.of(0)));
    Assert.assertTrue(state2.points().containsKey(RoomPointID.of(1)));
    Assert.assertEquals(2L, (long) eval.undoStackSize());

    eval.undo();
    Assert.assertEquals(1L, (long) eval.undoStackSize());

    final RoomModelState state3 = eval.state();
    Assert.assertEquals(state1, state3);

    eval.undo();
    Assert.assertEquals(0L, (long) eval.undoStackSize());

    final RoomModelState state4 = eval.state();
    Assert.assertEquals(state0, state4);
  }

  @Test
  public void testAddPointLimited()
  {
    final RoomModel eval = new RoomModel(1);
    final RoomModelState state0 = eval.state();

    final Integer r0 = eval.evaluate(new AddPointOp(0));
    Assert.assertEquals(Integer.valueOf(0), r0);
    Assert.assertFalse(state0.points().containsKey(RoomPointID.of(0)));
    Assert.assertFalse(state0.points().containsKey(RoomPointID.of(1)));

    final RoomModelState state1 = eval.state();
    Assert.assertNotEquals(state0, state1);
    Assert.assertFalse(state0.points().containsKey(RoomPointID.of(0)));
    Assert.assertFalse(state0.points().containsKey(RoomPointID.of(1)));
    Assert.assertTrue(state1.points().containsKey(RoomPointID.of(0)));
    Assert.assertFalse(state1.points().containsKey(RoomPointID.of(1)));
    Assert.assertEquals(1L, (long) eval.undoStackSize());

    final Integer r1 = eval.evaluate(new AddPointOp(1));
    Assert.assertEquals(Integer.valueOf(1), r1);

    final RoomModelState state2 = eval.state();
    Assert.assertNotEquals(state0, state1);
    Assert.assertNotEquals(state0, state2);
    Assert.assertNotEquals(state1, state2);
    Assert.assertFalse(state0.points().containsKey(RoomPointID.of(0)));
    Assert.assertFalse(state0.points().containsKey(RoomPointID.of(1)));
    Assert.assertTrue(state1.points().containsKey(RoomPointID.of(0)));
    Assert.assertFalse(state1.points().containsKey(RoomPointID.of(1)));
    Assert.assertTrue(state2.points().containsKey(RoomPointID.of(0)));
    Assert.assertTrue(state2.points().containsKey(RoomPointID.of(1)));
    Assert.assertEquals(1L, (long) eval.undoStackSize());

    eval.undo();
    Assert.assertEquals(0L, (long) eval.undoStackSize());

    final RoomModelState state3 = eval.state();
    Assert.assertEquals(state1, state3);

    eval.undo();
    Assert.assertEquals(0L, (long) eval.undoStackSize());

    final RoomModelState state4 = eval.state();
    Assert.assertEquals(state1, state4);
  }

  private static final class IdentityOp
    implements RoomModelOpType<Integer>
  {
    private final int result;

    public IdentityOp(final int r)
    {
      this.result = r;
    }

    @Override
    public RoomModelOpResult<Integer> evaluate(
      final RoomModelState state)
    {
      return RoomModelOpResult.of(Integer.valueOf(this.result), state);
    }

    @Override
    public RoomModelState undo(
      final RoomModelState state)
    {
      return state;
    }
  }

  private static final class AddPointOp
    implements RoomModelOpType<Integer>
  {
    private final int result;

    public AddPointOp(final int r)
    {
      this.result = r;
    }

    @Override
    public RoomModelOpResult<Integer> evaluate(
      final RoomModelState state)
    {
      final RoomPoint point =
        RoomPoint.of(RoomPointID.of(this.result), Vector2I.of(0, 0));
      return RoomModelOpResult.of(
        Integer.valueOf(this.result),
        state.withPoints(state.points().put(point.id(), point)));
    }

    @Override
    public RoomModelState undo(
      final RoomModelState state)
    {
      final RoomPointID id = RoomPointID.of(this.result);
      return state.withPoints(state.points().remove(id));
    }
  }
}
