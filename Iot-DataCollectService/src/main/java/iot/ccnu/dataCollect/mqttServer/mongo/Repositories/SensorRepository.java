package iot.ccnu.dataCollect.mqttServer.mongo.Repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorRepository extends ReactiveMongoRepository {
}
