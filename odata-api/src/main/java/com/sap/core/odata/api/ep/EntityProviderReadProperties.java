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
package com.sap.core.odata.api.ep;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.sap.core.odata.api.ep.callback.OnReadInlineContent;

/**
 * The {@link EntityProviderReadProperties} contains all necessary settings to read an entity with the {@link EntityProvider}.
 * 
 * The main settings are
 * <ul>
 * <li>the <code>mergeSemantic</code></li>
 * <li>the <code>callback for inlined navigation properties</code></li>
 * <li>and the <code>type mappings</code></li>
 * </ul>
 * 
 * @author SAP AG
 */
public class EntityProviderReadProperties {
  /** Callback which is necessary if entity contains inlined navigation properties. */
  private OnReadInlineContent callback;
  /**
   * if merge is <code>true</code> the input content is in context of an <b>merge</b> (e.g. MERGE, PATCH) read request, 
   * otherwise if <code>false</code> it is an <b>none merge</b> (e.g. CREATE) read request
   */
  private boolean merge;
  /**
   * typeMappings contains mappings from <code>edm property name</code> to <code>java class</code> which should be used 
   * for a type mapping during read of content. If according <code>edm property</code> can not be read
   * into given <code>java class</code> an {@link EntityProviderException} is thrown.
   * Supported mappings are documented in {@link com.sap.core.odata.api.edm.EdmSimpleType}.
   */
  final private Map<String, Object> typeMappings;
  final private Map<String, String> validatedPrefix2NamespaceUri;

  private EntityProviderReadProperties() {
    typeMappings = new HashMap<String, Object>();
    validatedPrefix2NamespaceUri = new HashMap<String, String>();
  }

  public static EntityProviderReadPropertiesBuilder init() {
    return new EntityProviderReadPropertiesBuilder();
  }

  public static EntityProviderReadPropertiesBuilder initFrom(final EntityProviderReadProperties properties) {
    return new EntityProviderReadPropertiesBuilder(properties);
  }

  public Map<String, String> getValidatedPrefixNamespaceUris() {
    return Collections.unmodifiableMap(validatedPrefix2NamespaceUri);
  }

  public Map<String, Object> getTypeMappings() {
    return Collections.unmodifiableMap(typeMappings);
  }

  public OnReadInlineContent getCallback() {
    return callback;
  }

  public boolean getMergeSemantic() {
    return merge;
  }

  /**
   * @author SAP AG
   */
  public static class EntityProviderReadPropertiesBuilder {
    private final EntityProviderReadProperties properties = new EntityProviderReadProperties();

    public EntityProviderReadPropertiesBuilder() {}

    public EntityProviderReadPropertiesBuilder(final EntityProviderReadProperties propertiesFrom) {
      properties.merge = propertiesFrom.merge;
      properties.callback = propertiesFrom.callback;
      addValidatedPrefixes(propertiesFrom.validatedPrefix2NamespaceUri);
      addTypeMappings(propertiesFrom.typeMappings);
    }

    public EntityProviderReadPropertiesBuilder mergeSemantic(final boolean mergeSemantic) {
      properties.merge = mergeSemantic;
      return this;
    }

    public EntityProviderReadPropertiesBuilder callback(final OnReadInlineContent callback) {
      properties.callback = callback;
      return this;
    }

    public EntityProviderReadPropertiesBuilder addValidatedPrefixes(final Map<String, String> prefix2NamespaceUri) {
      if (prefix2NamespaceUri != null) {
        properties.validatedPrefix2NamespaceUri.putAll(prefix2NamespaceUri);
      }
      return this;
    }

    public EntityProviderReadPropertiesBuilder addTypeMappings(final Map<String, Object> typeMappings) {
      if (typeMappings != null) {
        properties.typeMappings.putAll(typeMappings);
      }
      return this;
    }

    public EntityProviderReadProperties build() {
      return properties;
    }
  }
}
