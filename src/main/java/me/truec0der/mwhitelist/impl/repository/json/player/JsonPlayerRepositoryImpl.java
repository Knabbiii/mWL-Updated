package me.truec0der.mwhitelist.impl.repository.json.player;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import me.truec0der.mwhitelist.config.ConfigRegister;
import me.truec0der.mwhitelist.interfaces.repository.PlayerRepository;
import me.truec0der.mwhitelist.interfaces.repository.json.JsonRepository;
import me.truec0der.mwhitelist.model.entity.database.PlayerEntity;

import java.util.*;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JsonPlayerRepositoryImpl extends PlayerRepository {
    JsonRepository jsonRepository;

    public JsonPlayerRepositoryImpl(ConfigRegister configRegister, JsonRepository jsonRepository) {
        super(configRegister);
        this.jsonRepository = jsonRepository;
    }

    private List<JsonObject> findJsonObject() {
        JsonArray database = jsonRepository.getDatabase();

        List<JsonObject> findPlayers = new ArrayList<>();

        for (JsonElement jsonElement : database) {
            if (!jsonElement.isJsonObject()) continue;

            JsonObject jsonObject = jsonElement.getAsJsonObject();
            PlayerEntity playerEntity = PlayerEntity.toEntity(jsonObject);

            if (playerEntity == null) continue;

            findPlayers.add(jsonObject);
        }

        return findPlayers;
    }

    private JsonObject findJsonObject(UUID uuid, boolean isOnline) {
        List<JsonObject> jsonObjects = findJsonObject();

        return jsonObjects.stream()
                .filter(jsonObject -> {
                    PlayerEntity playerEntity = PlayerEntity.toEntity(jsonObject);
                    PlayerEntity.PlayerUuid playerUuid = playerEntity.getUuid();
                    return playerEntity != null && isOnline ? playerUuid.getOnline().equals(uuid) : playerUuid.getOffline().equals(uuid);
                })
                .findFirst()
                .orElse(null);
    }

    private int findJsonObjectIndex(JsonObject jsonObject) {
        List<JsonObject> jsonObjects = findJsonObject();
        return jsonObjects.indexOf(jsonObject);
    }

    @Override
    public List<PlayerEntity> find() {
        return findJsonObject().stream()
                .filter(jsonObject -> jsonObject != null && PlayerEntity.toEntity(jsonObject) != null)
                .map(PlayerEntity::toEntity)
                .toList();
    }

    @Override
    public Optional<PlayerEntity> find(UUID uuid, boolean isOnline) {
        JsonObject jsonObject = findJsonObject(uuid, isOnline);
        if (jsonObject == null) return Optional.empty();
        return Optional.ofNullable(PlayerEntity.toEntity(findJsonObject(uuid, isOnline)));
    }

    @Override
    public boolean isExists(UUID uuid, boolean isOnline) {
        return find(uuid, isOnline).isPresent();
    }

    @Override
    public void create(String nickname, UUID offlineUuid, UUID onlineUuid) {
        JsonArray database = jsonRepository.getDatabase();
        Gson gson = jsonRepository.getGson();

        PlayerEntity playerEntity = PlayerEntity.builder()
                .uuid(new PlayerEntity.PlayerUuid(offlineUuid, onlineUuid == null ? offlineUuid : onlineUuid))
                .info(new PlayerEntity.PlayerInfo(List.of(nickname), new Date().getTime()))
                .time(-1L)
                .build();

        database.add(gson.toJsonTree(playerEntity));
        jsonRepository.save();
    }

    @Override
    public void create(UUID uuid) {
        JsonArray database = jsonRepository.getDatabase();
        Gson gson = jsonRepository.getGson();

        PlayerEntity playerEntity = PlayerEntity.builder()
                .uuid(new PlayerEntity.PlayerUuid(uuid, uuid))
                .info(new PlayerEntity.PlayerInfo(List.of(), new Date().getTime()))
                .time(-1L)
                .build();

        database.add(gson.toJsonTree(playerEntity));
        jsonRepository.save();
    }

    @Override
    public void remove(UUID uuid, boolean isOnline) {
        JsonArray database = jsonRepository.getDatabase();

        database.remove(findJsonObject(uuid, isOnline));
        jsonRepository.save();
    }

    @Override
    public void setTime(UUID uuid, boolean isOnline, long time) {
        JsonArray database = jsonRepository.getDatabase();
        Gson gson = jsonRepository.getGson();

        Optional<PlayerEntity> optionalFindPlayer = find(uuid, isOnline);
        optionalFindPlayer.ifPresent(findPlayer -> {
            findPlayer.setTime(time);

            JsonObject jsonObject = findJsonObject(uuid, isOnline);
            int jsonObjectIndex = findJsonObjectIndex(jsonObject);

            database.set(jsonObjectIndex, gson.toJsonTree(findPlayer));
            jsonRepository.save();
        });
    }
}
