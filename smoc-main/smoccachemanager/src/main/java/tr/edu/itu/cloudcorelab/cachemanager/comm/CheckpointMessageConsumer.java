package tr.edu.itu.cloudcorelab.cachemanager.comm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import tr.edu.itu.cloudcorelab.cachemanager.db.DBService;

public class CheckpointMessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(CheckpointMessageConsumer.class);


    @Autowired
    private DBService dBService;
    
    public void onCheckpointMessage(final CheckpointMessage message) {
        logger.info("#**# onCheckpointMessage Message:{} ", message.toString());
        dBService.setDirectionQueue(message.getDirectionQueue());
    }
}
