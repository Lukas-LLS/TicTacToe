package lls.tictactoe;

import javafx.scene.image.ImageView;

import java.util.*;
import java.util.stream.Collectors;

public class Minimax {

    public static int getBestMove (TicTacToe instance, Map<ImageView, Tile> fieldMap, Tile.State playerStateType, Tile.State aiStateType) {

        List<Map<ImageView, Tile>> possibleChases = new ArrayList<>();

        Map<Map<ImageView, Tile>, Integer> caseLookupMap = new HashMap<>();

        List<Map.Entry<ImageView, Tile>> emptyPositions = fieldMap
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().isNotOccupied())
                .collect(Collectors.toList());

        for (int i = 0; i < emptyPositions.size(); i++) {
            Map<ImageView, Tile> currentMapCopy = new HashMap<>();
            fieldMap
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().isOccupied())
                    .forEach(entry -> currentMapCopy.put(entry.getKey(), entry.getValue()));
            for (int j = 0; j < emptyPositions.size(); j++) {
                if (j == i) {
                    Tile tileCopy = emptyPositions.get(j).getValue().clone();
                    if (!tileCopy.changeState(aiStateType)) {
                        throw new IllegalStateException("Corrupt Tile: " + tileCopy);
                    }
                    currentMapCopy.put(emptyPositions.get(j).getKey(), tileCopy);
                    continue;
                }
                currentMapCopy.put(emptyPositions.get(j).getKey(), emptyPositions.get(j).getValue());
            }
            caseLookupMap.put(currentMapCopy, emptyPositions.get(i).getValue().getPosition());
            possibleChases.add(currentMapCopy);
        }

        List<Map.Entry<Map<ImageView, Tile>, Integer>> valueLookupList = new ArrayList<>();

        for (Map<ImageView, Tile> possibleChase : possibleChases) {
            valueLookupList.add(
                    new EntryImpl<>(
                            possibleChase,
                            minimax(instance, Type.MIN, possibleChase, playerStateType, aiStateType)
                    )
            );
        }

        valueLookupList.sort((entry1, entry2) -> Integer.compare(entry2.getValue(), entry1.getValue()));

        return caseLookupMap.get(valueLookupList.get(0).getKey());
    }


    private static int minimax (TicTacToe instance, Type type, Map<ImageView, Tile> fieldMapCopy, Tile.State playerStateType, Tile.State aiStateType) {
        Tile.State possibleEndState = instance.checkIfGameEnded(fieldMapCopy);
        if (possibleEndState.equals(playerStateType)) return -1;
        if (possibleEndState.equals(aiStateType)) return 1;
        if (instance.isDraw(fieldMapCopy)) return 0;

        List<Map<ImageView, Tile>> possibleChases = new ArrayList<>();

        List<Map.Entry<ImageView, Tile>> emptyPositions = fieldMapCopy
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().isNotOccupied())
                .collect(Collectors.toList());

        for (int i = 0; i < emptyPositions.size(); i++) {
            Map<ImageView, Tile> currentMapCopy = new HashMap<>();
            fieldMapCopy
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().isOccupied())
                    .forEach(entry -> currentMapCopy.put(entry.getKey(), entry.getValue()));
            for (int j = 0; j < emptyPositions.size(); j++) {
                if (j == i) {
                    Tile tileCopy = emptyPositions.get(j).getValue().clone();
                    if (!tileCopy.changeState(type.equals(Type.MAX) ? aiStateType : playerStateType)) {
                        throw new IllegalStateException("Corrupt Tile: " + tileCopy);
                    }
                    currentMapCopy.put(emptyPositions.get(j).getKey(), tileCopy);
                    continue;
                }
                currentMapCopy.put(emptyPositions.get(j).getKey(), emptyPositions.get(j).getValue());
            }
            possibleChases.add(currentMapCopy);
        }

        int minimax = type.equals(Type.MAX) ? -2 : 2;

        for (Map<ImageView, Tile> possibleChase : possibleChases) {
            int result = minimax(instance, type.equals(Type.MAX) ? Type.MIN : Type.MAX, possibleChase, playerStateType, aiStateType);
            if (type.equals(Type.MAX)) {
                if (result > 0) return result;
                minimax = Math.max(minimax, result);
            } else {
                if (result < 0) return result;
                minimax = Math.min(minimax, result);
            }
        }

        return minimax;
    }

    private enum Type {
        MIN,
        MAX
    }

    private static class EntryImpl<K, V> implements Map.Entry<K, V> {

        private final K key;
        private V value;

        public EntryImpl (K key, V value) {
            this.key = Objects.requireNonNull(key);
            this.value = Objects.requireNonNull(value);
        }

        @Override
        public K getKey () {
            return key;
        }

        @Override
        public V getValue () {
            return value;
        }

        @Override
        public V setValue (V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        @Override
        public boolean equals (Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EntryImpl<?, ?> entry = (EntryImpl<?, ?>) o;
            return key.equals(entry.key) &&
                    value.equals(entry.value);
        }

        @Override
        public int hashCode () {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        @Override
        public String toString () {
            return key + "->" + value;
        }
    }

}
