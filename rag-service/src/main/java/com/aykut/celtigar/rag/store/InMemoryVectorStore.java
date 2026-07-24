package com.aykut.celtigar.rag.store;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

/**
 * A naive in-memory vector store: it keeps every chunk in a list and scans them all
 * on each search. Fine for a demo with a handful of documents; a real vector database
 * (pgvector/Qdrant) indexes vectors so search stays fast at scale. Swappable via
 * {@link VectorStore}.
 */
@Component
public class InMemoryVectorStore implements VectorStore {

    private record Entry(String documentId, String documentName, String text, float[] vector) {
    }

    private final List<Entry> entries = new CopyOnWriteArrayList<>();

    @Override
    public void add(String documentId, String documentName, String text, float[] vector) {
        entries.add(new Entry(documentId, documentName, text, vector));
    }

    @Override
    public List<SearchHit> search(float[] query, int topK) {
        List<SearchHit> hits = new ArrayList<>();
        for (Entry e : entries) {
            hits.add(new SearchHit(e.documentId(), e.documentName(), e.text(), cosine(query, e.vector())));
        }
        hits.sort(Comparator.comparingDouble(SearchHit::score).reversed());
        return hits.subList(0, Math.min(topK, hits.size()));
    }

    @Override
    public int size() {
        return entries.size();
    }

    /** Cosine similarity. Vectors are already unit-length, so this is just a dot product. */
    private double cosine(float[] a, float[] b) {
        double dot = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
        }
        return dot;
    }
}
