package com.ams.entity;

import com.ams.enums.MembershipStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "organizations")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToMany(
            mappedBy = "organization",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private Set<Membership> memberships = new HashSet<>();

    @Column(length = 1000)
    private String description;

    private String logoUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Transient
    public int getMemberCount() {
        return memberships.stream()
                .map(m -> m.getStatus() == MembershipStatus.ACTIVE ? 1 : 0)
                .reduce(0, Integer::sum);
    }
}
