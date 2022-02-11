package tr.edu.itu.cloudcorelab.geocoordinator.config;

import java.util.ArrayList;
import java.util.List;

public class ClusterConfig {
    
    public ZookeeperConfig zookeeper;
    
    public ClusterNode me;

    public ClusterNode leader;

    public List<ClusterNode> nodes;

    public List<SharedResourceConf> resources;

    public ClusterConfig() {
        super();

        nodes = new ArrayList<ClusterNode>();
        resources = new ArrayList<SharedResourceConf>();
    }
}
