package com.tapioca.MCPBE.domain.entity.erd;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "diagram")
public class DiagramEntity {
    @Id
    @Column(name = "diagram_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "diagram_name")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="erd_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonBackReference
    private ErdEntity erdEntity;

    @OneToMany(mappedBy = "diagram",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @Builder.Default
    @JsonManagedReference
    private Set<AttributeEntity> attributes = new HashSet<>();
}
