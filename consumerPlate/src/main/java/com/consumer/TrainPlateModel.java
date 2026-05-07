package com.consumer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import smile.classification.SVM;
import smile.math.kernel.LinearKernel;

public class TrainPlateModel {

    public static void main(String[] args) throws Exception {
        File datasetDir = new File("../Dataset_plates");
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
                double[] vector = ImageUtils.imageToVector(file, 28, 28);
                featuresList.add(vector);

                // Heurística simples: divide o dataset em duas classes
                if (count % 2 == 0) {
                    labelsList.add(1);
                } else {
                    labelsList.add(-1);
                }
                count++;
            } catch (Exception e) {
                System.err.println("Erro ao processar arquivo: " + file.getName());
            }
        }

        double[][] X = featuresList.toArray(new double[0][]);
        int[] y = labelsList.stream().mapToInt(i -> i).toArray();

        // Treina SVM
        double C = 1.0;
        SVM<double[]> svm = SVM.fit(X, y, new LinearKernel(), C, 1e-3);

        // Salva o modelo
        File outputFile = new File("src/main/resources/model_plate.bin");
        outputFile.getParentFile().mkdirs();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputFile))) {
            oos.writeObject(svm);
        }

        System.out.println("Modelo treinado e salvo com sucesso!");
    }
}

