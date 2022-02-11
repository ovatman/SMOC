package tr.edu.itu.cloudcorelab.geocoordinator.config;

import java.util.ArrayList;
import java.util.List;

public class SharedResourceConf {
    
    public String alias;

    public String ip_address;

    public int port;

    public ClusterNode first_to_access;

    public List<ClusterNode> nodes;

    public SharedResourceConf(String alias, String ip_address, int port) {
        super();
        this.alias = alias;
        this.ip_address = ip_address;
        this.port = port;

        nodes = new ArrayList<ClusterNode>();
    }

    public SharedResourceConf() {
        super();
        nodes = new ArrayList<ClusterNode>();
    }
}
