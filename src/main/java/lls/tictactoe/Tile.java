package lls.tictactoe;

public class Tile {

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

    public enum State {
        X,
        O,
        NONE
    }

}