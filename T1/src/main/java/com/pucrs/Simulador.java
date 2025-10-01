package com.pucrs;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class Simulador {

  private static final Logger logger = LoggerFactory.getLogger(Simulador.class);
  private List<Fila> filas;
  private Map<String, Integer> mapeamentoFilas;
  private Escalonador escalonador;
  private SimuladorUtils utilitarios;
  private Model modelo;

  public void executar(String arquivoYaml) throws Exception {
    carregarModelo(arquivoYaml);
    inicializarFilas();
    inicializarPrimeirosEventos();

    double tempoAtual = 0.0;

    while (escalonador.temEventos() && !utilitarios.limiteAtingido()) {
      Evento evento = escalonador.proximoEvento();
      double novoTempo = evento.getMomento();

      for (Fila fila : filas) {
        fila.atualizarTempo(novoTempo);
      }

      tempoAtual = novoTempo;

      if (evento.getCategoria() == 0) {
        processarChegada(evento, tempoAtual);
      } else {
        processarSaida(evento, tempoAtual);
      }
    }

    exibirResultados(tempoAtual);
  }

  private void carregarModelo(String arquivoYaml) throws Exception {
    LoaderOptions opcoes = new LoaderOptions();
    Constructor construtor = new Constructor(Model.class, opcoes);
    Yaml yaml = new Yaml(construtor);

    try (InputStream entrada = new FileInputStream(arquivoYaml)) {
      modelo = yaml.load(entrada);
    }
  }

  private void inicializarFilas() {
    filas = new ArrayList<>();
    mapeamentoFilas = new HashMap<>();
    escalonador = new Escalonador();
    utilitarios = new SimuladorUtils(modelo.rndnumbersPerSeed, modelo.seeds.get(0));

    int indice = 0;
    for (Map.Entry<String, Model.QueueDef> entrada : modelo.queues.entrySet()) {
      Model.QueueDef definicao = entrada.getValue();

      int capacidade;

      if (definicao.capacity.equalsIgnoreCase("INF")) {
        capacidade = Integer.MAX_VALUE;
      } else {
        try {
          capacidade = Integer.parseInt(definicao.capacity);
        } catch (NumberFormatException _) {
          throw new IllegalArgumentException("Invalid capacity: " + definicao.capacity);
        }
      }

      Fila fila = new Fila(
          entrada.getKey(),
          definicao.servers,
          capacidade,
          definicao.minArrival,
          definicao.maxArrival,
          definicao.minService,
          definicao.maxService);
      filas.add(fila);
      mapeamentoFilas.put(entrada.getKey(), indice++);
    }

    for (Model.Route rota : modelo.network) {
      int origem = mapeamentoFilas.get(rota.source);
      int destino;
      if (rota.target.equals("EXIT")) {
        destino = -1;
      } else {
        Integer destinoObj = mapeamentoFilas.get(rota.target);
        if (destinoObj == null) {
          throw new IllegalArgumentException("Target queue '" + rota.target + "' not found in mapeamentoFilas.");
        }
        destino = destinoObj;
      }
      filas.get(origem).adicionarRota(new Roteamento(destino, rota.probability));
    }
  }

  private void inicializarPrimeirosEventos() {
    if (modelo.arrivals != null) {
      for (Map.Entry<String, Double> entrada : modelo.arrivals.entrySet()) {
        int destino = mapeamentoFilas.get(entrada.getKey());
        escalonador.agendarEvento(new Evento(entrada.getValue(), -1, destino, 0));
      }
    }
  }

  private void processarChegada(Evento evento, double tempoAtual) {
    Fila fila = filas.get(evento.getDestino());

    if (fila.getClientes() >= fila.capacidade()) {
      fila.incrementarPerdas();
    } else {
      fila.incrementarClientes();

      if (fila.getClientes() <= fila.servidores()) {
        double tempoAtendimento = utilitarios.uniforme(fila.getMinAtendimento(), fila.getMaxAtendimento());
        escalonador.agendarEvento(new Evento(tempoAtual + tempoAtendimento, evento.getDestino(), evento.getDestino(), 1));
      }
    }

    if (evento.getOrigem() == -1 && fila.getMinChegada() != null) {
      double intervaloChegada = utilitarios.uniforme(fila.getMinChegada(), fila.getMaxChegada());
      escalonador.agendarEvento(new Evento(tempoAtual + intervaloChegada, -1, evento.getDestino(), 0));
    }
  }

  private void processarSaida(Evento evento, double tempoAtual) {
    Fila filaOrigem = filas.get(evento.getOrigem());

    filaOrigem.decrementarClientes();

    if (filaOrigem.getClientes() >= filaOrigem.servidores()) {
      double tempoAtendimento = utilitarios.uniforme(filaOrigem.getMinAtendimento(), filaOrigem.getMaxAtendimento());
      escalonador.agendarEvento(new Evento(tempoAtual + tempoAtendimento, evento.getOrigem(), evento.getDestino(), 1));
    }

    double valorAleatorio = utilitarios.uniforme01();
    double acumulado = 0.0;

    for (Roteamento rota : filaOrigem.getRotas()) {
      acumulado += rota.getProbability();
      if (valorAleatorio <= acumulado) {
        if (rota.getTarget() != -1) {
          escalonador.agendarEvento(new Evento(tempoAtual, evento.getOrigem(), rota.getTarget(), 0));
        }
        break;
      }
    }
  }

  private void exibirResultados(double tempoSimulacao) {
    if (logger.isInfoEnabled()) {
      logger.info("=== Fim da simulacao (tempo {}) ===", String.format("%.4f", tempoSimulacao));
    }
    logger.info("");
    
    for (Fila fila : filas) {
      exibirResultadosFila(fila, tempoSimulacao);
    }

    exibirResumoFinal(tempoSimulacao);
  }

  private void exibirResultadosFila(Fila fila, double tempoSimulacao) {
    exibirCabecalhoFila(fila);
    exibirTemposFila(fila, tempoSimulacao);
    exibirPerdasFila(fila);
  }

  private void exibirCabecalhoFila(Fila fila) {
    logger.info("*********************************************************");

    String cabecalho = String.format("Fila:   %s (G/G/%d%s)",
        fila.getNome(),
        fila.servidores(),
        fila.capacidade() == Integer.MAX_VALUE ? "" : ("/" + fila.capacidade()));
    logger.info(cabecalho);

    if (fila.getMinChegada() != null && logger.isInfoEnabled()) {
        logger.info("Chegada: {} ... {}", String.format("%.1f", fila.getMinChegada()), String.format("%.1f", fila.getMaxChegada()));
      }
    

    if (logger.isInfoEnabled()) {
      logger.info("Servico: {} ... {}", String.format("%.1f", fila.getMinAtendimento()), String.format("%.1f", fila.getMaxAtendimento()));
    }

    logger.info("*********************************************************");
    if (logger.isInfoEnabled()) {
      logger.info(String.format("%6s %20s %22s", "Estado", "Tempo", "Probabilidade"));
    }
  }

  private void exibirTemposFila(Fila fila, double tempoSimulacao) {
    double[] tempos = fila.getTempos();
    for (int i = 0; i < tempos.length; i++) {
      if (tempos[i] > 0.0) {
        double percentual = (tempoSimulacao > 0) ? (tempos[i] / tempoSimulacao) * 100 : 0;
        if (logger.isInfoEnabled()) {
          logger.info(String.format("%6d %20.4f %21.2f%%", i, tempos[i], percentual));
        }
      }
    }
  }

  private void exibirPerdasFila(Fila fila) {
    logger.info("");
    logger.info("Numero de perdas: {}", fila.perdas());
    logger.info("");
  }

  private void exibirResumoFinal(double tempoSimulacao) {
    logger.info("=========================================================");
    if (logger.isInfoEnabled()) {
      logger.info("Tempo medio de simulacao: {}", String.format("%.4f", tempoSimulacao));
    }
    logger.info("=========================================================");
  }

  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      logger.error("Uso: java SimuladorRedeFilas <config.yaml>");
      System.exit(1);
    }
    new Simulador().executar(args[0]);
  }
}
