package com.ceasacampinas.telnetClient.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

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
}