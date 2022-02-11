package tr.edu.itu.cloudcorelab.geocoordinator.sm;

import org.springframework.context.annotation.Configuration;

import tr.edu.itu.cloudcorelab.geocoordinator.config.ClusterConfig;
import tr.edu.itu.cloudcorelab.geocoordinator.queue.DistributedQueue;
import tr.edu.itu.cloudcorelab.geocoordinator.shresource.SharedResource;
import tr.edu.itu.cloudcorelab.geocoordinator.sm.actions.PayMoneyAction;
import tr.edu.itu.cloudcorelab.geocoordinator.sm.actions.ReserveRoomAction;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.context.annotation.Bean;

import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.ensemble.StateMachineEnsemble;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

@Configuration
@EnableStateMachine
public class StateMachineConfig extends StateMachineConfigurerAdapter<String, String> {

    public static ClusterConfig conf;
    private SharedResource shared_resource_payment;
    private SharedResource shared_resource_reservation;

    public StateMachineConfig() throws Exception {
        shared_resource_payment = new SharedResource(conf.resources.get(0));
        shared_resource_reservation = new SharedResource(conf.resources.get(0));
        shared_resource_payment.queue = new DistributedQueue(conf.zookeeper ,"payment");
        shared_resource_reservation.queue = new DistributedQueue(conf.zookeeper, "room_reservation");
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<String, String> config) throws Exception {

        StateMachineListenerAdapter<String, String> adapter = new StateMachineListenerAdapter<String, String>() {
            @Override
            public void stateChanged(State<String, String> from, State<String, String> to) {
                System.console().printf("\n State Changing from: " + from.toString() + " to: " + to.toString() + " \n");
                super.stateChanged(from, to);
            }
        };

        config.withDistributed().ensemble(stateMachineEnsemble()).and().withConfiguration().listener(adapter);
    }

    @Override
    public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
        states.withStates().initial("INITIAL").state("RESERVATION_REACHED").state("PAID")
        .state("RESERVED").state("CANCELLED");
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
        transitions.withExternal().
        source("INITIAL").target("RESERVATION_REACHED").event("MAKE_RESERVATION").and().withExternal().
        source("RESERVATION_REACHED").target("PAID").event("PAY_MONEY").action(new PayMoneyAction(conf, shared_resource_payment)).and().withExternal().
        source("RESERVATION_REACHED").target("CANCELLED").event("CANCEL").and().withExternal().
        source("PAID").target("RESERVED").event("TAKE_ROOM").action(new ReserveRoomAction(conf, shared_resource_reservation)).and().withExternal().
        source("PAID").target("CANCELLED").event("CANCEL").and().withExternal().
        source("RESERVED").target("CANCELLED").event("CANCEL").and().withExternal().
        source("RESERVED").target("RESERVATION_REACHED").event("MAKE_RESERVATION").and().withExternal().
        source("CANCELLED").target("RESERVATION_REACHED").event("MAKE_RESERVATION");
    }

    @Bean
    public StateMachineEnsemble<String, String> stateMachineEnsemble() throws Exception {
        return new SpecialZookeeperStateMachineEnsemble <String, String>(curatorClient(), "/stateMachineEnsemble");
    }

    @Bean
    public CuratorFramework curatorClient() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.builder().defaultData(new byte[0])
                .retryPolicy(new ExponentialBackoffRetry(1000, 3)).connectString("localhost:2181").build();
        client.start();
        return client;
    }

}
