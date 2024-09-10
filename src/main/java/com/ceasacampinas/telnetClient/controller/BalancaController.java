package com.ceasacampinas.telnetClient.controller;

import com.ceasacampinas.telnetClient.service.TelnetClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BalancaController {

    @Autowired
    private TelnetClient telnetClient;

    @GetMapping("/")
    public String iniciar() {
    	return "balanca";
    }
    
    @PostMapping("/capturar")
    public String capturarPeso(@RequestParam String modeloCaminhao,
                               @RequestParam String nomeBalanceiro,
                               @RequestParam String placaVeiculo,
                               Model model) {
        // Chama o service para capturar o peso da balança
       String pesoCapturado = telnetClient.capturarPeso();
    	//String pesoCapturado = "000";
    	if (pesoCapturado==null) {
    		pesoCapturado = "";
    	}
        // Adiciona os dados ao modelo para exibir na interface
        model.addAttribute("modeloCaminhao", modeloCaminhao);
        model.addAttribute("nomeBalanceiro", nomeBalanceiro);
        model.addAttribute("placaVeiculo", placaVeiculo);
        model.addAttribute("pesoCapturado", pesoCapturado != null ? pesoCapturado : "Erro ao capturar peso");

        return "balanca";  // Retorna à página com os dados
    }

    @PostMapping("/imprimir")
    public String imprimirRelatorio(@RequestParam String modeloCaminhao,
                                    @RequestParam String nomeBalanceiro,
                                    @RequestParam String placaVeiculo,
                                    @RequestParam String pesoCapturado) {
        // Aqui você pode gerar o relatório ou PDF com os dados capturados
        System.out.println("Relatório Gerado:");
        System.out.println("Modelo do Caminhão: " + modeloCaminhao);
        System.out.println("Nome do Balanceiro: " + nomeBalanceiro);
        System.out.println("Placa do Veículo: " + placaVeiculo);
        System.out.println("Peso Capturado: " + pesoCapturado);

        return "balanca";  // Volta para a página principal após gerar o relatório
    }
}
