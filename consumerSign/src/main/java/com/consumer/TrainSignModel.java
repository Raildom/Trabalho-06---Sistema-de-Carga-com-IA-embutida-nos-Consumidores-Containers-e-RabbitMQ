package com.consumer;

import smile.classification.LogisticRegression;

import java.io.*;
import java.util.*;

public class TrainSignModel {
    public static void main(String[] args) throws Exception {
        String datasetPath = "src/main/resources/dataset_times/";
        String labelsFile = datasetPath + "labels.csv";

        int numClasses = 8;

        List<double[]> featuresList = new ArrayList<>();
        List<Integer> labelsList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(labelsFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                String filename = parts[0];
                int label = Integer.parseInt(parts[1]);

                File imgFile = new File(datasetPath + filename);
                if (!imgFile.exists()) {
                    System.err.println("Arquivo de imagem não encontrado: " + imgFile.getAbsolutePath());
                    continue;
                }

                if (label < 0 || label >= numClasses) {
                    System.err.println("Rótulo inválido para arquivo " + filename + ": " + label);
                    continue;
                }

                double[] features = ImageUtils.imageToVector(imgFile, 28, 28);
                featuresList.add(features);
                labelsList.add(label);
            }
        }

        if (featuresList.isEmpty()) {
            System.err.println("Nenhum dado válido para treinar o modelo.");
            return;
        }

        double[][] features = featuresList.toArray(new double[0][]);
        int[] labels = labelsList.stream().mapToInt(i -> i).toArray();

        System.out.println("labels: " + Arrays.toString(labels));
        System.out.println("features: " + Arrays.toString(features));

        // Troque SVM por LogisticRegression para multiclasses
        LogisticRegression model = LogisticRegression.fit(features, labels);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("src/main/resources/model_team.bin"))) {
            oos.writeObject(model);
        }
        System.out.println("Modelo treinado e salvo em model_team.bin");
    }
}
