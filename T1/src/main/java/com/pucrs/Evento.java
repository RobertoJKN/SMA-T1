package com.pucrs;

public class Evento implements Comparable<Evento> {
  private final double momento;
  private final int origem;
  private final int destino;
  private final int categoria;

  public Evento(double momento, int origem, int destino, int categoria) {
    this.momento = momento;
    this.origem = origem;
    this.destino = destino;
    this.categoria = categoria;
  }

  public double getMomento() {
    return momento;
  }

  public int getCategoria() {
    return categoria;
  }

  public int getOrigem() {
    return origem;
  }

  public int getDestino() {
    return destino;
  }

  @Override
  public int compareTo(Evento outroEvento) {
    return Double.compare(this.momento, outroEvento.momento);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    Evento evento = (Evento) obj;
    return Double.compare(evento.momento, momento) == 0 &&
           origem == evento.origem &&
           destino == evento.destino &&
           categoria == evento.categoria;
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(momento, origem, destino, categoria);
  }
}
