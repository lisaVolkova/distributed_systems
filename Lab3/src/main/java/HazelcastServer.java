import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import java.io.Serializable;
import java.util.Map;

public class HazelcastServer {
    public static final String MAP_FIRST = "map_first";
    public static final String MAP_SECOND_NO_LOCK = "map_second";
    public static final String MAP_SECOND_OPT_LOCK = "map_third";
    public static final String MAP_SECOND_PES_LOCK = "map_fourth";

    public static final String MAP_KEY = "key";
    private static HazelcastInstance[] instances = {
            Hazelcast.newHazelcastInstance(getConfig(5701, "Instance1")),
            Hazelcast.newHazelcastInstance(getConfig(5702, "Instance2")),
            Hazelcast.newHazelcastInstance(getConfig(5703, "Instance3"))
    };
    private static final HazelcastInstance[] clients = {
            HazelcastClient.newHazelcastClient(),
            HazelcastClient.newHazelcastClient(),
            HazelcastClient.newHazelcastClient()
    };

    public static void main(String[] args) {

        HazelcastInstance client_firstExample = HazelcastClient.newHazelcastClient();

        Map<Integer, String> map1 = client_firstExample.getMap(MAP_FIRST);
        for (int i = 0; i < 1000; i++) {
            map1.put(i, MAP_KEY);
        }


        for (HazelcastInstance client : clients) {
            new Thread(() -> {
                Map<String, Integer> map = client.getMap(MAP_SECOND_NO_LOCK);
                map.putIfAbsent(MAP_KEY, 0);
                for (int i = 0; i < 1000; i++) {
                    int k = map.get(MAP_KEY);
                    map.put(MAP_KEY, ++k);
                }
                System.out.println("no_lock=" + client.getMap(MAP_SECOND_NO_LOCK).get(MAP_KEY));
            }).start();
            new Thread(() -> {
                IMap<String, Integer> map = client.getMap(MAP_SECOND_PES_LOCK);
                map.putIfAbsent(MAP_KEY, 0);
                for (int i = 0; i < 1000; i++) {
                    map.lock(MAP_KEY);
                    try {
                        int k = map.get(MAP_KEY);
                        Thread.sleep( 10 );
                        map.put(MAP_KEY, ++k);
                    } catch (InterruptedException ignored) {
                    } finally {
                        map.unlock(MAP_KEY);
                    }
                }
                System.out.println("pes_lock=" + client.getMap(MAP_SECOND_PES_LOCK).get(MAP_KEY));
            }).start();
            new Thread(() -> {
                IMap<String, Value> map = client.getMap(MAP_SECOND_OPT_LOCK);
                map.putIfAbsent(MAP_KEY, new Value());
                for (int i = 0; i < 1000; i++) {
                    try {
                        for (;;) {
                            Value oldValue = map.get(MAP_KEY);
                            Value newValue = new Value(oldValue);
                            Thread.sleep( 10 );
                            newValue.amount++;
                            if (map.replace(MAP_KEY, oldValue, newValue))
                                break;
                        }
                    } catch (InterruptedException ignored) {
                    }
                }
                System.out.println("opt_lock=" + client.getMap(MAP_SECOND_OPT_LOCK).get(MAP_KEY));
            }).start();
        }
    }

    private static Config getConfig(int port, String instanceName) {
        Config config = new Config(instanceName);
        config.getNetworkConfig().setPort(port);
        return config;
    }

    static class Value implements Serializable {
        private int amount;

        public Value() {
        }

        public Value(Value that) {
            this.amount = that.amount;
        }

        public boolean equals( Object o ) {
            if ( o == this ) return true;
            if ( !( o instanceof Value) ) return false;
            Value that = (Value) o;
            return that.amount == this.amount;
        }

        @Override
        public String toString() {
            return "Value{" +
                    "amount=" + amount +
                    '}';
        }
    }
}
