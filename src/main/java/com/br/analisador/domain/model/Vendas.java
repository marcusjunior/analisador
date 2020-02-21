package com.br.analisador.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class Vendas {

    private Long id;
    private List<ItemVendido> itensVendidos;
    private String nomeVendedor;
    private Double valorTotal;
}
