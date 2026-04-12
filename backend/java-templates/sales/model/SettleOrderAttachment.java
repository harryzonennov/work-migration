package com.company.IntelligentPlatform.sales.model;

import com.company.IntelligentPlatform.common.model.ServiceEntityNode;
import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinSalesDistribution - SettleOrderAttachment (extends ServiceEntityNode)
 * Table: SettleOrderAttachment (schema: sales)
 */
@Entity
@Table(name = "SettleOrderAttachment", schema = "sales")
public class SettleOrderAttachment extends ServiceEntityNode {

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
