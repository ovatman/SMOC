package tr.edu.itu.cloudcorelab.geocoordinator.sm.actions;

import tr.edu.itu.cloudcorelab.geocoordinator.config.ClusterConfig;
import tr.edu.itu.cloudcorelab.geocoordinator.models.Payment;
import tr.edu.itu.cloudcorelab.geocoordinator.shresource.SharedResource;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;

public class PayMoneyAction implements Action<String, String> {

    private ClusterConfig conf;
    private SharedResource resource;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Payment> collection;

    private final Log log = LogFactory.getLog(PayMoneyAction.class);

    public PayMoneyAction(ClusterConfig conf, SharedResource resource) {
        super();
        this.conf = conf;
        this.resource = resource;

        if (conf.me.equals(resource.conf.first_to_access)) {

            CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                    fromProviders(PojoCodecProvider.builder().automatic(true).build()));

            MongoClientSettings settings = MongoClientSettings.builder().codecRegistry(pojoCodecRegistry)
                    .applyConnectionString(
                            new ConnectionString("mongodb://" + resource.conf.ip_address + ':' + resource.conf.port))
                    .build();

            mongoClient = MongoClients.create(settings);

            database = mongoClient.getDatabase("payments");
            
            collection = database.getCollection("payments", Payment.class);

            consume_previous_messages();
        }
    }

    @Override
    public void execute(StateContext<String, String> context) {
        if (conf.me.equals(conf.leader) && conf.me.equals(resource.conf.first_to_access)) {
            Payment model = new Payment();
            model.cost = 61;
            log.debug("This device is leader and also owner of shared resource");
        } else if (conf.me.equals(resource.conf.first_to_access)) {
            Payment model = new Payment();
            try {
                resource.queue.pop(model);
                collection.insertOne(model);
                log.debug("Owner of Shared resouce process the payment");
            } catch (Exception e) {
                log.error("SharedResource owner can not pop the payment");
            }
        } else if (conf.me.equals(conf.leader)) {
            Payment model = new Payment();
            model.cost = 61;
            try {
                resource.queue.push(model);
                log.debug("Leader delegate the payment to near replica");
            } catch (Exception e) {
                log.error("Leader can not push the payment");
            }
        }

        log.info("Money is paid");
    }

    private void consume_previous_messages()
    {
        Payment model = new Payment();
        while(resource.queue.poll(model)){
            collection.insertOne(model);
        }
    }

}
