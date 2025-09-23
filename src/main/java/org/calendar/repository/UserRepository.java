package org.calendar.repository;

import org.calendar.entity.UserEntity;

import java.util.Optional;

public interface UserRepository extends GenericRepository<UserEntity, Integer> {
    Optional<UserEntity> findByDiscordId(Long discordId);
}
