package me.truec0der.mwhitelist.service.plugin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.truec0der.mwhitelist.config.configs.LangConfig;
import me.truec0der.mwhitelist.model.entity.plugin.PluginVersionEntity;
import me.truec0der.mwhitelist.util.MessageSerializer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PluginUpdateServiceImpl {
    String apiUrl;
    String destinationPath;
    String destinationName;
    LangConfig langConfig;

    private JsonArray getJsonVersions() {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return null;

            return new Gson().fromJson(response.body(), JsonArray.class);
        } catch (IOException | InterruptedException | JsonSyntaxException e) {
            return null;
        }
    }

    public List<PluginVersionEntity> getVersions() {
        JsonArray jsonVersions = getJsonVersions();

        if (jsonVersions == null) return null;

        List<PluginVersionEntity> versionList = new ArrayList<>();
        jsonVersions.forEach(element -> versionList.add(PluginVersionEntity.toEntity(element.getAsJsonObject())));

        return versionList;
    }

    public PluginVersionEntity getLastVersion() {
        List<PluginVersionEntity> versions = getVersions();

        if (versions == null || versions.isEmpty()) return null;

        return versions.get(versions.size() - 1);
    }

    public boolean isLastVersion(String currentVersion) {
        PluginVersionEntity pluginVersionEntity = getLastVersion();

        if (pluginVersionEntity == null) return false;

        return pluginVersionEntity.getVersion().equals(currentVersion);
    }

    public void handleCheck(String currentVersion, boolean canUpdate) {
        boolean isLastVersion = isLastVersion(currentVersion);
        if (isLastVersion) return;

        PluginVersionEntity lastVersion = getLastVersion();

        if (lastVersion == null) {
            Bukkit.getConsoleSender().sendMessage(langConfig.getMain().getUpdate().getNotifyFailed());
            return;
        }

        handleNotify(lastVersion, currentVersion);

        if (canUpdate) handleUpdate(lastVersion.getUrl(), destinationPath, destinationName);
    }

    public void handleNotify(PluginVersionEntity entity, String currentVersion) {
        MiniMessage miniMessage = MiniMessage.miniMessage();

        String notify = miniMessage.serialize(langConfig.getMain().getUpdate().getNotify());
        String version = miniMessage.serialize(langConfig.getMain().getUpdate().getVersionInfo());

        List<String> notifyComponents = new ArrayList<>(Arrays.asList(notify.split("\n")));
        List<String> versionInfo = entity.getInfo().stream()
                .map(info -> version.replace("%version_info%", info))
                .collect(Collectors.toList());

        Map<String, String> placeholders = Map.of(
                "current_version", currentVersion,
                "new_version", entity.getVersion(),
                "url", entity.getUrl()
        );

        replaceLineInList(notifyComponents, versionInfo, "%version_info%");
        List<String> resultText = notifyComponents.stream()
                .map(component -> MessageSerializer.serialize(component, placeholders))
                .toList();

        resultText.forEach(Bukkit.getConsoleSender()::sendMessage);
    }

    private void replaceLineInList(List<String> originalList, List<String> replacementList, String searchText) {
        Optional<Integer> indexOpt = originalList.stream()
                .filter(s -> s.contains(searchText))
                .findFirst()
                .map(originalList::indexOf);

        indexOpt.ifPresent(index -> {
            originalList.remove((int) index);
            originalList.addAll(index, replacementList);
        });
    }

    public void handleUpdate(String fileUrl, String destinationPath, String destinationName) {
        boolean replaceFileAttempt = replaceFileAttempt(fileUrl, destinationPath, destinationName);

        if (!replaceFileAttempt) {
            Bukkit.getConsoleSender().sendMessage(langConfig.getMain().getUpdate().getActionFailed());
            return;
        }

        Bukkit.getConsoleSender().sendMessage(langConfig.getMain().getUpdate().getAction());
    }

    private boolean replaceFileAttempt(String fileUrl, String destinationPath, String destinationName) {
        String fileName = Paths.get(fileUrl).getFileName().toString();
        Path destinationDir = Paths.get(destinationPath);

        try (Stream<Path> files = Files.walk(destinationDir)) {
            files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().contains(destinationName))
                    .forEach(this::deleteFile);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return downloadFile(fileUrl, destinationDir.resolve(fileName));
    }

    private void deleteFile(Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean downloadFile(String fileUrl, Path destination) {
        try (InputStream in = new URL(fileUrl).openStream()) {
            Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
