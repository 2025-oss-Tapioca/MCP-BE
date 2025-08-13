package com.tapioca.MCPBE.domain.entity.erd;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="attribute")
public class AttributeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="attribute_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="diagram_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonBackReference
    private DiagramEntity diagram;

    @Column(name = "attribute_name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "attribute_type")
    private AttributeType attributeType;

    @Column(name = "attribute_type_varchar_length")
    private Integer length;

    @Column(name = "is_pk", nullable = false)
    private boolean isPrimaryKey;

    @Column(name = "is_fk", nullable = false)
    private boolean isForeignKey;

    public void setDiagram(DiagramEntity diagramEntity) {
        this.diagram = diagramEntity;
    }

    public void setName(String s) {
    }
}
