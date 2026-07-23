package com.aykut.celtigar.rag.chunking;

import java.util.List;

/**
 * Splits a document's text into smaller, overlapping pieces suitable for embedding.
 * Different strategies (fixed-size, sentence-based, recursive) can implement this;
 * the pipeline depends only on the interface.
 */
public interface Chunker {

    List<String> chunk(String text);
}
