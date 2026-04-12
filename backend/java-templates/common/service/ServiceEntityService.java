package com.company.IntelligentPlatform.common.service;

import com.company.IntelligentPlatform.common.model.ServiceEntityNode;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Base service class for all domain services in IntelligentPlatform.
 *
 * Replaces: ThorsteinPlatform ServiceEntityManager
 *
 * Provides core CRUD, admin data management, bind-list diff, archive/delete,
 * and hierarchy traversal methods.  All domain ***Service classes must extend
 * this class instead of implementing their own create/update/delete helpers.
 *
 * Key design decisions:
 *  - Admin data (createdBy, createdTime, lastUpdateBy, lastUpdateTime,
 *    resEmployeeUUID, resOrgUUID) is managed here via setAdminData().
 *  - UUID is assigned here via insertSENode() — NOT in entity constructors.
 *  - Archive uses a CLIENT_ARCHIVE_PREFIX on the client field, matching
 *    the old archiveModuleCore() pattern.
 *  - Bind-list diff (updateSEBindList) compares a new list against a backup
 *    list and drives create / update / delete per item.
 */
@Transactional
public abstract class ServiceEntityService {

	/** Prefix added to client field when a node is archived */
	public static final String CLIENT_ARCHIVE_PREFIX = "arc_";

	/** processMode constants (equivalent to ServiceEntityBindModel) */
	public static final int PROCESS_MODE_CREATE = 1;

	public static final int PROCESS_MODE_UPDATE = 2;

	public static final int PROCESS_MODE_DELETE = 3;

	@PersistenceContext
	protected EntityManager entityManager;

	// ──────────────────────────────────────────────────────────────────────────
	// Admin data helpers
	// ──────────────────────────────────────────────────────────────────────────

	/**
	 * Set admin data fields on a node before a create operation.
	 * Equivalent to old setAdminData(seNode, PROCESS_MODE_CREATE, userUUID, orgUUID).
	 *
	 * @param node         the entity to stamp
	 * @param userUUID     UUID of the logged-in user
	 * @param orgUUID      UUID of the user's organization
	 */
	protected void setAdminDataCreate(ServiceEntityNode node,
			String userUUID, String orgUUID) {
		LocalDateTime now = LocalDateTime.now();
		node.setCreatedBy(userUUID);
		node.setCreatedTime(now);
		node.setLastUpdateBy(userUUID);
		node.setLastUpdateTime(now);
		node.setResEmployeeUUID(userUUID);
		node.setResOrgUUID(orgUUID);
	}

	/**
	 * Set admin data fields on a node before an update operation.
	 * Equivalent to old setAdminData(seNode, PROCESS_MODE_UPDATE, userUUID, orgUUID).
	 *
	 * @param node         the entity to stamp
	 * @param userUUID     UUID of the logged-in user
	 * @param orgUUID      UUID of the user's organization
	 */
	protected void setAdminDataUpdate(ServiceEntityNode node,
			String userUUID, String orgUUID) {
		node.setLastUpdateBy(userUUID);
		node.setLastUpdateTime(LocalDateTime.now());
		node.setResEmployeeUUID(userUUID);
		node.setResOrgUUID(orgUUID);
	}

	// ──────────────────────────────────────────────────────────────────────────
	// Factory helpers (newRootEntityNode / newEntityNode equivalents)
	// ──────────────────────────────────────────────────────────────────────────

	/**
	 * Prepare a new root-level entity node (nodeLevel = 0, parentNodeUUID = null).
	 * The caller must set domain-specific fields; the service must call
	 * insertSENode() to persist it.
	 *
	 * @param node    pre-constructed entity instance (fields not yet set)
	 * @param client  client identifier
	 */
	protected void prepareRootNode(ServiceEntityNode node, String client) {
		node.setClient(client);
		node.setNodeLevel(0);
		node.setParentNodeUUID(null);
		node.setRootNodeUUID(null);
	}

	/**
	 * Prepare a child entity node (nodeLevel = parentNodeLevel + 1).
	 *
	 * @param node        pre-constructed entity instance
	 * @param parentNode  the parent node (already persisted)
	 * @param nodeName    the nodeName to assign (e.g. class name)
	 */
	protected void prepareChildNode(ServiceEntityNode node,
			ServiceEntityNode parentNode, String nodeName) {
		node.setClient(parentNode.getClient());
		node.setNodeName(nodeName);
		node.setParentNodeUUID(parentNode.getUuid());
		node.setRootNodeUUID(
				parentNode.getRootNodeUUID() != null
						? parentNode.getRootNodeUUID()
						: parentNode.getUuid());
		node.setNodeLevel(parentNode.getNodeLevel() + 1);
	}

	// ──────────────────────────────────────────────────────────────────────────
	// Core CRUD: insertSENode, updateSENode, deleteSENode
	// ──────────────────────────────────────────────────────────────────────────

	/**
	 * Assign UUID, stamp admin data (create), then persist the entity.
	 * Equivalent to old insertSENode(seNode, logonUserUUID, organizationUUID).
	 *
	 * @param repository  the JpaRepository for this entity type
	 * @param node        the entity to insert
	 * @param userUUID    UUID of the logged-in user
	 * @param orgUUID     UUID of the user's organization
	 * @param <T>         entity type extending ServiceEntityNode
	 * @return the saved entity
	 */
	protected <T extends ServiceEntityNode> T insertSENode(
			JpaRepository<T, String> repository, T node,
			String userUUID, String orgUUID) {
		node.setUuid(UUID.randomUUID().toString());
		setAdminDataCreate(node, userUUID, orgUUID);
		return repository.save(node);
	}

	/**
	 * Stamp admin data (update), then persist the entity.
	 * Equivalent to old updateSENode(seNode, backNode, logonUserUUID, organizationUUID).
	 *
	 * @param repository  the JpaRepository for this entity type
	 * @param node        the entity with updated field values (uuid already set)
	 * @param userUUID    UUID of the logged-in user
	 * @param orgUUID     UUID of the user's organization
	 * @param <T>         entity type extending ServiceEntityNode
	 * @return the saved entity
	 */
	protected <T extends ServiceEntityNode> T updateSENode(
			JpaRepository<T, String> repository, T node,
			String userUUID, String orgUUID) {
		setAdminDataUpdate(node, userUUID, orgUUID);
		return repository.save(node);
	}

	/**
	 * Hard-delete an entity by UUID.
	 * Equivalent to old deleteSENode(seNode, logonUserUUID, organizationUUID).
	 *
	 * @param repository  the JpaRepository for this entity type
	 * @param uuid        UUID of the entity to delete
	 * @param <T>         entity type extending ServiceEntityNode
	 */
	protected <T extends ServiceEntityNode> void deleteSENode(
			JpaRepository<T, String> repository, String uuid) {
		repository.deleteById(uuid);
	}

	// ──────────────────────────────────────────────────────────────────────────
	// Bind-list diff: updateSEBindList
	// ──────────────────────────────────────────────────────────────────────────

	/**
	 * Compare a new list against a backup list and drive create / update / delete
	 * for each item.  Items in the new list that have a null/empty UUID are created;
	 * items present in the backup list but absent from the new list are deleted;
	 * items present in both are updated.
	 *
	 * Equivalent to old updateSEBindList(seBindList, seBindListBack, userUUID, orgUUID).
	 *
	 * @param repository  the JpaRepository for the item entity type
	 * @param newList     the updated list from the client
	 * @param backList    the persisted list retrieved before editing began
	 * @param userUUID    UUID of the logged-in user
	 * @param orgUUID     UUID of the user's organization
	 * @param <T>         entity type extending ServiceEntityNode
	 */
	protected <T extends ServiceEntityNode> void updateSEBindList(
			JpaRepository<T, String> repository,
			List<T> newList, List<T> backList,
			String userUUID, String orgUUID) {
		if (newList == null) {
			newList = new ArrayList<>();
		}
		if (backList == null) {
			backList = new ArrayList<>();
		}

		// Delete items that are in backList but no longer in newList
		for (T backItem : backList) {
			boolean stillPresent = false;
			for (T newItem : newList) {
				if (backItem.getUuid() != null
						&& backItem.getUuid().equals(newItem.getUuid())) {
					stillPresent = true;
					break;
				}
			}
			if (!stillPresent) {
				deleteSENode(repository, backItem.getUuid());
			}
		}

		// Insert or update items in newList
		for (T newItem : newList) {
			boolean existsInBack = false;
			for (T backItem : backList) {
				if (newItem.getUuid() != null
						&& newItem.getUuid().equals(backItem.getUuid())) {
					existsInBack = true;
					break;
				}
			}
			if (existsInBack) {
				updateSENode(repository, newItem, userUUID, orgUUID);
			} else {
				insertSENode(repository, newItem, userUUID, orgUUID);
			}
		}
	}

	// ──────────────────────────────────────────────────────────────────────────
	// Retrieval helpers
	// ──────────────────────────────────────────────────────────────────────────

	/**
	 * Load a single entity by UUID.
	 * Equivalent to old getEntityNodeByUUID(uuid, nodeName, client).
	 *
	 * @param repository  the JpaRepository for this entity type
	 * @param uuid        the UUID to look up
	 * @param <T>         entity type extending ServiceEntityNode
	 * @return the entity, or null if not found
	 */
	@Transactional(readOnly = true)
	protected <T extends ServiceEntityNode> T getEntityNodeByUUID(
			JpaRepository<T, String> repository, String uuid) {
		return repository.findById(uuid).orElse(null);
	}

	/**
	 * Load all entities for a given client.
	 * Equivalent to old getEntityNodeListByKey(client, CLIENT, nodeName).
	 *
	 * @param entityClass  the entity Class (used in JPQL)
	 * @param client       the client to filter by
	 * @param <T>          entity type extending ServiceEntityNode
	 * @return list of matching entities
	 */
	@Transactional(readOnly = true)
	protected <T extends ServiceEntityNode> List<T> getEntityNodeListByClient(
			Class<T> entityClass, String client) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(entityClass);
		Root<T> root = cq.from(entityClass);
		cq.select(root).where(cb.equal(root.get("client"), client));
		return entityManager.createQuery(cq).getResultList();
	}

	/**
	 * Load all child entities of a parent node.
	 * Equivalent to old getEntityNodeListByParentUUID(parentNodeUUID, nodeName, client).
	 *
	 * @param entityClass     the entity Class (used in JPQL)
	 * @param parentNodeUUID  UUID of the parent node
	 * @param <T>             entity type extending ServiceEntityNode
	 * @return list of child entities
	 */
	@Transactional(readOnly = true)
	protected <T extends ServiceEntityNode> List<T> getEntityNodeListByParentNodeUUID(
			Class<T> entityClass, String parentNodeUUID) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(entityClass);
		Root<T> root = cq.from(entityClass);
		cq.select(root).where(cb.equal(root.get("parentNodeUUID"), parentNodeUUID));
		return entityManager.createQuery(cq).getResultList();
	}

	/**
	 * Load all entities that belong to a root node (same tree).
	 * Equivalent to old getEntityNodeListByKey(rootNodeUUID, ROOTNODEUUID, nodeName).
	 *
	 * @param entityClass    the entity Class
	 * @param rootNodeUUID   UUID of the root node
	 * @param <T>            entity type extending ServiceEntityNode
	 * @return list of entities in the same tree
	 */
	@Transactional(readOnly = true)
	protected <T extends ServiceEntityNode> List<T> getEntityNodeListByRootNodeUUID(
			Class<T> entityClass, String rootNodeUUID) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(entityClass);
		Root<T> root = cq.from(entityClass);
		cq.select(root).where(cb.equal(root.get("rootNodeUUID"), rootNodeUUID));
		return entityManager.createQuery(cq).getResultList();
	}

	/**
	 * Load entities by an arbitrary single String field value.
	 * Equivalent to old getEntityNodeListByKey(keyValue, keyName, nodeName, client).
	 *
	 * @param entityClass  the entity Class
	 * @param fieldName    the Java field name to filter on
	 * @param fieldValue   the value to match
	 * @param client       optional client filter; pass null to skip
	 * @param <T>          entity type extending ServiceEntityNode
	 * @return list of matching entities
	 */
	@Transactional(readOnly = true)
	protected <T extends ServiceEntityNode> List<T> getEntityNodeListByKey(
			Class<T> entityClass, String fieldName, Object fieldValue,
			String client) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(entityClass);
		Root<T> root = cq.from(entityClass);
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.equal(root.get(fieldName), fieldValue));
		if (client != null && !client.isEmpty()) {
			predicates.add(cb.equal(root.get("client"), client));
		}
		cq.select(root).where(predicates.toArray(new Predicate[0]));
		return entityManager.createQuery(cq).getResultList();
	}

	/**
	 * Get the record count for an entity type filtered by client.
	 * Equivalent to old getRecordNum(nodeName, client).
	 *
	 * @param entityClass  the entity Class
	 * @param client       the client to filter by
	 * @param <T>          entity type extending ServiceEntityNode
	 * @return number of records
	 */
	@Transactional(readOnly = true)
	protected <T extends ServiceEntityNode> long getRecordNum(
			Class<T> entityClass, String client) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<T> root = cq.from(entityClass);
		cq.select(cb.count(root)).where(cb.equal(root.get("client"), client));
		return entityManager.createQuery(cq).getSingleResult();
	}

	// ──────────────────────────────────────────────────────────────────────────
	// Archive / restore / admin delete
	// ──────────────────────────────────────────────────────────────────────────

	/**
	 * Archive all entities in a module (tree) by prefixing the client field.
	 * Equivalent to old archiveModule(rootNodeUUID, nodeName, client).
	 *
	 * Sets client = "arc_" + client for all nodes whose rootNodeUUID matches,
	 * plus the root node itself.
	 *
	 * @param entityClass   the entity Class
	 * @param rootNodeUUID  UUID of the root node of the module to archive
	 * @param client        original client value
	 * @param <T>           entity type extending ServiceEntityNode
	 */
	protected <T extends ServiceEntityNode> void archiveModule(
			Class<T> entityClass, String rootNodeUUID, String client) {
		String archiveClient = CLIENT_ARCHIVE_PREFIX + client;

		// Archive root node
		List<T> rootList = getEntityNodeListByKey(entityClass, "uuid", rootNodeUUID, client);
		for (T node : rootList) {
			node.setClient(archiveClient);
			entityManager.merge(node);
		}

		// Archive all descendant nodes
		List<T> descendants = getEntityNodeListByRootNodeUUID(entityClass, rootNodeUUID);
		for (T node : descendants) {
			if (client.equals(node.getClient())) {
				node.setClient(archiveClient);
				entityManager.merge(node);
			}
		}
	}

	/**
	 * Restore an archived module by removing the archive client prefix.
	 * Equivalent to old restoreArchiveModule(rootNodeUUID, nodeName, client).
	 *
	 * @param entityClass   the entity Class
	 * @param rootNodeUUID  UUID of the root node to restore
	 * @param client        original (pre-archive) client value
	 * @param <T>           entity type extending ServiceEntityNode
	 */
	protected <T extends ServiceEntityNode> void restoreArchiveModule(
			Class<T> entityClass, String rootNodeUUID, String client) {
		String archiveClient = CLIENT_ARCHIVE_PREFIX + client;

		// Restore root node
		List<T> rootList = getEntityNodeListByKey(entityClass, "uuid", rootNodeUUID, archiveClient);
		for (T node : rootList) {
			node.setClient(client);
			entityManager.merge(node);
		}

		// Restore all descendant nodes
		List<T> descendants = getEntityNodeListByRootNodeUUID(entityClass, rootNodeUUID);
		for (T node : descendants) {
			if (archiveClient.equals(node.getClient())) {
				node.setClient(client);
				entityManager.merge(node);
			}
		}
	}

	/**
	 * Admin-delete: hard-delete all entities in a module (root + all descendants).
	 * Equivalent to old admDeleteModule(rootNodeUUID, nodeName, client).
	 *
	 * @param entityClass   the entity Class
	 * @param rootNodeUUID  UUID of the root node to delete
	 * @param client        the client value
	 * @param <T>           entity type extending ServiceEntityNode
	 */
	protected <T extends ServiceEntityNode> void admDeleteModule(
			Class<T> entityClass, String rootNodeUUID, String client) {
		// Delete all descendants first
		List<T> descendants = getEntityNodeListByRootNodeUUID(entityClass, rootNodeUUID);
		for (T node : descendants) {
			entityManager.remove(entityManager.contains(node) ? node : entityManager.merge(node));
		}

		// Delete root node
		List<T> rootList = getEntityNodeListByKey(entityClass, "uuid", rootNodeUUID, client);
		for (T node : rootList) {
			entityManager.remove(entityManager.contains(node) ? node : entityManager.merge(node));
		}
	}

	/**
	 * Delete all entities for a given client and field value.
	 * Equivalent to old deleteEntityNodeListByKey(keyValue, keyName, nodeName, client).
	 *
	 * @param entityClass  the entity Class
	 * @param fieldName    field name to match
	 * @param fieldValue   field value to match
	 * @param client       the client filter
	 * @param <T>          entity type extending ServiceEntityNode
	 */
	protected <T extends ServiceEntityNode> void deleteEntityNodeListByKey(
			Class<T> entityClass, String fieldName, Object fieldValue,
			String client) {
		List<T> toDelete = getEntityNodeListByKey(entityClass, fieldName, fieldValue, client);
		for (T node : toDelete) {
			entityManager.remove(entityManager.contains(node) ? node : entityManager.merge(node));
		}
	}

	// ──────────────────────────────────────────────────────────────────────────
	// Hierarchy helpers
	// ──────────────────────────────────────────────────────────────────────────

	/**
	 * Change the parent of a node, updating parentNodeUUID and nodeLevel.
	 * Equivalent to old changeParent(seNode, newParentUUID, newParentNodeLevel, ...).
	 *
	 * @param node              the entity whose parent is to be changed
	 * @param newParentNodeUUID UUID of the new parent node
	 * @param newParentLevel    nodeLevel of the new parent
	 * @param userUUID          UUID of the logged-in user
	 * @param orgUUID           UUID of the user's organization
	 */
	protected void changeParent(ServiceEntityNode node,
			String newParentNodeUUID, int newParentLevel,
			String userUUID, String orgUUID) {
		node.setParentNodeUUID(newParentNodeUUID);
		node.setNodeLevel(newParentLevel + 1);
		setAdminDataUpdate(node, userUUID, orgUUID);
		entityManager.merge(node);
	}

	// ──────────────────────────────────────────────────────────────────────────
	// Duplicate check
	// ──────────────────────────────────────────────────────────────────────────

	/**
	 * Check whether an entity with the given field value already exists
	 * (excluding the entity with the given excludeUUID).
	 * Equivalent to old checkIDDuplicate(id, nodeName, client, excludeUUID).
	 *
	 * @param entityClass  the entity Class
	 * @param fieldName    field to check (e.g. "id")
	 * @param fieldValue   the value to check for duplication
	 * @param client       the client scope
	 * @param excludeUUID  UUID of the entity being updated (excluded from check)
	 * @param <T>          entity type extending ServiceEntityNode
	 * @return true if a duplicate exists
	 */
	@Transactional(readOnly = true)
	protected <T extends ServiceEntityNode> boolean checkIDDuplicate(
			Class<T> entityClass, String fieldName, Object fieldValue,
			String client, String excludeUUID) {
		List<T> matches = getEntityNodeListByKey(entityClass, fieldName, fieldValue, client);
		for (T match : matches) {
			if (excludeUUID == null || !excludeUUID.equals(match.getUuid())) {
				return true;
			}
		}
		return false;
	}

	// ──────────────────────────────────────────────────────────────────────────
	// Paginated retrieval
	// ──────────────────────────────────────────────────────────────────────────

	/**
	 * Load entities by client, ordered by lastUpdateTime descending, with pagination.
	 * Equivalent to old getEntityNodeListByKeyLastUpdate(client, nodeName, offset, limit).
	 *
	 * @param entityClass  the entity Class
	 * @param client       the client to filter by
	 * @param offset       first-result offset (0-based)
	 * @param limit        max number of results
	 * @param <T>          entity type extending ServiceEntityNode
	 * @return page of entities
	 */
	@Transactional(readOnly = true)
	protected <T extends ServiceEntityNode> List<T> getEntityNodeListByKeyLastUpdate(
			Class<T> entityClass, String client, int offset, int limit) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(entityClass);
		Root<T> root = cq.from(entityClass);
		cq.select(root)
				.where(cb.equal(root.get("client"), client))
				.orderBy(cb.desc(root.get("lastUpdateTime")));
		TypedQuery<T> query = entityManager.createQuery(cq);
		query.setFirstResult(offset);
		query.setMaxResults(limit);
		return query.getResultList();
	}

}
