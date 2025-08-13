package com.tapioca.MCPBE.domain.entity.erd;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.tapioca.MCPBE.domain.entity.TeamEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.*;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "erd")
public class ErdEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "erd_id")
    private UUID id;

    @Column(name = "erd_name")
    private String name;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonBackReference
    private TeamEntity teamEntity;

    @OneToMany(mappedBy = "erdEntity",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @Builder.Default
    @JsonManagedReference
    private Set<DiagramEntity> diagrams = new HashSet<>();

    @OneToMany(mappedBy = "erdEntity",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @Builder.Default
    @JsonManagedReference
    private Set<AttributeLinkEntity> attributeLinks = new HashSet<>();
}
