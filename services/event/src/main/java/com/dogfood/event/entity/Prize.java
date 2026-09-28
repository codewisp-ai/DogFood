package com.dogfood.event.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "prizes", schema = "events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Prize {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id")
    private Track track;

    private String name;
    private String description;

    @Column(name = "sort_order")
    private Integer sortOrder;
}
