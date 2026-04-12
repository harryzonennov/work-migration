package com.company.IntelligentPlatform.production.model;

import com.company.IntelligentPlatform.common.model.ServiceEntityNode;
import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinProduction - ProcessRouteOrder (extends ServiceEntityNode)
 * Table: ProcessRouteOrder (schema: production)
 */
@Entity
@Table(name = "ProcessRouteOrder", schema = "production")
public class ProcessRouteOrder extends ServiceEntityNode {

    public static final int STATUS_INITIAL = 1;
    public static final int STATUS_INUSE   = 2;
    public static final int STATUS_RETIRED = 3;

    public static final int ROUTE_TYPE_STANDARD = 1;
    public static final int ROUTE_TYPE_REPAIR   = 2;

    @Column(name = "keyRouteFlag")
    private int keyRouteFlag;

    @Column(name = "status")
    private int status;

    @Column(name = "refParentProcessRouteUUID")
    private String refParentProcessRouteUUID;

    @Column(name = "refTemplateProcessRouteUUID")
    private String refTemplateProcessRouteUUID;

    @Column(name = "refMaterialSKUUUID")
    private String refMaterialSKUUUID;

    @Column(name = "routeType")
    private int routeType;

    @Column(name = "refUnitUUID")
    private String refUnitUUID;

    public int getKeyRouteFlag() { return keyRouteFlag; }
    public void setKeyRouteFlag(int keyRouteFlag) { this.keyRouteFlag = keyRouteFlag; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getRefParentProcessRouteUUID() { return refParentProcessRouteUUID; }
    public void setRefParentProcessRouteUUID(String refParentProcessRouteUUID) { this.refParentProcessRouteUUID = refParentProcessRouteUUID; }
    public String getRefTemplateProcessRouteUUID() { return refTemplateProcessRouteUUID; }
    public void setRefTemplateProcessRouteUUID(String refTemplateProcessRouteUUID) { this.refTemplateProcessRouteUUID = refTemplateProcessRouteUUID; }
    public String getRefMaterialSKUUUID() { return refMaterialSKUUUID; }
    public void setRefMaterialSKUUUID(String refMaterialSKUUUID) { this.refMaterialSKUUUID = refMaterialSKUUUID; }
    public int getRouteType() { return routeType; }
    public void setRouteType(int routeType) { this.routeType = routeType; }
    public String getRefUnitUUID() { return refUnitUUID; }
    public void setRefUnitUUID(String refUnitUUID) { this.refUnitUUID = refUnitUUID; }
}
