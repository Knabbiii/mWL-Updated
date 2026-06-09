package me.truec0der.mwhitelist.model.entity.plugin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PluginVersionEntity {
    String version;
    String url;
    List<String> info;

    public static PluginVersionEntity toEntity(JsonObject jsonObject) {
        JsonArray infoArray = jsonObject.getAsJsonArray("info");
        List<String> infoList = new ArrayList<>();

        infoArray.forEach(value -> infoList.add(value.getAsString()));

        return PluginVersionEntity.builder()
                .version(jsonObject.get("version").getAsString())
                .url(jsonObject.get("url").getAsString())
                .info(infoList)
                .build();
    }
}
