package com.company.IntelligentPlatform.finance.model;

import com.company.IntelligentPlatform.common.model.ReferenceNode;
import jakarta.persistence.*;

/**
 * Migrated from: ThorsteinFinance - FinAccDocRef.java
 * New table: FinAccDocRef (schema: finance)
 * Links a FinAccount to a source business document via refUUID.
 */
@Entity
@Table(name = "FinAccDocRef", schema = "finance")
public class FinAccDocRef extends ReferenceNode {
}
