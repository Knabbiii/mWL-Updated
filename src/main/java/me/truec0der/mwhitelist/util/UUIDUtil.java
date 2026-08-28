package me.truec0der.mwhitelist.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.experimental.UtilityClass;
import me.truec0der.mwhitelist.model.entity.mojang.OnlinePlayerEntity;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@UtilityClass
public class UUIDUtil {
    public UUID convertToUUID(String compactUUID) {
        if (compactUUID.length() != 32) {
            throw new IllegalArgumentException("Invalid UUID string length.");
        }

        String formattedUUID = compactUUID.replaceFirst(
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{12})",
                "$1-$2-$3-$4-$5"
        );

        return UUID.fromString(formattedUUID);
    }

    public UUID getOnlineUuid(String nickname) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.mojang.com/users/profiles/minecraft/" + nickname))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return null;

            return convertToUUID(gson.fromJson(response.body(), OnlinePlayerEntity.class).getId());
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }

    public CompletableFuture<UUID> getOnlineUuidAsync(String nickname) {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.mojang.com/users/profiles/minecraft/" + nickname))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) return null;
                    Gson gson = new Gson();
                    OnlinePlayerEntity entity = gson.fromJson(response.body(), OnlinePlayerEntity.class);
                    return convertToUUID(entity.getId());
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    public static boolean isUuid(String text) {
        if (text == null) return false;
        try {
            UUID.fromString(text);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
