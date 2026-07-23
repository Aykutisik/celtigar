package com.aykut.celtigar.rag.embedding;

import org.springframework.stereotype.Component;

/**
 * A stand-in embedding model — the RAG counterpart of the Gateway's EchoProvider.
 *
 * <p>It hashes each word into a bucket (bag-of-words hashing) and L2-normalizes the
 * result. This is NOT semantic: it only captures word overlap, so "izin" and "tatil"
 * look unrelated even though a real model would place them close. It exists purely to
 * prove the ingest → embed → store → retrieve plumbing without an API key. Phase 2
 * replaces it with a real {@link EmbeddingModel}.
 */
@Component
public class HashingEmbeddingModel implements EmbeddingModel {

    private static final int DIMENSION = 256;

    @Override
    public float[] embed(String text) {
        float[] vec = new float[DIMENSION];
        for (String token : text.toLowerCase().split("[^\\p{L}\\p{N}]+")) {
            if (token.isEmpty()) {
                continue;
            }
            int bucket = Math.floorMod(token.hashCode(), DIMENSION);
            vec[bucket] += 1.0f;
        }
        normalize(vec);
        return vec;
    }

    @Override
    public int dimension() {
        return DIMENSION;
    }

    /** Scale the vector to unit length so cosine similarity is a plain dot product. */
    private void normalize(float[] vec) {
        double sumSquares = 0.0;
        for (float v : vec) {
            sumSquares += v * v;
        }
        double norm = Math.sqrt(sumSquares);
        if (norm == 0.0) {
            return; // all-zero vector (e.g. empty text); leave as is
        }
        for (int i = 0; i < vec.length; i++) {
            vec[i] /= (float) norm;
        }
    }
}
