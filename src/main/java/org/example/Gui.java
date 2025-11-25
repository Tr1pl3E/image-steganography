package org.example;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import javafx.embed.swing.SwingFXUtils;

public class Gui extends Application {

    @FXML private StackPane imageDropPane;
    @FXML private Label dropLabel;
    @FXML private TextArea textArea;
    @FXML private Button encode;
    @FXML private Button decode;
    @FXML private Button download;
    @FXML private Label statusLabel;
    @FXML private ComboBox<String> algorithmSelector;
    @FXML private TextField keyField;

    private Image fxImage; // Original image
    private ImageView rightImageView;
    private HBox hbox;
    private boolean isEncoded = false;

    public static void launchGui() {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/Gui.fxml"));
        Scene scene = new Scene(root, 600, 500);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        primaryStage.setTitle("JavaFX Steganography Demo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @FXML
    public void initialize() {
        // Text area changes
        textArea.textProperty().addListener((obs, oldText, newText) -> {
            isEncoded = false;
            updateStatusLabel();
        });

        // Algorithm selection
        algorithmSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            if (newVal.equals("None")) {
                keyField.setDisable(true);
                keyField.setStyle("-fx-opacity: 0.6;");
                keyField.clear();
            } else {
                keyField.setDisable(false);
                keyField.setStyle("-fx-opacity: 1;");
            }
        });

        // Drag and drop
        imageDropPane.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles() && db.getFiles().size() == 1) {
                event.acceptTransferModes(TransferMode.COPY);
                onDragOverImageBox();
            }
            event.consume();
        });

        imageDropPane.setOnDragExited(event -> {
            resetImageBoxStyle();
            event.consume();
        });

        imageDropPane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasFiles() && db.getFiles().size() == 1) {
                success = true;
                File file = db.getFiles().get(0);
                imageDropPane.getChildren().remove(dropLabel);

                try {
                    fxImage = new Image(file.toURI().toString());

                    rightImageView = new ImageView(fxImage);
                    rightImageView.setPreserveRatio(true);
                    rightImageView.fitWidthProperty().bind(imageDropPane.widthProperty().multiply(0.45));
                    rightImageView.fitHeightProperty().bind(imageDropPane.heightProperty().multiply(0.9));

                    hbox = new HBox(10);
                    hbox.getChildren().addAll(rightImageView);
                    hbox.setAlignment(javafx.geometry.Pos.CENTER);
                    imageDropPane.getChildren().add(hbox);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            event.setDropCompleted(success);
            event.consume();
            resetImageBoxStyle();
        });

        // Buttons
        encode.setOnAction(e -> onEncodeClick());
        decode.setOnAction(e -> onDecodeClick());
        download.setOnAction(e -> onDownloadClick());

        algorithmSelector.setValue("None");
        keyField.setDisable(true);
        keyField.setStyle("-fx-opacity: 0.6;");
    }


    private void updateStatusLabel() {
        if (isEncoded) {
            statusLabel.setText("Encoded");
            statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        } else {
            statusLabel.setText("Not encoded");
            statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        }
    }

    private void onDragOverImageBox() {
        imageDropPane.setStyle("-fx-background-color: #d0ffd0; -fx-border-color: gray; -fx-border-width: 2;");
        dropLabel.setText("Release to drop the image!");
    }

    private void resetImageBoxStyle() {
        imageDropPane.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: gray; -fx-border-width: 2;");
        dropLabel.setText("Drag and drop images here");
    }

    private void onEncodeClick() {
        if (fxImage == null || textArea.getText().isEmpty()) return;

        String message = textArea.getText();
        String algo = algorithmSelector.getValue();
        String key = keyField.getText();

        try {
            String encryptedMessage = switch (algo) {
                case "AES" -> CryptoUtil.encryptAES(message, padKey(key, 16));
                case "DES" -> CryptoUtil.encryptDES(message, padKey(key, 8));
                case "XOR" -> CryptoUtil.encryptXOR(message, key);
                default -> message;
            };

            WritableImage stegoImage = SteganographyUtil.hideMessageLSB(fxImage, encryptedMessage);
            rightImageView.setImage(stegoImage);

            download.setStyle("-fx-background-color: #4CAF50;");
            isEncoded = true;
            updateStatusLabel();
        } catch (Exception e) {
            showErrorPopup("Encoding Error", "Failed to encode message: " + e.getMessage());
        }
    }

    private void onDecodeClick() {
        if (rightImageView == null || rightImageView.getImage() == null) return;

        String algo = algorithmSelector.getValue();
        String key = keyField.getText();

        try {
            String encodedMessage = SteganographyUtil.decodeMessageLSB(rightImageView.getImage());
            String decodedMessage;

            switch (algo) {
                case "AES" -> decodedMessage = CryptoUtil.decryptAES(encodedMessage, padKey(key, 16));
                case "DES" -> decodedMessage = CryptoUtil.decryptDES(encodedMessage, padKey(key, 8));
                case "XOR" -> decodedMessage = CryptoUtil.decryptXOR(encodedMessage, key);
                default -> decodedMessage = encodedMessage;
            }

            textArea.setText(decodedMessage);
            isEncoded = false;
            updateStatusLabel();

        } catch (javax.crypto.IllegalBlockSizeException | javax.crypto.BadPaddingException e) {
            showErrorPopup("Decoding Error", "Failed to decode message. Make sure you selected the correct algorithm and entered the correct key.");
        } catch (Exception e) {
            showErrorPopup("Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    private void onDownloadClick() {
        if (rightImageView == null || rightImageView.getImage() == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
        File file = fileChooser.showSaveDialog(download.getScene().getWindow());

        if (file != null) {
            try {
                BufferedImage bImage = SwingFXUtils.fromFXImage(rightImageView.getImage(), null);
                ImageIO.write(bImage, "png", file);
            } catch (Exception e) {
                showErrorPopup("Error", "Failed to save image: " + e.getMessage());
            }
        }
    }

    private String padKey(String key, int length) {
        if (key == null) key = "";
        if (key.length() >= length) return key.substring(0, length);
        return String.format("%-" + length + "s", key).replace(' ', '0');
    }

    private void showErrorPopup(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
