import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

public class TextScoreFrequencyGenerator {
    /**
     * Records the frequency with substrings of a given length appear in source material.
     * @param length The length of substring stored and analyzed.
     */
    public TextScoreFrequencyGenerator(int length) {
        if (length < 1) {
            throw new IllegalArgumentException();
        }
        nGramLength = length;
    }

     /**
     * Saves the current frequencies to a file so that they can be loaded later
     * @param fileName Path to output file to store precomputed list of frequencies
     */
    public void saveToFile(String fileName) throws IOException {
        try (FileWriter writer = new FileWriter(fileName)) {
            for (Map.Entry<String, Long> entry : frequencyCount.entrySet()) {
                StringBuilder sb = new StringBuilder();
                sb.append(entry.getKey());
                sb.append(' ');
                sb.append(entry.getValue());
                sb.append('\n');
                writer.write(sb.toString());
            }
        }
    }

    /**
     * Ingests known good input (text in the source language) and records statistics to use in ranking
     * future input.
     * @param input Known valid input text
     */
    public void recordKnownMaterial(String input) {
        TextScoreUtilities.forEachNGram(input, nGramLength,
            (String nGram) -> {
                Long currentCount = frequencyCount.get(nGram);
                if (currentCount == null) {
                    frequencyCount.put(nGram, 1L);
                } else {
                    frequencyCount.put(nGram, currentCount + 1L);
                }
            }
        );
    }

    /** The length of substrings stored.  Longer lengths give higher accuracy but require more memory */
    private final int nGramLength;
    private HashMap<String, Long> frequencyCount = new HashMap<String, Long>();
}
