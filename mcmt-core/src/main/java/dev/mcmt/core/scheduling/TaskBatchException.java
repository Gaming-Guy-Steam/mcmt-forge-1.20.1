package dev.mcmt.core.scheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregates failures from a parallel batch.
 * Contains a human-readable summary and individual causes as suppressed exceptions.
 */
public final class TaskBatchException extends Exception {

    private final List<String> summaries = new ArrayList<>();

    public TaskBatchException(String message) {
        super(message);
    }

    public void add(String summary, Throwable cause) {
        if (summary != null && !summary.isBlank()) summaries.add(summary);
        if (cause != null) addSuppressed(cause);
    }

    public List<String> summaries() {
        return Collections.unmodifiableList(summaries);
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder(super.getMessage());
        for (String s : summaries) {
            sb.append("\n - ").append(s);
        }
        return sb.toString();
    }
}
