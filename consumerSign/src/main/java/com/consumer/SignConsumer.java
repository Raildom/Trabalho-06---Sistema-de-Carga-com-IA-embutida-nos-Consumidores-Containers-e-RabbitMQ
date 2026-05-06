package com.consumer;

import com.rabbitmq.client.*;
import java.nio.charset.StandardCharsets;
import java.io.File;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;


public class SignConsumer {
    private final static String QUEUE_NAME = "fila_sign";

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

        SignModel model = new SignModel("/app/src/main/resources/model_sign.bin");

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
                String prediction = model.predict(img);
                System.out.println("[Arquivo] " + nomeArquivo + " | " + "[Sinal Detectado] " + prediction);
                
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

        System.out.println("Consumidor pronto, aguardando mensagens na fila 'sign'...");
    }
}
