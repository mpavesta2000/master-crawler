package com.avesta.mastercrawler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "social_accounts")
@Getter
@Setter
@ToString(exclude = {"accessToken", "refreshToken"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SocialAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private Users user; // nullable for site-wide bot accounts

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false)
    private SocialPlatform platform;

    @JsonIgnore
    @Column(name = "access_token", nullable = false, columnDefinition = "TEXT")
    @Lob
    private String accessToken; // Encrypted

    @JsonIgnore
    @Column(name = "refresh_token", columnDefinition = "TEXT")
    @Lob
    private String refreshToken; // Encrypted, nullable

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "settings", columnDefinition = "TEXT")
    @Lob
    private String settings; // JSON string for platform-specific settings

    @Column(name = "account_name")
    private String accountName; // Display name or username

    @Column(name = "account_id")
    private String accountId; // Platform-specific account identifier

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_global")
    @Builder.Default
    private Boolean isGlobal = false; // true for site-wide bot accounts

    public enum SocialPlatform {
        X, TELEGRAM, EITAA, BALE
    }
}
