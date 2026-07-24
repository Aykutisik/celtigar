package com.aykut.celtigar.rag.web;

import com.aykut.celtigar.rag.store.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Reports how much is currently in the knowledge base. Handy while developing to
 * confirm ingestion actually stored something.
 */
@RestController
public class StatsController {

    private final VectorStore vectorStore;

    public StatsController(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @GetMapping("/stats")
    public Stats stats() {
        return new Stats(vectorStore.size());
    }

    public record Stats(int chunks) {
    }
}
