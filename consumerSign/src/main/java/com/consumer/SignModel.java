package com.consumer;

import smile.classification.Classifier;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.awt.image.BufferedImage;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class SignModel {
    private Classifier<double[]> classifier;
    private Tesseract tesseract;

    public SignModel(String modelPath) {
        try {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(modelPath))) {
                classifier = (Classifier<double[]>) ois.readObject();
            }
            this.tesseract = new Tesseract();
            String tessDataPath = "/usr/share/tesseract-ocr/tessdata";
            if (!new File(tessDataPath).exists()) {
                tessDataPath = "/usr/share/tesseract-ocr/5/tessdata";
            }
            this.tesseract.setDatapath(tessDataPath);
            this.tesseract.setLanguage("por+eng");
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar modelo: " + e.getMessage(), e);
        }
    }

    public String predict(BufferedImage imageFile) throws Exception {
        if (imageFile == null) return "Erro: Imagem nula";

        // 1. Predição do Smile (IA)
        double[] features = ImageUtils.imageToVector(imageFile, 28, 28);
        int label = classifier.predict(features);

        // Categorias: Pare=0, Proibido Direita=1, Placa 50=2
        String[] signs = {"Pare", "Proibido Direita", "Placa 50"};
        String signType = "Desconhecido";
        if (label >= 0 && label < signs.length) {
            signType = signs[label];
        }

        // 2. OCR para detalhes (ex: valor da velocidade)
        String signContent = "";
        
        // Só tenta OCR se a imagem tiver um tamanho razoável
        if (imageFile.getWidth() > 10 && imageFile.getHeight() > 10) {
            try {
                // Redimensiona imagem para ser maior (3x) de forma estável
                int scale = 3;
                int newW = imageFile.getWidth() * scale;
                int newH = imageFile.getHeight() * scale;
                
                BufferedImage biggerImg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
                java.awt.Graphics2D g2d = biggerImg.createGraphics();
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2d.drawImage(imageFile, 0, 0, newW, newH, null);
                g2d.dispose();

                String result = tesseract.doOCR(biggerImg);
                if (result != null) {
                    // Filtra ruído: mantém apenas letras e números
                    signContent = result.replaceAll("[^A-Za-z0-9]", "").trim();
                    
                    // Se for muito longo, provavelmente é ruído, tenta pegar só os números
                    if (signContent.length() > 6) {
                        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\d+").matcher(signContent);
                        if (m.find()) signContent = m.group();
                        else signContent = "";
                    }
                }

                if (!signContent.isEmpty()) {
                    signContent = " | Conteúdo: " + signContent;
                }
            } catch (Exception e) {
                // Ignora erros de OCR para sinais
            }
        }

        return signType + signContent;
    }

    public static Object loadModel(String modelPath) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(modelPath))) {
            return ois.readObject();
        }
    }
}
