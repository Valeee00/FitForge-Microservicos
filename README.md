# FitForge — Microsserviços

Backend do **FitForge** com dois microsserviços em Java 21 + Spring Boot, cada um com seu próprio banco de dados (**database per service**).
Atividade Formativa 14 — Arquitetura de Soluções Computacionais. Integrantes em [GRUPO.md](GRUPO.md).

## Endereços publicados

| Microsserviço | Aplicativo Web (Azure) | Swagger |
|---|---|---|
| treinos-service | https://fitforge-treinos-cchfd7b9dyf7cjgu.italynorth-01.azurewebsites.net | `/swagger-ui.html` |
| progresso-service | https://fitforge-progresso-gvfgfua7b0dggrht.italynorth-01.azurewebsites.net | `/swagger-ui.html` |

**Site do FitForge (Azure Storage — site estático):** https://fitforgevalentin.z38.web.core.windows.net/

## Repositórios do projeto

| Camada | Repositório |
|---|---|
| Frontend (Module Federation) | [Valeee00/FitForge](https://github.com/Valeee00/FitForge) |
| Microsserviços (este) | [Valeee00/FitForge-Microservicos](https://github.com/Valeee00/FitForge-Microservicos) |
| API Gateway + BFF | [Valeee00/FitForge-API_Gateway](https://github.com/Valeee00/FitForge-API_Gateway) |

## Arquitetura

```
┌──────────────────────────────┐      ┌──────────────────────────────┐
│ treinos-service  :8081       │      │ progresso-service  :8082     │
│ Contexto: Planejamento       │      │ Contexto: Acompanhamento     │
│ • CRUD de fichas             │      │ • CRUD de sessões            │
│ • Exercícios da ficha        │      │ • Indicadores da semana      │
│ • Estatísticas do plano      │      │ • Esforço médio, sequência   │
│ ┌──────────────────────────┐ │      │ ┌──────────────────────────┐ │
│ │ H2: treinosdb            │ │      │ │ H2: progressodb          │ │
│ └──────────────────────────┘ │      │ └──────────────────────────┘ │
└──────────────────────────────┘      └──────────────────────────────┘
```

- **Database per service:** nenhuma tabela é compartilhada. A sessão guarda `workoutId` e uma cópia do `workoutName` (snapshot), por isso excluir uma ficha não apaga o histórico.
- **Contrato igual ao frontend:** os campos JSON são exatamente os do FitForge (`id`, `name`, `days`, `exercises`…), e as validações espelham `packages/shared/src/domain.js`.
- **Sem chamadas entre serviços:** quem junta os dados é o BFF do [API Gateway](https://github.com/Valeee00/FitForge-API_Gateway).

**Stack:** Java 21 · Spring Boot 3.3.4 · Spring Data JPA · H2 · Bean Validation · Actuator · SpringDoc OpenAPI.

## CRUD 1 — Fichas de treino (`treinos-service`)

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/treinos` | Lista fichas (`?dia=Seg`, `?busca=texto`) |
| GET | `/api/treinos/{id}` | Busca uma ficha |
| POST | `/api/treinos` | Cria ficha (201; aceita o UUID gerado pelo frontend) |
| PUT | `/api/treinos/{id}` | Atualiza ficha |
| DELETE | `/api/treinos/{id}` | Exclui ficha (204) |
| GET | `/api/treinos/estatisticas` | Total de fichas e exercícios, volume, fichas por dia, exercícios por grupo |

```json
{
  "id": "template-a", "name": "Treino A · Peito e tríceps", "description": "…",
  "days": ["Seg", "Qui"],
  "exercises": [{ "id": "a1", "name": "Supino reto", "muscle": "Peito", "sets": 3, "reps": 12, "load": 20, "rest": 90 }]
}
```

## CRUD 2 — Sessões de treino (`progresso-service`)

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/sessoes` | Lista sessões, mais recentes primeiro (`?workoutId=…`, `?periodo=semana\|mes`) |
| GET | `/api/sessoes/{id}` | Busca uma sessão |
| POST | `/api/sessoes` | Registra sessão (201; data não pode ser futura) |
| PUT | `/api/sessoes/{id}` | Atualiza sessão |
| DELETE | `/api/sessoes/{id}` | Exclui sessão (204) |
| GET | `/api/sessoes/estatisticas` | Totais, semana atual (seg–dom), esforço médio, último peso, sequência de dias |

```json
{ "id": "…", "workoutId": "template-a", "workoutName": "Treino A · Peito e tríceps",
  "date": "2026-10-20", "duration": 45, "effort": 7, "bodyWeight": 78.5, "notes": "" }
```

Erros seguem um formato único: 400 (validação, com `fieldErrors`), 404 (não encontrado) e 409 (id já existe).

## Executar localmente

Requisitos: JDK 21 e Maven.

```bash
./start-all.sh          # Windows: start-all.bat
mvn test                # testes dos dois CRUDs
```

- treinos-service: http://localhost:8081/swagger-ui.html
- progresso-service: http://localhost:8082/swagger-ui.html
- Console H2: `/h2-console` (JDBC URL `jdbc:h2:mem:treinosdb` ou `jdbc:h2:mem:progressodb`, usuário `sa`)

Os dois serviços sobem com dados de exemplo (3 fichas e 4 sessões). Para desligar, use `SEED_DATA=false`.

## Publicação no Azure (2 Aplicativos Web)

Para **cada** serviço:

1. Criar o **Aplicativo Web**: Publicar = Código · Pilha = **Java 21** · Servidor = **Java SE** · SO Linux · mesmo grupo de recursos. Na aba *Implantação*, **habilitar a autenticação básica**.
2. **Visão geral → Baixar perfil de publicação**.
3. No GitHub (**Settings → Secrets and variables → Actions**):

| Tipo | treinos-service | progresso-service |
|---|---|---|
| Secret (conteúdo do perfil) | `AZURE_PUBLISHPROFILE_TREINOS` | `AZURE_PUBLISHPROFILE_PROGRESSO` |
| Variable (nome do Web App) | `AZURE_WEBAPP_TREINOS` | `AZURE_WEBAPP_PROGRESSO` |

4. **Actions → Deploy treinos-service / Deploy progresso-service → Run workflow**.

O banco H2 fica em memória: ao reiniciar o Web App, os dados voltam aos exemplos. Para persistir, crie a variável de ambiente `DB_URL=jdbc:h2:file:/home/data/treinosdb` (ou `progressodb`) no Web App.
