import java.util.concurrent.*;

public class SubstitutionCipherSolver {
    /**
     * Solves substitution ciphers.  This finds a locally optimized solution, but because it relies on random start
     * points, it is not guaranteed to find the optimal answer.
     * @param textScorer A scorer to rank the fitness of strings to the source language.
     */
    public SubstitutionCipherSolver(TextScorer textScorer) {
        scorer = textScorer;
    }

    /**
     * Sets the number of iterations that will proceed without random improvement
     * before determining that we have reached an optimum solution.
     * @param threshold The number of iterations to continue despite a lack of progress
     */
    public void SetRandomImprovementThreshold(int threshold) {
        randomImprovementThreshold = threshold;
    }

    /**
     * Sets the number of incremental changes allowed in each iteration without improvement
     * before determining that we have reached a locally optimal solution.
     * @param threshold The number of key swaps allowed without improvement before declaring success
     */
    public void SetKeyImprovementThreshold(int threshold) {
        keyImprovementThreshold = threshold;
    }

    /**
     * Find a (locally) optimum solution to a ciphertext encoded with a simple substitution cipher.
     * @param cipherText The ciphertext
     * @return The best plaintext candidate
     */
    public String Solve(String cipherText) {
        PlainText bestCandidate = new PlainText();
        bestCandidate.text = cipherText;
        bestCandidate.score = scorer.score(cipherText);

        final int threadPoolSize = Runtime.getRuntime().availableProcessors() + 1;
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadPoolSize);
        CompletionService<PlainText> completionService = new ExecutorCompletionService<PlainText>(executor);

        // We continue until we haven't seen an improvement in a while.  We don't have a fixed number of tasks to wait
        // for, so fill the threadpool, then queue new tasks as we get results back until we're finished
        for (int i = 0; i < threadPoolSize; ++i) {
            completionService.submit(new Callable<PlainText>() {
                @Override public PlainText call() throws Exception {
                    return FindAnswer(cipherText, scorer, keyImprovementThreshold);
                }
            });
        }

        int iterationsSinceImprovement = 0;
        try {
            while (iterationsSinceImprovement < randomImprovementThreshold) {
                ++iterationsSinceImprovement;

                Future<PlainText> f = completionService.take();
                PlainText answer = f.get();

                // TODO: Keep a list of the top n answers instead of just the top 1
                if (answer.score > bestCandidate.score) {
                    bestCandidate = answer;
                    iterationsSinceImprovement = 0;
                }

                // Queue a new random task to replace this one
                completionService.submit(new Callable<PlainText>() {
                    @Override public PlainText call() throws Exception {
                        return FindAnswer(cipherText, scorer, keyImprovementThreshold);
                    }
                });
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            System.err.println(e.toString());
        }

        // We don't need any more results
        executor.shutdownNow();

        return bestCandidate.text;
    }

    private static class PlainText {
        String text;
        double score;
    }

    /** Searches for a locally optimum solution starting at a key.  Returns the highest-scoring answer
     * @param cipherText The ciphertext to be decoded
     * @return The best plain text candidate and its score
     */
    private static PlainText FindAnswer(String cipherText, TextScorer scorer, int keyThreshold) {
        KeyedSubstitution replacement = KeyedSubstitution.random();
        replacement.shuffle();

        PlainText bestCandidate = new PlainText();
        bestCandidate.text = cipherText;
        bestCandidate.score = scorer.score(cipherText);

        int iterationsSinceImprovement = 0;
        while (iterationsSinceImprovement < keyThreshold) {
            replacement.swapRandomPair();
            String plainText = replacement.applySubstitution(cipherText);
            double score = scorer.score(plainText);
            if (score > bestCandidate.score) {
                bestCandidate.score = score;
                bestCandidate.text = plainText;
                iterationsSinceImprovement = 0;
            } else {
                replacement.undoLastRandomSwap();
                ++iterationsSinceImprovement;
            }
        }

        return bestCandidate;
    }

    private TextScorer scorer;
    private int randomImprovementThreshold = 20;
    private int keyImprovementThreshold = 1000;
}