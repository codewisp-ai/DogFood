package com.dogfood.judging.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "criteria")
public class Criterion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "rubric_id")
    private UUID rubricId;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal weight;
    
    @Column(name = "max_score")
    private Integer maxScore;
    
    @Column(name = "sort_order")
    private Integer sortOrder;
}
