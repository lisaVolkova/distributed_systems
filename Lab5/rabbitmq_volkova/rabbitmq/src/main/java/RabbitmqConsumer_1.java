import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


public class RabbitmqConsumer_1 {

    private static Channel channel;
    public final static String QUEUE_1 = "queue_1";

    public static void main(String[] arguments) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        Connection connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDelete(QUEUE_1);
        channel.queueDeclare(QUEUE_1, true, false, false, null);

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
                processMessage(message);
                System.out.println(" [x] Sending ack");
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };
        channel.basicConsume(RabbitmqProducer_1.QUEUE_2, false,
                deliverCallback, consumerTag -> { });
    }

    private static void processMessage(String message) throws IOException {
        message = message.substring(message.length() / 2);
        channel.basicPublish("", QUEUE_1, null, message.getBytes());
        System.out.println(" [x] Sent '" + message);
    }
}
