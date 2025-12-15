# MoraisDaBet -- Goltrix Adapter

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white)
![Spring
Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=flat&logo=springboot&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=flat&logo=gradle&logoColor=white)
![Coroutines](https://img.shields.io/badge/Coroutines-0095D5?style=flat&logo=kotlin&logoColor=white)

## Descrição

Adaptador em **Kotlin + Spring Boot** para integração com a **API
pública da Betfair**, responsável por consultar eventos, extrair
mercados relevantes (Over/Under e First Half Goals), transformar dados
em DTOs e persistir resultados na tabela `goltrix` utilizando
R2DBC.

## Tecnologias

-   Kotlin
-   Spring Boot / Data (JPA/R2DBC)
-   Coroutines
-   Gradle
- Betfair / Fulltrader / Sofascore

## Arquitetura

- O projeto segue o padrão Hexagonal Architecture (Ports & Adapters), garantindo baixo acoplamento e facilidade de manutenção.

### Domínio (Core)

- Contém as regras de negócio e modelos centrais (EventBetfairDto, MarketBetfairDto, Back, Lay, GoltrixDto).

- Define ports para comunicação com sistemas externos:

- BetfairPort — responsável por consultas e padronização de eventos e mercados.

- GoltrixPersistencePort — responsável pela persistência dos registros goltrix.

### Aplicação (Use Cases)

- Implementa os fluxos de transformação, validação e normalização dos dados.

- Orquestra chamadas entre os ports, isolando o domínio da infraestrutura.

- Converte dados externos para estruturas internas padronizadas.

### Adapters (Entradas e Saídas)

- Inbound adapters: controladores REST, serviços agendados ou qualquer ponto de entrada que aciona os casos de uso.

- Outbound adapters: implementações concretas dos ports, incluindo:
Cliente HTTP que consome a API da Betfair.

- Adapter de persistência para leitura/escrita na tabela goltrix usando JPA ou R2DBC.

- Responsáveis por parsing de JSON, tradução via marketMap e conversão para entidades.