package com.aykut.celtigar.rag.chunking;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Splits text into fixed-size word windows with overlap, so a sentence straddling a
 * boundary still appears whole in one chunk. Simple but a reasonable default; smarter
 * strategies (paragraph/heading aware) can replace it behind {@link Chunker}.
 */
@Component
public class FixedSizeChunker implements Chunker {

    private static final int CHUNK_WORDS = 40;
    private static final int OVERLAP_WORDS = 10;

    @Override
    public List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }
        String[] words = text.trim().split("\\s+");
        int step = CHUNK_WORDS - OVERLAP_WORDS;
        for (int start = 0; start < words.length; start += step) {
            int end = Math.min(start + CHUNK_WORDS, words.length);
            chunks.add(String.join(" ", java.util.Arrays.copyOfRange(words, start, end)));
            if (end == words.length) {
                break;
            }
        }
        return chunks;
    }
}
