package me.truec0der.mwhitelist.interfaces.repository;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.truec0der.mwhitelist.config.ConfigRegister;
import me.truec0der.mwhitelist.model.entity.database.PlayerEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public abstract class PlayerRepository {
    ConfigRegister configRegister;

    abstract public List<PlayerEntity> find();

    abstract public Optional<PlayerEntity> find(UUID uuid);

    abstract public boolean isExists(UUID uuid);

    abstract public void create(String nickname, UUID uuid);

    abstract public void create(UUID uuid);

    abstract public void remove(UUID uuid);

    abstract public void setTime(UUID uuid, long time);

    abstract public void updateNickname(UUID uuid, String name);
}
