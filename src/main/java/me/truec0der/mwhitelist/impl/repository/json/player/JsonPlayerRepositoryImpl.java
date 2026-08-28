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

    private JsonObject findJsonObject(UUID uuid) {
        List<JsonObject> jsonObjects = findJsonObject();

        return jsonObjects.stream()
                .filter(jsonObject -> {
                    PlayerEntity playerEntity = PlayerEntity.toEntity(jsonObject);
                    return playerEntity != null && playerEntity.getUuid().equals(uuid);
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
    public Optional<PlayerEntity> find(UUID uuid) {
        JsonObject jsonObject = findJsonObject(uuid);
        if (jsonObject == null) return Optional.empty();
        return Optional.ofNullable(PlayerEntity.toEntity(jsonObject));
    }

    @Override
    public boolean isExists(UUID uuid) {
        return find(uuid).isPresent();
    }

    @Override
    public void create(String nickname, UUID uuid) {
        JsonArray database = jsonRepository.getDatabase();
        Gson gson = jsonRepository.getGson();

        PlayerEntity playerEntity = PlayerEntity.builder()
                .uuid(uuid)
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
                .uuid(uuid)
                .info(new PlayerEntity.PlayerInfo(List.of(), new Date().getTime()))
                .time(-1L)
                .build();

        database.add(gson.toJsonTree(playerEntity));
        jsonRepository.save();
    }

    @Override
    public void remove(UUID uuid) {
        JsonArray database = jsonRepository.getDatabase();

        database.remove(findJsonObject(uuid));
        jsonRepository.save();
    }

    @Override
    public void setTime(UUID uuid, long time) {
        JsonArray database = jsonRepository.getDatabase();
        Gson gson = jsonRepository.getGson();

        Optional<PlayerEntity> optionalFindPlayer = find(uuid);
        optionalFindPlayer.ifPresent(findPlayer -> {
            findPlayer.setTime(time);

            JsonObject jsonObject = findJsonObject(uuid);
            int jsonObjectIndex = findJsonObjectIndex(jsonObject);

            database.set(jsonObjectIndex, gson.toJsonTree(findPlayer));
            jsonRepository.save();
        });
    }

    @Override
    public void updateNickname(UUID uuid, String name) {
        JsonArray database = jsonRepository.getDatabase();
        Gson gson = jsonRepository.getGson();

        Optional<PlayerEntity> optionalFindPlayer = find(uuid);
        optionalFindPlayer.ifPresent(findPlayer -> {
            List<String> nicknameHistory = findPlayer.getInfo().getNicknameHistory();
            if (!nicknameHistory.isEmpty() && nicknameHistory.get(nicknameHistory.size() - 1).equals(name)) return;

            List<String> updatedHistory = new ArrayList<>(nicknameHistory);
            updatedHistory.add(name);

            findPlayer.setInfo(new PlayerEntity.PlayerInfo(updatedHistory, new Date().getTime()));

            JsonObject jsonObject = findJsonObject(uuid);
            int jsonObjectIndex = findJsonObjectIndex(jsonObject);

            database.set(jsonObjectIndex, gson.toJsonTree(findPlayer));
            jsonRepository.save();
        });
    }
}
