package br.com.hat.hat_api.spdata.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "CCCADCIR")
public class CentroCirurgico {
    @Id
    @Column(name = "ID")
    private Long id;
}
