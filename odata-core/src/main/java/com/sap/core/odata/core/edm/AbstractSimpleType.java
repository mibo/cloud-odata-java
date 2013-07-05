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

import com.sap.core.odata.api.edm.Edm;
import com.sap.core.odata.api.edm.EdmException;
import com.sap.core.odata.api.edm.EdmFacets;
import com.sap.core.odata.api.edm.EdmLiteralKind;
import com.sap.core.odata.api.edm.EdmSimpleType;
import com.sap.core.odata.api.edm.EdmSimpleTypeException;
import com.sap.core.odata.api.edm.EdmTypeKind;

/**
 * Abstract implementation of the EDM simple-type interface.
 * @author SAP AG
 */
public abstract class AbstractSimpleType implements EdmSimpleType {

  @Override
  public boolean equals(final Object obj) {
    return this == obj || (obj != null && getClass() == obj.getClass());
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public String getNamespace() throws EdmException {
    return EDM_NAMESPACE;
  }

  @Override
  public EdmTypeKind getKind() {
    return EdmTypeKind.SIMPLE;
  }

  @Override
  public String getName() throws EdmException {
    final String name = getClass().getSimpleName();
    return name.startsWith(EDM_NAMESPACE) ? name.substring(3) : name;
  }

  @Override
  public boolean isCompatible(final EdmSimpleType simpleType) {
    return equals(simpleType);
  }

  @Override
  public boolean validate(final String value, final EdmLiteralKind literalKind, final EdmFacets facets) {
    try {
      valueOfString(value, literalKind, facets, getDefaultType());
      return true;
    } catch (final EdmSimpleTypeException e) {
      return false;
    }
  }

  @Override
  public final <T> T valueOfString(final String value, final EdmLiteralKind literalKind, final EdmFacets facets, final Class<T> returnType) throws EdmSimpleTypeException {
    if (value == null) {
      if (facets == null || facets.isNullable() == null || facets.isNullable()) {
        return null;
      } else {
        throw new EdmSimpleTypeException(EdmSimpleTypeException.LITERAL_NULL_NOT_ALLOWED);
      }
    }

    if (literalKind == null) {
      throw new EdmSimpleTypeException(EdmSimpleTypeException.LITERAL_KIND_MISSING);
    }

    return internalValueOfString(value, literalKind, facets, returnType);
  }

  protected abstract <T> T internalValueOfString(String value, EdmLiteralKind literalKind, EdmFacets facets, Class<T> returnType) throws EdmSimpleTypeException;

  @Override
  public final String valueToString(final Object value, final EdmLiteralKind literalKind, final EdmFacets facets) throws EdmSimpleTypeException {
    if (value == null) {
      if (facets == null || facets.isNullable() == null || facets.isNullable()) {
        return null;
      } else {
        throw new EdmSimpleTypeException(EdmSimpleTypeException.VALUE_NULL_NOT_ALLOWED);
      }
    }

    if (literalKind == null) {
      throw new EdmSimpleTypeException(EdmSimpleTypeException.LITERAL_KIND_MISSING);
    }

    final String result = internalValueToString(value, literalKind, facets);
    return literalKind == EdmLiteralKind.URI ? toUriLiteral(result) : result;
  }

  protected abstract <T> String internalValueToString(T value, EdmLiteralKind literalKind, EdmFacets facets) throws EdmSimpleTypeException;

  @Override
  public String toUriLiteral(final String literal) throws EdmSimpleTypeException {
    return literal;
  }

  @Override
  public String toString() {
    try {
      return getNamespace() + Edm.DELIMITER + getName();
    } catch (final EdmException e) {
      return super.toString();
    }
  }
}
