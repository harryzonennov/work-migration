package com.company.IntelligentPlatform.production.dto;

import com.company.IntelligentPlatform.production.model.ProdPickingOrder;
import java.time.LocalDate;

public class ProdPickingOrderDto {

    private String uuid;
    private String name;
    private int status;
    private int category;
    private int processType;
    private String approveBy;
    private LocalDate approveDate;
    private int approveType;
    private String processBy;
    private LocalDate processDate;
    private double grossCost;
    private String note;

    public ProdPickingOrder toEntity() {
        ProdPickingOrder e = new ProdPickingOrder();
        if (uuid != null) e.setUuid(uuid);
        if (name != null) e.setName(name);
        e.setStatus(status);
        e.setCategory(category);
        e.setProcessType(processType);
        if (approveBy != null) e.setApproveBy(approveBy);
        if (approveDate != null) e.setApproveDate(approveDate);
        e.setApproveType(approveType);
        if (processBy != null) e.setProcessBy(processBy);
        if (processDate != null) e.setProcessDate(processDate);
        e.setGrossCost(grossCost);
        if (note != null) e.setNote(note);
        return e;
    }

    public void applyTo(ProdPickingOrder e) {
        if (name != null) e.setName(name);
        e.setStatus(status);
        e.setCategory(category);
        e.setProcessType(processType);
        if (approveBy != null) e.setApproveBy(approveBy);
        if (approveDate != null) e.setApproveDate(approveDate);
        e.setApproveType(approveType);
        if (processBy != null) e.setProcessBy(processBy);
        if (processDate != null) e.setProcessDate(processDate);
        e.setGrossCost(grossCost);
        if (note != null) e.setNote(note);
    }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public int getCategory() { return category; }
    public void setCategory(int category) { this.category = category; }
    public int getProcessType() { return processType; }
    public void setProcessType(int processType) { this.processType = processType; }
    public String getApproveBy() { return approveBy; }
    public void setApproveBy(String approveBy) { this.approveBy = approveBy; }
    public LocalDate getApproveDate() { return approveDate; }
    public void setApproveDate(LocalDate approveDate) { this.approveDate = approveDate; }
    public int getApproveType() { return approveType; }
    public void setApproveType(int approveType) { this.approveType = approveType; }
    public String getProcessBy() { return processBy; }
    public void setProcessBy(String processBy) { this.processBy = processBy; }
    public LocalDate getProcessDate() { return processDate; }
    public void setProcessDate(LocalDate processDate) { this.processDate = processDate; }
    public double getGrossCost() { return grossCost; }
    public void setGrossCost(double grossCost) { this.grossCost = grossCost; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
