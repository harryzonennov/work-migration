package com.company.IntelligentPlatform.sales.dto;

import com.company.IntelligentPlatform.sales.model.SalesArea;

/**
 * DTO for SalesArea create/update requests.
 */
public class SalesAreaDto {

    private String name;
    private String client;
    private String parentAreaUUID;
    private String rootAreaUUID;
    private int level;
    private int convLabelType;
    private String note;

    public SalesArea toEntity() {
        SalesArea area = new SalesArea();
        applyTo(area);
        return area;
    }

    public void applyTo(SalesArea area) {
        if (name != null)           area.setName(name);
        if (client != null)         area.setClient(client);
        if (parentAreaUUID != null) area.setParentAreaUUID(parentAreaUUID);
        if (rootAreaUUID != null)   area.setRootAreaUUID(rootAreaUUID);
        if (note != null)           area.setNote(note);
        area.setLevel(level);
        area.setConvLabelType(convLabelType);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getClient() { return client; }
    public void setClient(String client) { this.client = client; }
    public String getParentAreaUUID() { return parentAreaUUID; }
    public void setParentAreaUUID(String parentAreaUUID) { this.parentAreaUUID = parentAreaUUID; }
    public String getRootAreaUUID() { return rootAreaUUID; }
    public void setRootAreaUUID(String rootAreaUUID) { this.rootAreaUUID = rootAreaUUID; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public int getConvLabelType() { return convLabelType; }
    public void setConvLabelType(int convLabelType) { this.convLabelType = convLabelType; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
