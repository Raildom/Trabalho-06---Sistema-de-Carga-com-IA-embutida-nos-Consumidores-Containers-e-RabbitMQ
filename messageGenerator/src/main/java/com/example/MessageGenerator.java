package com.example;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.File;
import java.util.Random;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class MessageGenerator {
    private static final Random random = new Random();
    private static File[] plates;
    private static File[] signs;

    public static void main(String[] args) throws Exception {
        // Conectar no RabbitMQ
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rabbitmq"); // nome do container RabbitMQ no docker-compose
        factory.setUsername("guest");
        factory.setPassword("guest");

        while(true) {
            try (Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()) {
                String exchangeName = "images";
                channel.exchangeDeclare(exchangeName, "topic");

                // Cria filas
                channel.queueDeclare("fila_plate", true, false, false, null);
                channel.queueDeclare("fila_sign", true, false, false, null);

                // Faz o bind da fila com a exchange e routing key
                channel.queueBind("fila_plate", exchangeName, "plate");
                channel.queueBind("fila_sign", exchangeName, "sign");

                int messagesPerSecond = 5; // Requisito: 5 mensagens por segundo ou mais
                long delay = 1000 / messagesPerSecond;

                while (true) {
                    plates = getAllFiles(new File("/app/images/plates"));
                    signs = getAllFiles(new File("/app/images/signs"));

                    boolean isPlate = random.nextBoolean();
                    String message;
                    String routingKey;

                    if (isPlate) {
                        routingKey = "plate";
                        message = generateMessage(plates, "mock_plate.png");
                    } else {
                        routingKey = "sign";
                        message = generateMessage(signs, "mock_sign.png");
                    }

                    channel.basicPublish(exchangeName, routingKey, null, message.getBytes("UTF-8"));

                    Thread.sleep(delay);
                }
            } catch (Exception e) {
                System.out.println("RabbitMQ não disponível ou erro: " + e.getMessage());
                Thread.sleep(3000);
            }
        }
    }

    private static File[] getAllFiles(File dir) {
        List<File> fileList = new ArrayList<>();
        if (dir.exists() && dir.isDirectory()) {
            for (File sub : dir.listFiles()) {
                if (sub.isDirectory()) {
                    File[] subFiles = sub.listFiles((d, name) -> name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png"));
                    if (subFiles != null) {
                        for (File f : subFiles) fileList.add(f);
                    }
                } else if (sub.getName().toLowerCase().endsWith(".jpg") || sub.getName().toLowerCase().endsWith(".png")) {
                    fileList.add(sub);
                }
            }
        }
        return fileList.toArray(new File[0]);
    }

    private static String generateMessage(File[] files, String mockName) throws Exception {
        if (files == null || files.length == 0) {
            System.out.println("Aviso: Diretório vazio ou sem imagens nas subpastas. Gerando mensagem mock para " + mockName);
            return "mock_base64_data:::" + mockName;
        }

        File imagemArquivo = files[random.nextInt(files.length)];
        String nomeArquivo = imagemArquivo.getName();
        String categoria = imagemArquivo.getParentFile().getName();
        System.out.println("Imagem: " + categoria + "/" + nomeArquivo + " | Timestamp: " + System.currentTimeMillis());

        byte[] bytes = Files.readAllBytes(imagemArquivo.toPath());
        String base64 = Base64.getEncoder().encodeToString(bytes);

        return base64 + ":::" + nomeArquivo;
    }
}
