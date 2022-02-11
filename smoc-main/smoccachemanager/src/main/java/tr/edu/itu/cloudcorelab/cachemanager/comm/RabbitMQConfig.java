package tr.edu.itu.cloudcorelab.cachemanager.comm;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tr.edu.itu.cloudcorelab.cachemanager.utils.*;

@Configuration
public class RabbitMQConfig {
	@Bean
	Queue queue() {
		return new Queue(Utils.QUEUE, true);
	}

	@Bean
	DirectExchange exchange() {
		return new DirectExchange(Utils.EXCHANGE);
	}

    @Bean
    Binding binding(final Queue queue, final Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(Utils.ROUTE_KEY).noargs();
    }

/*
    @Bean
    CheckpointMessageConsumer checkpointMessageConsumer(){
        return new CheckpointMessageConsumer();
    }

    @Bean
    MessageListenerAdapter messageListenerAdapter(CheckpointMessageConsumer checkpointMessageConsumer){
        return new MessageListenerAdapter(checkpointMessageConsumer, "onCheckpointMessage");
    }

    @Bean
    SimpleMessageListenerContainer simpleMessageListenerContainer(final ConnectionFactory connectionFactory,
                                                                         final MessageListenerAdapter messageListenerAdapter){
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(Utils.QUEUE);
        container.setMessageListener(messageListenerAdapter);
        return container;
    }
*/
}
