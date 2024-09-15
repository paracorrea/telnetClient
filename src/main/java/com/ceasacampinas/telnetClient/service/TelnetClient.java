package com.ceasacampinas.telnetClient.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;

import org.springframework.stereotype.Service;

import com.itextpdf.io.IOException;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
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
import java.io.FileWriter;
import java.io.InputStream;

@Service
public class TelnetClient {

    private static final String IP_ADDRESS = "192.168.131.170";  // IP do equipamento
    private static final int PORT = 9000;  // Porta do equipamento
    private String pesoCapturado;  // Variável para armazenar o peso capturado

    // Método para capturar o peso sob demanda
    
    public BigDecimal capturarPeso1() {
        try {
            // Simulação da captura do peso via socket
            String pesoBruto = "00000900000";  // Exemplo do que a balança pode enviar
            System.out.println("Peso recebido bruto: " + pesoBruto);

            // Remover zeros à esquerda e ajustar o formato
            //pesoBruto = pesoBruto.replaceFirst("^0+", "");  // Remove zeros à esquerda

            // Se necessário, remover zeros extras no final
            //pesoBruto = pesoBruto.replaceAll("0+$", "");  // Remove zeros à direita

            System.out.println("Peso processado: " + pesoBruto);

            // Converter a string para BigDecimal, e dividir por 1000 para considerar decimais (se for o caso)
            BigDecimal pesoFormatado = new BigDecimal(pesoBruto).divide(BigDecimal.valueOf(1000));

            System.out.println("Peso formatado: " + pesoFormatado + " kg");

            return pesoFormatado;  // Retorna o peso formatado
        } catch (Exception e) {
            System.err.println("Erro ao processar o peso: " + e.getMessage());
            return BigDecimal.ZERO;  // Retorna zero em caso de erro
        }
    }
    
    public String capturarPeso() {
        try {
            // Conectando ao IP e porta do equipamento que coleta os dados da balança
            Socket socket = new Socket(IP_ADDRESS, PORT);
            System.out.println("Conectado ao equipamento de coleta de peso no IP " + IP_ADDRESS + " e porta " + PORT);

            // Lendo o dado de peso enviado pelo equipamento
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pesoCapturado = in.readLine();  // Captura a primeira linha de dados
            
            
            
            System.out.println("Peso recebido: " + pesoCapturado);
           

            socket.close();
        } catch (Exception e) {
            System.err.println("Erro ao conectar ao equipamento: " + e.getMessage());
            pesoCapturado = null;  // Caso haja erro, o peso será null
        }

        return pesoCapturado;  // Retorna o peso capturado para ser utilizado na aplicação
    }

    public String getPesoCapturado() {
        return pesoCapturado;
    }
    
    public void imprimirEtiqueta(String placa, String destino, String valor, String data, String hora) {
        // Obter o peso capturado
        String peso = getPesoCapturado();
        if (peso == null) {
            peso = "Erro na captura do peso";  // Tratar o caso de falha na captura do peso
        }
        
        // Gerar o comando ZPL
        String zpl = "^XA\n" +
                     "^FT49,212^A0N,25,28^FH\\^CI28^FDPlaca:^FS^CI27\n" +
                     "^FT152,215^A0N,28,28^FH\\^CI28^FD" + placa + "^FS^CI27\n" +
                     "^FT49,182^A0N,28,28^FH\\^CI28^FDDestino:^FS^CI27\n" +
                     "^FT152,182^A0N,28,28^FH\\^CI28^FD" + destino + "^FS^CI27\n" +
                     "^FT52,255^A0N,25,28^FH\\^CI28^FDValor:^FS^CI27\n" +
                     "^FT150,254^A0N,28,28^FH\\^CI28^FD" + valor + "^FS^CI27\n" +
                     "^FT125,114^A0N,39,51^FH\\^CI28^FD" + peso + " kg^FS^CI27\n" +
                     "^FT381,33^A0N,23,23^FH\\^CI28^FD" + hora + "^FS^CI27\n" +
                     "^FT361,55^A0N,23,23^FH\\^CI28^FD" + data + "^FS^CI27\n" +
                     "^PQ1,0,1,Y\n" +
                     "^XZ";

        // Enviar o comando ZPL para a impressora
       imprimirViaUSB(zpl);
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
    public String gerarEtiquetaZPL(String placa, String destino, String valor, String dataPesagem, BigDecimal peso) {
      //  String pesoFormatado = String.format("%.3f", peso);

        // Código ZPL gerado dinamicamente com os valores passados
        String zpl = "^XA\n" +
                     "^PW480\n" +
                     "^LL320\n" +
                     "^FT49,212^A0N,25,28^FH\\^CI28^FDPlaca:^FS^CI27\n" +
                     "^FT152,215^A0N,28,28^FH\\^CI28^FD" + placa + "^FS^CI27\n" +  // Placa do veículo
                     "^FT49,182^A0N,28,28^FH\\^CI28^FDDestino:^FS^CI27\n" +
                     "^FT152,182^A0N,28,28^FH\\^CI28^FD" + destino + "^FS^CI27\n" +  // Destino
                     "^FT52,255^A0N,25,28^FH\\^CI28^FDValor:^FS^CI27\n" +
                     "^FT150,254^A0N,28,28^FH\\^CI28^FD" + valor + "^FS^CI27\n" +  // Valor
                     "^FT125,114^A0N,39,51^FH\\^CI28^FD" + peso + " kg^FS^CI27\n" +  // Peso formatado
                     "^FT381,33^A0N,23,23^FH\\^CI28^FD" + dataPesagem + "^FS^CI27\n" +  // Data de pesagem
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
	
    
