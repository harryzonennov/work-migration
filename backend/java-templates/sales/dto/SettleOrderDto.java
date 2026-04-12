package com.company.IntelligentPlatform.sales.dto;

import com.company.IntelligentPlatform.sales.model.SettleOrder;
import java.time.LocalDate;

/**
 * DTO for SettleOrder create/update requests.
 */
public class SettleOrderDto {

    private String name;
    private String client;
    private String refOrderUUID;
    private int refOrderType;
    private LocalDate executionDate;
    private double curGrossSettlePrice;
    private String note;

    public SettleOrder toEntity() {
        SettleOrder settleOrder = new SettleOrder();
        applyTo(settleOrder);
        return settleOrder;
    }

    public void applyTo(SettleOrder settleOrder) {
        if (name != null)         settleOrder.setName(name);
        if (client != null)       settleOrder.setClient(client);
        if (refOrderUUID != null) settleOrder.setRefOrderUUID(refOrderUUID);
        if (executionDate != null) settleOrder.setExecutionDate(executionDate);
        if (note != null)         settleOrder.setNote(note);
        settleOrder.setRefOrderType(refOrderType);
        settleOrder.setCurGrossSettlePrice(curGrossSettlePrice);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getClient() { return client; }
    public void setClient(String client) { this.client = client; }
    public String getRefOrderUUID() { return refOrderUUID; }
    public void setRefOrderUUID(String refOrderUUID) { this.refOrderUUID = refOrderUUID; }
    public int getRefOrderType() { return refOrderType; }
    public void setRefOrderType(int refOrderType) { this.refOrderType = refOrderType; }
    public LocalDate getExecutionDate() { return executionDate; }
    public void setExecutionDate(LocalDate executionDate) { this.executionDate = executionDate; }
    public double getCurGrossSettlePrice() { return curGrossSettlePrice; }
    public void setCurGrossSettlePrice(double curGrossSettlePrice) { this.curGrossSettlePrice = curGrossSettlePrice; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
