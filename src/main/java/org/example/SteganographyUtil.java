package org.example;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class SteganographyUtil {

    public static WritableImage hideMessageLSB(Image fxImage, String message) {
        int width = (int) fxImage.getWidth();
        int height = (int) fxImage.getHeight();
        PixelReader reader = fxImage.getPixelReader();
        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter writer = writableImage.getPixelWriter();

        // Convert message to binary string
        StringBuilder binaryMessage = new StringBuilder();
        for (char c : message.toCharArray()) {
            String bin = String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0');
            binaryMessage.append(bin);
        }
        binaryMessage.append("00000000"); // terminator

        int messageIndex = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = reader.getColor(x, y);

                int red = (int) (color.getRed() * 255);
                int green = (int) (color.getGreen() * 255);
                int blue = (int) (color.getBlue() * 255);
                int alpha = (int) (color.getOpacity() * 255);

                if (messageIndex < binaryMessage.length()) {
                    red = (red & 0xFE) | (binaryMessage.charAt(messageIndex) - '0');
                    messageIndex++;
                }
                if (messageIndex < binaryMessage.length()) {
                    green = (green & 0xFE) | (binaryMessage.charAt(messageIndex) - '0');
                    messageIndex++;
                }
                if (messageIndex < binaryMessage.length()) {
                    blue = (blue & 0xFE) | (binaryMessage.charAt(messageIndex) - '0');
                    messageIndex++;
                }

                writer.setColor(x, y, Color.rgb(red, green, blue, alpha / 255.0));
            }
        }

        return writableImage; // no extra loop needed
    }


    // Decode a message from LSB of an image
    public static String decodeMessageLSB(Image fxImage) {
        PixelReader reader = fxImage.getPixelReader();
        StringBuilder binary = new StringBuilder();

        int width = (int) fxImage.getWidth();
        int height = (int) fxImage.getHeight();

        outerLoop:
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color c = reader.getColor(x, y);
                int red = (int) (c.getRed() * 255);
                int green = (int) (c.getGreen() * 255);
                int blue = (int) (c.getBlue() * 255);

                binary.append(red & 1);
                binary.append(green & 1);
                binary.append(blue & 1);

                // Check for terminator
                if (binary.length() >= 8) {
                    String lastByte = binary.substring(binary.length() - 8);
                    if (lastByte.equals("00000000")) break outerLoop;
                }
            }
        }

        // Convert binary string to characters
        StringBuilder message = new StringBuilder();
        for (int i = 0; i + 8 <= binary.length(); i += 8) {
            String byteStr = binary.substring(i, i + 8);
            if (byteStr.equals("00000000")) break;
            message.append((char) Integer.parseInt(byteStr, 2));
        }
        return message.toString();
    }
}
