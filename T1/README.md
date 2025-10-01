# Integrantes do grupo: Roberto Neto, João Pedro Ramos, Arthur Ariel dos Santos, Erik Ribeiro

# Como Executar o Simulador

## Pré-requisitos

- **Java 24** ou superior configurado no sistema
- **Make** (ferramenta opcional para automação)

## Configuração do arquivo `modelo.yaml`

O arquivo de configuração `modelo.yaml` define todos os parâmetros necessários para executar a simulação, incluindo características das filas, número de servidores, intervalos de chegada e atendimento, regras de roteamento entre filas e sementes para geração de números aleatórios.

Segue um exemplo de configuração utilizada nos testes do projeto:

```yaml
arrivals:
  Q1: 2.0
queues:
  Q1:
    servers: 1
    capacity: "INF"
    minArrival: 2.0
    maxArrival: 4.0
    minService: 1.0
    maxService: 2.0
  Q2:
    servers: 2
    capacity: 5
    minService: 4.0
    maxService: 8.0
  Q3:
    servers: 2
    capacity: 10
    minService: 5.0
    maxService: 15.0

network:
  - source: Q1
    target: Q2
    probability: 0.8
  - source: Q1
    target: Q3
    probability: 0.2
  - source: Q2
    target: Q1
    probability: 0.3
  - source: Q2
    target: Q3
    probability: 0.5
  - source: Q2
    target: EXIT
    probability: 0.2
  - source: Q3
    target: Q2
    probability: 0.7
  - source: Q3
    target: EXIT
    probability: 0.3

rndnumbersPerSeed: 100000
seeds:
  - 1
  - 2
  - 3
  - 4
  - 5
```

**Importante:** Mantenha a sintaxe YAML válida e modifique os parâmetros de acordo com o cenário de simulação desejado.

## Executando o Simulador

Verifique se o arquivo `modelo.yaml` está localizado no mesmo diretório do executável `.jar` ou ajuste o caminho conforme necessário.

### Execução via linha de comando:

```bash
java -jar target/SMA-T1-1.0-SNAPSHOT-jar-with-dependencies.jar ./modelo.yaml
```

### Execução com Make (alternativa):

Se você possui Make instalado no sistema:

```bash
make start
```
