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
package com.sap.core.odata.core.debug;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.binary.Base64;

import com.sap.core.odata.api.commons.HttpContentType;
import com.sap.core.odata.api.ep.EntityProviderException;
import com.sap.core.odata.api.processor.ODataResponse;
import com.sap.core.odata.core.ep.BasicEntityProvider;
import com.sap.core.odata.core.ep.util.JsonStreamWriter;

/**
 * @author SAP AG
 */
public class DebugInfoBody implements DebugInfo {

  private final ODataResponse response;

  public DebugInfoBody(final ODataResponse response) {
    this.response = response;
  }

  @Override
  public String getName() {
    return "Body";
  }

  @Override
  public void appendJson(final JsonStreamWriter jsonStreamWriter) throws IOException {
    final String contentType = response.getContentHeader();
    if (contentType.startsWith("image/")) {
      if (response.getEntity() instanceof InputStream) {
        jsonStreamWriter.stringValueRaw(Base64.encodeBase64String(getBinaryFromInputStream((InputStream) response.getEntity())));
      } else if (response.getEntity() instanceof String) {
        jsonStreamWriter.stringValueRaw(getContentString());
      } else {
        throw new ClassCastException("Unsupported content entity class: " + response.getEntity().getClass().getName());
      }
    } else if (contentType.startsWith(HttpContentType.APPLICATION_JSON)) {
      jsonStreamWriter.unquotedValue(getContentString());
    } else {
      jsonStreamWriter.stringValue(getContentString());
    }
  }

  private String getContentString() {
    String content;
    if (response.getEntity() instanceof String) {
      content = (String) response.getEntity();
    } else if (response.getEntity() instanceof InputStream) {
      content = getStringFromInputStream((InputStream) response.getEntity());
    } else {
      throw new ClassCastException("Unsupported content entity class: " + response.getEntity().getClass().getName());
    }
    return content;
  }

  private static byte[] getBinaryFromInputStream(final InputStream inputStream) {
    try {
      return new BasicEntityProvider().readBinary(inputStream);
    } catch (final EntityProviderException e) {
      return null;
    }
  }

  private static String getStringFromInputStream(final InputStream inputStream) {
    try {
      return new BasicEntityProvider().readText(inputStream);
    } catch (final EntityProviderException e) {
      return null;
    }
  }
}
