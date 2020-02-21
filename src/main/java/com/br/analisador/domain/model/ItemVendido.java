package com.br.analisador.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemVendido {

    private Long id;
    private Integer quantidade;
    private Double preco;

}
