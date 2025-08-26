# DESAFIO — Consulta de Vendas (DSMeta)

API REST em **Spring Boot** para consultar vendas com **filtros por período e nome do vendedor**, **paginação** e **sumário agregado por vendedor**.

---

## ✨ Funcionalidades

- `GET /sales/report` – relatório **paginado** de vendas  
  Retorna: `id`, `date`, `amount`, `sellerName`
- `GET /sales/summary` – **agregado por vendedor**  
  Retorna: `sellerName`, `total`
- `GET /sales/{id}` – detalhe de uma venda (DTO reduzido)

**Parâmetros opcionais e *defaults*:**
- `maxDate` → **hoje**
- `minDate` → **1 ano antes de `maxDate`**
- `name` → **""** (texto vazio)

> Datas no formato `yyyy-MM-dd` (ex.: `2025-06-30`).

---

## 🧱 Stack

- Java 17+
- Spring Boot (Web, Data JPA)
- Banco (dev): H2 em memória (ou outro conforme configuração)
- Build: Maven/Gradle

---

## 🗂️ Estrutura (resumo)

- **Entities**: `Sale`, `Seller`
- **DTOs**
  - `SaleReportDTO { id, date, amount, sellerName }`
  - `SaleSummaryDTO { sellerName, total }`
  - `SaleMinDTO { id, amount, date }` (usado em `/sales/{id}`)
- **Repository**
  - `searchReport(min, max, name, pageable)` – JPQL com `BETWEEN` nas datas, `LIKE` case-insensitive no nome, **ORDER BY `s.date DESC, s.id DESC`**.
  - `searchSummary(min, max)` – JPQL com `SUM(s.amount)` e `GROUP BY s.seller.name` (opcional: `ORDER BY SUM(...) DESC`).
- **Service**
  - Recebe strings do controller, converte para `LocalDate` e aplica defaults:  
    `max = LocalDate.ofInstant(Instant.now(), ZoneId.systemDefault())`  
    `min = max.minusYears(1L)`  
    `name = (blank ? "" : trim)`
  - Mapeia resultados do repository → DTOs.
- **Controller**
  - Recebe parâmetros como `String` e delega ao service; retorna os DTOs esperados.

---

## ▶️ Como rodar

### 1) Clonar
```bash
git clone git@github.com:hennanhfalcao/desafio-consulta-vendas.git
cd desafio-consulta-vendas
```

### 2) Subir a aplicação
Maven:
```bash
./mvnw spring-boot:run
```

API em: `http://localhost:8080`

### 3) Base de dados
- Em dev, o **H2** em memória é carregado com `import.sql` (em `src/main/resources`).
- **Atenção:** IDs podem ficar “altos” ou ter lacunas, porque são gerados na **ordem de inserção**, não pela data da venda. Isso é esperado.

---

## 🔗 Endpoints

### 1) Relatório de vendas (paginado)
```
GET /sales/report
```

**Query params:**
- `minDate` (opcional, `yyyy-MM-dd`)
- `maxDate` (opcional, `yyyy-MM-dd`)
- `name` (opcional, *contains*, case-insensitive)
- Paginação (Spring): `page` (0-based), `size` (padrão 20)

**Exemplos:**
```http
# Últimos 12 meses (sem params)
GET http://localhost:8080/sales/report

# Período + nome
GET http://localhost:8080/sales/report?minDate=2022-05-01&maxDate=2022-05-31&name=odinson

# Segunda página com 10 itens
GET http://localhost:8080/sales/report?page=1&size=10
```

**Resposta (exemplo):**
```json
{
  "content": [
    { "id": 125, "date": "2022-05-22", "amount": 19476.0, "sellerName": "Loki Odinson" },
    { "id": 126, "date": "2022-05-18", "amount": 20530.0, "sellerName": "Thor Odinson" }
  ],
  "totalPages": 3,
  "totalElements": 51,
  "number": 0,
  "size": 20
}
```
---

### 2) Sumário por vendedor (agregado)
```
GET /sales/summary
```

**Query params:**
- `minDate` (opcional, `yyyy-MM-dd`)
- `maxDate` (opcional, `yyyy-MM-dd`)

**Exemplos:**
```http
# Últimos 12 meses
GET http://localhost:8080/sales/summary

# Período específico
GET http://localhost:8080/sales/summary?minDate=2022-01-01&maxDate=2022-06-30
```

**Resposta (exemplo):**
```json
[
  { "sellerName": "Loki Odinson", "total": 150597.0 },
  { "sellerName": "Thor Odinson", "total": 144896.0 }
]
```

> **Validação prática:** some os `amount` do `/sales/report` filtrado por um vendedor e compare com o `total` do `/sales/summary` no mesmo período — deve bater.

---

### 3) Detalhe por id
```
GET /sales/{id}
```
**Resposta:**
```json
{ "id": 126, "amount": 20530.0, "date": "2022-05-18" }
```

---

## 🧪 cURL rápido

```bash
# 1) Report sem parâmetros
curl "http://localhost:8080/sales/report"

# 2) Report com período + nome
curl "http://localhost:8080/sales/report?minDate=2022-05-01&maxDate=2022-05-31&name=odinson"

# 3) Summary sem parâmetros
curl "http://localhost:8080/sales/summary"

# 4) Summary com período
curl "http://localhost:8080/sales/summary?minDate=2022-01-01&maxDate=2022-06-30"
```

---

## 🔧 Boas práticas adotadas

- Filtro de nome com `UPPER(...) LIKE UPPER(CONCAT('%', :name, '%'))`.
- Conversão de agregados `Object[]` → `SaleSummaryDTO` no service  
  (opcional: projeção direta em DTO na query via *constructor expression*).

---
