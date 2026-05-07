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

        List<double[]> featuresList = new ArrayList<>();
        List<Integer> labelsList = new ArrayList<>();

        // Categorias esperadas: Carro, Moto, Caminhao
        String[] categories = {"Carro", "Moto", "Caminhao"};
        
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
                    double[] vector = ImageUtils.imageToVector(file, 28, 28);
                    featuresList.add(vector);
                    labelsList.add(i); // Label: 0=Carro, 1=Moto, 2=Caminhao
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

