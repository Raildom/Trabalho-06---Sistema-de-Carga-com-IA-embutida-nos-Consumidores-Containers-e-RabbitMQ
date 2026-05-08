package com.consumer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import smile.classification.LogisticRegression;

public class TrainPlateModel {

    public static void main(String[] args) throws Exception {
        File datasetDir = new File("../Dataset_plates");
        
        if (!datasetDir.exists() || !datasetDir.isDirectory()) {
            System.err.println("Diretório de dataset não encontrado: " + datasetDir.getAbsolutePath());
            return;
        }

        File[] files = datasetDir.listFiles((dir, name) -> 
            name.toLowerCase().endsWith(".jpg") || 
            name.toLowerCase().endsWith(".jpeg") || 
            name.toLowerCase().endsWith(".png"));
        if (files == null || files.length == 0) {
            System.err.println("Nenhuma imagem encontrada em " + datasetDir.getAbsolutePath());
            return;
        }

        List<double[]> featuresList = new ArrayList<>();
        List<Integer> labelsList = new ArrayList<>();

        // Categorias: Carro=0, Moto=1, Caminhao=2
        for (File file : files) {
            String name = file.getName().toLowerCase();
            int label = -1;
            if (name.startsWith("carro")) label = 0;
            else if (name.startsWith("moto")) label = 1;
            else if (name.startsWith("caminhao")) label = 2;

            if (label != -1) {
                try {
                    double[] vector = ImageUtils.imageToVector(file, 28, 28);
                    featuresList.add(vector);
                    labelsList.add(label);
                } catch (Exception e) {
                    System.err.println("Erro ao processar arquivo: " + file.getName());
                }
            }
        }

        if (featuresList.isEmpty()) {
            System.err.println("Nenhuma imagem encontrada nas subpastas Carro, Moto ou Caminhao!");
            return;
        }

        double[][] X = featuresList.toArray(new double[0][]);
        int[] y = labelsList.stream().mapToInt(i -> i).toArray();

        // Treina LogisticRegression para multiclasse
        System.out.println("Treinando modelo com " + X.length + " imagens...");
        LogisticRegression model = LogisticRegression.fit(X, y);

        // Salva o modelo
        File outputFile = new File("src/main/resources/model_plate.bin");
        outputFile.getParentFile().mkdirs();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputFile))) {
            oos.writeObject(model);
        }

        System.out.println("Modelo treinado e salvo com sucesso em " + outputFile.getAbsolutePath());
    }
}

