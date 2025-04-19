package me.truec0der.mwhitelist.impl.repository.mongo.player;

import com.mongodb.client.MongoCollection;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.truec0der.mwhitelist.config.ConfigRegister;
import me.truec0der.mwhitelist.config.configs.MainConfig;
import me.truec0der.mwhitelist.interfaces.repository.PlayerRepository;
import me.truec0der.mwhitelist.interfaces.repository.mongo.MongoRepository;
import me.truec0der.mwhitelist.model.entity.database.PlayerEntity;
import org.bson.Document;

import java.util.*;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MongoPlayerRepositoryImpl extends PlayerRepository {
    MongoRepository mongoRepository;

    public MongoPlayerRepositoryImpl(ConfigRegister configRegister, MongoRepository mongoRepository) {
        super(configRegister);
        this.mongoRepository = mongoRepository;
    }

    @Override
    public Optional<PlayerEntity> find(UUID uuid, boolean isOnline) {
        Document query = new Document("uuid." + (isOnline ? "online" : "offline"), uuid.toString());
        Document result = getPlayerCollection().find(query).first();
        return Optional.ofNullable(result).map(PlayerEntity::fromDocument);
    }

    @Override
    public boolean isExists(UUID uuid, boolean isOnline) {
        Document query = new Document("uuid." + (isOnline ? "online" : "offline"), uuid.toString());
        return getPlayerCollection().countDocuments(query) > 0;
    }

    @Override
    public void create(String nickname, UUID offlineUuid, UUID onlineUuid) {
        PlayerEntity playerEntity = PlayerEntity.builder()
                .uuid(new PlayerEntity.PlayerUuid(offlineUuid, onlineUuid == null ? offlineUuid : onlineUuid))
                .info(new PlayerEntity.PlayerInfo(List.of(nickname), new Date().getTime()))
                .time(-1L)
                .build();

        getPlayerCollection().insertOne(playerEntity.toDocument());
    }

    @Override
    public void remove(UUID uuid, boolean isOnline) {
        Document query = new Document("uuid." + (isOnline ? "online" : "offline"), uuid.toString());
        getPlayerCollection().deleteOne(query);
    }

    @Override
    public List<PlayerEntity> find() {
        List<PlayerEntity> players = new ArrayList<>();
        for (Document document : getPlayerCollection().find()) {
            players.add(PlayerEntity.fromDocument(document));
        }
        return players;
    }

    @Override
    public void setTime(UUID uuid, boolean isOnline, long time) {
        Document query = new Document("uuid." + (isOnline ? "online" : "offline"), uuid.toString());
        Document update = new Document("$set", new Document("time", time));
        getPlayerCollection().updateOne(query, update);
    }

    private MongoCollection<Document> getPlayerCollection() {
        MainConfig mainConfig = getConfigRegister().getMainConfig();
        return mongoRepository.getCollection(mainConfig.getDatabase().getMongodb().getCollections().getUsers());
    }
}
