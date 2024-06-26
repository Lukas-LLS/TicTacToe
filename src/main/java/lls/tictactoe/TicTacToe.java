package lls.tictactoe;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.*;

public class TicTacToe extends Application {

    private static final Random RANDOM = new Random();

    private Stage stage;

    @Override
    public void start(Stage stage) {

        this.stage = stage;

        Label difficultySelectionLabel = new Label("Choose the Difficulty:");
        Button difficultySelectionEasy = new Button("Easy");
        Button difficultySelectionMedium = new Button("Medium");
        Button difficultySelectionUnbeatable = new Button("Unbeatable");

        difficultySelectionEasy.setOnAction(e -> preGame2(stage, Difficulty.EASY));
        difficultySelectionMedium.setOnAction(e -> preGame2(stage, Difficulty.MEDIUM));
        difficultySelectionUnbeatable.setOnAction(e -> preGame2(stage, Difficulty.UNBEATABLE));

        GridPane layout = new GridPane();
        layout.setAlignment(Pos.CENTER);
        layout.setPrefSize(350, 100);
        layout.setHgap(15);
        layout.add(difficultySelectionLabel, 0, 0);
        layout.add(difficultySelectionEasy, 1, 0);
        layout.add(difficultySelectionMedium, 2, 0);
        layout.add(difficultySelectionUnbeatable, 3, 0);

        Scene preScene = new Scene(layout);

        stage.setTitle("TicTacToe");
        stage.setScene(preScene);
        stage.show();
    }

    private void preGame2(Stage stage, Difficulty difficulty) {

        Label typeSelectionLabel = new Label("Choose your symbol:");
        Button typeSelectionX = new Button("X");
        Button typeSelectionO = new Button("O");

        typeSelectionX.setOnAction(e -> startGame(stage, difficulty, Tile.State.X, Tile.State.O));
        typeSelectionO.setOnAction(e -> startGame(stage, difficulty, Tile.State.O, Tile.State.X));

        GridPane layout = new GridPane();

        layout.setAlignment(Pos.CENTER);
        layout.setPrefSize(250, 100);
        layout.setHgap(15);
        layout.add(typeSelectionLabel, 0, 0);
        layout.add(typeSelectionX, 1, 0);
        layout.add(typeSelectionO, 2, 0);

        Scene preScene2 = new Scene(layout);

        stage.setScene(preScene2);
    }

    private void startGame(Stage stage, Difficulty difficulty, Tile.State playerStateType, Tile.State aiStateType) {
        Image imageX = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/X.png")));
        Image imageO = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/O.png")));
        Image imageNone = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/NONE.png")));

        Map<ImageView, Tile> fieldMap = new HashMap<>();

        GridPane layout = new GridPane();
        layout.setAlignment(Pos.CENTER);
        layout.setPrefSize(400, 400);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                final ImageView currentImageView = new ImageView(imageNone);
                final Tile currentTile = new Tile((i == 0 ? 0 : (i == 1 ? 3 : 6)) + j);
                fieldMap.put(currentImageView, currentTile);
                layout.add(currentImageView, i, j);
                currentImageView.setOnMouseClicked(e -> {
                    if (currentTile.changeState(playerStateType)) {
                        switch (currentTile.getState()) {
                            case X -> {
                                currentImageView.setImage(imageX);
                                {
                                    if (potentialGameEnd(fieldMap)) return;
                                }
                            }
                            case O -> {
                                currentImageView.setImage(imageO);
                                {
                                    if (potentialGameEnd(fieldMap)) return;
                                }
                            }
                            default -> throw new IllegalStateException("Tile was change, but state is NONE");
                        }
                        moveAI(difficulty, aiStateType, fieldMap, imageX, imageO);
                    }
                });
            }
        }

        Scene gameScene = new Scene(layout);
        stage.setScene(gameScene);

        if (playerStateType.equals(Tile.State.O)) {
            moveAI(difficulty, aiStateType, fieldMap, imageX, imageO);
        }
    }

    private boolean potentialGameEnd(Map<ImageView, Tile> fieldMap) {
        Tile.State endState = checkIfGameEnded(fieldMap);
        if (!endState.equals(Tile.State.NONE)) {
            endGame(fieldMap, endState);
            return true;
        }
        if (isDraw(fieldMap)) {
            endGame(fieldMap, Tile.State.NONE);
            return true;
        }
        return false;
    }

    private void moveAI(Difficulty difficulty, Tile.State aiStateType, Map<ImageView, Tile> fieldMap,
                        Image imageX, Image imageO) {

        switch (difficulty) {
            case EASY -> moveAIEasy(aiStateType, fieldMap, imageX, imageO);
            case MEDIUM -> moveAIMedium(aiStateType, fieldMap, imageX, imageO);
            case UNBEATABLE -> moveAIUnbeatable(aiStateType, fieldMap, imageX, imageO);
            default -> throw new NullPointerException("Difficulty was null");
        }

    }

    private void moveAIEasy(Tile.State aiStateType, Map<ImageView, Tile> fieldMap, Image imageX, Image imageO) {
        List<ImageView> freeTiles = new ArrayList<>();
        fieldMap.forEach((imageView, tile) -> {
            if (tile.isNotOccupied()) {
                freeTiles.add(imageView);
            }
        });
        if (freeTiles.isEmpty()) return;
        ImageView targetImageView = freeTiles.get(RANDOM.nextInt(freeTiles.size()));

        performAIMove(aiStateType, fieldMap, imageX, imageO, targetImageView);
    }

    private void moveAIMedium(Tile.State aiStateType, Map<ImageView, Tile> fieldMap, Image imageX, Image imageO) {
        List<ImageView> imageViews = new ArrayList<>();
        fieldMap.forEach((imageView, tile) -> imageViews.add(imageView));
        int move = checkForWinningMove(fieldMap);
        if (move != -1) {
            Optional<ImageView> optionalMove = imageViews.stream().filter(imageView -> fieldMap.get(imageView).getPosition() == move).findAny();
            if (optionalMove.isPresent()) {
                ImageView targetImageView = optionalMove.get();
                performAIMove(aiStateType, fieldMap, imageX, imageO, targetImageView);
                return;
            }
        }
        moveAIEasy(aiStateType, fieldMap, imageX, imageO);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void moveAIUnbeatable(Tile.State aiStateType, Map<ImageView, Tile> fieldMap, Image imageX, Image imageO) {
        Tile.State playerState = Arrays.stream(Tile.State.values())
                .filter(state -> !state.equals(Tile.State.NONE) && !state.equals(aiStateType))
                .findAny().get();
        int move = Minimax.getBestMove(this, fieldMap, playerState, aiStateType);

        List<ImageView> imageViews = new ArrayList<>();
        fieldMap.forEach((imageView, tile) -> imageViews.add(imageView));
        imageViews.sort(Comparator.comparingInt(i -> fieldMap.get(i).getPosition()));

        performAIMove(aiStateType, fieldMap, imageX, imageO, imageViews.get(move));
    }

    private void performAIMove(Tile.State aiStateType, Map<ImageView, Tile> fieldMap, Image imageX, Image imageO, ImageView targetImageView) {
        if (!fieldMap.get(targetImageView).changeState(aiStateType)) {
            throw new IllegalStateException("Tile which was not occupied, is now suddenly occupied");
        }
        switch (fieldMap.get(targetImageView).getState()) {
            case O -> performMoveForAIState(fieldMap, imageO, targetImageView);
            case X -> performMoveForAIState(fieldMap, imageX, targetImageView);
            default -> throw new IllegalStateException("Tile was change, but state is NONE");
        }
    }

    private void performMoveForAIState(Map<ImageView, Tile> fieldMap, Image imageX, ImageView targetImageView) {
        targetImageView.setImage(imageX);
        {
            Tile.State endState = checkIfGameEnded(fieldMap);
            if (!endState.equals(Tile.State.NONE)) {
                endGame(fieldMap, endState);
            }
            if (isDraw(fieldMap)) {
                endGame(fieldMap, Tile.State.NONE);
            }
        }
    }

    private void endGame(Map<ImageView, Tile> fieldMap, Tile.State endState) {

        String winnerText = switch (endState) {
            case X -> "X won";
            case O -> "O won";
            case NONE -> "Draw";
        };

        Label winnerLabel = new Label(winnerText);
        Button rematchButton = new Button("Rematch");
        Button exitButton = new Button("Exit");

        rematchButton.setOnAction(e -> start(stage));
        exitButton.setOnAction(e -> stage.close());

        GridPane layout = new GridPane();
        layout.setAlignment(Pos.CENTER);
        layout.setPrefSize(400, 400);
        layout.setHgap(10);
        layout.setVgap(10);

        layout.add(winnerLabel, 0, 0);
        layout.add(rematchButton, 1, 0);
        layout.add(exitButton, 2, 0);

        List<ImageView> imageViews = new ArrayList<>();
        fieldMap.forEach((imageView, tile) -> imageViews.add(imageView));
        imageViews.sort(Comparator.comparingInt(imageView -> fieldMap.get(imageView).getPosition()));

        for (int i = 0; i < 3; i++) {
            for (int j = 1; j < 4; j++) {
                layout.add(imageViews.getFirst(), i, j);
                imageViews.getFirst().setOnMouseClicked(null);
                imageViews.removeFirst();
            }
        }

        Scene endScene = new Scene(layout);

        stage.setScene(endScene);
    }

    private int checkForWinningMove(Map<ImageView, Tile> fieldMap) {
        List<Tile> tiles = new ArrayList<>(fieldMap.values());
        tiles.sort(Comparator.comparingInt(Tile::getPosition));

        for (int i = 0; i < 7; i += 3) {
            if (haveSameNotNoneState(tiles.get(i), tiles.get(1 + i)) && tiles.get(2 + i).isNotOccupied()) return 2 + i;
            if (haveSameNotNoneState(tiles.get(i), tiles.get(2 + i)) && tiles.get(1 + i).isNotOccupied()) return 1 + i;
            if (haveSameNotNoneState(tiles.get(1 + i), tiles.get(2 + i)) && tiles.get(i).isNotOccupied()) return i;
        }

        for (int i = 0; i < 3; i++) {
            if (haveSameNotNoneState(tiles.get(i), tiles.get(3 + i)) && tiles.get(6 + i).isNotOccupied()) return 6 + i;
            if (haveSameNotNoneState(tiles.get(i), tiles.get(6 + i)) && tiles.get(3 + i).isNotOccupied()) return 3 + i;
            if (haveSameNotNoneState(tiles.get(3 + i), tiles.get(6 + i)) && tiles.get(i).isNotOccupied()) return i;
        }

        if (haveSameNotNoneState(tiles.get(0), tiles.get(4)) && tiles.get(8).isNotOccupied()) return 8;
        if (haveSameNotNoneState(tiles.get(0), tiles.get(8)) && tiles.get(4).isNotOccupied()) return 4;
        if (haveSameNotNoneState(tiles.get(4), tiles.get(8)) && tiles.get(0).isNotOccupied()) return 0;

        if (haveSameNotNoneState(tiles.get(2), tiles.get(4)) && tiles.get(6).isNotOccupied()) return 6;
        if (haveSameNotNoneState(tiles.get(2), tiles.get(6)) && tiles.get(4).isNotOccupied()) return 4;
        if (haveSameNotNoneState(tiles.get(4), tiles.get(6)) && tiles.get(2).isNotOccupied()) return 2;

        return -1;
    }

    public Tile.State checkIfGameEnded(Map<ImageView, Tile> fieldMap) {
        List<Tile> tiles = new ArrayList<>(fieldMap.values());
        tiles.sort(Comparator.comparingInt(Tile::getPosition));

        for (int i = 0; i < 7; i += 3) {
            if (haveSameNotNoneState(tiles.get(i), tiles.get(i + 1)) && haveSameNotNoneState(tiles.get(i), tiles.get(i + 2))) {
                return tiles.get(i).getState();
            }
        }

        for (int i = 0; i < 3; i++) {
            if (haveSameNotNoneState(tiles.get(i), tiles.get(i + 3)) && haveSameNotNoneState(tiles.get(i), tiles.get(i + 6))) {
                return tiles.get(i).getState();
            }
        }

        if (haveSameNotNoneState(tiles.get(0), tiles.get(4)) && haveSameNotNoneState(tiles.get(0), tiles.get(8))) {
            return tiles.getFirst().getState();
        }

        if (haveSameNotNoneState(tiles.get(2), tiles.get(4)) && haveSameNotNoneState(tiles.get(2), tiles.get(6))) {
            return tiles.get(2).getState();
        }

        return Tile.State.NONE;
    }

    public boolean isDraw(Map<ImageView, Tile> fieldMap) {
        return fieldMap.values().stream().noneMatch(Tile::isNotOccupied);
    }

    private boolean haveSameNotNoneState(Tile t1, Tile t2) {
        if (t1.getState().equals(Tile.State.NONE)) return false;
        return t1.getState().equals(t2.getState());
    }

    private enum Difficulty {
        EASY,
        MEDIUM,
        UNBEATABLE
    }

}
