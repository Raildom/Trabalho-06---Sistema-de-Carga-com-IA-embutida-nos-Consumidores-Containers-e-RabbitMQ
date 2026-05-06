package com.consumer;

import smile.classification.LogisticRegression;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.*;

public class SignModel {
    private LogisticRegression classifier;

    public SignModel(String modelPath) {
        try {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(modelPath))) {
                classifier = (LogisticRegression) ois.readObject();
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar modelo: " + e.getMessage(), e);
        }
    }

    public String predict(BufferedImage imageFile) throws Exception {
        BufferedImage img = imageFile;
        BufferedImage resized = new BufferedImage(28, 28, BufferedImage.TYPE_BYTE_GRAY);

        Graphics2D g = resized.createGraphics();
        g.drawImage(img, 0, 0, 28, 28, null);
        g.dispose();

        double[] features = new double[28 * 28];
        for (int y = 0; y < 28; y++) {
            for (int x = 0; x < 28; x++) {
                int rgb = resized.getRGB(x, y);
                int gray = rgb & 0xFF;
                features[y * 28 + x] = gray / 255.0;
            }
        }

        int label = classifier.predict(features);

        String[] signs = {"Pare", "Velocidade Máxima", "Proibido Virar", "Atenção"};
        if (label < 0 || label >= signs.length) {
            return "Desconhecido";
        }
        return signs[label];
    }

    public static Object loadModel(String modelPath) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(modelPath))) {
            return ois.readObject();
        }
    }
}
