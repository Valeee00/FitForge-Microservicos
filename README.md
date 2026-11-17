# FitForge — Microsserviços (Backend)

Dois microsserviços em **Java 21 + Spring Boot 3**, cada um com **seu próprio banco** e **arquitetura Hexagonal (Ports & Adapters)**, que trocam mensagens pelo **Azure Service Bus**.
Projeto Arquitetura Completa — Arquitetura de Soluções Computacionais (PUCPR). Integrantes em [GRUPO.md](GRUPO.md).

## Integrantes

- Andryus Zolet
- Nicolas Medeiros
- Valentin Rojas
- Leonardo Henrique

## Vídeo de apresentação

▶️ **YouTube (não listado):** https://youtu.be/PREENCHER

## Endereços publicados (Azure)

| Componente | Endereço |
|---|---|
| Frontend (React, Storage Static Website) | https://fitforgevalentin.z38.web.core.windows.net/ |
| API Gateway + BFF | https://fitforge-gateway-hvaqcubhf8d0a2hc.italynorth-01.azurewebsites.net |
| treinos-service (Microsserviço 1 · MongoDB) | https://fitforge-treinos-cchfd7b9dyf7cjgu.italynorth-01.azurewebsites.net/swagger-ui.html |
| progresso-service (Microsserviço 2 · Azure SQL) | https://fitforge-progresso-gvfgfua7b0dggrht.italynorth-01.azurewebsites.net/swagger-ui.html |

## Repositórios do projeto

| Camada | Repositório |
|---|---|
| Frontend (Microfrontend React) | [Valeee00/FitForge](https://github.com/Valeee00/FitForge) |
| API Gateway + BFF | [Valeee00/FitForge-API_Gateway](https://github.com/Valeee00/FitForge-API_Gateway) |
| Backend / Microsserviços (este) | [Valeee00/FitForge-Microservicos](https://github.com/Valeee00/FitForge-Microservicos) |

## Arquitetura adotada: Hexagonal (Ports & Adapters)

Os dois serviços seguem a **Arquitetura Hexagonal** (família Clean/Onion): o domínio fica no centro, sem conhecer framework, banco ou fila; tudo que é tecnologia fica nas bordas (adapters), ligada ao núcleo por **portas** (interfaces).

```
treinos-service (br.fitforge.treinos)             progresso-service (br.fitforge.progresso)
├── domain/        modelo + exceções              ├── domain/        modelo + exceções
├── application/                                  ├── application/
│   ├── port/in    casos de uso (o que o app faz) │   ├── port/in    casos de uso
│   ├── port/out   repositório e publicador       │   ├── port/out   repositório e publicador
│   └── service    regras de negócio (sem Spring) │   └── service    regras de negócio (sem Spring)
├── adapter/                                      ├── adapter/
│   ├── in/web          REST (controller, DTOs)   │   ├── in/web          REST
│   ├── in/messaging    consumidor Service Bus    │   ├── in/messaging    consumidor Service Bus
│   ├── out/persistence MongoDB                   │   ├── out/persistence JPA → Azure SQL
│   └── out/messaging   produtor Service Bus      │   └── out/messaging   produtor Service Bus
└── config/        injeção de dependências, CORS, OpenAPI
```

A regra de dependência é **sempre de fora para dentro**: `adapter → application → domain`. Isso é verificado automaticamente pelos testes de arquitetura (ver seção Testes).

## Regra de negócio e mensageria (Azure Service Bus)

Cada serviço é dono dos seus dados (*database per service*). Para manter os dois coerentes, eles se comunicam **por eventos**, sem chamadas diretas:

| Fila | Quem publica | Quem consome | Evento → efeito |
|---|---|---|---|
| `fichas-eventos` | treinos-service | progresso-service | `FICHA_RENOMEADA` → atualiza o nome da ficha guardado nas sessões · `FICHA_EXCLUIDA` → marca as sessões com `fichaAtiva=false` (o histórico é preservado) |
| `sessoes-eventos` | progresso-service | treinos-service | `SESSAO_REGISTRADA` → atualiza `totalSessoes` e `ultimaSessao` da ficha · `SESSAO_EXCLUIDA` → desconta da ficha |

O consumo é **idempotente** (a lista de sessões da ficha é um conjunto por id), então reentregas do Service Bus não duplicam contagens. Sem `SERVICEBUS_CONNECTION_STRING`, os serviços funcionam normalmente e apenas registram os eventos em log (útil para rodar local).

## CRUD 1 — Fichas de treino (`treinos-service` · MongoDB Atlas)

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/treinos` | Lista fichas (`?dia=Seg`, `?busca=texto`) |
| GET | `/api/treinos/{id}` | Busca uma ficha |
| POST | `/api/treinos` | Cria ficha (201) |
| PUT | `/api/treinos/{id}` | Atualiza ficha (publica `FICHA_RENOMEADA` se o nome mudou) |
| DELETE | `/api/treinos/{id}` | Exclui ficha (204, publica `FICHA_EXCLUIDA`) |
| GET | `/api/treinos/estatisticas` | Totais, volume, fichas por dia, exercícios por grupo |

## CRUD 2 — Sessões de treino (`progresso-service` · Azure SQL)

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/sessoes` | Lista sessões (`?workoutId=`, `?periodo=semana\|mes`) |
| GET | `/api/sessoes/{id}` | Busca uma sessão |
| POST | `/api/sessoes` | Registra sessão (201; data não pode ser futura; publica `SESSAO_REGISTRADA`) |
| PUT | `/api/sessoes/{id}` | Atualiza sessão |
| DELETE | `/api/sessoes/{id}` | Exclui sessão (204; publica `SESSAO_EXCLUIDA`) |
| GET | `/api/sessoes/estatisticas` | Totais, semana atual, esforço médio, último peso, sequência de dias |

Erros seguem formato único: 400 (validação, com `fieldErrors`), 404 e 409.

## Executar localmente

Requisitos: **JDK 21**, **Maven** e (para o treinos-service) **Docker** com MongoDB.

```bash
docker compose up -d          # MongoDB em localhost:27017
./start-all.sh                # Windows: start-all.bat  (treinos :8081, progresso :8082)
mvn test                      # testes unitários e de arquitetura dos dois serviços
```

- treinos-service: http://localhost:8081/swagger-ui.html
- progresso-service: http://localhost:8082/swagger-ui.html (banco H2 em memória; console em `/h2-console`)
- Suba também o [API Gateway](https://github.com/Valeee00/FitForge-API_Gateway) (porta 8080) e o [Frontend](https://github.com/Valeee00/FitForge) com `FITFORGE_API_URL=http://localhost:8080`.

Variáveis de ambiente (todas opcionais localmente):

| Variável | Serviço | Para quê |
|---|---|---|
| `MONGODB_URI`, `MONGODB_DATABASE` | treinos | Conexão com MongoDB (Atlas na nuvem) |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | progresso | Conexão JDBC com Azure SQL (padrão: H2 em memória) |
| `SERVICEBUS_CONNECTION_STRING` | ambos | Habilita as filas do Azure Service Bus |
| `SEED_DATA=false` | ambos | Desliga os dados de exemplo |

## Testes

- **Unitários** (`application/*ServiceTest`): regras de negócio e tratamento de eventos testados com repositórios e publicadores falsos, sem Spring.
- **API** (`WorkoutApiTests`, `SessionApiTests`): CRUD completo pela camada web.
- **Arquitetura** (`architecture/HexagonalArchitectureTest`, **ArchUnit**): `onionArchitecture()` + regras extras — domínio e aplicação não dependem de Spring, adapters não se conhecem, serviços não usam o banco do outro.

## Publicação no Azure

Cada serviço é um **Aplicativo Web Linux (Java SE 21, plano F1 gratuito)** implantado por GitHub Actions (`.github/workflows/deploy-*.yml`). Secrets: `AZURE_PUBLISHPROFILE_TREINOS` / `AZURE_PUBLISHPROFILE_PROGRESSO`; variables: `AZURE_WEBAPP_TREINOS` / `AZURE_WEBAPP_PROGRESSO`. As variáveis de ambiente da tabela acima são configuradas em **Configurações → Variáveis de ambiente** de cada Web App.

---

# Documentação de arquitetura (Arc42)

## 1. Introdução e metas
**Visão.** O FitForge permite montar fichas de treino e registrar o progresso das sessões. Este repositório é o backend: dois microsserviços independentes.
**Requisitos principais:** (1) CRUD de fichas e de sessões; (2) cada serviço com banco próprio (MongoDB e Azure SQL); (3) comunicação assíncrona entre serviços via Azure Service Bus; (4) acesso apenas pelo API Gateway/BFF; (5) testes unitários e de arquitetura.
**Metas de qualidade:** manutenibilidade (camadas isoladas), testabilidade (núcleo sem framework), baixo acoplamento entre serviços, custo zero (camadas gratuitas, exceto Service Bus Basic ≈ centavos).
**Partes interessadas:** alunos/equipe (desenvolvimento), professor (avaliação), usuários finais do app.

## 2. Restrições
- Java 21 / Spring Boot 3.3; Maven multi-módulo.
- Bancos: MongoDB (Atlas M0 gratuito) e Azure SQL (oferta gratuita, sem cobrança excedente).
- Mensageria: Azure Service Bus (camada Basic, apenas filas).
- Hospedagem: Azure App Service F1 (gratuito, cota de CPU diária) e região Italy North (política da assinatura estudantil).
- Repositórios públicos no GitHub; deploy por GitHub Actions.

## 3. Contexto e escopo
**Contexto de negócio:** o usuário usa o frontend; o frontend fala somente com o Gateway/BFF; o Gateway encaminha para os serviços.

```
Usuário → Frontend (React) → API Gateway + BFF → treinos-service ──► MongoDB Atlas
                                              └→ progresso-service ─► Azure SQL
                       treinos-service ◄──── Azure Service Bus ────► progresso-service
```

**Contexto técnico:** REST/JSON sobre HTTPS (entrada), AMQP/Service Bus SDK (eventos), driver MongoDB, JDBC (mssql-jdbc).

## 4. Estratégia de solução
| Meta | Decisão |
|---|---|
| Domínio isolado | Arquitetura Hexagonal; núcleo em Java puro, framework só nos adapters |
| Autonomia dos dados | *Database per service*; sessão guarda `workoutId` + cópia (`workoutName`) |
| Consistência entre serviços | Consistência eventual por eventos no Service Bus, consumo idempotente |
| Resiliência | Falha ao publicar/consumir não derruba a API; fallback para log |
| Qualidade garantida | ArchUnit no build impede violar as camadas |

## 5. Visão de blocos de construção
**Nível 1:** `treinos-service` e `progresso-service` (módulos Maven irmãos, sem dependência entre si).
**Nível 2 (cada serviço):** `domain` (modelo, exceções) · `application` (portas de entrada/saída e serviço) · `adapter.in.web` · `adapter.in.messaging` · `adapter.out.persistence` · `adapter.out.messaging` · `config`.

| Componente | Responsabilidade |
|---|---|
| `FichaService` / `SessaoService` | Regras de negócio, validações, publicação de eventos, tratamento de eventos recebidos |
| `*RepositoryPort` | Contrato de persistência (implementado por Mongo / JPA) |
| `*EventPublisherPort` | Contrato de publicação (implementado por Service Bus / log) |
| `*Controller` + DTOs | Tradução HTTP ↔ casos de uso |
| `ServiceBus*Listener` | Consome a fila e chama o caso de uso de evento |

## 6. Visão de tempo de execução
**Cenário A — renomear uma ficha:** Frontend `PUT /api/treinos/{id}` → Gateway → `FichaService` valida e salva no Mongo → publica `FICHA_RENOMEADA` em `fichas-eventos` → `ServiceBusFichaListener` (progresso) recebe → `SessaoService` atualiza `workout_name` no Azure SQL.
**Cenário B — registrar uma sessão:** `POST /api/sessoes` → grava no Azure SQL → publica `SESSAO_REGISTRADA` em `sessoes-eventos` → treinos atualiza `totalSessoes`/`ultimaSessao` da ficha no Mongo.
**Cenário C — Service Bus indisponível:** a operação HTTP conclui; o erro de publicação é registrado e a API segue funcionando.

## 7. Visão de implantação
```
GitHub Actions ──deploy──► App Service F1 (treinos)   ─► MongoDB Atlas M0
                 ──────► App Service F1 (progresso) ─► Azure SQL (free offer, serverless)
                              └─────── Azure Service Bus (Basic: fichas-eventos, sessoes-eventos)
```
Região Italy North; configuração por variáveis de ambiente do App Service.

## 8. Conceitos transversais
- **Validação:** Bean Validation nos DTOs + regras de domínio no serviço (erro único 400/404/409).
- **Documentação:** SpringDoc/OpenAPI em `/swagger-ui.html`.
- **CORS:** liberado para o frontend/gateway (`app.cors.*`).
- **Observabilidade:** logs de requisição, `X-Correlation-Id` propagado pelo gateway, Actuator `/actuator/health`.
- **Dados de exemplo:** `DataInitializer` com *try/catch* (nunca derruba a aplicação).

## 9. Decisões de arquitetura
| # | Decisão | Motivo |
|---|---|---|
| ADR-1 | Hexagonal em vez de MVC em camadas | Isolar domínio e trocar infraestrutura (H2 → Azure SQL) sem tocar nas regras |
| ADR-2 | Mongo no treinos e SQL no progresso | Ficha é documento aninhado (exercícios); sessão é registro tabular consultado por período |
| ADR-3 | Service Bus com filas | Requisito do trabalho; desacopla os serviços; Basic é a opção mais barata |
| ADR-4 | MongoDB Atlas M0 no lugar de Cosmos DB | Regiões do Cosmos bloqueadas pela política da assinatura; M0 é gratuito |
| ADR-5 | Snapshot do nome da ficha na sessão | Excluir a ficha não apaga o histórico |

## 10. Requisitos de qualidade
| Qualidade | Cenário | Como é verificado |
|---|---|---|
| Manutenibilidade | Camada de domínio não importa Spring | ArchUnit |
| Testabilidade | Regras testáveis sem banco/fila | `FichaServiceTest`, `SessaoServiceTest` |
| Interoperabilidade | JSON idêntico ao contrato do frontend | `WorkoutApiTests`, `SessionApiTests` |
| Custo | Nenhum recurso pago além do Service Bus Basic | Camadas gratuitas, *overage* desabilitado |

## 11. Riscos e débitos técnicos
- Plano **F1** tem cota de CPU (60 min/dia) e dorme por inatividade: a primeira chamada pode demorar.
- Azure SQL gratuito **pausa ao inativar**: a primeira conexão pode levar ~1 min (timeouts configurados no Hikari).
- Consistência eventual: após um evento, o outro serviço leva instantes para refletir.
- Segredos em variáveis de ambiente (sem Key Vault, por custo).

## 12. Glossário
| Termo | Significado |
|---|---|
| Ficha | Plano de treino com exercícios, séries, repetições e carga |
| Sessão | Registro de um treino realizado (data, duração, esforço, peso) |
| Porta / Adapter | Interface do núcleo / implementação tecnológica da borda |
| Database per service | Cada microsserviço tem seu banco exclusivo |
| Idempotente | Processar a mesma mensagem duas vezes tem o mesmo efeito que uma |
