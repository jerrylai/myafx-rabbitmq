package cn.myafx.rabbitmq;

import java.util.ArrayList;
import java.util.List;

import com.rabbitmq.client.BasicProperties;

/**
 * Hello world!
 *
 */
public class AppTest {
    public static void main(String[] args) throws Exception {
        var url = AppTest.class.getClassLoader().getResource("mq-config.xml");
        try (var config = new MQConfig(url)) {

            var es = config.getExchanges();
            var qs = config.getQueues();

            try (var pool = new MQPool("192.168.2.231", 5672, "admin", "admin", "/", 2,
                    15, "test",
                    new IJsonMapper() {
                        @Override
                        public <T> String serialize(T m) throws Exception {
                            return JsonUtils.serialize(m);
                        }

                        @Override
                        public <T> T deserialize(String json, Class<T> clazz) throws Exception {
                            return JsonUtils.deserialize(json, clazz);
                        }
                    })) {
                pool.exchangeDeclare(es);
                pool.queueDeclare(qs);
                var sc = config.getSubConfig("TestDto");
                pool.sub(new ISubHander<String>() {

                    @Override
                    public boolean hander(String m, BasicProperties properties) throws Exception {
                        System.out.println("sub: " + m);
                        return true;
                    }

                }, String.class, sc.Queue, false);

                var pc = config.getPubConfig("TestDto");
                pool.pubDelay("333", pc, 5, null, null);
                pool.pub("ssdddds", pc, null, null, null);
                pool.pub("222", pc, null, null, null);
                List<String> mlist = new ArrayList<>();
                mlist.add("221344");
                mlist.add("efd3");
                mlist.add("d45g");
                pool.pub(mlist, pc, null, null, null);
                System.out.println("11111");
                System.in.read();
            }
        }
        System.out.print("end...............");
    }
}
