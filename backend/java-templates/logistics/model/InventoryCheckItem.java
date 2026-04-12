package com.company.IntelligentPlatform.logistics.model;

import com.company.IntelligentPlatform.common.model.DocMatItemNode;
import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinLogistics - InventoryCheckItem (extends DocMatItemNode)
 * Table: InventoryCheckItem (schema: logistics)
 */
@Entity
@Table(name = "InventoryCheckItem", schema = "logistics")
public class InventoryCheckItem extends DocMatItemNode {

    public static final int CHECK_RESULT_BALANCE = 1;
    public static final int CHECK_RESULT_PROFIT  = 2;
    public static final int CHECK_RESULT_LOSS    = 3;

    @Column(name = "refWarehouseStoreItemUUID")
    private String refWarehouseStoreItemUUID;

    @Column(name = "declaredValue")
    private double declaredValue;

    @Column(name = "resultAmount")
    private double resultAmount;

    @Column(name = "resultUnitUUID")
    private String resultUnitUUID;

    @Column(name = "resultDeclaredValue")
    private double resultDeclaredValue;

    @Column(name = "inventCheckResult")
    private int inventCheckResult;

    @Column(name = "updateAmount")
    private double updateAmount;

    @Column(name = "updateDeclaredValue")
    private double updateDeclaredValue;

    @Column(name = "updateUnitUUID")
    private String updateUnitUUID;

    public String getRefWarehouseStoreItemUUID() { return refWarehouseStoreItemUUID; }
    public void setRefWarehouseStoreItemUUID(String refWarehouseStoreItemUUID) { this.refWarehouseStoreItemUUID = refWarehouseStoreItemUUID; }

    public double getDeclaredValue() { return declaredValue; }
    public void setDeclaredValue(double declaredValue) { this.declaredValue = declaredValue; }

    public double getResultAmount() { return resultAmount; }
    public void setResultAmount(double resultAmount) { this.resultAmount = resultAmount; }

    public String getResultUnitUUID() { return resultUnitUUID; }
    public void setResultUnitUUID(String resultUnitUUID) { this.resultUnitUUID = resultUnitUUID; }

    public double getResultDeclaredValue() { return resultDeclaredValue; }
    public void setResultDeclaredValue(double resultDeclaredValue) { this.resultDeclaredValue = resultDeclaredValue; }

    public int getInventCheckResult() { return inventCheckResult; }
    public void setInventCheckResult(int inventCheckResult) { this.inventCheckResult = inventCheckResult; }

    public double getUpdateAmount() { return updateAmount; }
    public void setUpdateAmount(double updateAmount) { this.updateAmount = updateAmount; }

    public double getUpdateDeclaredValue() { return updateDeclaredValue; }
    public void setUpdateDeclaredValue(double updateDeclaredValue) { this.updateDeclaredValue = updateDeclaredValue; }

    public String getUpdateUnitUUID() { return updateUnitUUID; }
    public void setUpdateUnitUUID(String updateUnitUUID) { this.updateUnitUUID = updateUnitUUID; }
}
