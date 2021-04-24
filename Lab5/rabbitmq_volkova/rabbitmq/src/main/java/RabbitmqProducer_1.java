import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RabbitmqProducer_1 {
    public final static String QUEUE_2 = "queue_2";
    private static final ConnectionFactory factory = new ConnectionFactory();

    public static void main(String[] arguments) throws Exception {
        factory.setHost("localhost");
        // Consume
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received modified message:'" + message + "'");
        };
        channel.basicConsume(RabbitmqConsumer_1.QUEUE_1, false,
                deliverCallback, consumerTag -> {});

        // Send
        Connection connection1 = factory.newConnection();
        Channel channel2 = connection1.createChannel();
        Map<String, Object> args = new HashMap<>();
        args.put("x-max-length", 100);
        args.put("x-message-ttl", 5_000);
        channel2.queueDeclare(QUEUE_2, true, false, false, args);
        send(channel2);
    }

    private static void send(Channel channel) throws Exception {
        String message = "Hello!";
        for (int i = 0; i < 100; i++) {
            channel.basicPublish("", QUEUE_2, null, (message + i).getBytes());
            System.out.println(" [x] Sent '" + (message + i) + "'");
            Thread.sleep(1000);
        }
    }
}
