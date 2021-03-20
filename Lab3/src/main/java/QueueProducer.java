import com.hazelcast.client.HazelcastClient;
import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;

public class QueueProducer {
    public static final String QUEUE_NAME = "queue";
    public static void main(String[] args) {
        HazelcastInstance client = HazelcastClient.newHazelcastClient();
        IQueue<Integer> queue = client.getQueue(QUEUE_NAME);
        System.out.println("Size " + queue.size());
        new Thread(() -> {
            while (true){
                try {
                    queue.put((int) (System.currentTimeMillis() / 1000));
                    Thread.sleep(1000);
                    System.out.println("Size " + queue.size());
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        }).start();
    }
}
