package tr.edu.itu.cloudcorelab.geocoordinator.config;

public class ClusterNode {

    public int id;

    public String alias;

    public int priority;

    public ClusterNode(int id, String alias, int priority) {
        this.id = id;
        this.alias = alias;
        this.priority = priority;
    }

    public ClusterNode() {
        super();
    }

    @Override
    public String toString() {
        return "Cluster Node Id: " + id + " Alias: " + alias + " Priority: " + priority + "\n";
    }

    @Override
    public boolean equals(Object obj) {
        ClusterNode node = (ClusterNode) obj;

        if (node.id == this.id)
            return true;
        return false;
    }
}
