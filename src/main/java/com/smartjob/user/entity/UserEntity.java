package com.smartjob.user.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


/**
 * JPA entity that represents a user in the system.
 * Includes personal information, credentials, JWT tokens, and phone management.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_email", columnList = "email", unique = true)
    })
public class UserEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 75)
    private String email;

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @CreationTimestamp
    @Column(name = "created", nullable = false, updatable = false)
    private LocalDateTime created;

    @UpdateTimestamp
    @Column(name = "modified", nullable = false)
    private LocalDateTime modified;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "token", length = 500)
    private String token;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<PhoneEntity> phones = new ArrayList<>();

    public void addPhone(PhoneEntity phone) {
        phones.add(phone);
        phone.setUser(this);
    }

    public void removePhone(PhoneEntity phone) {
        phones.remove(phone);
        phone.setUser(null);
    }

    @PrePersist
    protected void onCreate() {
        if (this.lastLogin == null) {
            this.lastLogin = LocalDateTime.now();
        }
    }
}