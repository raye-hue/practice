package battle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

final class MoveBalanceTable {

    private static final BalanceEntry DEFAULT_ENTRY = new BalanceEntry();

    private final Map<String, BalanceEntry> entries = new HashMap<>();

    MoveBalanceTable(String filePath) {
        load(filePath);
    }

    double getDamageMultiplier(String characterName, String moveName) {
        return getEntry(characterName, moveName).damageMultiplier;
    }

    double getManaMultiplier(String characterName, String moveName) {
        return getEntry(characterName, moveName).manaMultiplier;
    }

    double getCooldownMultiplier(String characterName, String moveName) {
        return getEntry(characterName, moveName).cooldownMultiplier;
    }

    private void load(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                parseLine(line);
            }
        } catch (IOException ignored) {
            // Fall back to default balance values.
        }
    }

    private void parseLine(String rawLine) {
        String line = rawLine.trim();
        if (line.isEmpty() || line.startsWith("#")) {
            return;
        }

        String[] parts = line.split("\\|");
        if (parts.length < 2) {
            return;
        }

        String characterName = parts[0].trim();
        String moveName = parts[1].trim();
        if (characterName.isEmpty() || moveName.isEmpty()) {
            return;
        }

        BalanceEntry entry = new BalanceEntry();
        for (int i = 2; i < parts.length; i++) {
            String segment = parts[i].trim();
            int equalsIndex = segment.indexOf('=');
            if (equalsIndex <= 0 || equalsIndex >= segment.length() - 1) {
                continue;
            }

            String key = segment.substring(0, equalsIndex).trim().toLowerCase();
            String valueText = segment.substring(equalsIndex + 1).trim();
            try {
                double value = Double.parseDouble(valueText);
                if ("damage".equals(key) || "damagemultiplier".equals(key)) {
                    entry.damageMultiplier = Math.max(0d, value);
                } else if ("mana".equals(key) || "manamultiplier".equals(key)) {
                    entry.manaMultiplier = Math.max(0d, value);
                } else if ("cooldown".equals(key) || "cooldownmultiplier".equals(key)) {
                    entry.cooldownMultiplier = Math.max(0d, value);
                }
            } catch (NumberFormatException ignored) {
                // Ignore malformed values and keep defaults.
            }
        }

        entries.put(makeKey(characterName, moveName), entry);
    }

    private BalanceEntry getEntry(String characterName, String moveName) {
        BalanceEntry entry = entries.get(makeKey(characterName, moveName));
        return entry != null ? entry : DEFAULT_ENTRY;
    }

    private String makeKey(String characterName, String moveName) {
        return characterName + "|" + moveName;
    }

    private static final class BalanceEntry {
        private double damageMultiplier = 1.0d;
        private double manaMultiplier = 1.0d;
        private double cooldownMultiplier = 1.0d;
    }
}