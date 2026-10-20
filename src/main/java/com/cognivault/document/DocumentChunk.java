
package com.cognivault.document;

public class DocumentChunk {

    private final String id;
    private final String documentName;
    private final int chunkIndex;
    private final String content;

    public DocumentChunk(
            String id,
            String documentName,
            int chunkIndex,
            String content) {
        this.id = id;
        this.documentName = documentName;
        this.chunkIndex = chunkIndex;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public String getDocumentName() {
        return documentName;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public String getContent() {
        return content;
    }
}
