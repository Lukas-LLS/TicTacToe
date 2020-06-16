package lls.tictactoe;

import java.util.Objects;

public class Tile implements Cloneable {

    private State state;
    private final int position;

    public Tile (int position) {
        this.state = State.NONE;
        this.position = position;
    }

    public boolean changeState (State newState) {
        if (isOccupied() || newState == null) return false;
        state = newState;
        return true;
    }

    public boolean isOccupied () {
        return !state.equals(State.NONE);
    }

    public boolean isNotOccupied () {
        return state.equals(State.NONE);
    }

    public State getState () {
        return state;
    }

    public int getPosition () {
        return position;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public Tile clone () {
        Tile clone = new Tile(position);
        clone.changeState(state);
        return clone;
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tile tile = (Tile) o;
        return position == tile.position &&
                state == tile.state;
    }

    @Override
    public int hashCode () {
        return Objects.hash(state, position);
    }

    @Override
    public String toString () {
        return "Tile{" +
                "state=" + state.name() +
                ", position=" + position +
                '}';
    }

    public enum State {
        X,
        O,
        NONE
    }

}