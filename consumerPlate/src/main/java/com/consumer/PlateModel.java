package com.consumer;

import smile.classification.Classifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.awt.image.BufferedImage;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import nu.pattern.OpenCV;

public class PlateModel {

    private Classifier<double[]> model;
    private Tesseract tesseract;

    @SuppressWarnings("unchecked")
    public PlateModel(String path) throws Exception {
        this.model = (Classifier<double[]>) loadModel(path);
        
        // Inicializa OpenCV
        nu.pattern.OpenCV.loadShared();
        
        this.tesseract = new Tesseract();
        String tessDataPath = "/usr/share/tesseract-ocr/tessdata";
        if (!new File(tessDataPath).exists()) {
            tessDataPath = "/usr/share/tesseract-ocr/5/tessdata";
        }
        this.tesseract.setDatapath(tessDataPath);
        this.tesseract.setLanguage("por");
        this.tesseract.setPageSegMode(7); // Single line
    }

    public String predict(BufferedImage imageFile) throws Exception {
        if (imageFile == null) return "Erro: Imagem nula";

        // 1. Predição do Smile (IA) para o tipo de veículo
        double[] vector = ImageUtils.imageToVector(imageFile, 28, 28);
        int label = model.predict(vector);
        
        String vehicleType = "Desconhecido";
        if (label == 0) vehicleType = "Carro";
        else if (label == 1) vehicleType = "Moto";
        else if (label == 2) vehicleType = "Caminhao";

        // 2. VISÃO COMPUTACIONAL COM OPENCV PARA A PLACA
        String plateContent = "Não identificado";
        
        if (imageFile.getWidth() >= 30 && imageFile.getHeight() >= 15) {
            try {
                // FORÇA CONVERSÃO PARA BGR (Evita o erro de "multiple of 3")
                BufferedImage bgrImage = new BufferedImage(imageFile.getWidth(), imageFile.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
                java.awt.Graphics2D g = bgrImage.createGraphics();
                g.drawImage(imageFile, 0, 0, null);
                g.dispose();

                byte[] pixels = ((java.awt.image.DataBufferByte) bgrImage.getRaster().getDataBuffer()).getData();
                org.opencv.core.Mat mat = new org.opencv.core.Mat(imageFile.getHeight(), imageFile.getWidth(), org.opencv.core.CvType.CV_8UC3);
                mat.put(0, 0, pixels);

                // A. ÁREA DE BUSCA (Focada)
                int startY = mat.rows() * 4 / 10;
                org.opencv.core.Mat searchArea = new org.opencv.core.Mat(mat, new org.opencv.core.Rect(0, startY, mat.cols(), mat.rows() - startY));

                // B. RESTAURAÇÃO FORENSE DE IMAGEM
                org.opencv.core.Mat gray = new org.opencv.core.Mat();
                org.opencv.imgproc.Imgproc.cvtColor(searchArea, gray, org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY);
                
                // 1. Black-Hat: Isola apenas o texto (preto no branco)
                org.opencv.core.Mat kernelHat = org.opencv.imgproc.Imgproc.getStructuringElement(org.opencv.imgproc.Imgproc.MORPH_RECT, new org.opencv.core.Size(15, 3));
                org.opencv.core.Mat blackHat = new org.opencv.core.Mat();
                org.opencv.imgproc.Imgproc.morphologyEx(gray, blackHat, org.opencv.imgproc.Imgproc.MORPH_BLACKHAT, kernelHat);

                // 2. FUSÃO HORIZONTAL: Faz as letras "derreterem" umas nas outras para formar uma barra
                org.opencv.core.Mat kernelMerge = org.opencv.imgproc.Imgproc.getStructuringElement(org.opencv.imgproc.Imgproc.MORPH_RECT, new org.opencv.core.Size(35, 3));
                org.opencv.core.Mat merged = new org.opencv.core.Mat();
                org.opencv.imgproc.Imgproc.dilate(blackHat, merged, kernelMerge);
                
                // 3. Binarização e Limpeza
                org.opencv.core.Mat binary = new org.opencv.core.Mat();
                org.opencv.imgproc.Imgproc.threshold(merged, binary, 0, 255, org.opencv.imgproc.Imgproc.THRESH_BINARY + org.opencv.imgproc.Imgproc.THRESH_OTSU);

                // C. LOCALIZAÇÃO DO BLOCO DE TEXTO
                java.util.List<org.opencv.core.MatOfPoint> contours = new java.util.ArrayList<>();
                org.opencv.imgproc.Imgproc.findContours(binary, contours, new org.opencv.core.Mat(), org.opencv.imgproc.Imgproc.RETR_EXTERNAL, org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE);
                
                org.opencv.core.Mat finalPlate = null;
                double maxArea = 0;

                for (org.opencv.core.MatOfPoint contour : contours) {
                    org.opencv.core.Rect rect = org.opencv.imgproc.Imgproc.boundingRect(contour);
                    double aspectRatio = (double) rect.width / rect.height;
                    double area = rect.width * rect.height;
                    
                    // Um bloco de 7 letras fundidas deve ser bem largo (aspect ratio > 2.5)
                    if (aspectRatio > 2.5 && aspectRatio < 7.0 && area > 400) {
                        if (area > maxArea) {
                            maxArea = area;
                            
                            // Recorta da imagem cinza original com uma margem de segurança
                            int padding = 5;
                            int rx = Math.max(0, rect.x - padding);
                            int ry = Math.max(0, rect.y - padding);
                            int rw = Math.min(gray.cols() - rx, rect.width + (padding * 2));
                            int rh = Math.min(gray.rows() - ry, rect.height + (padding * 2));
                            
                            try {
                                finalPlate = new org.opencv.core.Mat(gray, new org.opencv.core.Rect(rx, ry, rw, rh));
                            } catch (Exception e) {}
                        }
                    }
                }

                // D. PROCESSAMENTO FINAL DO RECORTE
                if (finalPlate != null) {
                    // Normalização de Contraste
                    org.opencv.core.Core.normalize(finalPlate, finalPlate, 0, 255, org.opencv.core.Core.NORM_MINMAX);
                    
                    org.opencv.core.Mat forOCR = new org.opencv.core.Mat();
                    org.opencv.imgproc.Imgproc.threshold(finalPlate, forOCR, 0, 255, org.opencv.imgproc.Imgproc.THRESH_BINARY + org.opencv.imgproc.Imgproc.THRESH_OTSU);
                    
                    int psm = vehicleType.equalsIgnoreCase("Moto") ? 6 : 7;
                    plateContent = tryOCR(forOCR, psm);
                } else {
                    plateContent = "Não localizada";
                }

            } catch (Exception e) {
                System.err.println("Erro na restauração: " + e.getMessage());
                plateContent = "Erro no motor";
            }
        }
        
        return vehicleType + " | Placa: [" + plateContent + "]";
    }

    private String tryOCR(org.opencv.core.Mat binary, int psm) {
        try {
            // Zoom 3.0x (Superior para imagens HD)
            org.opencv.core.Mat zoomed = new org.opencv.core.Mat();
            org.opencv.imgproc.Imgproc.resize(binary, zoomed, new org.opencv.core.Size(), 3.0, 3.0, org.opencv.imgproc.Imgproc.INTER_CUBIC);

            this.tesseract.setPageSegMode(psm);
            this.tesseract.setTessVariable("tessedit_char_whitelist", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");

            BufferedImage img = matToBufferedImage(zoomed);
            String result = tesseract.doOCR(img);
            if (result != null) {
                String clean = result.toUpperCase().replaceAll("[^A-Z0-9]", "").trim();
                if (clean.length() > 7) return clean.substring(0, 7);
                return clean;
            }
        } catch (Exception e) { }
        return "";
    }

    private BufferedImage matToBufferedImage(org.opencv.core.Mat mat) {
        int type = (mat.channels() > 1) ? BufferedImage.TYPE_3BYTE_BGR : BufferedImage.TYPE_BYTE_GRAY;
        int bufferSize = mat.channels() * mat.cols() * mat.rows();
        byte[] buffer = new byte[bufferSize];
        mat.get(0, 0, buffer);
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        final byte[] targetPixels = ((java.awt.image.DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
        return image;
    }

    public static Object loadModel(String modelPath) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(modelPath))) {
            return ois.readObject();
        }
    }
}
