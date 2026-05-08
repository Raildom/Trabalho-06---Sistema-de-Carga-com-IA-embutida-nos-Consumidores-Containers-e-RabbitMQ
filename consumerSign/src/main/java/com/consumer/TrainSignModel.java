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

        File[] files = datasetDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png"));
        if (files == null || files.length == 0) {
            System.err.println("Nenhuma imagem encontrada em " + datasetDir.getAbsolutePath());
            return;
        }

        List<double[]> featuresList = new ArrayList<>();
        List<Integer> labelsList = new ArrayList<>();

        // Categorias: Pare=0, Proibido Direita=1, Placa 50=2
        for (File file : files) {
            String name = file.getName().toLowerCase();
            int label = -1;
            if (name.contains("pare")) label = 0;
            else if (name.contains("proibidodireita")) label = 1;
            else if (name.contains("placa50")) label = 2;

            if (label != -1) {
                try {
                    double[] features = ImageUtils.imageToVector(file, 28, 28);
                    featuresList.add(features);
                    labelsList.add(label);
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
