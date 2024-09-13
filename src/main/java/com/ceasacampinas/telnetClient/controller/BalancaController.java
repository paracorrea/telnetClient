package com.ceasacampinas.telnetClient.controller;

import com.ceasacampinas.telnetClient.service.TelnetClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class BalancaController {

    @Autowired
    private TelnetClient telnetClient;

    @GetMapping("/")
    public String iniciar() {
    	return "balanca";
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

    
    @PostMapping("/imprimir")
    public ResponseEntity<byte[]> imprimirRelatorio(@RequestParam String proprietarioCaminhao,
                                                    @RequestParam String motoristaCaminhao,
                                                    @RequestParam String modeloCaminhao,
                                                    @RequestParam String nomeBalanceiro,
                                                    @RequestParam String placaVeiculo,
                                                    @RequestParam String pesoCapturado) {
        // Gerar PDF com os dados
        byte[] pdfBytes = telnetClient.gerarPdf(proprietarioCaminhao, motoristaCaminhao, modeloCaminhao, nomeBalanceiro, placaVeiculo, pesoCapturado);

        // Configurar o retorno do PDF
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "relatorio_peso.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
