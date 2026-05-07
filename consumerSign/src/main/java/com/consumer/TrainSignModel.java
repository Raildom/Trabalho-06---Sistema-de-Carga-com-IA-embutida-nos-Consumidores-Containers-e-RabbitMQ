package com.consumer;

import smile.classification.LogisticRegression;

import java.io.*;
import java.util.*;

public class TrainSignModel {
    public static void main(String[] args) throws Exception {
        File datasetDir = new File("../Dataset_signs");
        
        if (!datasetDir.exists() || !datasetDir.isDirectory()) {
            System.err.println("Diretório de dataset não encontrado: " + datasetDir.getAbsolutePath());
            return;
        }

        List<double[]> featuresList = new ArrayList<>();
        List<Integer> labelsList = new ArrayList<>();

        // Categorias esperadas: Pare, Velocidade, Proibido, Atencao
        String[] categories = {"Pare", "Velocidade", "Proibido", "Atencao"};
        
        for (int i = 0; i < categories.length; i++) {
            File subDir = new File(datasetDir, categories[i]);
            if (!subDir.exists() || !subDir.isDirectory()) {
                System.out.println("Aviso: Subdiretório não encontrado: " + subDir.getName());
                continue;
            }

            File[] files = subDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png"));
            if (files == null) continue;

            for (File file : files) {
                try {
                    double[] features = ImageUtils.imageToVector(file, 28, 28);
                    featuresList.add(features);
                    labelsList.add(i); // Label: 0=Pare, 1=Velocidade, 2=Proibido, 3=Atencao
                } catch (Exception e) {
                    System.err.println("Erro ao processar imagem: " + file.getName());
                }
            }
        }

        if (featuresList.isEmpty()) {
            System.err.println("Nenhuma imagem encontrada nas subpastas!");
            return;
        }

        double[][] features = featuresList.toArray(new double[0][]);
        int[] labels = labelsList.stream().mapToInt(i -> i).toArray();

        // Treina LogisticRegression para multiclasse
        System.out.println("Treinando modelo com " + features.length + " imagens...");
        LogisticRegression model = LogisticRegression.fit(features, labels);

        File outputFile = new File("src/main/resources/model_sign.bin");
        outputFile.getParentFile().mkdirs();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputFile))) {
            oos.writeObject(model);
        }
        System.out.println("Modelo treinado e salvo em model_sign.bin");
    }
}
