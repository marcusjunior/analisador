package com.br.analisador.resource;

import com.br.analisador.domain.model.Cliente;
import com.br.analisador.domain.model.ItemVendido;
import com.br.analisador.domain.model.Vendas;
import com.br.analisador.domain.model.Vendedor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Component
public class AnalisadorDeDados {

    private static final String caminhoArquivosParaAnalise = System.getProperty("user.home") + "\\data\\in";
    private static final String caminhoArquivosAnalisados = System.getProperty("user.home") + "\\data\\out";

    private static final String cedilha = "ç";
    private static final String virgula = ",";
    private static final String traco = "-";

    @PostConstruct
    public void iniciarAnaliseDosArquivos() throws IOException {

        Files.walk(Paths.get(caminhoArquivosParaAnalise))
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    try {
                        analisarArquivo(path.toFile());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    public void escreverNoArquivo(OutputStream arquivo, String texto) throws IOException {
        arquivo.write(texto.getBytes());
    }

    public void analisarArquivo(File arquivo) throws IOException {

        Scanner scanner = new Scanner(arquivo);

        List<Vendedor>vendedores = new ArrayList<>();
        List<Cliente> clientes = new ArrayList<>();
        List<Vendas> vendas = new ArrayList<>();

        while(scanner.hasNextLine()){
            String linha = scanner.nextLine();
            String[] dadosDalinha = linha.split(cedilha);

            switch(dadosDalinha[0]) {
                case "001":
                    vendedores.add(
                            Vendedor.builder()
                                .CPF(dadosDalinha[1])
                                .nome(dadosDalinha[2])
                                .salario(Double.valueOf(dadosDalinha[3]))
                            .build()
                    );
                    break;
                case "002":
                    clientes.add(
                            Cliente.builder()
                                    .CNPJ(dadosDalinha[1])
                                    .nome(dadosDalinha[2])
                                    .areaDeNegocio(dadosDalinha[3])
                                    .build()
                    );
                    break;
                case "003":
                    List<ItemVendido> itensVendidos = extrairDadosItensVendidos(dadosDalinha[2]);
                    AtomicReference<Double> valorTotal = new AtomicReference<>(0.0);

                    itensVendidos
                            .forEach( itemVendido ->
                                    valorTotal.set(valorTotal.get() + (itemVendido.getQuantidade() * itemVendido.getPreco()))
                            );

                    vendas.add(
                            Vendas.builder()
                                .id(Long.valueOf(dadosDalinha[1]))
                                .itensVendidos(itensVendidos)
                                .nomeVendedor(dadosDalinha[3])
                                .valorTotal(valorTotal.get())
                            .build()
                    );
                    break;
                default:
            }
        }

        OutputStream arquivoDeSaida =
                new FileOutputStream(caminhoArquivosAnalisados+ "\\"
                        + arquivo.getName().replace(".dat", "")
                        + ".done.dat");
        try {
            escreverNoArquivo(arquivoDeSaida, "Quantidade de clientes no arquivo de entrada: ");
            escreverNoArquivo(arquivoDeSaida, String.valueOf(clientes.size()) + "\n");
            escreverNoArquivo(arquivoDeSaida, "Quantidade de vendedores no arquivo de entrada: ");
            escreverNoArquivo(arquivoDeSaida, String.valueOf(vendedores.size()) + "\n");
            escreverNoArquivo(arquivoDeSaida, "ID da venda mais cara: ");
            escreverNoArquivo(arquivoDeSaida, extrairIdVendaMaisCara(vendas).toString() + "\n");
            escreverNoArquivo(arquivoDeSaida, "O pior vendedor: ");
            escreverNoArquivo(arquivoDeSaida, extrairPiorVendedor(vendas, vendedores));
        } finally {
            arquivoDeSaida.close();
        }
    }

    private List<ItemVendido> extrairDadosItensVendidos(String itensVendidos){

        List<ItemVendido> itensVendidosExtraidos = new ArrayList<>();

        String[] itens = itensVendidos
                            .replace("[", "")
                            .replace("]", "")
                            .split(virgula);

        Arrays.stream(itens)
                .forEach(itemExtraido -> {
                    String[] item = itemExtraido.split(traco);
                    itensVendidosExtraidos.add(
                            ItemVendido.builder()
                                .id(Long.valueOf(item[0]))
                                .quantidade(Integer.valueOf(item[1]))
                                .preco(Double.valueOf(item[2]))
                            .build()

                    );
                });
        return itensVendidosExtraidos;
    }

    public Long extrairIdVendaMaisCara(List<Vendas> vendas) {

        Vendas maiorVenda =
                vendas
                    .stream()
                        .max(Comparator.comparingDouble(Vendas::getValorTotal))
                        .orElseThrow(NoSuchElementException::new);

        return maiorVenda.getId();
    }

    private String extrairPiorVendedor(List<Vendas> vendas, List<Vendedor> vendedores){

        Map<String, List<Vendas>> vendasPorVendedor =
                vendas.stream().collect(Collectors.groupingBy( venda -> venda.getNomeVendedor()));


        AtomicReference<Double> valorTotalPiorVendedor = new AtomicReference<>(0.0);
        AtomicReference<Double> valorTotal = new AtomicReference<>(0.0);
        AtomicReference<String> nomePiorVendedor =  new AtomicReference<>("");

        vendedores
            .stream()
            .forEach( vendedor -> {

                    vendasPorVendedor.get(vendedor.getNome())
                            .forEach( venda ->
                                    valorTotal.set(valorTotal.get() + venda.getValorTotal())
                            );

                    if(valorTotalPiorVendedor.get() == 0.0d){
                        nomePiorVendedor.set(vendedor.getNome());
                        valorTotalPiorVendedor.set(valorTotal.get());
                    }

                    if(valorTotal.get() < valorTotalPiorVendedor.get()){
                        nomePiorVendedor.set(vendedor.getNome());
                        valorTotalPiorVendedor.set(valorTotal.get());
                    }

                    valorTotal.set(0.0d);
                }
            );

        return nomePiorVendedor.get();
    }

}
