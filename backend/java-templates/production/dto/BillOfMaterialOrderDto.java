package com.company.IntelligentPlatform.production.dto;

import com.company.IntelligentPlatform.production.model.BillOfMaterialOrder;

public class BillOfMaterialOrderDto {

    private String uuid;
    private String name;
    private int status;
    private String refMaterialSKUUUID;
    private double amount;
    private String refUnitUUID;
    private int itemCategory;
    private String refRouteOrderUUID;
    private int leadTimeCalMode;
    private String refWocUUID;
    private int versionNumber;
    private int patchNumber;
    private String refTemplateUUID;
    private String note;

    public BillOfMaterialOrder toEntity() {
        BillOfMaterialOrder e = new BillOfMaterialOrder();
        if (uuid != null) e.setUuid(uuid);
        if (name != null) e.setName(name);
        e.setStatus(status);
        if (refMaterialSKUUUID != null) e.setRefMaterialSKUUUID(refMaterialSKUUUID);
        e.setAmount(amount);
        if (refUnitUUID != null) e.setRefUnitUUID(refUnitUUID);
        e.setItemCategory(itemCategory);
        if (refRouteOrderUUID != null) e.setRefRouteOrderUUID(refRouteOrderUUID);
        e.setLeadTimeCalMode(leadTimeCalMode);
        if (refWocUUID != null) e.setRefWocUUID(refWocUUID);
        e.setVersionNumber(versionNumber);
        e.setPatchNumber(patchNumber);
        if (refTemplateUUID != null) e.setRefTemplateUUID(refTemplateUUID);
        if (note != null) e.setNote(note);
        return e;
    }

    public void applyTo(BillOfMaterialOrder e) {
        if (name != null) e.setName(name);
        e.setStatus(status);
        if (refMaterialSKUUUID != null) e.setRefMaterialSKUUUID(refMaterialSKUUUID);
        e.setAmount(amount);
        if (refUnitUUID != null) e.setRefUnitUUID(refUnitUUID);
        e.setItemCategory(itemCategory);
        if (refRouteOrderUUID != null) e.setRefRouteOrderUUID(refRouteOrderUUID);
        e.setLeadTimeCalMode(leadTimeCalMode);
        if (refWocUUID != null) e.setRefWocUUID(refWocUUID);
        e.setVersionNumber(versionNumber);
        e.setPatchNumber(patchNumber);
        if (refTemplateUUID != null) e.setRefTemplateUUID(refTemplateUUID);
        if (note != null) e.setNote(note);
    }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getRefMaterialSKUUUID() { return refMaterialSKUUUID; }
    public void setRefMaterialSKUUUID(String refMaterialSKUUUID) { this.refMaterialSKUUUID = refMaterialSKUUUID; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getRefUnitUUID() { return refUnitUUID; }
    public void setRefUnitUUID(String refUnitUUID) { this.refUnitUUID = refUnitUUID; }
    public int getItemCategory() { return itemCategory; }
    public void setItemCategory(int itemCategory) { this.itemCategory = itemCategory; }
    public String getRefRouteOrderUUID() { return refRouteOrderUUID; }
    public void setRefRouteOrderUUID(String refRouteOrderUUID) { this.refRouteOrderUUID = refRouteOrderUUID; }
    public int getLeadTimeCalMode() { return leadTimeCalMode; }
    public void setLeadTimeCalMode(int leadTimeCalMode) { this.leadTimeCalMode = leadTimeCalMode; }
    public String getRefWocUUID() { return refWocUUID; }
    public void setRefWocUUID(String refWocUUID) { this.refWocUUID = refWocUUID; }
    public int getVersionNumber() { return versionNumber; }
    public void setVersionNumber(int versionNumber) { this.versionNumber = versionNumber; }
    public int getPatchNumber() { return patchNumber; }
    public void setPatchNumber(int patchNumber) { this.patchNumber = patchNumber; }
    public String getRefTemplateUUID() { return refTemplateUUID; }
    public void setRefTemplateUUID(String refTemplateUUID) { this.refTemplateUUID = refTemplateUUID; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
