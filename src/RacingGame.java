package src;
// code to run the appilcation java --module-path C:/javafx-sdk-22/lib --add-modules javafx.controls,javafx.fxml -cp bin src.RacingGame

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class RacingGame extends Application {
    private final int WIDTH = 1000;
    private final int HEIGHT = 800;
    private final double CAR_WIDTH = 60;
    private final double CAR_HEIGHT = 30;
    private final int LAP_COUNT = 5;
    private final double TRACK_CENTER_X = WIDTH / 2.0;
    private final double TRACK_CENTER_Y = HEIGHT / 2.0;
    private final double RADIUS_X = (WIDTH - 200) / 2.0;
    private final double RADIUS_Y = (HEIGHT - 200) / 2.0;

    private double userCarX = 500;
    private double userCarY = 600;
    private double speed = 0;
    private double angle = 0;
    private double acceleration = 0.5;
    private double maxSpeed = 15;
    private double braking = 0.7;
    private double friction = 0.05;
    private boolean engineOn = false;
    private boolean inPit = false;
    private int userLaps = 0;
    private boolean upPressed = false;
    private boolean downPressed = false;
    private boolean leftPressed = false;
    private boolean rightPressed = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("2D Car Racing Game - Daytona Track");

        Pane root = new Pane();
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);

        // Engine Start/Stop Button
        Button engineButton = new Button("Start Engine");
        engineButton.setLayoutX(20); // Place button at a more accessible position
        engineButton.setLayoutY(20);
        engineButton.setOnAction(e -> {
            engineOn = !engineOn;
            if (engineOn) {
                engineButton.setText("Stop Engine");
            } else {
                engineButton.setText("Start Engine");
                speed = 0; // Stop the car when the engine is off
            }
        });
        root.getChildren().add(engineButton);

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Handle key presses for car control
        scene.setOnKeyPressed(event -> handleKeyPress(event));
        scene.setOnKeyReleased(event -> handleKeyRelease(event));

        // Game Loop
        new AnimationTimer() {
            public void handle(long currentNanoTime) {
                update();
                draw(gc);
            }
        }.start();
    }

    private void handleKeyPress(KeyEvent event) {
        if (engineOn) {
            switch (event.getCode()) {
                case UP -> upPressed = true;
                case DOWN -> downPressed = true;
                case LEFT -> leftPressed = true;
                case RIGHT -> rightPressed = true;
                default -> {} // Handle other keys without errors
            }
        }
    }

    private void handleKeyRelease(KeyEvent event) {
        switch (event.getCode()) {
            case UP -> upPressed = false;
            case DOWN -> downPressed = false;
            case LEFT -> leftPressed = false;
            case RIGHT -> rightPressed = false;
            default -> {} // Handle other keys without errors
        }
    }

    private void update() {
        // Update car speed and direction based on key presses
        if (engineOn) {
            if (upPressed) {
                speed = Math.min(speed + acceleration, maxSpeed);
            }
            if (downPressed) {
                speed = Math.max(speed - braking, 0);
            }
            if (leftPressed) {
                angle -= 3;
            }
            if (rightPressed) {
                angle += 3;
            }
        }

        // Apply friction
        if (speed > 0) {
            speed = Math.max(speed - friction, 0);
        } else if (speed < 0) {
            speed = Math.min(speed + friction, 0);
        }

        // Update user car position based on angle and speed
        userCarX += speed * Math.cos(Math.toRadians(angle));
        userCarY += speed * Math.sin(Math.toRadians(angle));

        // Keep user car on the track (stick to oval)
        double dx = userCarX - TRACK_CENTER_X;
        double dy = userCarY - TRACK_CENTER_Y;
        double distance = Math.sqrt((dx * dx) / (RADIUS_X * RADIUS_X) + (dy * dy) / (RADIUS_Y * RADIUS_Y));
        if (distance > 1) {
            userCarX = TRACK_CENTER_X + dx / distance;
            userCarY = TRACK_CENTER_Y + dy / distance;
        }

        // Handle pit stop logic for user car
        if (inPit) {
            speed = 2; // Limit speed in pit lane
            if (userCarX > WIDTH / 2 - 50 && userCarX < WIDTH / 2 + 50 && userCarY > HEIGHT - 150) {
                inPit = false; // Exit pit once at end of pit lane
            }
        }

        // Check if user completed a lap
        if (userCarX > WIDTH / 2 - 50 && userCarX < WIDTH / 2 + 50 && userCarY < 120 && speed > 0) {
            userLaps++;
            if (userLaps > LAP_COUNT) {
                userLaps = LAP_COUNT;
            }
        }
    }

    private void draw(GraphicsContext gc) {
        // Clear canvas
        gc.clearRect(0, 0, WIDTH, HEIGHT);

        // Draw Daytona-style track with pits and lighting
        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(5);
        gc.strokeOval(100, 100, WIDTH - 200, HEIGHT - 200);

        // Draw pit lane
        gc.setStroke(Color.BEIGE);
        gc.setLineWidth(4);
        gc.strokeRect(WIDTH / 2 - 60, HEIGHT - 150, 120, 150);
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(WIDTH / 2 - 60, HEIGHT - 150, 120, 150);

        // Draw start/finish line
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(3);
        gc.strokeLine(WIDTH / 2, 100, WIDTH / 2, 180);

        // Draw lane markings
        gc.setStroke(Color.YELLOW);
        gc.setLineWidth(2);
        gc.strokeOval(150, 150, WIDTH - 300, HEIGHT - 300);
        gc.strokeOval(200, 200, WIDTH - 400, HEIGHT - 400);

        // Draw user car
        gc.setFill(Color.RED);
        gc.save();
        gc.translate(userCarX + CAR_WIDTH / 2, userCarY + CAR_HEIGHT / 2);
        gc.rotate(angle);
        gc.fillRect(-CAR_WIDTH / 2, -CAR_HEIGHT / 2, CAR_WIDTH, CAR_HEIGHT);
        gc.restore();
        gc.setFill(Color.BLACK);
        gc.fillText("User Car", userCarX - 20, userCarY - 10);

        // Draw Lap Counter
        gc.setFill(Color.BLACK);
        gc.setFont(new Font(20));
        gc.fillText("Laps: " + userLaps + "/" + LAP_COUNT, 20, 50);
    }
}
