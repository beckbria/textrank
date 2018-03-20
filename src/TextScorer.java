import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

public class TextScorer {
    /**
     * Default nGram Constructor
     * @param length The size of nGram used by this scorer
     */
    private TextScorer(int length) {
        if (length < 1) {
            throw new IllegalArgumentException();
        }
        nGramLength = length;
    }

    /**
     * Creates an object loading values previously saved to a file
     * @param fileName Path to input file containing precomputed list of frequencies
     * @return TextRank object containing the frequencies from the file
     */
    public static TextScorer fromFrequencyFile(String fileName) throws IOException, IllegalArgumentException {
        // For good frequency files, see http://practicalcryptography.com/cryptanalysis/text-characterisation/quadgrams/
        // To generate your own, use TextScoreFrequencyGenerator
        TextScorer scorer = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            // Read all of the known frequency counts
            String line;
            while ((line = reader.readLine()) != null) {
                String[] components = line.split(" ");
                if (components.length != 2) {
                    throw new IllegalArgumentException();
                }

                if (scorer == null) {
                    scorer = new TextScorer(components[0].length());
                }

                // We should never see the same nGram multiple times in an input file.
                // All ngrams should be of equal length
                if ((scorer.nGramScores.get(components[0]) != null) || (components[0].length() != scorer.nGramLength)) {
                    throw new IllegalArgumentException();
                }

                Double count = Double.parseDouble(components[1]);
                scorer.nGramScores.put(components[0], count);
                scorer.totalCount += count;
            }

            // Normalize the counts
            for (Map.Entry<String, Double> entry : scorer.nGramScores.entrySet()) {
                entry.setValue(Math.log10(entry.getValue() / scorer.totalCount));
            }
            scorer.baselineFloor = Math.log10(0.01 / scorer.totalCount);
        }
        return scorer;
    }

    private class ScoreCalculator implements TextScoreUtilities.NGramHandler {
        /**
         * Calculates the score for a text string by accumulating scores for each substring
         * @param ngrams Mapping of substring to score
         */
        public ScoreCalculator(Map<String, Double> ngrams) {
            ngramScores = ngrams;
        }

        public void HandleNGram(String nGram) {
            Double currentScore = ngramScores.get(nGram);
            if (currentScore == null) {
                // TODO: Don't filter out invalid characters before this function.  Penalize strings with unprintable
                // characters particularly harshly (baseline * 100) so that brute force attempts that can produce
                // invalid characters (XOR, etc.) are penalized accordingly
                score += baselineFloor;
            } else {
                score += currentScore;
            }
        }

        public double getScore() { return score; }
        private double score = 0.0;
        private Map<String, Double> ngramScores;
    }

    /**
     * Ranks the resemblance of the input text to the source language of the scorer
     * @param input Text to be ranked
     * @return
     */
    public double score(String input) {
        // TODO: Consider checking the length of input versus the length of the cleaned input to make sure we're not
        // throwing out too many invalid characters
        ScoreCalculator calc = new ScoreCalculator(nGramScores);
        TextScoreUtilities.forEachNGram(input, nGramLength, calc);
        return calc.getScore();
    }

    /** The length of substrings stored.  Longer lengths give higher accuracy but require more memory */
    private final int nGramLength;
    private double baselineFloor;
    private HashMap<String, Double> nGramScores = new HashMap<>();
    private long totalCount = 0;
}
