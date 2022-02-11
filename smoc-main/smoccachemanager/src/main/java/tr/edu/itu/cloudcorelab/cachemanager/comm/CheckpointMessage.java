package tr.edu.itu.cloudcorelab.cachemanager.comm;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import tr.edu.itu.cloudcorelab.cachemanager.utils.*;

public class CheckpointMessage implements Serializable {

    public String ipAddr;
    public String hostname;
    public UUID smocUuid;
    Queue<EventDirections> m_directionQueue  = new LinkedList<>();

    public CheckpointMessage(){}

    public CheckpointMessage(String ipAddr, String hostname,  UUID smocUuid){
    	this.ipAddr = ipAddr;
    	this.hostname = hostname;
        this.smocUuid = smocUuid;
    }

    public CheckpointMessage(String ipAddr, String hostname){
    	this.ipAddr = ipAddr;
    	this.hostname = hostname;
    }

    public CheckpointMessage(String ipAddr, String hostname, Queue<EventDirections> qdirectionQueue){
    	this.ipAddr = ipAddr;
    	this.hostname = hostname;
    	this.m_directionQueue = qdirectionQueue;
    }
    
    public String toString() {
        return "ip:[" + this.ipAddr 
        		+ "]  host:[" +this.hostname 
        		+ "]  m_directionQueue:[" +Utils.printDirectionQueue(this.m_directionQueue) 
        		+ "]";
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public UUID getSmocUuid() {
        return smocUuid;
    }

    public void setSmocUuid(UUID smocUuid) {
        this.smocUuid = smocUuid;
    }

	public Queue<EventDirections> getDirectionQueue() {
		return m_directionQueue;
	}

	public void setDirectionQueue(Queue<EventDirections> directionQueue) {
		this.m_directionQueue = directionQueue;
	}
    
    
}