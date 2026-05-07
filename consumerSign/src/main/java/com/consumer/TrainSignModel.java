package com.consumer;

import smile.classification.LogisticRegression;

import java.io.*;
import java.util.*;

public class TrainSignModel {
    public static void main(String[] args) throws Exception {
        File datasetDir = new File("../Dataset_signs");
        File[] files = datasetDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png"));

        if (files == null || files.length == 0) {
            System.err.println("Diretório de dataset não encontrado ou vazio: " + datasetDir.getAbsolutePath());
            return;
        }

        List<double[]> featuresList = new ArrayList<>();
        List<Integer> labelsList = new ArrayList<>();

        int count = 0;
        for (File file : files) {
            try {
                double[] features = ImageUtils.imageToVector(file, 28, 28);
                featuresList.add(features);
                
                // Heurística simples: divide em 4 classes fictícias
                labelsList.add(count % 4);
                count++;
            } catch (Exception e) {
                System.err.println("Erro ao processar imagem: " + file.getName());
            }
        }

        if (featuresList.isEmpty()) {
            System.err.println("Nenhum dado válido para treinar o modelo.");
            return;
        }

        double[][] features = featuresList.toArray(new double[0][]);
        int[] labels = labelsList.stream().mapToInt(i -> i).toArray();

        // Treina LogisticRegression
        LogisticRegression model = LogisticRegression.fit(features, labels);

        File outputFile = new File("src/main/resources/model_sign.bin");
        outputFile.getParentFile().mkdirs();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputFile))) {
            oos.writeObject(model);
        }
        System.out.println("Modelo treinado e salvo em model_sign.bin");
    }
}
