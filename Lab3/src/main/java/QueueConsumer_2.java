import com.hazelcast.config.Config;
import com.hazelcast.config.QueueConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

public class QueueConsumer_2 {
    public static void main(String[] args) {

        HazelcastInstance instance = Hazelcast.newHazelcastInstance(getConfig());
        BlockingQueue<Integer> queue = instance.getQueue(QueueProducer.QUEUE_NAME);
        new Thread(() -> {
            for (;;) {
                try {
                    int take = queue.take();
                    System.out.println("take() " + take + " from queue");
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    System.out.println(e.toString());
                }
            }
        }).start();
    }

    private static Config getConfig() {
        Config config = new Config();
        HashMap<String, QueueConfig> configHashMap = new HashMap<>();
        QueueConfig queueConfig = new QueueConfig(QueueProducer.QUEUE_NAME);
        queueConfig.setMaxSize(10);
        configHashMap.put(QueueProducer.QUEUE_NAME, queueConfig);
        config.setQueueConfigs(configHashMap);
        config.getNetworkConfig().setPort(5703);
        return config;
    }
}
