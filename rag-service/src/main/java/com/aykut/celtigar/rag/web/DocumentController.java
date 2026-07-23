package com.aykut.celtigar.rag.web;

import com.aykut.celtigar.rag.ingest.IngestionService;
import com.aykut.celtigar.rag.ingest.IngestionService.IngestResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ingestion endpoint. Takes raw text for now; file upload (PDF/DOCX via Tika) comes later.
 */
@RestController
public class DocumentController {

    private final IngestionService ingestionService;

    public DocumentController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/documents")
    public IngestResult ingest(@RequestBody IngestRequest request) {
        return ingestionService.ingest(request.name(), request.text());
    }

    public record IngestRequest(String name, String text) {
    }
}
