package me.truec0der.mwhitelist.impl.repository.mongo.player;

import com.mongodb.client.MongoCollection;
import lombok.AccessLevel;
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
    public Optional<PlayerEntity> find(UUID uuid) {
        Document query = new Document("uuid", uuid.toString());
        Document result = getPlayerCollection().find(query).first();
        return Optional.ofNullable(result).map(PlayerEntity::fromDocument);
    }

    @Override
    public boolean isExists(UUID uuid) {
        Document query = new Document("uuid", uuid.toString());
        return getPlayerCollection().countDocuments(query) > 0;
    }

    @Override
    public void create(String nickname, UUID uuid) {
        PlayerEntity playerEntity = PlayerEntity.builder()
                .uuid(uuid)
                .info(new PlayerEntity.PlayerInfo(List.of(nickname), new Date().getTime()))
                .time(-1L)
                .build();

        getPlayerCollection().insertOne(playerEntity.toDocument());
    }

    @Override
    public void create(UUID uuid) {
        PlayerEntity playerEntity = PlayerEntity.builder()
                .uuid(uuid)
                .info(new PlayerEntity.PlayerInfo(List.of(), new Date().getTime()))
                .time(-1L)
                .build();

        getPlayerCollection().insertOne(playerEntity.toDocument());
    }

    @Override
    public void remove(UUID uuid) {
        Document query = new Document("uuid", uuid.toString());
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
    public void setTime(UUID uuid, long time) {
        Document query = new Document("uuid", uuid.toString());
        Document update = new Document("$set", new Document("time", time));
        getPlayerCollection().updateOne(query, update);
    }

    @Override
    public void updateNickname(UUID uuid, String name) {
        Optional<PlayerEntity> optionalFindPlayer = find(uuid);
        optionalFindPlayer.ifPresent(findPlayer -> {
            List<String> nicknameHistory = findPlayer.getInfo().getNicknameHistory();
            if (!nicknameHistory.isEmpty() && nicknameHistory.get(nicknameHistory.size() - 1).equals(name)) return;

            List<String> updatedHistory = new ArrayList<>(nicknameHistory);
            updatedHistory.add(name);

            Document query = new Document("uuid", uuid.toString());
            Document infoUpdate = new Document("nicknameHistory", updatedHistory).append("lastUpdate", new Date().getTime());
            Document update = new Document("$set", new Document("info", infoUpdate));

            getPlayerCollection().updateOne(query, update);
        });
    }

    private MongoCollection<Document> getPlayerCollection() {
        MainConfig mainConfig = getConfigRegister().getMainConfig();
        return mongoRepository.getCollection(mainConfig.getDatabase().getMongodb().getCollections().getUsers());
    }
}
