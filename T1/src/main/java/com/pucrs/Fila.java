package com.pucrs;

import java.util.ArrayList;
import java.util.List;

public class Fila {
  private final String nome;
  private final int servidores;
  private final int capacidade;
  private int clientes;
  private int perdas;
  private final Double minChegada;
  private final Double maxChegada;
  private final Double minAtendimento;
  private final Double maxAtendimento;
  private Double ultimaAtualizacao;
  private final double[] tempos;
  private final List<Roteamento> rotas;

  public Fila(String nome, int servidores, int capacidade, Double minChegada, Double maxChegada, double minAtendimento,
      double maxAtendimento) {
    this.nome = nome;
    this.servidores = servidores;
    this.capacidade = capacidade;
    this.minChegada = minChegada;
    this.maxChegada = maxChegada;
    this.minAtendimento = minAtendimento;
    this.maxAtendimento = maxAtendimento;
    this.clientes = 0;
    this.perdas = 0;

    if (capacidade == Integer.MAX_VALUE) {
      this.tempos = new double[75000];
    } else {
      this.tempos = new double[capacidade + 1];
    }

    this.ultimaAtualizacao = 0.0;
    this.rotas = new ArrayList<>();
  }

  public String getNome() {
    return nome;
  }

  public int capacidade() {
    return capacidade;
  }

  public int servidores() {
    return servidores;
  }

  public int perdas() {
    return perdas;
  }

  public Double getMinChegada() {
    return minChegada;
  }

  public Double getMaxChegada() {
    return maxChegada;
  }

  public Double getMinAtendimento() {
    return minAtendimento;
  }

  public Double getMaxAtendimento() {
    return maxAtendimento;
  }

  public int getClientes() {
    return clientes;
  }

  public List<Roteamento> getRotas() {
    return rotas;
  }

  public void adicionarRota(Roteamento r) {
    rotas.add(r);
  }

  public void atualizarTempo(double agora) {
    double diferenca = agora - ultimaAtualizacao;
    int estado = clientes;
    if (estado >= tempos.length) {
      estado = tempos.length - 1;
    }
    tempos[estado] += diferenca;
    ultimaAtualizacao = agora;
  }

  public void incrementarPerdas() {
    perdas++;
  }

  public void incrementarClientes() {
    clientes++;
  }

  public void decrementarClientes() {
    if (clientes > 0) {
      clientes--;
    }
  }

  public double[] getTempos() {
    return tempos;
  }
}
