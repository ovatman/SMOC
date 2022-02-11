package tr.edu.itu.cloudcorelab.geocoordinator.sm.actions;

import tr.edu.itu.cloudcorelab.geocoordinator.config.ClusterConfig;
import tr.edu.itu.cloudcorelab.geocoordinator.models.RoomReservation;
import tr.edu.itu.cloudcorelab.geocoordinator.shresource.SharedResource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;

public class ReserveRoomAction implements Action<String, String> {

    private final Log log = LogFactory.getLog(ReserveRoomAction.class);
    private ClusterConfig conf;
    private SharedResource resource; 
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<RoomReservation> collection;

    public ReserveRoomAction(ClusterConfig conf, SharedResource resource) {
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

            database = mongoClient.getDatabase("reservations");
            
            collection = database.getCollection("reservations", RoomReservation.class);

            consume_previous_messages();
        }
    }

    @Override
    public void execute(StateContext<String, String> context) {
        if (conf.me.equals(conf.leader) && conf.me.equals(resource.conf.first_to_access)) {
            RoomReservation model = new RoomReservation();
            model.room_id = 14;
            log.debug("This device is leader and also owner of shared resource");
        } else if (conf.me.equals(resource.conf.first_to_access)) {
            RoomReservation model = new RoomReservation();
            try {
                resource.queue.pop(model);
                collection.insertOne(model);
                log.debug("Owner of Shared resource process the room reservation");
            } catch (Exception e) {
                log.error("SharedResource owner can not pop the room reservation");
            }
        } else if (conf.me.equals(conf.leader)) {
            RoomReservation model = new RoomReservation();
            model.room_id = 14;
            try {
                resource.queue.push(model);
                log.debug("Leader delegate the room reservation to near replica");
            } catch (Exception e) {
                log.error("Leader can not push the room reservation");
            }
        }

        log.info("Room is reserved");
    }

    private void consume_previous_messages()
    {
        RoomReservation model = new RoomReservation();
        while(resource.queue.poll(model)){
            collection.insertOne(model);
        }
    }
}
