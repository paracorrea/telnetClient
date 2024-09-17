package com.ceasacampinas.telnetClient.controller;

import com.ceasacampinas.telnetClient.domain.Balanca;
import com.ceasacampinas.telnetClient.service.TelnetClient;

import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class BalancaController {

    @Autowired
    private TelnetClient telnetClient;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(BigDecimal.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                if (text != null && !text.isEmpty()) {
                    // Substitui a vírgula por ponto antes de converter para BigDecimal
                    text = text.replace(".", "").replace(",", ".");
                    setValue(new BigDecimal(text));
                } else {
                    setValue(null);
                }
            }
        });
    }

    
    @GetMapping("/balanca")
    public String exibirFormularioBalanca(Model model) {
        // Capturar o peso da balançagi
       
    	 //BigDecimal pesoCapturado = capturarPeso();  // Chama o método de teste para capturar o peso
    	BigDecimal pesoCapturado = telnetClient.capturarPeso(); // metodo correto
    	
    	  //BigDecimal pesokg = pesoCapturado.divide(BigDecimal.valueOf(1000), 0, RoundingMode.DOWN);
         
    	  // Verifica se o peso resultante é menor que 5 kg
         // if (pesokg.compareTo(BigDecimal.valueOf(5)) < 0) {
          	// Elimina o valor ou trata como necessário, por exemplo, definindo o peso como zero ou outra ação
         // 	pesokg = BigDecimal.ZERO; // ou outro tratamento que desejar
         // }
    	
          // BigDecimal peso = new BigDecimal(pesoCapturado).divide(BigDecimal.valueOf(1000));
        // Instância de Balanca com valores padrão (caso queira preencher alguns campos previamente)
       
        Balanca balanca = new Balanca();
        balanca.setDataPesagem(LocalDateTime.now()); // Define a data atual para a pesagem
        balanca.setPeso(pesoCapturado);  // Define o peso capturado
        balanca.setContador(new BigDecimal(1));  // Exemplo de contador padrão

        System.out.println("Peso capturado no controller: "+pesoCapturado);
        
       // BigDecimal pesokg = pesoCapturado.divide(BigDecimal.valueOf(1000));

       
        
        // Adiciona a instância ao modelo para que o formulário a utilize
        model.addAttribute("balanca", balanca);
        model.addAttribute("pesoCapturado", pesoCapturado);  // Passa o peso capturado para o modelo

        return "balanca"; // Nome da página HTML do formulário
    }
	/*
	 * @PostMapping("/capturar") public String capturarPeso(@RequestParam String
	 * proprietarioCaminhao,
	 * 
	 * @RequestParam String motoristaCaminhao,
	 * 
	 * @RequestParam String modeloCaminhao,
	 * 
	 * @RequestParam String nomeBalanceiro,
	 * 
	 * @RequestParam String placaVeiculo, Model model) { // Chama o service para
	 * capturar o peso da balança BigDecimal pesoCapturado =
	 * telnetClient.capturarPeso();
	 * 
	 * System.out.println("Peso capturado para o front: " +pesoCapturado);
	 * 
	 * if (pesoCapturado == null) { System.out.println("Erro peso não capturado"); }
	 * 
	 * // Adiciona os dados ao modelo com nomes corretos para evitar sobrescrever
	 * model.addAttribute("proprietarioCaminhao", proprietarioCaminhao);
	 * model.addAttribute("motoristaCaminhao", motoristaCaminhao);
	 * model.addAttribute("modeloCaminhao", modeloCaminhao);
	 * model.addAttribute("nomeBalanceiro", nomeBalanceiro);
	 * model.addAttribute("placaVeiculo", placaVeiculo);
	 * model.addAttribute("pesoCapturado", pesoCapturado);
	 * 
	 * return "redirect:/balanca"; // Retorna à página com os dados }
	 */

   

    
    @PostMapping("/balanca/imprimir")
    public String imprimirBalanca(@ModelAttribute Balanca balanca, Model model) {
        // Preenche os dados automáticos do sistema
       
    	balanca.setPeso(telnetClient.capturarPeso());
        
      
     
        balanca.setDataPesagem(LocalDateTime.now()); // Captura a data e hora atuais
        balanca.setContador(new BigDecimal(1)); // Exemplo de contador

        // Envia para impressão
        telnetClient.imprimirEtiqueta(
        		 balanca.getPlaca(),
                 balanca.getDestino(),
                 balanca.getValor().toString(),
                 balanca.getDataPesagem(),
                 balanca.getPeso()
            
        );

        // Retorna uma mensagem de sucesso
        model.addAttribute("mensagem", "Etiqueta impressa com sucesso!");
        return "redirect:/balanca";
    }
    
	/*
	 * @PostMapping("/balanca/imprimirZPL") public String
	 * imprimirZPL(@ModelAttribute Balanca balanca, Model model) throws IOException
	 * { // Simulação da captura de peso
	 * 
	 * balanca.setPeso(telnetClient.capturarPeso());
	 * //balanca.setPeso(telnetClient.capturarPeso());
	 * balanca.setDataPesagem(LocalDateTime.now());
	 * 
	 * // Gera o código ZPL String zpl = telnetClient.gerarEtiquetaZPL(
	 * balanca.getPlaca(), balanca.getDestino(), balanca.getValor().toString(),
	 * balanca.getDataPesagem(), balanca.getPeso() );
	 * 
	 * // Retorna o código ZPL gerado para ser exibido na página
	 * model.addAttribute("zplCode", zpl); model.addAttribute("mensagem",
	 * "ZPL gerado com sucesso!");
	 * 
	 * telnetClient.salvarEmArquivo("zpl.txt", zpl); return "redirect:/balanca"; //
	 * Página que exibirá o ZPL gerado }
	 * 
	 * // Método fictício para capturar o peso
	 */   
    private BigDecimal converterParaBigDecimal(String valor) {
        // Substitui vírgula por ponto para fazer a conversão correta
        String valorFormatado = valor.replace(".", "").replace(",", ".");
        return new BigDecimal(valorFormatado);
    }
}
