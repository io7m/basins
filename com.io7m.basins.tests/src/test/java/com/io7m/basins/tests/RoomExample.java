package com.io7m.basins.tests;

import javaslang.collection.HashMap;
import javaslang.collection.List;

import java.util.function.Function;

public final class RoomExample
{
  private RoomExample()
  {

  }

  final class Result<T>
  {
    final State state;
    final T result;

    Result(
      final State state,
      final T result)
    {
      this.state = state;
      this.result = result;
    }
  }

  final class Point
  {

  }

  final class Polygon
  {

  }

  final class State
  {
    final HashMap<Integer, Point> points;
    final HashMap<Integer, Polygon> polygons;

    State(
      final HashMap<Integer, Point> points,
      final HashMap<Integer, Polygon> polygons)
    {
      this.points = points;
      this.polygons = polygons;
    }
  }

  interface RoomOp<T>
  {
    Result<T> evaluate(State s);

    State undo(State s);
  }

  final class CreatePoint implements RoomOp<Integer>
  {
    CreatePoint()
    {

    }

    @Override
    public Result<Integer> evaluate(
      final State s)
    {
      final State s0 = new State(
        s.points.put(Integer.valueOf(0), new Point()),
        s.polygons);
      return new Result<>(s0, Integer.valueOf(0));
    }

    @Override
    public State undo(final State s)
    {
      return new State(
        s.points.remove(Integer.valueOf(0)),
        s.polygons);
    }
  }

  final class CreatePolygon implements RoomOp<Integer>
  {
    private final List<Integer> points;

    CreatePolygon(
      final List<Integer> points)
    {
      this.points = points;
    }

    @Override
    public Result<Integer> evaluate(
      final State s)
    {
      final State s0 = new State(
        s.points,
        s.polygons.put(Integer.valueOf(0), new Polygon()));
      return new Result<>(s0, Integer.valueOf(0));
    }

    @Override
    public State undo(final State s)
    {
      return new State(
        s.points,
        s.polygons.remove(Integer.valueOf(0)));
    }
  }

  static <A, B> RoomOp<B> combine(
    final RoomOp<A> op,
    final Function<A, RoomOp<B>> f)
  {
    return new RoomOp<B>()
    {
      private RoomOp<B> iop;

      @Override
      public Result<B> evaluate(final State s)
      {
        final Result<A> r = op.evaluate(s);
        this.iop = f.apply(r.result);
        return this.iop.evaluate(r.state);
      }

      @Override
      public State undo(final State s)
      {
        final State s0 = this.iop.undo(s);
        return op.undo(s0);
      }
    };
  }
}
