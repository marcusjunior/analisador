package com.br.analisador.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Vendedor {

    private String CPF;
    private String nome;
    private Double salario;

}
