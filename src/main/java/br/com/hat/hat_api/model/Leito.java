package br.com.hat.hat_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "RILEITOS")
public class Leito {
    @Id
    @Column(name = "ID")
    private Long id;

}
