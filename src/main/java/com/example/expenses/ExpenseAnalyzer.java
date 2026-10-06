package com.example.expenses;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.Map;
import java.util.TreeMap;

public final class ExpenseAnalyzer {
    private static final BigDecimal DEFAULT_BUDGET = new BigDecimal("2000.00");
    public static void main(String[] args) {
        if (args.length < 1 || args.length > 2) {
            System.out.println("Usage: mvn exec:java -Dexec.args=\"<csv-file> [monthly-budget]\"");
            System.out.println("Example: mvn exec:java -Dexec.args=\"sample-transactions.csv 2000\""); return;
        }
        try {
            BigDecimal budget = args.length == 2 ? new BigDecimal(args[1]) : DEFAULT_BUDGET;
            if (budget.signum() <= 0) throw new IllegalArgumentException("Monthly budget must be greater than zero.");
            var transactions = new CsvTransactionImporter().importFile(Path.of(args[0]));
            Map<YearMonth, BigDecimal> monthly = new TreeMap<>();
            Map<String, BigDecimal> categories = new TreeMap<>();
            for (Transaction t : transactions) {
                YearMonth month = YearMonth.from(t.date());
                monthly.merge(month, t.amount(), BigDecimal::add);
                categories.merge(t.category(), t.amount(), BigDecimal::add);
            }
            System.out.println("\nMONTHLY SPENDING");
            monthly.forEach((month, total) -> {
                System.out.printf("%s  %s", month, money(total));
                if (total.compareTo(budget) > 0) System.out.print("  ALERT: over budget by " + money(total.subtract(budget)));
                else System.out.print("  Budget remaining: " + money(budget.subtract(total)));
                System.out.println();
            });
            System.out.println("\nSPENDING BY CATEGORY");
            categories.forEach((category, total) -> System.out.printf("%-18s %s%n", category, money(total)));
            BigDecimal total = transactions.stream().map(Transaction::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
            System.out.println("\nTotal spending: " + money(total));
            System.out.println("Budget used: " + total.multiply(BigDecimal.valueOf(100)).divide(budget.multiply(BigDecimal.valueOf(monthly.size())), 1, RoundingMode.HALF_UP) + "% of average monthly budget");
        } catch (Exception e) {
            System.err.println("Could not analyze expenses: " + e.getMessage());
            System.err.println("Check the file path, CSV headers, dates (YYYY-MM-DD), amounts, and budget.");
            System.exit(1);
        }
    }
    private static String money(BigDecimal amount) { return "$" + amount.setScale(2, RoundingMode.HALF_UP); }
}
