/*******************************************************************************
 * Copyright 2013 SAP AG
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.sap.core.odata.processor.core.jpa.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;

import javax.persistence.JoinColumn;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import com.sap.core.odata.api.edm.FullQualifiedName;
import com.sap.core.odata.api.edm.provider.Association;
import com.sap.core.odata.api.edm.provider.AssociationEnd;
import com.sap.core.odata.processor.api.jpa.access.JPAEdmBuilder;
import com.sap.core.odata.processor.api.jpa.exception.ODataJPAModelException;
import com.sap.core.odata.processor.api.jpa.exception.ODataJPARuntimeException;
import com.sap.core.odata.processor.core.jpa.common.ODataJPATestConstants;
import com.sap.core.odata.processor.core.jpa.mock.model.JPAAttributeMock;
import com.sap.core.odata.processor.core.jpa.mock.model.JPAJavaMemberMock;
import com.sap.core.odata.processor.core.jpa.mock.model.JPAManagedTypeMock;

public class JPAEdmReferentialConstraintTest extends JPAEdmTestModelView {

  private static JPAEdmReferentialConstraint objJPAEdmReferentialConstraint = null;
  private static JPAEdmReferentialConstraintTest objJPAEdmReferentialConstraintTest = null;

  @Before
  public void setUp() {
    objJPAEdmReferentialConstraintTest = new JPAEdmReferentialConstraintTest();
    objJPAEdmReferentialConstraint = new JPAEdmReferentialConstraint(
        objJPAEdmReferentialConstraintTest,
        objJPAEdmReferentialConstraintTest,
        objJPAEdmReferentialConstraintTest);
    try {
      objJPAEdmReferentialConstraint.getBuilder().build();
    } catch (ODataJPAModelException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void testGetBuilder() {
    assertNotNull(objJPAEdmReferentialConstraint.getBuilder());
  }

  @Test
  public void testGetBuilderIdempotent() {
    JPAEdmBuilder builder1 = objJPAEdmReferentialConstraint.getBuilder();
    JPAEdmBuilder builder2 = objJPAEdmReferentialConstraint.getBuilder();

    assertEquals(builder1.hashCode(), builder2.hashCode());
  }

  @Test
  public void testGetEdmReferentialConstraint() {
    assertNotNull(objJPAEdmReferentialConstraint
        .getEdmReferentialConstraint());
  }

  @Test
  public void testIsExistsTrue() {
    objJPAEdmReferentialConstraintTest = new JPAEdmReferentialConstraintTest();
    objJPAEdmReferentialConstraint = new JPAEdmReferentialConstraint(
        objJPAEdmReferentialConstraintTest,
        objJPAEdmReferentialConstraintTest,
        objJPAEdmReferentialConstraintTest);
    try {
      objJPAEdmReferentialConstraint.getBuilder().build();
      objJPAEdmReferentialConstraint.getBuilder().build();
    } catch (ODataJPAModelException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataJPARuntimeException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    assertTrue(objJPAEdmReferentialConstraint.isExists());
  }

  @Test
  public void testGetRelationShipName() {
    assertEquals("Assoc_SalesOrderHeader_SalesOrderItem",
        objJPAEdmReferentialConstraint.getEdmRelationShipName());
  }

  @Override
  public Association getEdmAssociation() {
    Association association = new Association();
    association.setName("Assoc_SalesOrderHeader_SalesOrderItem");
    association.setEnd1(new AssociationEnd().setType(
        new FullQualifiedName("salesorderprocessing", "String"))
        .setRole("SalesOrderHeader"));
    association.setEnd2(new AssociationEnd()
        .setType(
            new FullQualifiedName("salesorderprocessing",
                "SalesOrderItem")).setRole("SalesOrderItem"));
    return association;
  }

  private Attribute<?, ?> getJPAAttributeLocal() {
    AttributeMock<Object, String> attr = new AttributeMock<Object, String>();
    return attr;
  }

  @Override
  public Attribute<?, ?> getJPAAttribute() {
    return getJPAAttributeLocal();
  }

  @SuppressWarnings("hiding")
  private class AttributeMock<Object, String> extends
      JPAAttributeMock<Object, String> {

    @Override
    public Member getJavaMember() {
      return new JavaMemberMock();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<String> getJavaType() {
      return (Class<String>) java.lang.String.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ManagedType<Object> getDeclaringType() {
      return (ManagedType<Object>) getManagedTypeLocal();
    }

    private ManagedType<?> getManagedTypeLocal() {
      ManagedTypeMock<String> managedTypeMock = new ManagedTypeMock<String>();
      return managedTypeMock;
    }
  }

  @SuppressWarnings("hiding")
  private class ManagedTypeMock<String> extends JPAManagedTypeMock<String> {

    @SuppressWarnings("unchecked")
    @Override
    public Class<String> getJavaType() {
      return (Class<String>) java.lang.String.class;
    }
  }

  private class JavaMemberMock extends JPAJavaMemberMock {
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
      JoinColumn joinColumn = EasyMock.createMock(JoinColumn.class);
      EasyMock.expect(joinColumn.referencedColumnName())
          .andReturn("SOID");
      EasyMock.expect(joinColumn.name()).andReturn("SOID");

      EasyMock.replay(joinColumn);
      return (T) joinColumn;
    }
  }

}
