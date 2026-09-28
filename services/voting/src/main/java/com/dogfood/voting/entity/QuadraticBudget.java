package com.dogfood.voting.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(name = "quadratic_budgets", schema = "voting")
public class QuadraticBudget {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private UUID eventId;
    private UUID voterId;
    private Integer totalBudget = 100;
    private Integer spent = 0;
}
