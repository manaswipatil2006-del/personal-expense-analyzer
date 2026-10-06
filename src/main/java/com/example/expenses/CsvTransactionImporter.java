package com.example.expenses;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CsvTransactionImporter {
    public List<Transaction> importFile(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);
        if (lines.isEmpty()) throw new IllegalArgumentException("The CSV file is empty.");
        List<String> header = parseLine(lines.get(0));
        int dateCol = column(header, "date"), descCol = column(header, "description");
        int amountCol = column(header, "amount"), categoryCol = column(header, "category");
        if (dateCol < 0 || descCol < 0 || amountCol < 0)
            throw new IllegalArgumentException("CSV headers must include date, description, amount; category is optional.");
        List<Transaction> result = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).isBlank()) continue;
            try {
                List<String> cells = parseLine(lines.get(i));
                String dateText = cell(cells, dateCol), description = cell(cells, descCol);
                String amountText = cell(cells, amountCol);
                if (dateText.isBlank() || description.isBlank() || amountText.isBlank()) throw new IllegalArgumentException("date, description, and amount are required");
                LocalDate date = LocalDate.parse(dateText);
                BigDecimal amount = new BigDecimal(amountText.replace("$", "").replace(",", "").trim());
                if (amount.signum() <= 0) throw new IllegalArgumentException("amount must be greater than zero (enter expenses as positive values)");
                String category = categoryCol < 0 ? "" : cell(cells, categoryCol).trim();
                if (category.isBlank() || category.equalsIgnoreCase("uncategorized")) category = categorize(description);
                result.add(new Transaction(date, description.trim(), amount, category));
            } catch (RuntimeException ex) {
                errors.add("Line " + (i + 1) + ": " + ex.getMessage());
            }
        }
        if (result.isEmpty()) throw new IllegalArgumentException("No valid transactions were found. " + String.join("; ", errors));
        System.out.println("Imported " + result.size() + " transaction(s).");
        if (!errors.isEmpty()) {
            System.out.println("Skipped " + errors.size() + " invalid row(s):");
            errors.forEach(error -> System.out.println("  - " + error));
        }
        return result;
    }

    static String categorize(String description) {
        String text = description.toLowerCase(Locale.ROOT);
        if (contains(text, "grocery", "supermarket", "whole foods", "trader joe", "market")) return "Groceries";
        if (contains(text, "uber", "lyft", "train", "bus", "fuel", "gas station", "metro")) return "Transport";
        if (contains(text, "netflix", "spotify", "cinema", "movie", "steam")) return "Entertainment";
        if (contains(text, "electric", "water bill", "internet", "utility", "rent")) return "Bills";
        if (contains(text, "pharmacy", "doctor", "clinic", "health")) return "Healthcare";
        if (contains(text, "restaurant", "cafe", "coffee", "pizza", "takeaway")) return "Dining";
        return "Other";
    }
    private static boolean contains(String value, String... terms) {
        for (String term : terms) if (value.contains(term)) return true;
        return false;
    }
    private static String cell(List<String> row, int index) { return index < row.size() ? row.get(index).trim() : ""; }
    private static int column(List<String> header, String name) {
        for (int i = 0; i < header.size(); i++) if (header.get(i).trim().equalsIgnoreCase(name)) return i;
        return -1;
    }
    // Handles commas inside double-quoted fields and escaped quotes (""), as in standard CSV.
    private static List<String> parseLine(String line) {
        List<String> cells = new ArrayList<>(); StringBuilder value = new StringBuilder(); boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') { value.append('"'); i++; }
                else quoted = !quoted;
            } else if (ch == ',' && !quoted) { cells.add(value.toString()); value.setLength(0); }
            else value.append(ch);
        }
        if (quoted) throw new IllegalArgumentException("unclosed quote in CSV row");
        cells.add(value.toString()); return cells;
    }
}
