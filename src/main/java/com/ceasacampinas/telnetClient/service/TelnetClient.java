package com.ceasacampinas.telnetClient.service;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;

import org.springframework.stereotype.Service;

import com.itextpdf.io.IOException;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;

import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import com.itextpdf.layout.property.TextAlignment;

import com.itextpdf.layout.property.VerticalAlignment;

import jakarta.servlet.http.HttpServletResponse;

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
                peso = new BigDecimal(pesoFiltrado).divide(BigDecimal.valueOf(1000),0, RoundingMode.DOWN);
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
    	
    	 BigDecimal pesokg = peso.divide(BigDecimal.valueOf(1000),0, RoundingMode.DOWN);  // Dividir por 1000 para converter gramas para kg
    	
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
                "^FT30,255^A0N,28,29^FH\\^CI28^FDValor R$:^FS^CI27\n" +
                "^FT150,255^A0N,28,29^FH\\^CI28^FD" + valor + "^FS^CI27\n" +  // Valor
                "^FT125,114^A0N,39,51^FH\\^CI28^FD" + pesokg + "kg^FS^CI27\n" +  // Peso
                "^FT30,33^A0N,23,23^FH\\^CI28^FD" + dataFormatada + "^FS^CI27\n" +  // Data formatada
                "^FT30,55^A0N,23,23^FH\\^CI28^FD" + horaFormatada + "^FS^CI27\n" +  // Hora formatada
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
                "^FT30,255^A0N,28,29^FH\\^CI28^FDValor R$ :^FS^CI27\n" +
                "^FT115,300^A0N,28,29^FH\\^CI28^FD" + valor + "^FS^CI27\n" +  // Valor
                "^FT125,114^A0N,39,51^FH\\^CI28^FD" + peso + "kg^FS^CI27\n" +  // Peso
                "^FT30,33^A0N,23,23^FH\\^CI28^FD" + dataFormatada + "^FS^CI27\n" +  // Data formatada
                "^FT30,55^A0N,23,23^FH\\^CI28^FD" + horaFormatada + "^FS^CI27\n" +  // Hora formatada
                "^PQ2,0,1,Y\n" +
                "^XZ";


    	    return zpl;
    	}
    
 
    public void gerarPdfRelatorioNumerado(HttpServletResponse response, int numeroInicial, int quantidade) throws IOException, java.io.IOException {
        PdfWriter writer = new PdfWriter(response.getOutputStream());
        PdfDocument pdf = new PdfDocument(writer);
        PageSize pageSize = PageSize.A4;
        Document document = new Document(pdf, pageSize);

        // Loop para gerar a quantidade de páginas
        for (int i = numeroInicial; i < numeroInicial + quantidade; i++) {
                        

            // Criação do cabeçalho
            Table headerTable = new Table(new float[]{4, 1}); // Definir 2 colunas (proporção 4:1)
            headerTable.setWidth(500); // Ocupa 100% da largura da página

            // Logo à esquerda
            Image logo = new Image(ImageDataFactory.create("src/main/resources/logo4.png"));
            logo.setWidth(130); // Largura de 4 cm (aproximado)
            logo.setHeight(55); // Altura de 3 cm (aproximado)
            Cell logoCell = new Cell().add(logo);
            logoCell.setBorder(Border.NO_BORDER); // Sem borda
            logoCell.setTextAlignment(TextAlignment.LEFT);
            headerTable.addCell(logoCell);

            // Numeração à direita
            Cell numeroCell = new Cell().add(new Paragraph(" Nº " + i)
            		
                .setFontSize(14)
                .setBold());
            numeroCell.setBorder(new SolidBorder(1));
            numeroCell.setPaddingRight(40);
            
            
           // numeroCell.setBorderRadius(new BorderRadius(1));
            // Sem borda ao redor do número
            numeroCell.setMarginLeft(400 	);// Espaçamento interno
            numeroCell.setTextAlignment(TextAlignment.RIGHT);
            numeroCell.setVerticalAlignment(VerticalAlignment.MIDDLE); // Centralizado verticalmente
            
            
            headerTable.addCell(numeroCell);

            // Adicionar a tabela de cabeçalho ao documento
            document.add(headerTable);

            // Definir a largura das colunas para a área de pesagem e borda ao lado
            float[] columnWidths = {150, 900}; // Proporção de 1:5 entre as colunas de pesagem e a borda ao lado

            // Criar tabela para as pesagens e bordas ao lado
            Table pesagemTable = new Table(columnWidths);
            pesagemTable.setWidth(500); // Ocupa 100% da largura da página
            pesagemTable.setMarginTop(50); // Espaçamento do topo

            // Adicionar as quatro áreas de pesagem e bordas ao lado
            for (int j = 1; j <= 4; j++) {
                // Célula para a pesagem
                Cell pesagemCell = new Cell().add(new Paragraph(j + "ª \nPESAGEM")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(ColorConstants.GRAY)
                    .setPaddingTop(45)
                    .setTextAlignment(TextAlignment.CENTER));
                pesagemCell.setHeight(150); // Altura de cada espaço para pesagem
              
                pesagemCell.setBorder(new SolidBorder(1)); // Borda ao redor de cada espaço de pesagem
                pesagemTable.addCell(pesagemCell);

                // Célula para a borda vazia ao lado
                Cell bordaVaziaCell = new Cell().add(new Paragraph("")) // Célula vazia ao lado
                    .setHeight(150) // Altura igual à das pesagens
                    .setWidth(900)
                    .setBorder(new SolidBorder(1)); // Borda ao redor da célula vazia
                pesagemTable.addCell(bordaVaziaCell);
            }

            // Adicionar a tabela de pesagens ao documento
            document.add(pesagemTable);
        }

        document.close();
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
	
    
