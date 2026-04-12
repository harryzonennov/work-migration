package com.company.IntelligentPlatform.common.model;

import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinPlatform - ReferenceNode.java
 *
 * Reference service entity node — links an item node back to its parent document.
 * nodeSpecifyType should be set to NODESPECIFYTYPE_REFERENCE by the Manager layer.
 *
 * Hierarchy: ServiceEntityNode → ReferenceNode
 */
@MappedSuperclass
public abstract class ReferenceNode extends ServiceEntityNode {

    @Column(name = "refUUID")
    protected String refUUID;

    @Column(name = "refSEName")
    protected String refSEName;

    @Column(name = "refNodeName")
    protected String refNodeName;

    @Column(name = "refPackageName")
    protected String refPackageName;

    public String getRefUUID() { return refUUID; }
    public void setRefUUID(String refUUID) { this.refUUID = refUUID; }

    public String getRefSEName() { return refSEName; }
    public void setRefSEName(String refSEName) { this.refSEName = refSEName; }

    public String getRefNodeName() { return refNodeName; }
    public void setRefNodeName(String refNodeName) { this.refNodeName = refNodeName; }

    public String getRefPackageName() { return refPackageName; }
    public void setRefPackageName(String refPackageName) { this.refPackageName = refPackageName; }
}
