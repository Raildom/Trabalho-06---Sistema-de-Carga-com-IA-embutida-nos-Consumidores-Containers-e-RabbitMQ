package com.consumer;

import smile.classification.Classifier;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.awt.image.BufferedImage;


public class PlateModel {

    private Classifier<double[]> model;

    @SuppressWarnings("unchecked")
    public PlateModel(String path) throws Exception {
        this.model = (Classifier<double[]>) loadModel(path);
    }

    public String predict(BufferedImage imageFile) throws Exception {
        double[] vector = ImageUtils.imageToVector(imageFile, 28, 28);
        int label = model.predict(vector);
        if (label == 0) return "Carro";
        if (label == 1) return "Moto";
        if (label == 2) return "Caminhao";
        return "Desconhecido";
    }

    public static Object loadModel(String modelPath) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(modelPath))) {
            return ois.readObject();
        }
    }
}
