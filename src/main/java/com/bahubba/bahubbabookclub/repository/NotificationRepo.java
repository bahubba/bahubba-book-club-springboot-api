package com.bahubba.bahubbabookclub.repository;

import com.bahubba.bahubbabookclub.model.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repository for the {@link Notification} entity
 */
public interface NotificationRepo extends JpaRepository<Notification, UUID> {
}
