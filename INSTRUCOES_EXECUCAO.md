# Guia de Execução: Sistema de Carga com IA e RabbitMQ

Este guia descreve os passos necessários para treinar os modelos de Inteligência Artificial e executar o sistema distribuído usando Docker.

## 1. Preparação dos Datasets
Certifique-se de que as imagens de teste estão localizadas na raiz do projeto:
*   `./Dataset_plates/` -> Deve conter as imagens de veículos/placas.
*   `./Dataset_signs/` -> Deve conter as imagens de sinais de trânsito.

## 2. Treinamento dos Modelos (IA)
Para que os consumidores identifiquem os padrões nas fotos, é necessário treinar os modelos. Como o Maven já está instalado, execute os seguintes comandos no terminal:

### Treinar Consumidor de Placas:
```bash
cd consumerPlate
mvn compile exec:java -Dexec.mainClass="com.consumer.TrainPlateModel"
cd ..
```

### Treinar Consumidor de Sinais:
```bash
cd consumerSign
mvn compile exec:java -Dexec.mainClass="com.consumer.TrainSignModel"
cd ..
```
> [!NOTE]
> Isso gerará os arquivos `model_plate.bin` e `model_sign.bin` dentro das pastas de recursos.

---

## 3. Execução do Sistema (Docker)
Com os modelos treinados, agora podemos subir a infraestrutura completa (RabbitMQ + Gerador + Consumidores).

### Comando para iniciar:
```bash
docker-compose up --build
```

---

## 4. Monitoramento e Testes

### Logs do Sistema:
*   **Gerador:** Verá mensagens sendo enviadas com base nas imagens dos datasets.
*   **Consumidores:** Verá a identificação em tempo real (ex: `[Tipo do Veículo] Caminhão`).

### Painel do RabbitMQ:
1.  Acesse: [http://localhost:15672](http://localhost:15672)
2.  Login/Senha: `guest` / `guest`
3.  Vá em **Queues** para observar as filas `fila_plate` e `fila_sign` enchendo.

---

## 5. Finalização
Para parar o sistema e limpar os recursos do Docker:
```bash
# Pressione Ctrl+C para parar os logs e depois:
docker-compose down
```
