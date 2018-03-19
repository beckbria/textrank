import java.io.IOException;
import java.util.List;

public class SolverTests {
    /** Set this to where your test data is stored */
    private static final String testDataLocation = "D:\\TextRank\\";

    public static void main(String[] args) {
        try {
            generatorTests();
            TextScorer scorer = TextScorer.fromFrequencyFile(testDataLocation + "english_quadgrams.txt");
            scorerTests(scorer);
            keyedSubstitutionTests();
            substitutionCipherTests(scorer);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    public static void generatorTests() throws IOException {
        TextScoreFrequencyGenerator generator = new TextScoreFrequencyGenerator(4);
        generator.recordKnownMaterial("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        generator.saveToFile(testDataLocation + "test1.txt");
    }

    public static void scorerTests(TextScorer scorer) {
        // Note: Always compare strings of equal length due to how the scoring mechanism works
        if (scorer.score("THISISENGLISHTEXT") < scorer.score("QWERTYUIOPASDFGHJ")) {
            throw new AssertionError();
        }
        if (scorer.score("THIS IS \"ALSO\" {ENGLISH}\nTEXT") < scorer.score("ASDFGHJKLZXCVBNMQWERT")) {
            throw new AssertionError();
        }
    }

    public static void keyedSubstitutionTests() throws IllegalArgumentException {
        // Ensure that invalid keys aren't accepted
        for (String invalidKey : List.of("", "ABCDEFGHIJKLMNOPQRSTUVWXYZ ", "AAAAAAAAAAAAAAAAAAAAAAAAAA")) {
            boolean exceptionWasThrown = false;
            try {
                KeyedSubstitution ks = KeyedSubstitution.fromKey(invalidKey);
            } catch (IllegalArgumentException ex) {
                exceptionWasThrown = true;
            }
            if (!exceptionWasThrown) {
                throw new AssertionError();
            }
        }

        final String key = "DEFCBAGHIJKLMNOPQRSTUVWXYZ";
        KeyedSubstitution ks = KeyedSubstitution.fromKey(key);
        if (!ks.getKey().equals(key)) {
            // The inverse of the inverse should get the original key back
            throw new AssertionError();
        }
        ks.invertKey();
        if (!ks.getKey().equals("FEDABCGHIJKLMNOPQRSTUVWXYZ")) {
            // The inverse of the inverse should get the original key back
            throw new AssertionError();
        }
        ks.invertKey();
        if (!ks.getKey().equals(key)) {
            // The inverse of the inverse should get the original key back
            throw new AssertionError();
        }

        final String output = "BAKED APPLE PIE";
        final String expected = "EDKBC DPPLB PIB";
        if (!ks.applySubstitution(output).equals(expected)) {
            throw new AssertionError();
        }
    }

    public static void substitutionCipherTests(TextScorer scorer) {
        SubstitutionCipherSolver solver = new SubstitutionCipherSolver(scorer);
        String plainText = TextScoreUtilities.filterContent("This is a test of the substitution cipher solver function.  Longer samples of source material will enable more accurate decoding.");
        KeyedSubstitution ks = KeyedSubstitution.random();
        String cipherText = ks.applySubstitution(plainText);
        String answer = solver.Solve(cipherText);
        System.out.println(answer);
        assert(answer.equals(plainText));
        // We don't explicitly throw an AssertionError here - due to the random nature of the solver, it's entirely
        // possible that it will miss a character or two.
        // TODO: A proper test would write a text similarity function and measure it against a threshold
    }
}
