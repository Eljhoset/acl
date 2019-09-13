package com.example.testAcl;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author Daniel
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Pos implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToOne
    @JoinColumn(name = "bettingshop", updatable = false)
    private Bettingshop bettingshop;

    @Enumerated(EnumType.STRING)
    private EntityStatus status;

}
