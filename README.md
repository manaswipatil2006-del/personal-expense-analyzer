# Personal Expense Analyzer (Java)

A small Java command-line portfolio project that imports expense transactions from CSV, validates rows, assigns a category when one is missing, summarizes spending by month and category, and flags months over a user-set budget.

## Requirements

- Java 17 or later
- Apache Maven
- Git and a GitHub account

## Run it

Open a terminal in this project folder and run:

```sh
mvn compile
mvn exec:java -Dexec.args="sample-transactions.csv 200"
```

The second argument is the monthly budget. If omitted, the demo uses `$2000`.

## CSV format

Required headers are `date,description,amount`; `category` is optional. Dates use `YYYY-MM-DD`; enter expense amounts as positive numbers. A missing or `Uncategorized` category is inferred from common description keywords. Unknown descriptions are grouped under `Other`.

```csv
date,description,amount,category
2026-09-01,"Market, downtown",84.50,
2026-09-02,Bus pass,42.00,Transport
```

The importer reports invalid rows and skips them while keeping valid rows. It accepts quoted fields and escaped quotes in a CSV line.

## Features

- CSV import with header, date, required-field, and amount validation
- Automatic keyword based categories with optional user supplied categories
- Monthly totals, category totals, and configurable budget alerts
- Clear row level errors; bad rows do not silently enter totals
- Uses `BigDecimal` for currency calculations

## Project structure

```text
src/main/java/com/example/expenses/
  CsvTransactionImporter.java
  ExpenseAnalyzer.java
  Transaction.java
sample-transactions.csv
```

## Limitations and next steps

This is a portfolio demo, not financial advice or a bank-connected product. Category rules are intentionally simple and can be expanded. Useful next steps: add automated unit tests, export a monthly report, build charts with a Java chart library, or expose the analyzer through a Spring Boot web interface.

## GitHub description suggestion

"A Java 17 expense analyzer that imports and validates CSV transactions, categorizes spending, summarizes monthly totals, and reports budget alerts."
