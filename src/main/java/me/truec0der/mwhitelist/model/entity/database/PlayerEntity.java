package me.truec0der.mwhitelist.model.entity.database;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlayerEntity {
    @Nullable UUID uuid;
    @Nullable PlayerInfo info;
    @Nullable Long time;

    public static PlayerEntity toEntity(JsonObject jsonObject) {
        Gson gson = new Gson();

        if (!jsonObject.has("uuid") || !jsonObject.has("info")) return null;

        UUID uuid = readUuid(jsonObject.get("uuid"));
        JsonObject info = jsonObject.getAsJsonObject("info");

        List<String> nicknameHistory = Arrays.asList(gson.fromJson(info.get("nicknameHistory").getAsJsonArray(), String[].class));
        long lastUpdate = info.get("lastUpdate").getAsLong();

        Long time = jsonObject.has("time") ? jsonObject.get("time").getAsLong() : -1;

        return PlayerEntity.builder()
                .uuid(uuid)
                .info(new PlayerInfo(nicknameHistory, lastUpdate))
                .time(time)
                .build();
    }

    /**
     * Legacy entries (pre offline-mode removal) stored uuid as a nested
     * {online, offline} object instead of a plain string. Migrate them
     * transparently by preferring the online uuid.
     */
    private static UUID readUuid(JsonElement uuidElement) {
        if (uuidElement.isJsonObject()) {
            JsonObject uuidObject = uuidElement.getAsJsonObject();
            String legacyUuid = uuidObject.has("online") ? uuidObject.get("online").getAsString() : uuidObject.get("offline").getAsString();
            return UUID.fromString(legacyUuid);
        }

        return UUID.fromString(uuidElement.getAsString());
    }

    public static PlayerEntity fromDocument(Document document) {
        Document infoDocument = (Document) document.get("info");

        Object rawUuid = document.get("uuid");
        UUID uuid = rawUuid instanceof Document legacyUuid
                ? UUID.fromString(legacyUuid.containsKey("online") ? legacyUuid.getString("online") : legacyUuid.getString("offline"))
                : UUID.fromString((String) rawUuid);

        return PlayerEntity.builder()
                .uuid(uuid)
                .info(
                        new PlayerInfo(
                                infoDocument.getList("nicknameHistory", String.class),
                                infoDocument.getLong("lastUpdate")
                        )
                )
                .time(document.getLong("time"))
                .build();
    }

    public Document toDocument() {
        Document infoDocument = new Document()
                .append("nicknameHistory", info.getNicknameHistory())
                .append("lastUpdate", info.getLastUpdate());

        return new Document()
                .append("uuid", uuid.toString())
                .append("info", infoDocument)
                .append("time", time);
    }

    public boolean isTimeExists() {
        return isTimeExists(new Date().getTime());
    }

    public boolean isTimeExists(long currentTime) {
        return time >= 0 && time > currentTime;
    }

    public Long getEstimatedTime() {
        return time - new Date().getTime();
    }

    public boolean isTimeExpired() {
        return time > 0 && new Date().getTime() > time;
    }

    public boolean isTimeInfinity() {
        return time < 0;
    }

    public String formatTime(SimpleDateFormat format) {
        return format.format(new Date(time));
    }

    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @Getter
    @Setter
    public static class PlayerInfo {
        List<String> nicknameHistory;
        Long lastUpdate;
    }
}
