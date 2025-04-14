package me.truec0der.mwhitelist.model.entity.database;

import com.google.gson.Gson;
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
    @Nullable PlayerUuid uuid;
    @Nullable PlayerInfo info;
    @Nullable Long time;

    public static PlayerEntity toEntity(JsonObject jsonObject) {
        Gson gson = new Gson();

        if (!jsonObject.has("uuid") || !jsonObject.has("info")) return null;

        JsonObject uuid = jsonObject.getAsJsonObject("uuid");
        JsonObject info = jsonObject.getAsJsonObject("info");

        UUID offlineUuid = UUID.fromString(uuid.get("offline").getAsString());
        UUID onlineUuid = UUID.fromString(uuid.get("online").getAsString());

        List<String> nicknameHistory = Arrays.asList(gson.fromJson(info.get("nicknameHistory").getAsJsonArray(), String[].class));
        long lastUpdate = info.get("lastUpdate").getAsLong();

        Long time = jsonObject.has("time") ? jsonObject.get("time").getAsLong() : -1;

        return PlayerEntity.builder()
                .uuid(new PlayerUuid(offlineUuid, onlineUuid))
                .info(new PlayerInfo(nicknameHistory, lastUpdate))
                .time(time)
                .build();
    }

    public static PlayerEntity fromDocument(Document document) {
        Document uuidDocument = (Document) document.get("uuid");
        Document infoDocument = (Document) document.get("info");

        return PlayerEntity.builder()
                .uuid(
                        new PlayerUuid(
                                UUID.fromString(uuidDocument.getString("offline")),
                                UUID.fromString(uuidDocument.getString("online"))
                        )
                )
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
        Document uuidDocument = new Document()
                .append("offline", uuid.getOffline().toString())
                .append("online", uuid.getOnline().toString());

        Document infoDocument = new Document()
                .append("nicknameHistory", info.getNicknameHistory())
                .append("lastUpdate", info.getLastUpdate());

        return new Document()
                .append("uuid", uuidDocument)
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
    public static class PlayerUuid {
        UUID offline;
        UUID online;
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
