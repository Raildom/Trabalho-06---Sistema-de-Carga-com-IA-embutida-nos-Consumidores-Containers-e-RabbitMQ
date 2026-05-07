package com.consumer;

import smile.classification.Classifier;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.awt.image.BufferedImage;
import java.awt.*;

public class SignModel {
    private Classifier<double[]> classifier;

    public SignModel(String modelPath) {
        try {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(modelPath))) {
                classifier = (Classifier<double[]>) ois.readObject();
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar modelo: " + e.getMessage(), e);
        }
    }

    public String predict(BufferedImage imageFile) throws Exception {
        double[] features = ImageUtils.imageToVector(imageFile, 28, 28);
        int label = classifier.predict(features);

        // Categorias: Pare, Velocidade, Proibido, Atencao
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
