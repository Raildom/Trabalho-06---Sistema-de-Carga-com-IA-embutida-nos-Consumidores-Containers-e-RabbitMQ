// package com.consumer;

// import java.io.File;

// public class TestSign {
//     public static void main(String[] args) throws Exception {
//         SignModel model = new SignModel("src/main/resources/model_team.bin");

//         File time1 = new File("/home/alef/programas/IASys/consumerTeam/src/main/resources/dataset_times/terra.png");
//         File time2 = new File("/home/alef/programas/IASys/consumerTeam/src/main/resources/dataset_times/marte.png");
//         File time3 = new File("/home/alef/programas/IASys/consumerTeam/src/main/resources/dataset_times/venus.png");

//         File[] testImages = new File[]{time1, time2, time3};

//         for (File file : testImages) {
//             if (file.exists()) {
//                 System.out.println(file.getName() + " -> " + model.predict(file));
//             } else {
//                 System.err.println("Arquivo não encontrado: " + file.getAbsolutePath());
//             }
//         }
//     }
// }
