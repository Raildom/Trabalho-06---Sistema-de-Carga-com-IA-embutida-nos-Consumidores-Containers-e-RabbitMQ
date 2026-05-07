package com.consumer;

import com.rabbitmq.client.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Random;

public class PlateConsumer {
    private final static String QUEUE_NAME = "fila_plate";

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rabbitmq");
        factory.setUsername("guest");
        factory.setPassword("guest");

        Connection connection = null;

        // Loop de retry até o RabbitMQ estar disponível
        while (connection == null) {
            try {
                connection = factory.newConnection();
            } catch (Exception e) {
                System.out.println("RabbitMQ ainda não disponível, tentando novamente em 3s...");
                Thread.sleep(3000);
            }
        }

        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);

        PlateModel model = new PlateModel("/app/src/main/resources/model_plate.bin");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            try {
                byte[] body = delivery.getBody();
                String message = new String(body);

                String[] parts = message.split(":::");
                String base64 = parts[0];
                String nomeArquivo = parts[1];

                byte[] imageBytes = Base64.getDecoder().decode(base64);

                // Converte para BufferedImage
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
                String vehicleType = model.predict(img);
                String simulatedPlateCharacters = simulateOCR(nomeArquivo);

                System.out.println("[Placa Lida: " + simulatedPlateCharacters + "] Arquivo: " + nomeArquivo + " | [Tipo do Veículo] " + vehicleType);
                
                // Requisito: Cada consumidor deve processar mais lentamente que a taxa de geração para a fila encher
                Thread.sleep(2000); 

                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }  catch (Exception e) {
                System.err.println("Erro ao processar a imagem: " + e.getMessage());
                e.printStackTrace();
            }
        };

        channel.basicQos(1);
        channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {});

        System.out.println("Consumidor pronto, aguardando mensagens na fila 'plate'...");
    }

    private static String simulateOCR(String filename) {
        Random random = new Random();
        StringBuilder letters = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            letters.append((char) ('A' + random.nextInt(26)));
        }
        int number = random.nextInt(10000);
        return String.format("%s-%04d", letters.toString(), number);
    }
}
