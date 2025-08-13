package com.tapioca.MCPBE.domain.entity.erd;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "attribute_link")
public class AttributeLinkEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="attribute_link_id")
    private UUID id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name="from_attribute", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private AttributeEntity fromAttribute;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name="to_attribute", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private AttributeEntity toAttribute;

    @Enumerated(EnumType.STRING)
    @Column(name="link_type")
    private LinkType linkType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="erd_id", nullable=false)
    @JsonBackReference
    private ErdEntity erdEntity;
}
