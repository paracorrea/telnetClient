package com.ceasacampinas.telnetClient.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BalancaDto {

	private Long id;
	private BigDecimal count;
	private BigDecimal peso;
	private LocalDateTime dataPesagem;
	public Long getId() {
		return id;
	}
	public BigDecimal getCount() {
		return count;
	}
	public BigDecimal getPeso() {
		return peso;
	}
	public LocalDateTime getDataPesagem() {
		return dataPesagem;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public void setCount(BigDecimal count) {
		this.count = count;
	}
	public void setPeso(BigDecimal peso) {
		this.peso = peso;
	}
	public void setDataPesagem(LocalDateTime dataPesagem) {
		this.dataPesagem = dataPesagem;
	}
	
	
	
	
}
