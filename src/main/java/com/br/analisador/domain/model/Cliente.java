package com.br.analisador.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Cliente {

    private String CNPJ;
    private String nome;
    private String areaDeNegocio;

}
