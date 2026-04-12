package com.company.IntelligentPlatform.production.model;

import com.company.IntelligentPlatform.common.model.ServiceEntityNode;
import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinProduction - ProdProcess (extends ServiceEntityNode)
 * Table: ProdProcess (schema: production)
 */
@Entity
@Table(name = "ProdProcess", schema = "production")
public class ProdProcess extends ServiceEntityNode {

    public static final int STATUS_INITIAL = 1;
    public static final int STATUS_INUSE   = 2;
    public static final int STATUS_RETIRED = 3;

    @Column(name = "keyProcessFlag")
    private int keyProcessFlag;

    @Column(name = "status")
    private int status;

    @Column(name = "refWorkCenterUUID")
    private String refWorkCenterUUID;

    @Column(name = "productionBatchSize")
    private double productionBatchSize;

    @Column(name = "moveBatchSize")
    private double moveBatchSize;

    @Column(name = "fixedExecutionTime")
    private double fixedExecutionTime;

    @Column(name = "varExecutionTime")
    private double varExecutionTime;

    @Column(name = "prepareTime")
    private double prepareTime;

    @Column(name = "queueTime")
    private double queueTime;

    @Column(name = "fixedMoveTime")
    private double fixedMoveTime;

    @Column(name = "varMoveTime")
    private double varMoveTime;

    public int getKeyProcessFlag() { return keyProcessFlag; }
    public void setKeyProcessFlag(int keyProcessFlag) { this.keyProcessFlag = keyProcessFlag; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getRefWorkCenterUUID() { return refWorkCenterUUID; }
    public void setRefWorkCenterUUID(String refWorkCenterUUID) { this.refWorkCenterUUID = refWorkCenterUUID; }
    public double getProductionBatchSize() { return productionBatchSize; }
    public void setProductionBatchSize(double productionBatchSize) { this.productionBatchSize = productionBatchSize; }
    public double getMoveBatchSize() { return moveBatchSize; }
    public void setMoveBatchSize(double moveBatchSize) { this.moveBatchSize = moveBatchSize; }
    public double getFixedExecutionTime() { return fixedExecutionTime; }
    public void setFixedExecutionTime(double fixedExecutionTime) { this.fixedExecutionTime = fixedExecutionTime; }
    public double getVarExecutionTime() { return varExecutionTime; }
    public void setVarExecutionTime(double varExecutionTime) { this.varExecutionTime = varExecutionTime; }
    public double getPrepareTime() { return prepareTime; }
    public void setPrepareTime(double prepareTime) { this.prepareTime = prepareTime; }
    public double getQueueTime() { return queueTime; }
    public void setQueueTime(double queueTime) { this.queueTime = queueTime; }
    public double getFixedMoveTime() { return fixedMoveTime; }
    public void setFixedMoveTime(double fixedMoveTime) { this.fixedMoveTime = fixedMoveTime; }
    public double getVarMoveTime() { return varMoveTime; }
    public void setVarMoveTime(double varMoveTime) { this.varMoveTime = varMoveTime; }
}
