package com.ceasacampinas.telnetClient.controller;

import com.ceasacampinas.telnetClient.domain.Balanca;
import com.ceasacampinas.telnetClient.service.TelnetClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class BalancaController {

    @Autowired
    private TelnetClient telnetClient;

    @GetMapping("/balanca")
    public String exibirFormularioBalanca(Model model) {
        // Instância de Balanca com valores padrão (caso queira preencher alguns campos previamente)
        Balanca balanca = new Balanca();
        balanca.setDataPesagem(LocalDateTime.now()); // Define a data atual para a pesagem
        balanca.setPeso(BigDecimal.ZERO);  // Define um valor inicial (ajustável)
        balanca.setContador(new BigDecimal(1));  // Exemplo de contador padrão

        // Adiciona a instância ao modelo para que o formulário a utilize
        model.addAttribute("balanca", balanca);

        return "balanca"; // Nome da página HTML do formulário
    }
    
    @PostMapping("/capturar")
    public String capturarPeso(@RequestParam String proprietarioCaminhao,
                               @RequestParam String motoristaCaminhao,
                               @RequestParam String modeloCaminhao,
                               @RequestParam String nomeBalanceiro,
                               @RequestParam String placaVeiculo,
                               Model model) {
        // Chama o service para capturar o peso da balança
        String pesoCapturado = telnetClient.capturarPeso();
        if (pesoCapturado == null) {
            pesoCapturado = "";
        }

        // Adiciona os dados ao modelo com nomes corretos para evitar sobrescrever
        model.addAttribute("proprietarioCaminhao", proprietarioCaminhao);
        model.addAttribute("motoristaCaminhao", motoristaCaminhao);
        model.addAttribute("modeloCaminhao", modeloCaminhao);
        model.addAttribute("nomeBalanceiro", nomeBalanceiro);
        model.addAttribute("placaVeiculo", placaVeiculo);
        model.addAttribute("pesoCapturado", pesoCapturado);

        return "balanca";  // Retorna à página com os dados
    }

    @GetMapping("/teste-impressao")
    @ResponseBody
    public String testarImpressao() {
    	 String placa = "BAV-3232";
         String destino = "JARI FRUTAS";
         String valor = "R$ 17,88";
         String peso = "1.550 kg";  // Peso fixo para teste
         String data = "13/06/2022";
         String hora = "13:06:26";
        telnetClient.imprimirEtiqueta(placa, destino,valor, data, hora);
        return "Teste de impressão enviado para a impressora!";
    }

    
    @PostMapping("/balanca/imprimir")
    public String imprimirBalanca(@ModelAttribute Balanca balanca, Model model) {
        // Preenche os dados automáticos do sistema
        balanca.setPeso(telnetClient.capturarPeso1()); // Método fictício para capturar peso
        balanca.setDataPesagem(LocalDateTime.now()); // Captura a data e hora atuais
        balanca.setContador(new BigDecimal(1)); // Exemplo de contador

        // Envia para impressão
        telnetClient.imprimirEtiqueta(
            balanca.getPlaca(),
            balanca.getDestino(),
            balanca.getValor().toString(),
            balanca.getDataPesagem().toString(),
            balanca.getPeso().toString()
        );

        // Retorna uma mensagem de sucesso
        model.addAttribute("mensagem", "Etiqueta impressa com sucesso!");
        return "balanca_sucesso";
    }

    // Método fictício para capturar o peso
    private BigDecimal capturarPeso() {
        return new BigDecimal("1550.0"); // Simulando o peso da balança
    }
}
