package com.ceasacampinas.telnetClient.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Properties;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;

import org.springframework.stereotype.Service;

import com.itextpdf.io.IOException;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;

import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;

@Service
public class TelnetClient {

	 private static final String CONTADOR_PATH = "contador.properties";
    private static final String IP_ADDRESS = "192.168.131.170";  // IP do equipamento
    private static final int PORT = 9000;  // Porta do equipamento
    private String pesoCapturado;  // Variável para armazenar o peso capturado
    public BigDecimal peso;

    // Método para capturar o peso sob demanda
    private int lerContador() {
        Properties prop = new Properties();
        int contador = 1; // Valor inicial padrão
        try (FileInputStream input = new FileInputStream(CONTADOR_PATH)) {
            prop.load(input);
            contador = Integer.parseInt(prop.getProperty("contador"));
        } catch (IOException | java.io.IOException e) {
            System.out.println("Erro ao ler o arquivo de contador: " + e.getMessage());
        }
        return contador;
    }
    
    private void salvarContador(int contador) throws FileNotFoundException, java.io.IOException {
        Properties prop = new Properties();
        try (FileOutputStream output = new FileOutputStream(CONTADOR_PATH)) {
            prop.setProperty("contador", String.valueOf(contador));
            prop.store(output, null);
        } catch (IOException e) {
            System.out.println("Erro ao salvar o contador: " + e.getMessage());
        }
    }
   
    
    public BigDecimal capturarPeso() {
        BigDecimal peso = BigDecimal.ZERO;
        try {
            // Conectando ao IP e porta do equipamento que coleta os dados da balança
            Socket socket = new Socket(IP_ADDRESS, PORT);
            System.out.println("Conectado ao equipamento de coleta de peso no IP " + IP_ADDRESS + " e porta " + PORT);

            // Lendo o dado de peso enviado pelo equipamento
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String pesoCapturado = in.readLine();  // Captura a primeira linha de dados

            // Remover caracteres não numéricos (exceto ponto ou vírgula, se necessário)
            String pesoFiltrado = pesoCapturado.replaceAll("[^\\d]", ""); // Mantém apenas os dígitos
            
            if (!pesoFiltrado.isEmpty()) {
                // Convertendo a string para BigDecimal, assumindo que o peso está em gramas e dividindo por 1000 para obter em kg
                peso = new BigDecimal(pesoFiltrado).divide(BigDecimal.valueOf(1000));
            }
            
            System.out.println("Peso recebido: " + peso);
            socket.close();
        } catch (Exception e) {
            System.err.println("Erro ao conectar ao equipamento: " + e.getMessage());
        }

        return peso;  // Retorna o peso capturado para ser utilizado na aplicação
    }
    public BigDecimal getPesoCapturado() {
        return peso;
    }
    
    public void imprimirEtiqueta(String placa, String destino, String valor, LocalDateTime dataPesagem, BigDecimal peso) {
        // Obter o peso capturado
       
    	 int contador = lerContador();
    	
    	 BigDecimal pesokg = peso.divide(BigDecimal.valueOf(1000));  // Dividir por 1000 para converter gramas para kg
    	
        if (pesokg == null) {
        	System.out.println("Erro na captura do peso");  // Tratar o caso de falha na captura do peso
        	
        }
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dataFormatada =  dataPesagem.format(dateFormatter);  // Usa 'format()' para a data

        // Formata a hora como "HH:mm"
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String horaFormatada = dataPesagem.format(timeFormatter);  // Usa 'format()' para a hora
        // Gerar o comando ZPL
        String zpl = "^XA\n" +
                "^PW480\n" +
                "^LL320\n" +
                "^FT30,212^A0N,23,25^FH\\^CI28^FDPlaca:^FS^CI27\n" +
                "^FT115,215^A0N,23,25^FH\\^CI28^FD" + placa + "^FS^CI27\n" +  // Placa do veículo
                "^FT30,182^A0N,23,25^FH\\^CI28^FDDestino:^FS^CI27\n" +
                "^FT115,182^A0N,23,25^FH\\^CI28^FD" + destino + "^FS^CI27\n" +  // Destino
                "^FT30,255^A0N,23,25^FH\\^CI28^FDValor:^FS^CI27\n" +
                "^FT115,254^A0N,23,25^FH\\^CI28^FD" + valor + "^FS^CI27\n" +  // Valor
                "^FT125,114^A0N,39,51^FH\\^CI28^FD" + pesokg + "kg^FS^CI27\n" +  // Peso
                "^FT30,33^A0N,23,23^FH\\^CI28^FD" + dataFormatada + "^FS^CI27\n" +  // Data formatada
                "^FT30,55^A0N,23,23^FH\\^CI28^FD" + horaFormatada + "^FS^CI27\n" +  // Hora formatada
                "^FT30,300^A0N,18,18^FH\\^CI28^FDContador: " + contador + "^FS^CI27\n" +  // Contador
                "^PQ2,0,1,Y\n" +
                "^XZ";

        // Enviar o comando ZPL para a impressora
       imprimirViaUSB(zpl);
       
       // Incrementar o contador e salvar de volta no arquivo
       contador++;
       try {
		salvarContador(contador);
	} catch (FileNotFoundException e) {
		System.out.println("Erro file not found exception");
		e.printStackTrace();
	} catch (java.io.IOException e) {
		System.out.println("Erro javaio exceexception");
		e.printStackTrace();
	}
    }
    
    public void imprimirViaUSB(String zplComando) {
        try {
            // Localizar a impressora Zebra conectada via USB
            PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
            PrintService zebraPrinter = null;
            for (PrintService printer : printServices) {
                if (printer.getName().contains("ZDesigner ZD220-203dpi ZPL")) {  // Substitua "Zebra" pelo nome exato da sua impressora
                    zebraPrinter = printer;
                    break;
                }
            }

            if (zebraPrinter == null) {
                System.out.println("Impressora Zebra não encontrada!");
                return;
            }

            // Converter o comando ZPL para InputStream
            InputStream zplInputStream = new ByteArrayInputStream(zplComando.getBytes());

            // Definir os atributos de impressão
            DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
            Doc doc = new SimpleDoc(zplInputStream, flavor, null);
            PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();

            // Criar um trabalho de impressão e enviá-lo para a impressora
            DocPrintJob job = zebraPrinter.createPrintJob();
            job.print(doc, pras);

            System.out.println("Etiqueta enviada para a impressora Zebra via USB.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public <LocalDataTime> String gerarEtiquetaZPL(String placa, String destino, String valor, LocalDateTime  dataPesagem, BigDecimal peso) {
      //  String pesoFormatado = String.format("%.3f", peso);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dataFormatada =  dataPesagem.format(dateFormatter);  // Usa 'format()' para a data

        // Formata a hora como "HH:mm"
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String horaFormatada = dataPesagem.format(timeFormatter);  // Usa 'format()' para a hora


    	    // Código ZPL gerado dinamicamente com os valores passados
    	    String zpl = "^XA\n" +
    	                 "^PW480\n" +
    	                 "^LL320\n" +
    	                 "^FT30,212^A0N,23,25^FH\\^CI28^FDPlaca:^FS^CI27\n" +
    	                 "^FT115,215^A0N,23,25^FH\\^CI28^FD" + placa + "^FS^CI27\n" +  // Placa do veículo
    	                 "^FT30,182^A0N,23,25^FH\\^CI28^FDDestino:^FS^CI27\n" +
    	                 "^FT115,182^A0N,23,25^FH\\^CI28^FD" + destino + "^FS^CI27\n" +  // Destino
    	                 "^FT30,255^A0N,23,25^FH\\^CI28^FDValor:^FS^CI27\n" +
    	                 "^FT115,254^A0N,23,25^FH\\^CI28^FD" + valor + "^FS^CI27\n" +  // Valor
    	                 "^FT125,114^A0N,39,51^FH\\^CI28^FD" + peso + "kg^FS^CI27\n" +  // Peso
    	                 "^FT30,33^A0N,23,23^FH\\^CI28^FD" + dataFormatada + "^FS^CI27\n" +  // Data formatada
    	                 "^FT30,55^A0N,23,23^FH\\^CI28^FD" + horaFormatada + "^FS^CI27\n" +  // Hora formatada
    	                 "^PQ1,0,1,Y\n" +
    	                 "^XZ";

    	    return zpl;
    	}

    public  void salvarEmArquivo(String nomeArquivo, String conteudo) throws java.io.IOException {
        try (FileWriter writer = new FileWriter(nomeArquivo)) {
            writer.write(conteudo);
            System.out.println("Arquivo ZPL salvo com sucesso: " + nomeArquivo);
        } catch (IOException e) {
            System.err.println("Erro ao salvar arquivo: " + e.getMessage());
        }
    }
}
	
    
