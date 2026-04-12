package com.company.IntelligentPlatform.finance.model;

import com.company.IntelligentPlatform.common.model.ServiceEntityNode;
import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinFinance - FinAccountAttachment.java
 * New table: FinAccountAttachment (schema: finance)
 */
@Entity
@Table(name = "FinAccountAttachment", schema = "finance")
public class FinAccountAttachment extends ServiceEntityNode {

    @Lob
    @Column(name = "content")
    private byte[] content;

    @Column(name = "fileType")
    private String fileType;

    public byte[] getContent() { return content; }
    public void setContent(byte[] content) { this.content = content; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
}
