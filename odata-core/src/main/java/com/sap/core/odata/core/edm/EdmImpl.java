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
package com.sap.core.odata.core.edm;

import java.util.HashMap;
import java.util.Map;

import com.sap.core.odata.api.edm.Edm;
import com.sap.core.odata.api.edm.EdmAssociation;
import com.sap.core.odata.api.edm.EdmComplexType;
import com.sap.core.odata.api.edm.EdmEntityContainer;
import com.sap.core.odata.api.edm.EdmEntityType;
import com.sap.core.odata.api.edm.EdmException;
import com.sap.core.odata.api.edm.EdmServiceMetadata;
import com.sap.core.odata.api.edm.FullQualifiedName;
import com.sap.core.odata.api.exception.ODataException;

/**
 * @author SAP AG
 */
public abstract class EdmImpl implements Edm {

  private Map<String, EdmEntityContainer> edmEntityContainers;
  private Map<FullQualifiedName, EdmEntityType> edmEntityTypes;
  private Map<FullQualifiedName, EdmComplexType> edmComplexTypes;
  private Map<FullQualifiedName, EdmAssociation> edmAssociations;

  protected EdmServiceMetadata edmServiceMetadata;

  public EdmImpl(final EdmServiceMetadata edmServiceMetadata) {
    edmEntityContainers = new HashMap<String, EdmEntityContainer>();
    edmEntityTypes = new HashMap<FullQualifiedName, EdmEntityType>();
    edmComplexTypes = new HashMap<FullQualifiedName, EdmComplexType>();
    edmAssociations = new HashMap<FullQualifiedName, EdmAssociation>();
    this.edmServiceMetadata = edmServiceMetadata;
  }

  @Override
  public EdmEntityContainer getEntityContainer(final String name) throws EdmException {
    if (edmEntityContainers.containsKey(name)) {
      return edmEntityContainers.get(name);
    }

    EdmEntityContainer edmEntityContainer = null;

    try {
      edmEntityContainer = createEntityContainer(name);
      if (edmEntityContainer != null) {
        if (name == null && edmEntityContainers.containsKey(edmEntityContainer.getName())) {
          //ensure that the same default entity container is stored in the HashMap under null and its name 
          edmEntityContainer = edmEntityContainers.get(edmEntityContainer.getName());
          edmEntityContainers.put(name, edmEntityContainer);
        } else if (edmEntityContainers.containsKey(null) && edmEntityContainers.get(null) != null && name.equals(edmEntityContainers.get(null).getName())) {
          //ensure that the same default entity container is stored in the HashMap under null and its name        
          edmEntityContainer = edmEntityContainers.get(null);
          edmEntityContainers.put(name, edmEntityContainer);
        } else {
          edmEntityContainers.put(name, edmEntityContainer);
        }
      }
    } catch (ODataException e) {
      throw new EdmException(EdmException.COMMON, e);
    }

    return edmEntityContainer;
  }

  @Override
  public EdmEntityType getEntityType(final String namespace, final String name) throws EdmException {
    FullQualifiedName fqName = new FullQualifiedName(namespace, name);
    if (edmEntityTypes.containsKey(fqName)) {
      return edmEntityTypes.get(fqName);
    }

    EdmEntityType edmEntityType = null;

    try {
      edmEntityType = createEntityType(fqName);
      if (edmEntityType != null) {
        edmEntityTypes.put(fqName, edmEntityType);
      }
    } catch (ODataException e) {
      throw new EdmException(EdmException.COMMON, e);
    }

    return edmEntityType;
  }

  @Override
  public EdmComplexType getComplexType(final String namespace, final String name) throws EdmException {
    FullQualifiedName fqName = new FullQualifiedName(namespace, name);
    if (edmComplexTypes.containsKey(fqName)) {
      return edmComplexTypes.get(fqName);
    }

    EdmComplexType edmComplexType = null;

    try {
      edmComplexType = createComplexType(fqName);
      if (edmComplexType != null) {
        edmComplexTypes.put(fqName, edmComplexType);
      }
    } catch (ODataException e) {
      throw new EdmException(EdmException.COMMON, e);
    }

    return edmComplexType;
  }

  @Override
  public EdmAssociation getAssociation(final String namespace, final String name) throws EdmException {
    FullQualifiedName fqName = new FullQualifiedName(namespace, name);
    if (edmAssociations.containsKey(fqName)) {
      return edmAssociations.get(fqName);
    }

    EdmAssociation edmAssociation = null;

    try {
      edmAssociation = createAssociation(fqName);
      if (edmAssociation != null) {
        edmAssociations.put(fqName, edmAssociation);
      }
    } catch (ODataException e) {
      throw new EdmException(EdmException.COMMON, e);
    }

    return edmAssociation;
  }

  @Override
  public EdmServiceMetadata getServiceMetadata() {
    return edmServiceMetadata;
  }

  @Override
  public EdmEntityContainer getDefaultEntityContainer() throws EdmException {
    return getEntityContainer(null);
  }

  protected abstract EdmEntityContainer createEntityContainer(String name) throws ODataException;

  protected abstract EdmEntityType createEntityType(FullQualifiedName fqName) throws ODataException;

  protected abstract EdmComplexType createComplexType(FullQualifiedName fqName) throws ODataException;

  protected abstract EdmAssociation createAssociation(FullQualifiedName fqName) throws ODataException;
}
