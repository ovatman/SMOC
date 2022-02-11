package tr.edu.itu.cloudcorelab.checkpointing.ipc;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Value("${EVENT_QUEUE_SMOC2}")
    private String EVENT_QUEUE_SMOC2;

    @Value("${EVENT_EXCHANGE_SMOC2}")
    private String EVENT_EXCHANGE_SMOC2;

    /* Add queues and events for other smocs */

    @Bean
    Queue smoc2Queue() {
        return new Queue(EVENT_QUEUE_SMOC2, false);
    }

    @Bean
    DirectExchange smoc2Exchange() {
        return new DirectExchange(EVENT_EXCHANGE_SMOC2);
    }

    @Bean
    Binding binding(Queue smoc2Queue, DirectExchange smoc2Exchange) {
        return BindingBuilder.bind(smoc2Queue).to(smoc2Exchange).with("rpc");
    }

}
