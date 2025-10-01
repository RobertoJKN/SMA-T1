package com.pucrs;

import java.util.PriorityQueue;

public class Escalonador {

  private final PriorityQueue<Evento> filaEventos;

  public Escalonador() {
    this.filaEventos = new PriorityQueue<>();
  }

  public void agendarEvento(Evento evento) {
    filaEventos.offer(evento);
  }

  public Evento proximoEvento() {
    return filaEventos.poll();
  }

  public boolean temEventos() {
    return !filaEventos.isEmpty();
  }

  public int quantidadeEventos() {
    return filaEventos.size();
  }
}
