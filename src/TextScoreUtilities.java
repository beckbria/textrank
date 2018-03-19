import java.text.Normalizer;

class TextScoreUtilities {

    /**
     * Prepares the input string for ranking or ingestion by enforcing character set standards
     * @param input The raw input string
     */
    static String filterContent(String input) {
        // Attempt to convert unicode characters to their nearest ASCII equivalent i.e. Ä -> A
        String content = Normalizer.normalize(input, Normalizer.Form.NFKD).toUpperCase();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < content.length(); ++i) {
            // Ignore any non-alphabetic characters
            char c = content.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /** Callback interface to handle substrings */
    interface NGramHandler {
        void HandleNGram(String nGram);
    }

    /**
     * Splits a source string into overlapping substrings to be processed by a callback
     * @param input The string to be processed
     * @param nGramLength The length of each substring
     * @param handler The callback to be invoked with each substring
     */
    static void forEachNGram(String input, int nGramLength, NGramHandler handler) {
        String content = filterContent(input);
        for (int i = 0; i <= (content.length() - nGramLength); ++i) {
            handler.HandleNGram(content.substring(i, i + nGramLength));
        }
    }
}
