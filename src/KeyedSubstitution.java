import java.lang.IllegalArgumentException;
import java.util.Arrays;
import java.security.SecureRandom;

/**
 * Applies a key-based text substitution to a string.  For example, a key of DEFABCGHIJKLMNOPQRSTUVWXYZ applied
 * to the string "BAKED APPLE PIE" would result in "EDKBA DPPLB PIB".
 */
public class KeyedSubstitution {
    private KeyedSubstitution() {}

    /**
     * Generates a random key for scrambling a plaintext.
     * @return
     */
    public static KeyedSubstitution random() {
        KeyedSubstitution ks = new KeyedSubstitution();
        ks.shuffle();
        return ks;
    }

    /**
     * Generates a substitution object from a provided key
     * @param key The provided key.  It must be a complete alphabet (a permutation of ABCDEFGHIJKLMNOPQRSTUVWXYZ)
     * @return A substitution object capable of transforming objects based on the provided key
     * @throws IllegalArgumentException
     */
    public static KeyedSubstitution fromKey(String key) throws IllegalArgumentException {
        // Validate the key
        KeyedSubstitution ks = new KeyedSubstitution();
        String defaultKey = ks.getKey();
        if (key.length() != defaultKey.length()) {
            throw new IllegalArgumentException();
        }

        // Ensure that the keys contain the same characters
        char[] ksKey = defaultKey.toCharArray();
        char[] providedKey = key.toCharArray();
        Arrays.sort(ksKey);
        Arrays.sort(providedKey);
        if (Arrays.equals(ksKey, providedKey)) {
            ks.replacements = key.toCharArray();
        } else {
            throw new IllegalArgumentException();
        }

        return ks;
    }

    /** Randomly permutes the key */
    public void shuffle() {
        // Fisher-Yates Shuffle
        for (int i = replacements.length - 1; i >= 1; --i) {
            int j = rand.nextInt(i + 1);
            swapCharAtIndices(i, j);
        }
    }

    /** Swaps a random pair of characters in the key */
    public void swapRandomPair() {
        lastSwappedIndices[0] = rand.nextInt(replacements.length);
        lastSwappedIndices[1] = rand.nextInt(replacements.length);
        while (lastSwappedIndices[0] == lastSwappedIndices[1]) {
            lastSwappedIndices[1] = rand.nextInt(replacements.length);
        }
        swapCharAtIndices(lastSwappedIndices[0], lastSwappedIndices[1]);
    }

    /** Reverses the most recent random swap of key characters.  Does not undo a shuffle. */
    public void undoLastRandomSwap() {
        swapCharAtIndices(lastSwappedIndices[0], lastSwappedIndices[1]);
    }

    /** Returns the key that is currently used for encoding/decoding */
    public String getKey() {
        StringBuilder key = new StringBuilder();
        for (char c : replacements) {
            key.append(c);
        }
        return key.toString();
    }

    /** Generates an inverted key - that is, a key that will solve texts encoded with the current key. */
    public void invertKey() {
        char[] newKey = new char[replacements.length];
        for (int i = 0; i < replacements.length; ++i) {
            final int index = ((int) replacements[i] - (int) 'A');
            newKey[index] = (char)((int)'A' + i);
        }
        replacements = newKey;
    }

    /**
     * Applies a substitution cipher to the provided string
     * @param cipherText The text to be encoded
     * @return The encoded string
     */
    public String applySubstitution(String cipherText) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cipherText.length(); ++i) {
            char current = cipherText.charAt(i);
            if (current >= 'A' && current <= 'Z') {
                final int index = ((int) cipherText.charAt(i) - (int) 'A');
                sb.append(replacements[index]);
            } else {
                sb.append(current);
            }
        }

        return sb.toString();
    }

    /** Swaps two characters in the key */
    private void swapCharAtIndices(int i, int j) {
        // Swap
        final char tmp = replacements[i];
        replacements[i] = replacements[j];
        replacements[j] = tmp;
    }

    private char[] replacements = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    };
    private SecureRandom rand = new SecureRandom();
    private int[] lastSwappedIndices = {0, 0};
}
