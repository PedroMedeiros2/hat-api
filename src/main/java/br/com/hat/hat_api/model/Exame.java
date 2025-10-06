package br.com.hat.hat_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "SILANEXA")
public class Exame {
    @Id
    @Column(name = "ID")
    private Long id;
}
