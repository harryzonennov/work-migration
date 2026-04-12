package com.company.IntelligentPlatform.finance.model;

import com.company.IntelligentPlatform.common.model.DocMatItemNode;
import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinFinance - FinAccountMaterialItem.java
 * New table: FinAccountMaterialItem (schema: finance)
 * Hierarchy: ServiceEntityNode → ReferenceNode → DocMatItemNode → FinAccountMaterialItem
 * Line-item node of a FinAccount. All material/price fields inherited from DocMatItemNode.
 */
@Entity
@Table(name = "FinAccountMaterialItem", schema = "finance")
public class FinAccountMaterialItem extends DocMatItemNode {
}
