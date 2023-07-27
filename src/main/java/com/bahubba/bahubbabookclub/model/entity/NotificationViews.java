package com.bahubba.bahubbabookclub.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "notification_views")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationViews implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "notification_id")
    private Notification notification;

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "reader_id")
    private Reader reader;
}
