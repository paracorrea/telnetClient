package com.ceasacampinas.telnetClient.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ceasacampinas.telnetClient.enums.TipoPesagem;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.validation.constraints.Digits;




public class Balanca implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	

	@Digits(integer = 10, fraction =0, message = "O valor deve ter no máximo 10 dígitos inteiros" )
	private BigDecimal contador;
	

	@Digits(integer = 10, fraction =1, message = "O valor deve ter no máximo 10 dígitos inteiros e 2 decimal" )
	private BigDecimal peso;
	
	
	private LocalDateTime dataPesagem;
	
	@Enumerated(EnumType.STRING)
	private TipoPesagem tipo;

	private String destino;
	
	private String placa;
	
	@Digits(integer = 10, fraction =1, message = "O valor deve ter no máximo 10 dígitos inteiros e 2 decimal" )
	private BigDecimal valor;

	public Long getId() {
		return id;
	}

	public BigDecimal getContador() {
		return contador;
	}

	public BigDecimal getPeso() {
		return peso;
	}

	public LocalDateTime getDataPesagem() {
		return dataPesagem;
	}

	public TipoPesagem getTipo() {
		return tipo;
	}

	public String getDestino() {
		return destino;
	}

	public String getPlaca() {
		return placa;
	}

	public BigDecimal getValor() {
		return valor;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setContador(BigDecimal contador) {
		this.contador = contador;
	}

	public void setPeso(BigDecimal peso) {
		this.peso = peso;
	}

	public void setDataPesagem(LocalDateTime dataPesagem) {
		this.dataPesagem = dataPesagem;
	}

	public void setTipo(TipoPesagem tipo) {
		this.tipo = tipo;
	}

	public void setDestino(String destino) {
		this.destino = destino;
	}

	public void setPlaca(String placa) {
		this.placa = placa;
	}

	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}
	
	
	
}
