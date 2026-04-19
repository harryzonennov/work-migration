
# Overview

The `DocumentItemMultiSelect` is an essential UI tool framework designed for cross-document item selection and interaction . 
For instance, you can select multiple items from a source document to generate a new target document based on these selected items. 
A typical use case is selecting multiple delivered items from a purchase order to create an inbound delivery for a warehouse.

## UI Components Framework

This UI framework integrates several UI components that play significant roles. There are two primary types of UI component selectors:

1. **Target Document Selector**: This is for creating the target document within the UI tool. It's managed by the Vue class `DocumentItemMultiSelect` and its subclasses. 
This UI is rendered by different target document type inside the class: `DocumentItemMultiSelectFactory`.

2. **Source Selector Component**: This component is rendered with multiple selectors for different source types. The source document selector is managed by the class `SrcSelectInputUnion` and its subclasses.
This UI is renderd by different source document type inside the class: `DocumentItemMultiSelect`.


![DocumentItemMultiSelectFramework.png](DocumentItemMultiSelectFramework.png)

### Use Cases
The `DocumentItemMultiSelect` tool can be applied in various scenarios, based on pre-defined use cases managed by the `DocumentItemMultiSelectFactory.setDefaultConfig` method:

1. **Cross Document Creation** (`DocumentItemMultiSelectFactory.USE_CASE.CROSS_DOC_CREATION`): Create a new target document using selected items from a source document.

2. **Cross Document Creation by Reserved Document** (`DocumentItemMultiSelectFactory.USE_CASE.CROSS_DOC_CREATION_RESERVED`): 
Generate a target document from selected reserved items, which also calculate the source document items from the reserved items.

3. **Cross Document Creation from Previous Profession Item** (`DocumentItemMultiSelectFactory.USE_CASE.CROSS_DOC_CREATION_FROM_PREVPROF`): 
Use selected previous profession items to create a new target document.

4. **Cross Document Creation to Previous Profession Item** (`DocumentItemMultiSelectFactory.USE_CASE.CROSS_DOC_CREATION_TO_PREVPROF`): 
Similar to the above, this use case also creates a new target document using selected previous profession items.

5. **Merge Documents** (`DocumentItemMultiSelectFactory.USE_CASE.MERGE_DOC`): Merge multiple documents by selecting document items.

## Working Process

The following details explain how this tool manages the cross-document workflow:

### 1. Initialization Configuration
Begin with `DocumentItemMultiSelectFactory.initBatchSelection`, which sets up the configuration for the target document selection UI component.  
The UI component, rendered by `DocumentItemMultiSelect` and its subclasses (via `DocumentItemMultiSelectFactory.initMultiSelectorArray`), initializes the target document selection through `DocumentItemMultiSelect.initBatchSelection`.

### 2. Source Selector Initialization
Within `DocumentItemMultiSelect.initBatchSelection`, the source selector component is configured using metadata from `vm.meta.srcModelId` via the `DocumentItemMultiSelect.setSrcModelId` method.  
Default configurations are applied through `DocumentItemMultiSelect.setDefaultConfig`, depending on the use case. For example, in "Cross Document Creation," source document type and ID selection are disabled by default.


### 3. Loading Selection Data Using Field Metadata
The UI Control `DocumentItemMultiSelect` and its subcomponent `SrcSelectInputUnion` are built using the `Async-field` framework, which renders all fields based on field metadata.  
These field metadata control key data selections that trigger workflow events, including:
- Source document type selection
- Source document root node selection
- Source document item list selection (based on the selected root node)
- Target document type selection
- Target document root node selection

Metadata loading for these selections is initiated via the `postUpdate` method in `DocumentItemMultiSelect.initBatchSelection` 
after configurations for `DocumentItemMultiSelect` and `SrcSelectInputUnion` are completed.  
The `postUpdate` method invokes `updateConfig` and `loadMetaData` to finalize the selection loading process.

### 4. Source Document Selection and Material Item List Rendering
This section explains how the material item list is rendered within the workflow:
- **By Source Document Root Node Selection**:  
  When the `searchSrcDocMeta` parameter is set, the source document selection is triggered using data from `searchSrcDocMeta`.  
  After selecting the source document root node, the material item list is loaded as sub-nodes and rendered on the UI.

- **By Direct Source Document Item Search**:  
  When the `searchSrcDocItemMeta` parameter is configured, the source document item search is directly triggered.  
  The material item list is then loaded and rendered on the UI.


## FAQ

### How is the material item list rendered once a source document is specified?

#### **Answer: Workflow Overview**

1. **Loading the Source Document:**  
   - The first step involves loading the source document service's UI model. This is accomplished using the method `SrcSelectInputUnion.loadSrcDocumentSelectList`, which populates the source document selection list.  
   - If a specific source document is selected, the method `loadSrcDocument` is invoked to load that document.  
   - Once the document loading is complete, the method `fnSrcDocumentSelected` is called to handle post-load activities.

2. **Handling the Selected Source Document:**  
   - After the method `fnSrcDocumentSelected` processes the loaded source document, it triggers the `srcDocSelect` event from `SrcSelectInputUnion`.  
   - The material item list data associated with the selected source document is included in the event as the attribute `itemModelList`.

3. **Event Handling in MultiSelect:**  
   - The `srcDocSelect` event is handled by the method `DocumentItemMultiSelect.fnSrcDocumentSelected`.  
   - This method is responsible for rendering the material item list data on the appropriate UI control.

### How to Refresh the Warehouse Selection in `DocumentItemMultiSelect` After Updating `refWarehouseUUID`

#### **Answer: Workflow Overview**

When the target document is updated in the `DocumentItemMultiSelect` control, follow these steps to update the `refWarehouseUUID` property and refresh the warehouse selection UI component:

1. **Trigger the `postUpdate` Method**  
   When the target document is updated, the `fnTargetDocumentSelected` method is invoked. Inside this method, the `postUpdate` method is also triggered.  
   The `postUpdate` method is a standard lifecycle function used to handle cases where data from the backend or other sources must be updated on the UI client. It performs the following actions:
   - Refreshes the UI controls.
   - Triggers the `postUpdate` method of child components.

2. **Warehouse Selection as a Child Component**  
   Warehouse selection is implemented as a child component (`warehouse-area-union`) within the `DocumentItemMultiSelect` control. 
   When the `postUpdate` method in the parent component is triggered, the same method is also invoked in the child component.  
   The child component's `postUpdate` method performs these tasks:
   - **Initialize the Selection Configuration:** Sets up or refresh the initialize configuration for the warehouse selection.
   - **Load the Base Module Selection:** Loads the necessary selection data for the base module.
   - **Trigger the Base Module Selection Event:** Trigger the standard `select2` component `change` event.


### How to Search and Refreshing the Source Material List When Warehouse Selection is Updated

#### **Background**

Certain subclasses of `DocumentItemMultiSelect`, such as `InventoryTransferOrderMultiSelect` and `InboundDeliveryMultiSelect`, 
require the source material item list to dynamically update based on the selected warehouse. 
When the warehouse selection changes, the source material item list needs to be refreshed and searched using the updated warehouse and area values as request data.

#### **Working Process**

To implement this functionality, the following process is followed:

1. **Data Refresh Mechanism**  
   The method `DocumentItemMultiSelect.loadSrcDataWrapper` is responsible for triggering the source data search and refresh. It retrieves search request data from the following path:  
   `vm.cache.searchSrcDoc.searchSrcDocItemMeta.requestData`.

2. **Warehouse Selection Handling**  
   In subclasses like `InventoryTransferOrderMultiSelect` and `InboundDeliveryMultiSelect`, specific warehouse selection handler methods (e.g., `fnWarehouseUUID` or `fnOutboundWarehouseUUID`) 
are used to retrieve the newly updated warehouse UUID value.
   - These handler methods invoke `WarehouseStoreManager.refreshWarehouseSearch`, which updates the warehouse UUID value in the `vm.cache.searchSrcDoc.searchSrcDocItemMeta.requestData`.
   - Subsequently, `DocumentItemMultiSelect.loadSrcDataWrapper` is called to initiate the source data search and refresh the source material item list accordingly.
