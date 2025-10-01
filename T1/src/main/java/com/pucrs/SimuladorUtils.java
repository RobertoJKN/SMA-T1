package com.pucrs;

import java.util.Random;

public class SimuladorUtils {

  private final Random gerador;
  private final int limiteNumeros;
  private int utilizados;

  public SimuladorUtils(int limiteNumeros, int semente) {
    this.gerador = new Random(semente);
    this.limiteNumeros = limiteNumeros;
    this.utilizados = 0;
  }

  public double uniforme(double minimo, double maximo) {
    return minimo + (maximo - minimo) * uniforme01();
  }

  public double uniforme01() {
    if (utilizados >= limiteNumeros)
      throw new RuntimeException("Limite de números aleatórios atingido.");

    double valor = gerador.nextDouble();
    utilizados++;
    return valor;
  }

  public int getUtilizados() {
    return utilizados;
  }

  public boolean limiteAtingido() {
    return utilizados >= limiteNumeros;
  }
}
