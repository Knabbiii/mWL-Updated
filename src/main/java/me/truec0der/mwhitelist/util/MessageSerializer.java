package me.truec0der.mwhitelist.util;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public class MessageSerializer {
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    public Component create(String message) {
        if (message == null) return Component.empty();

        return miniMessage.deserialize(message);
    }

    public Component create(String message, Map<String, String> placeholders) {
        if (message == null) return Component.empty();

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            String placeholder = "%" + entry.getKey() + "%";
            String replacement = entry.getValue();
            message = message.replace(placeholder, replacement);
        }

        return miniMessage.deserialize(message);
    }

    public String serialize(String message) {
        return legacySerializer.serialize(create(message));
    }

    public String serialize(String message, Map<String, String> placeholders) {
        return legacySerializer.serialize(create(message, placeholders));
    }

    public String serialize(Component component) {
        return legacySerializer.serialize(component);
    }

    public void send(CommandSender sender, Component component) {
        String message = serialize(component);

        if (sender instanceof ConsoleCommandSender) {
            message = ChatColor.stripColor(message);
            message = Arrays.stream(message.split("\n"))
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.equals("»"))
                    .collect(Collectors.joining(" "))
                    .replaceAll("\\s+", " ")
                    .trim();
        }

        sender.sendMessage(message);
    }
}
