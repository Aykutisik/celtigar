package com.aykut.celtigar.rag.web;

import com.aykut.celtigar.rag.embedding.EmbeddingModel;
import com.aykut.celtigar.rag.store.SearchHit;
import com.aykut.celtigar.rag.store.VectorStore;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Retrieval endpoint: embed the query, then return the most similar stored chunks.
 * This is what the Gateway/Orchestrator will call (over gRPC) in later phases.
 */
@RestController
public class SearchController {

    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;

    public SearchController(EmbeddingModel embeddingModel, VectorStore vectorStore) {
        this.embeddingModel = embeddingModel;
        this.vectorStore = vectorStore;
    }

    @GetMapping("/search")
    public List<SearchHit> search(@RequestParam("q") String query,
                                  @RequestParam(value = "k", defaultValue = "5") int topK) {
        float[] queryVector = embeddingModel.embed(query);
        return vectorStore.search(queryVector, topK);
    }
}
