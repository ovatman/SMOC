package tr.edu.itu.cloudcorelab.cachemanager.comm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import tr.edu.itu.cloudcorelab.cachemanager.utils.*;

@Service
public class RabbitMQSender {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQSender.class);
    
	@Autowired
	private AmqpTemplate amqpTemplate;
 
	public void convertAndSend(CheckpointMessage msg) {
		amqpTemplate.convertAndSend(Utils.EXCHANGE, Utils.ROUTE_KEY, msg);
        logger.info("#**# Exhange:{} Route:{} send Message:{} ",Utils.EXCHANGE, Utils.ROUTE_KEY, msg.toString());
	}
	public void convertAndSend_2(CheckpointMessage msg) {
		amqpTemplate.convertAndSend(Utils.EXCHANGE_2, Utils.ROUTE_KEY_2, msg);
        logger.info("#**# Exhange:{} Route:{} send Message:{} ",Utils.EXCHANGE_2, Utils.ROUTE_KEY_2, msg.toString());
	}
	public void convertAndSend_4(CheckpointMessage msg) {
		amqpTemplate.convertAndSend(Utils.EXCHANGE_4, Utils.ROUTE_KEY_4, msg);
        logger.info("#**# Exhange:{} Route:{} send Message:{} ",Utils.EXCHANGE_4, Utils.ROUTE_KEY_4, msg.toString());
	}
	public void convertAndSend_5(CheckpointMessage msg) {
		amqpTemplate.convertAndSend(Utils.EXCHANGE_5, Utils.ROUTE_KEY_5, msg);
        logger.info("#**# Exhange:{} Route:{} send Message:{} ",Utils.EXCHANGE_5, Utils.ROUTE_KEY_5, msg.toString());
	}
}