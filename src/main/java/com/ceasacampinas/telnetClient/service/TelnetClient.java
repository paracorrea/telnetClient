package com.ceasacampinas.telnetClient.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;

import org.springframework.stereotype.Service;

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
import java.io.InputStream;

@Service
public class TelnetClient {

    private static final String IP_ADDRESS = "192.168.131.170";  // IP do equipamento
    private static final int PORT = 9000;  // Porta do equipamento
    private String pesoCapturado;  // Variável para armazenar o peso capturado

    // Método para capturar o peso sob demanda
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

    
    	
        public byte[] gerarPdf(String proprietarioCaminhao, String motoristaCaminhao, String modeloCaminhao,
                               String nomeBalanceiro, String placaVeiculo, String pesoCapturado) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

                // Configurando o PDFWriter
                PdfWriter writer = new PdfWriter(baos);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf, PageSize.A4);

                // Adicionar logo (substitua "logo.png" com o caminho do seu logo)
                Image logo = new Image(ImageDataFactory.create((getClass().getClassLoader().getResource("logo.png"))));
                logo.setWidth(UnitValue.createPercentValue(100));  // Ajusta a largura para 100%
                logo.setHeight(120);  // Altura ajustada para 120px
                document.add(logo);

                // Adicionar título com borda
                Paragraph title = new Paragraph("Sistema CEASA de controle de PESO")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setBold()
                        .setFontSize(16);
                document.add(title);

                // Adicionar linha separadora
               // document.add(new AreaBreak());

                // Adicionar detalhes
                document.add(new Paragraph("Proprietário do Caminhão: " + proprietarioCaminhao).setFontSize(12));
                document.add(new Paragraph("Motorista: " + motoristaCaminhao).setFontSize(12));
                document.add(new Paragraph("Modelo: " + modeloCaminhao + " - Placa: " + placaVeiculo).setFontSize(12));
                document.add(new Paragraph("Peso Capturado: " + pesoCapturado).setFontSize(12));

                // Adicionar data atual
                String dataAtual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                document.add(new Paragraph("Data: " + dataAtual).setFontSize(12));

                // Adicionar nome do balanceiro
                document.add(new Paragraph("Nome do Balanceiro: " + nomeBalanceiro).setFontSize(12));

                // Adicionar espaço para assinatura
                document.add(new Paragraph("\n\nAssinatura: ____________________________________").setFontSize(12));

                document.close();
                return baos.toByteArray();  // Retorna o conteúdo do PDF como byte[]
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
