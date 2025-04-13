package me.truec0der.mwhitelist.service.database;

import com.mongodb.ConnectionString;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import me.truec0der.mwhitelist.config.ConfigRegister;
import me.truec0der.mwhitelist.impl.repository.json.JsonRepositoryImpl;
import me.truec0der.mwhitelist.impl.repository.json.player.JsonPlayerRepositoryImpl;
import me.truec0der.mwhitelist.impl.repository.mongo.MongoRepositoryImpl;
import me.truec0der.mwhitelist.impl.repository.mongo.player.MongoPlayerRepositoryImpl;
import me.truec0der.mwhitelist.interfaces.repository.PlayerRepository;
import me.truec0der.mwhitelist.interfaces.repository.json.JsonRepository;
import me.truec0der.mwhitelist.interfaces.repository.mongo.MongoRepository;
import org.bukkit.plugin.Plugin;

import java.io.File;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class DatabaseConnectionService {
    final ConfigRegister configRegister;
    MongoRepository mongoRepository;
    JsonRepository jsonRepository;

    public DatabaseConnectionService(ConfigRegister configRegister) {
        this.configRegister = configRegister;
    }

    public PlayerRepository initMongoPlayerService(ConnectionString connectionString, String databaseName) {
        mongoRepository = new MongoRepositoryImpl(connectionString, databaseName);
        mongoRepository.init();
        return new MongoPlayerRepositoryImpl(configRegister, mongoRepository);
    }

    public PlayerRepository initJsonPlayerService(Plugin plugin, File filePath, String file) {
        jsonRepository = new JsonRepositoryImpl(plugin, filePath, file);
        jsonRepository.init();
        return new JsonPlayerRepositoryImpl(configRegister, jsonRepository);
    }

    public void closeMongoService() {
        if (mongoRepository == null) return;
        mongoRepository.closeConnection();
        mongoRepository = null;
    }

    public void closeJsonService() {
        if (jsonRepository == null) return;
        jsonRepository = null;
    }

    public void close() {
        closeMongoService();
        closeJsonService();
    }
}
