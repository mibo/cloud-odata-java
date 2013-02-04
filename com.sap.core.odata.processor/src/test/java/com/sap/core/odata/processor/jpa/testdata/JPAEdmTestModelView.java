package com.sap.core.odata.processor.jpa.testdata;

import java.util.List;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.Metamodel;

import com.sap.core.odata.api.edm.FullQualifiedName;
import com.sap.core.odata.api.edm.provider.Association;
import com.sap.core.odata.api.edm.provider.AssociationEnd;
import com.sap.core.odata.api.edm.provider.AssociationSet;
import com.sap.core.odata.api.edm.provider.ComplexProperty;
import com.sap.core.odata.api.edm.provider.ComplexType;
import com.sap.core.odata.api.edm.provider.EntityContainer;
import com.sap.core.odata.api.edm.provider.EntitySet;
import com.sap.core.odata.api.edm.provider.EntityType;
import com.sap.core.odata.api.edm.provider.Key;
import com.sap.core.odata.api.edm.provider.NavigationProperty;
import com.sap.core.odata.api.edm.provider.Property;
import com.sap.core.odata.api.edm.provider.ReferentialConstraint;
import com.sap.core.odata.api.edm.provider.Schema;
import com.sap.core.odata.api.edm.provider.SimpleProperty;
import com.sap.core.odata.processor.jpa.api.access.JPAEdmBuilder;
import com.sap.core.odata.processor.jpa.api.model.JPAEdmAssociationEndView;
import com.sap.core.odata.processor.jpa.api.model.JPAEdmAssociationSetView;
import com.sap.core.odata.processor.jpa.api.model.JPAEdmAssociationView;
import com.sap.core.odata.processor.jpa.api.model.JPAEdmBaseView;
import com.sap.core.odata.processor.jpa.api.model.JPAEdmComplexPropertyView;
import com.sap.core.odata.processor.jpa.api.model.JPAEdmComplexTypeView;
import com.sap.core.odata.processor.jpa.api.model.JPAEdmEntityContainerView;
import com.sap.core.odata.processor.jpa.api.model.JPAEdmEntitySetView;
import com.sap.core.odata.processor.jpa.api.model.JPAEdmEntityTypeView;
import com.sap.core.odata.processor.jpa.api.model.JPAEdmKeyView;
import com.sap.core.odata.processor.jpa.api.model.JPAEdmModelView;
import com.sap.core.odata.processor.jpa.api.model.JPAEdmNavigationPropertyView;
import com.sap.core.odata.processor.jpa.api.model.JPAEdmPropertyView;
import com.sap.core.odata.processor.jpa.api.model.JPAEdmReferentialConstraintView;
import com.sap.core.odata.processor.jpa.api.model.JPAEdmSchemaView;

public abstract class JPAEdmTestModelView implements
JPAEdmAssociationEndView,
JPAEdmAssociationSetView,
JPAEdmAssociationView,
JPAEdmBaseView,
JPAEdmComplexPropertyView,
JPAEdmComplexTypeView,
JPAEdmEntityContainerView,
JPAEdmEntitySetView,
JPAEdmEntityTypeView,
JPAEdmKeyView,
JPAEdmModelView,
JPAEdmNavigationPropertyView,
JPAEdmPropertyView,
JPAEdmReferentialConstraintView,
JPAEdmSchemaView
{

	@Override
	public List<String> getNonKeyComplexTypeList() {
		
		return null;
	}

	@Override
	public void addNonKeyComplexName(String complexTypeName) {
		
		
	}

	@Override
	public ReferentialConstraint getEdmReferentialConstraint() {
		
		return null;
	}

	@Override
	public String getRelationShipName() {
		
		return null;
	}

	@Override
	public boolean isExists() {
		
		return false;
	}

	@Override
	public EntityType searchEdmEntityType(String arg0) {
		
		return null;
	}

	@Override
	public JPAEdmReferentialConstraintView getJPAEdmReferentialConstraintView() {
		
		return null;
	}

	@Override
	public Schema getEdmSchema() {
		
		return null;
	}

	@Override
	public JPAEdmAssociationView getJPAEdmAssociationView() {
		
		return null;
	}

	@Override
	public JPAEdmComplexTypeView getJPAEdmComplexTypeView() {
		
		return null;
	}

	@Override
	public JPAEdmEntityContainerView getJPAEdmEntityContainerView() {
		
		return null;
	}

	@Override
	public Attribute<?, ?> getJPAAttribute() {
		
		return null;
	}

	@Override
	public JPAEdmKeyView getJPAEdmKeyView() {
		
		return null;
	}

	@Override
	public List<Property> getPropertyList() {
		
		return null;
	}

	@Override
	public SimpleProperty getSimpleProperty() {
		
		return null;
	}

	@Override
	public JPAEdmSchemaView getSchemaView() {
		
		return null;
	}

	@Override
	public Key getEdmKey() {
		
		return null;
	}

	@Override
	public List<EntityType> getConsistentEdmEntityTypes() {
		
		return null;
	}

	@Override
	public EntityType getEdmEntityType() {
		
		return null;
	}

	@Override
	public javax.persistence.metamodel.EntityType<?> getJPAEntityType() {
		
		return null;
	}

	@Override
	public List<EntitySet> getConsistentEntitySetList() {
		
		return null;
	}

	@Override
	public EntitySet getEdmEntitySet() {
		
		return null;
	}

	@Override
	public JPAEdmEntityTypeView getJPAEdmEntityTypeView() {
		
		return null;
	}

	@Override
	public List<EntityContainer> getConsistentEdmEntityContainerList() {
		
		return null;
	}

	@Override
	public JPAEdmAssociationSetView getEdmAssociationSetView() {
		
		return null;
	}

	@Override
	public EntityContainer getEdmEntityContainer() {
		
		return null;
	}

	@Override
	public JPAEdmEntitySetView getJPAEdmEntitySetView() {
		
		return null;
	}

	@Override
	public void addCompleTypeView(JPAEdmComplexTypeView arg0) {
		
		
	}

	/*@Override
	public void expandEdmComplexType(ComplexType arg0, List<Property> arg1) {
		
		
	}*/

	@Override
	public List<ComplexType> getConsistentEdmComplexTypes() {
		
		return null;
	}

	@Override
	public ComplexType getEdmComplexType() {
		
		return null;
	}

	@Override
	public EmbeddableType<?> getJPAEmbeddableType() {
		
		return null;
	}

	@Override
	public ComplexType searchComplexType(String arg0) {
		
		return null;
	}

	@Override
	public ComplexType searchComplexType(FullQualifiedName arg0) {
		
		return null;
	}

	@Override
	public ComplexProperty getEdmComplexProperty() {
		
		return null;
	}

	@Override
	public void clean() {
		
		
	}

	@Override
	public JPAEdmBuilder getBuilder() {
		
		return null;
	}

	@Override
	public Metamodel getJPAMetaModel() {
		
		return null;
	}

	@Override
	public String getpUnitName() {
		
		return null;
	}

	@Override
	public boolean isConsistent() {
		
		return false;
	}

	@Override
	public void addJPAEdmAssociationView(JPAEdmAssociationView arg0) {
		
		
	}

	@Override
	public void addJPAEdmRefConstraintView(JPAEdmReferentialConstraintView arg0) {
		
		
	}

	@Override
	public List<Association> getConsistentEdmAssociationList() {
		
		return null;
	}

	@Override
	public Association searchAssociation(JPAEdmAssociationEndView arg0) {
		
		return null;
	}

	@Override
	public List<AssociationSet> getConsistentEdmAssociationSetList() {
		
		return null;
	}

	@Override
	public Association getEdmAssociation() {
		
		return null;
	}

	@Override
	public AssociationSet getEdmAssociationSet() {
		
		return null;
	}

	@Override
	public boolean compare(AssociationEnd arg0, AssociationEnd arg1) {
		
		return false;
	}

	@Override
	public AssociationEnd getAssociationEnd1() {
		
		return null;
	}

	@Override
	public AssociationEnd getAssociationEnd2() {
		
		return null;
	}
	
	@Override
	public void addJPAEdmNavigationPropertyView(
			JPAEdmNavigationPropertyView view) {
		
		
	}

	@Override
	public List<NavigationProperty> getConsistentEdmNavigationProperties() {
		
		return null;
	}

	@Override
	public NavigationProperty getEdmNavigationProperty() {
		
		return null;
	}

	@Override
	public JPAEdmNavigationPropertyView getJPAEdmNavigationPropertyView() {
		
		return null;
	}

	@Override
	public void expandEdmComplexType(ComplexType complexType,
			List<Property> expandedPropertyList, String embeddablePropertyName) {
		
		
	}
}
