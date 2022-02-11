package tr.edu.itu.cloudcorelab.geocoordinator.queue;

import tr.edu.itu.cloudcorelab.geocoordinator.config.ZookeeperConfig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.queue.SimpleDistributedQueue;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class DistributedQueue {

    private SimpleDistributedQueue m_queue;
    private ZookeeperConfig conf;
    private final Log log = LogFactory.getLog(DistributedQueue.class);

    public DistributedQueue(ZookeeperConfig conf, String name) throws Exception {
        super();
        this.conf = conf;
        m_queue = new SimpleDistributedQueue(curatorClient(), "/" + name);
        log.trace("Distributeed is created");
    }

    private CuratorFramework curatorClient() throws Exception {
        String connect_str = conf.ip_address + ":" + String.valueOf(conf.port);
        log.trace("Connection String : " + connect_str);
        CuratorFramework client = CuratorFrameworkFactory.builder().defaultData(new byte[0])
                .retryPolicy(new ExponentialBackoffRetry(1000, 3)).connectString(connect_str).build();
        log.trace("Client is created");
        client.start();
        log.trace("Client is started");
        return client;
    }

    public void push(IElement element) throws Exception {
        m_queue.offer(element.toByte());
    }

    public void pop(IElement element) throws Exception {
        element.fromByte(m_queue.take());

    }

    public Boolean poll(IElement element) {

        try {
            byte[] data = m_queue.poll();
            element.fromByte(data);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
