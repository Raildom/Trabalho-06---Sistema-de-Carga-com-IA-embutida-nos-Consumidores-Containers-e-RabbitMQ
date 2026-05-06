// package com.consumer;

// import java.io.File;

// public class TestPlate {
//     public static void main(String[] args) throws Exception {
//         PlateModel model = new PlateModel("src/main/resources/model_sentiment.bin");

//         File feliz1 = new File("/home/alef/programas/IASys/consumerFace/src/main/resources/dataset_sentiment/feliz1.png");
//         File triste1 = new File("/home/alef/programas/IASys/consumerFace/src/main/resources/dataset_sentiment/triste1.png");
//         File feliz2 = new File("/home/alef/programas/IASys/consumerFace/src/main/resources/dataset_sentiment/feliz2.png");
//         File triste2 = new File("/home/alef/programas/IASys/consumerFace/src/main/resources/dataset_sentiment/triste2.png");
//         File feliz3 = new File("/home/alef/programas/IASys/consumerFace/src/main/resources/dataset_sentiment/feliz3.png");
//         File triste3 = new File("/home/alef/programas/IASys/consumerFace/src/main/resources/dataset_sentiment/triste3.png");
//         File feliz4 = new File("/home/alef/programas/IASys/consumerFace/src/main/resources/dataset_sentiment/feliz4.png");
//         File triste4 = new File("/home/alef/programas/IASys/consumerFace/src/main/resources/dataset_sentiment/triste4.png");
        
//         for (File file : new File[]{feliz1, feliz2, feliz3, feliz4}) {
//             if (file.exists()) {
//                 System.out.println(file.getName() + " -> " + model.predict(file));
//             } else {
//                 System.err.println("Arquivo não encontrado: " + file.getAbsolutePath());
//             }
//         }
        

//         for (File file : new File[]{triste1, triste2, triste3, triste4}) {
//             if (file.exists()) {
//                 System.out.println(file.getName() + " -> " + model.predict(file));
//             } else {
//                 System.err.println("Arquivo não encontrado: " + file.getAbsolutePath());
//             }
//         }
        
//     }
// }
