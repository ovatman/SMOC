package tr.edu.itu.cloudcorelab.geocoordinator.shresource;

import tr.edu.itu.cloudcorelab.geocoordinator.config.SharedResourceConf;
import tr.edu.itu.cloudcorelab.geocoordinator.queue.DistributedQueue;

public class SharedResource {
    
    public DistributedQueue queue;

    public SharedResourceConf conf;

    public SharedResource(SharedResourceConf conf, DistributedQueue queue) {
        super();
        this.conf = conf;
        this.queue = queue;
    }

    public SharedResource(SharedResourceConf conf) {
        super();
        this.conf = conf;
    }

}
