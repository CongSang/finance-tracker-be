package com.congsang.financetracker.entity;

import com.congsang.financetracker.common.enums.AuthProvider;
import com.congsang.financetracker.common.enums.Role;
import com.congsang.financetracker.common.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name="users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    private String fullName;

    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    private Status status = Status.INACTIVE;

    @Enumerated(EnumType.STRING)
    private Role role = Role.CUSTOMER;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider = AuthProvider.LOCAL;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<WalletEntity> wallets;

    // Active account when login by email and password
    private String activationToken;
}
