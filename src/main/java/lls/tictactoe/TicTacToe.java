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

    /**
     * The primary stage for this application.
     * This stage is used to display the various scenes of the Tic-Tac-Toe game.
     */
    private Stage stage;

    @Override
    public void start(Stage stage) {

        // Store the primary stage for this application
        this.stage = stage;

        // Create a label for difficulty selection
        Label difficultySelectionLabel = new Label("Choose the Difficulty:");

        // Create buttons for each difficulty level
        Button difficultySelectionEasy = new Button("Easy");
        Button difficultySelectionMedium = new Button("Medium");
        Button difficultySelectionUnbeatable = new Button("Unbeatable");

        // Set actions for the difficulty buttons to set up the game with the selected difficulty
        difficultySelectionEasy.setOnAction(e -> gameSetup(stage, Difficulty.EASY));
        difficultySelectionMedium.setOnAction(e -> gameSetup(stage, Difficulty.MEDIUM));
        difficultySelectionUnbeatable.setOnAction(e -> gameSetup(stage, Difficulty.UNBEATABLE));

        // Create and configure the layout for the difficulty selection screen
        GridPane layout = new GridPane();
        layout.setAlignment(Pos.CENTER);
        layout.setPrefSize(350, 100);
        layout.setHgap(15);
        layout.add(difficultySelectionLabel, 0, 0);
        layout.add(difficultySelectionEasy, 1, 0);
        layout.add(difficultySelectionMedium, 2, 0);
        layout.add(difficultySelectionUnbeatable, 3, 0);

        // Create and set the scene for the difficulty selection screen
        Scene preScene = new Scene(layout);

        // Set the title and scene for the primary stage and show it
        stage.setTitle("TicTacToe");
        stage.setScene(preScene);
        stage.show();
    }

    /**
     * Sets up the game by allowing the user to choose their symbol (X or O).
     *
     * @param stage      The primary stage for this application.
     * @param difficulty The difficulty level of the AI (EASY, MEDIUM, UNBEATABLE).
     */
    private void gameSetup(Stage stage, Difficulty difficulty) {

        // Create a label for symbol selection
        Label typeSelectionLabel = new Label("Choose your symbol:");

        // Create buttons for symbol selection
        Button typeSelectionX = new Button("X");
        Button typeSelectionO = new Button("O");

        // Set actions for the buttons to start the game with the selected symbol
        typeSelectionX.setOnAction(e -> startGame(stage, difficulty, Tile.State.X, Tile.State.O));
        typeSelectionO.setOnAction(e -> startGame(stage, difficulty, Tile.State.O, Tile.State.X));

        // Create and configure the layout for the symbol selection screen
        GridPane layout = new GridPane();
        layout.setAlignment(Pos.CENTER);
        layout.setPrefSize(250, 100);
        layout.setHgap(15);
        layout.add(typeSelectionLabel, 0, 0);
        layout.add(typeSelectionX, 1, 0);
        layout.add(typeSelectionO, 2, 0);

        // Create and set the scene for the symbol selection screen
        Scene preScene2 = new Scene(layout);
        stage.setScene(preScene2);
    }

    /**
     * Starts the Tic-Tac-Toe game with the specified difficulty and player/AI symbols.
     *
     * @param stage           The primary stage for this application.
     * @param difficulty      The difficulty level of the AI (EASY, MEDIUM, UNBEATABLE).
     * @param playerStateType The state (X or O) representing the player's symbol.
     * @param aiStateType     The state (X or O) representing the AI's symbol.
     */
    private void startGame(Stage stage, Difficulty difficulty, Tile.State playerStateType, Tile.State aiStateType) {
        // Load images for X, O, and NONE states
        Image imageX = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/X.png")));
        Image imageO = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/O.png")));
        Image imageNone = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/NONE.png")));

        // Create a map to associate ImageViews with Tiles
        Map<ImageView, Tile> fieldMap = new HashMap<>();

        // Create and configure the layout for the game board
        GridPane layout = new GridPane();
        layout.setAlignment(Pos.CENTER);
        layout.setPrefSize(400, 400);

        // Initialize the game board with ImageViews and Tiles
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                final ImageView currentImageView = new ImageView(imageNone);
                final Tile currentTile = new Tile((i == 0 ? 0 : (i == 1 ? 3 : 6)) + j);
                fieldMap.put(currentImageView, currentTile);
                layout.add(currentImageView, i, j);
                currentImageView.setOnMouseClicked(e -> {
                    // Handle player move
                    if (currentTile.changeState(playerStateType)) {
                        switch (currentTile.getState()) {
                            case X -> {
                                currentImageView.setImage(imageX);
                                if (potentialGameEnd(fieldMap)) return;
                            }
                            case O -> {
                                currentImageView.setImage(imageO);
                                if (potentialGameEnd(fieldMap)) return;
                            }
                            default -> throw new IllegalStateException("Tile was changed, but state is NONE");
                        }
                        // Execute AI move after player move
                        moveAI(difficulty, aiStateType, fieldMap, imageX, imageO);
                    }
                });
            }
        }

        // Create and set the game scene
        Scene gameScene = new Scene(layout);
        stage.setScene(gameScene);

        // If the player chose O, the AI makes the first move
        if (playerStateType.equals(Tile.State.O)) {
            moveAI(difficulty, aiStateType, fieldMap, imageX, imageO);
        }
    }

    /**
     * Checks if the game has potentially ended by evaluating the current state of the board.
     * <p>
     * This method checks if there is a winning state or if the game is a draw.
     * If either condition is met, it ends the game and returns true.
     * Otherwise, it returns false.
     *
     * @param fieldMap A map of ImageView to Tile representing the current state of the board.
     * @return true if the game has ended (either a win or a draw), false otherwise.
     */
    private boolean potentialGameEnd(Map<ImageView, Tile> fieldMap) {
        // Check if there is a winning state
        Tile.State endState = checkIfGameEnded(fieldMap);
        if (!endState.equals(Tile.State.NONE)) {
            // End the game if a winning state is detected
            endGame(fieldMap, endState);
            return true;
        }
        // Check if the game is a draw
        if (isDraw(fieldMap)) {
            // End the game if a draw is detected
            endGame(fieldMap, Tile.State.NONE);
            return true;
        }
        // Return false if the game has not ended
        return false;
    }

    /**
     * Executes the AI's move based on the selected difficulty level.
     *
     * @param difficulty  The difficulty level of the AI (EASY, MEDIUM, UNBEATABLE).
     * @param aiStateType The state (X or O) representing the AI's symbol.
     * @param fieldMap    A map of ImageView to Tile representing the current state of the board.
     * @param imageX      The image to set for the X symbol.
     * @param imageO      The image to set for the O symbol.
     * @throws NullPointerException if the difficulty is null.
     */
    private void moveAI(Difficulty difficulty, Tile.State aiStateType, Map<ImageView, Tile> fieldMap,
                        Image imageX, Image imageO) {
        switch (difficulty) {
            case EASY -> moveAIEasy(aiStateType, fieldMap, imageX, imageO);
            case MEDIUM -> moveAIMedium(aiStateType, fieldMap, imageX, imageO);
            case UNBEATABLE -> moveAIUnbeatable(aiStateType, fieldMap, imageX, imageO);
            default -> throw new NullPointerException("Difficulty was null");
        }
    }

    /**
     * Executes the AI's move for the easy difficulty level.
     *
     * @param aiStateType The state (X or O) representing the AI's symbol.
     * @param fieldMap    A map of ImageView to Tile representing the current state of the board.
     * @param imageX      The image to set for the X symbol.
     * @param imageO      The image to set for the O symbol.
     */
    private void moveAIEasy(Tile.State aiStateType, Map<ImageView, Tile> fieldMap, Image imageX, Image imageO) {
        // Create a list to store free (unoccupied) tiles
        List<ImageView> freeTiles = new ArrayList<>();

        // Iterate through the fieldMap to find unoccupied tiles and add them to the freeTiles list
        fieldMap.forEach((imageView, tile) -> {
            if (tile.isNotOccupied()) {
                freeTiles.add(imageView);
            }
        });

        // If there are no free tiles, return without making a move
        if (freeTiles.isEmpty()) return;

        // Select a random free tile for the AI's move
        ImageView targetImageView = freeTiles.get(RANDOM.nextInt(freeTiles.size()));

        // Perform the AI's move on the selected tile
        performAIMove(aiStateType, fieldMap, imageX, imageO, targetImageView);
    }

    /**
     * Executes the AI's move for the medium difficulty level.
     *
     * @param aiStateType The state (X or O) representing the AI's symbol.
     * @param fieldMap    A map of ImageView to Tile representing the current state of the board.
     * @param imageX      The image to set for the X symbol.
     * @param imageO      The image to set for the O symbol.
     */
    private void moveAIMedium(Tile.State aiStateType, Map<ImageView, Tile> fieldMap, Image imageX, Image imageO) {
        // Create a list of ImageViews from the fieldMap
        List<ImageView> imageViews = new ArrayList<>();
        fieldMap.forEach((imageView, tile) -> imageViews.add(imageView));

        // Check for a winning move
        int move = checkForWinningMove(fieldMap);
        if (move != -1) {
            // If a winning move is found, perform the AI's move on the target ImageView
            Optional<ImageView> optionalMove = imageViews.stream()
                    .filter(imageView -> fieldMap.get(imageView).getPosition() == move)
                    .findAny();
            if (optionalMove.isPresent()) {
                ImageView targetImageView = optionalMove.get();
                performAIMove(aiStateType, fieldMap, imageX, imageO, targetImageView);
                return;
            }
        }

        // If no winning move is found, fallback to the easy AI move
        moveAIEasy(aiStateType, fieldMap, imageX, imageO);
    }

    /**
     * Executes the AI's move for the unbeatable difficulty level.
     *
     * @param aiStateType The state (X or O) representing the AI's symbol.
     * @param fieldMap    A map of ImageView to Tile representing the current state of the board.
     * @param imageX      The image to set for the X symbol.
     * @param imageO      The image to set for the O symbol.
     */
    private void moveAIUnbeatable(Tile.State aiStateType, Map<ImageView, Tile> fieldMap, Image imageX, Image imageO) {
        // Determine the player's state (X or O) by finding the state that is not NONE and not the AI's state
        Tile.State playerState = Arrays.stream(Tile.State.values())
                .filter(state -> !state.equals(Tile.State.NONE) && !state.equals(aiStateType))
                .findAny()
                .orElseThrow();

        // Get the best move for the AI using the Minimax algorithm
        int move = Minimax.getBestMove(this, fieldMap, playerState, aiStateType);

        // Create a list of ImageViews from the fieldMap
        List<ImageView> imageViews = new ArrayList<>();
        fieldMap.forEach((imageView, tile) -> imageViews.add(imageView));

        // Sort the ImageViews by their position on the board
        imageViews.sort(Comparator.comparingInt(i -> fieldMap.get(i).getPosition()));

        // Perform the AI's move on the target ImageView
        performAIMove(aiStateType, fieldMap, imageX, imageO, imageViews.get(move));
    }

    /**
     * Performs the AI's move on the Tic-Tac-Toe board.
     *
     * @param aiStateType     The state (X or O) representing the AI's symbol.
     * @param fieldMap        A map of ImageView to Tile representing the current state of the board.
     * @param imageX          The image to set for the X symbol.
     * @param imageO          The image to set for the O symbol.
     * @param targetImageView The ImageView to update with the AI's move.
     * @throws IllegalStateException if the target tile is already occupied.
     */
    private void performAIMove(Tile.State aiStateType, Map<ImageView, Tile> fieldMap, Image imageX, Image imageO, ImageView targetImageView) {
        // Attempt to change the state of the target tile to the AI's state
        if (!fieldMap.get(targetImageView).changeState(aiStateType)) {
            throw new IllegalStateException("Tile which was not occupied, is now suddenly occupied");
        }
        // Update the target ImageView based on the new state of the tile
        switch (fieldMap.get(targetImageView).getState()) {
            case O -> performMoveForAIState(fieldMap, imageO, targetImageView);
            case X -> performMoveForAIState(fieldMap, imageX, targetImageView);
            default -> throw new IllegalStateException("Tile was change, but state is NONE");
        }
    }

    /**
     * Updates the target ImageView with the AI's move and checks for game end conditions.
     *
     * @param fieldMap        A map of ImageView to Tile representing the current state of the board.
     * @param imageX          The image to set for the AI's move.
     * @param targetImageView The ImageView to update with the AI's move.
     */
    private void performMoveForAIState(Map<ImageView, Tile> fieldMap, Image imageX, ImageView targetImageView) {
        // Set the image for the AI's move
        targetImageView.setImage(imageX);
        {
            // Check if the game has ended after the AI's move
            Tile.State endState = checkIfGameEnded(fieldMap);
            if (!endState.equals(Tile.State.NONE)) {
                // End the game if a winning state is detected
                endGame(fieldMap, endState);
            }
            // Check if the game is a draw after the AI's move
            if (isDraw(fieldMap)) {
                // End the game if a draw is detected
                endGame(fieldMap, Tile.State.NONE);
            }
        }
    }

    /**
     * Ends the game and displays the result.
     *
     * @param fieldMap A map of ImageView to Tile representing the current state of the board.
     * @param endState The state of the game at the end (X, O, or NONE for draw).
     */
    private void endGame(Map<ImageView, Tile> fieldMap, Tile.State endState) {

        // Determine the winner text based on the end state
        String winnerText = switch (endState) {
            case X -> "X won";
            case O -> "O won";
            case NONE -> "Draw";
        };

        // Create UI elements for the end game screen
        Label winnerLabel = new Label(winnerText);
        Button rematchButton = new Button("Rematch");
        Button exitButton = new Button("Exit");

        // Set actions for the buttons
        rematchButton.setOnAction(e -> start(stage));
        exitButton.setOnAction(e -> stage.close());

        // Create and configure the layout for the end game screen
        GridPane layout = new GridPane();
        layout.setAlignment(Pos.CENTER);
        layout.setPrefSize(400, 400);
        layout.setHgap(10);
        layout.setVgap(10);

        // Add UI elements to the layout
        layout.add(winnerLabel, 0, 0);
        layout.add(rematchButton, 1, 0);
        layout.add(exitButton, 2, 0);

        // Collect and sort the ImageViews from the field map
        List<ImageView> imageViews = new ArrayList<>();
        fieldMap.forEach((imageView, tile) -> imageViews.add(imageView));
        imageViews.sort(Comparator.comparingInt(imageView -> fieldMap.get(imageView).getPosition()));

        // Add the ImageViews to the layout and disable their click actions
        for (int i = 0; i < 3; i++) {
            for (int j = 1; j < 4; j++) {
                layout.add(imageViews.getFirst(), i, j);
                imageViews.getFirst().setOnMouseClicked(null);
                imageViews.removeFirst();
            }
        }

        // Create and set the end game scene
        Scene endScene = new Scene(layout);
        stage.setScene(endScene);
    }

    /**
     * Checks for a winning move on the Tic-Tac-Toe board.
     *
     * @param fieldMap A map of ImageView to Tile representing the current state of the board.
     * @return The position of the winning move if available, otherwise -1.
     */
    private int checkForWinningMove(Map<ImageView, Tile> fieldMap) {
        List<Tile> tiles = new ArrayList<>(fieldMap.values());
        tiles.sort(Comparator.comparingInt(Tile::getPosition));

        // Check rows and columns for a winning move
        for (int i = 0; i < 3; i++) {
            // Rows
            if (checkLine(tiles, i * 3, 1)) return i * 3 + 2;
            if (checkLine(tiles, i * 3, 2)) return i * 3 + 1;
            if (checkLine(tiles, i * 3 + 1, 1)) return i * 3;

            // Columns
            if (checkLine(tiles, i, 3)) return i + 6;
            if (checkLine(tiles, i, 6)) return i + 3;
            if (checkLine(tiles, i + 3, 3)) return i;
        }

        // Check diagonals for a winning move
        if (checkLine(tiles, 0, 4)) return 8;
        if (checkLine(tiles, 0, 8)) return 4;
        if (checkLine(tiles, 4, 4)) return 0;
        if (checkLine(tiles, 2, 4)) return 6;
        if (checkLine(tiles, 2, 6)) return 4;
        if (checkLine(tiles, 4, 6)) return 2;

        // No winning move found
        return -1;
    }

    /**
     * Checks if a line of tiles has the same state that is not NONE and the third tile in the line is not occupied.
     *
     * @param tiles The list of tiles representing the current state of the board.
     * @param start The starting index of the line to check.
     * @param step  The step size to move from the start index to check the line.
     * @return true if the first two tiles in the line have the same state that is not NONE and the third tile is not occupied,
     * false otherwise.
     */
    private boolean checkLine(List<Tile> tiles, int start, int step) {
        return haveSameNotNoneState(tiles.get(start), tiles.get(start + step)) && tiles.get(start + 2 * step).isNotOccupied();
    }

    /**
     * Checks if the game has ended by evaluating the current state of the board.
     *
     * @param fieldMap A map of ImageView to Tile representing the current state of the board.
     * @return The state of the winning player if the game has ended, otherwise Tile.State.NONE.
     */
    public Tile.State checkIfGameEnded(Map<ImageView, Tile> fieldMap) {
        // Convert the fieldMap values to a list and sort them by their position
        List<Tile> tiles = new ArrayList<>(fieldMap.values());
        tiles.sort(Comparator.comparingInt(Tile::getPosition));

        // Check rows for a winning state
        for (int i = 0; i < 7; i += 3) {
            if (haveSameNotNoneState(tiles.get(i), tiles.get(i + 1)) && haveSameNotNoneState(tiles.get(i), tiles.get(i + 2))) {
                return tiles.get(i).getState();
            }
        }

        // Check columns for a winning state
        for (int i = 0; i < 3; i++) {
            if (haveSameNotNoneState(tiles.get(i), tiles.get(i + 3)) && haveSameNotNoneState(tiles.get(i), tiles.get(i + 6))) {
                return tiles.get(i).getState();
            }
        }

        // Check diagonals for a winning state
        if (haveSameNotNoneState(tiles.get(0), tiles.get(4)) && haveSameNotNoneState(tiles.get(0), tiles.get(8))) {
            return tiles.getFirst().getState();
        }

        if (haveSameNotNoneState(tiles.get(2), tiles.get(4)) && haveSameNotNoneState(tiles.get(2), tiles.get(6))) {
            return tiles.get(2).getState();
        }

        // Return NONE if no winning state is found
        return Tile.State.NONE;
    }

    /**
     * Checks if the game is a draw.
     * <p>
     * This method evaluates the current state of the board to determine if all tiles are occupied,
     * and no winning move is possible, indicating a draw.
     *
     * @param fieldMap A map of ImageView to Tile representing the current state of the board.
     * @return true if the game is a draw (i.e., no tiles are unoccupied), false otherwise.
     */
    public boolean isDraw(Map<ImageView, Tile> fieldMap) {
        return fieldMap.values().stream().noneMatch(Tile::isNotOccupied);
    }

    /**
     * Checks if two tiles have the same state that is not NONE.
     *
     * @param first  The first tile to compare.
     * @param second The second tile to compare.
     * @return true if both tiles have the same state and it is not NONE, false otherwise.
     */
    private boolean haveSameNotNoneState(Tile first, Tile second) {
        if (first.getState().equals(Tile.State.NONE)) return false;
        return first.getState().equals(second.getState());
    }

    private enum Difficulty {
        EASY,
        MEDIUM,
        UNBEATABLE
    }

}
