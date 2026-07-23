package com.aykut.celtigar.rag.store;

/**
 * One retrieval result: a stored chunk plus how similar it was to the query, and
 * enough source info to cite it later.
 */
public record SearchHit(String documentId, String documentName, String text, double score) {
}
