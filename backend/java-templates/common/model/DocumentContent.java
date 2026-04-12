package com.company.IntelligentPlatform.common.model;

import jakarta.persistence.*;

/**
 * Intermediate superclass for all document root node entities.
 * Migrated from: ThorsteinPlatform - DocumentContent.hbm.xml
 * Table: DocumentContent (schema: common) — only if used as a standalone entity.
 *
 * Hierarchy:
 *   ServiceEntityNode
 *       └── DocumentContent          (adds document-specific fields)
 *               ├── SalesContract    (schema: sales)
 *               ├── PurchaseContract (schema: logistics)
 *               └── ProductionOrder  (schema: production)
 *
 * DocumentContent adds the common document root node fields:
 *   status, priorityCode, documentCategoryType,
 *   and the document chain links (prev/next doc UUID references).
 */
@MappedSuperclass
public abstract class DocumentContent extends ServiceEntityNode {

    @Column(name = "status")
    private int status;

    @Column(name = "priorityCode")
    private int priorityCode;

    @Column(name = "documentCategoryType")
    private int documentCategoryType;

    // Document chain — previous document links
    @Column(name = "prevProfDocType")
    private int prevProfDocType;

    @Column(name = "prevProfDocUUID")
    private String prevProfDocUUID;

    @Column(name = "prevDocType")
    private int prevDocType;

    @Column(name = "prevDocUUID")
    private String prevDocUUID;

    // Document chain — next document links
    @Column(name = "nextProfDocType")
    private int nextProfDocType;

    @Column(name = "nextProfDocUUID")
    private String nextProfDocUUID;

    @Column(name = "nextDocType")
    private int nextDocType;

    @Column(name = "nextDocUUID")
    private String nextDocUUID;

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public int getPriorityCode() { return priorityCode; }
    public void setPriorityCode(int priorityCode) { this.priorityCode = priorityCode; }

    public int getDocumentCategoryType() { return documentCategoryType; }
    public void setDocumentCategoryType(int documentCategoryType) { this.documentCategoryType = documentCategoryType; }

    public int getPrevProfDocType() { return prevProfDocType; }
    public void setPrevProfDocType(int prevProfDocType) { this.prevProfDocType = prevProfDocType; }

    public String getPrevProfDocUUID() { return prevProfDocUUID; }
    public void setPrevProfDocUUID(String prevProfDocUUID) { this.prevProfDocUUID = prevProfDocUUID; }

    public int getPrevDocType() { return prevDocType; }
    public void setPrevDocType(int prevDocType) { this.prevDocType = prevDocType; }

    public String getPrevDocUUID() { return prevDocUUID; }
    public void setPrevDocUUID(String prevDocUUID) { this.prevDocUUID = prevDocUUID; }

    public int getNextProfDocType() { return nextProfDocType; }
    public void setNextProfDocType(int nextProfDocType) { this.nextProfDocType = nextProfDocType; }

    public String getNextProfDocUUID() { return nextProfDocUUID; }
    public void setNextProfDocUUID(String nextProfDocUUID) { this.nextProfDocUUID = nextProfDocUUID; }

    public int getNextDocType() { return nextDocType; }
    public void setNextDocType(int nextDocType) { this.nextDocType = nextDocType; }

    public String getNextDocUUID() { return nextDocUUID; }
    public void setNextDocUUID(String nextDocUUID) { this.nextDocUUID = nextDocUUID; }
}
