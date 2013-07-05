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
package com.sap.core.odata.core.ep.consumer;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.sap.core.odata.api.edm.Edm;
import com.sap.core.odata.api.edm.EdmEntitySet;
import com.sap.core.odata.api.ep.EntityProviderException;
import com.sap.core.odata.core.ep.util.FormatXml;

/**
 * @author SAP AG
 */
public class XmlLinkConsumer {

  /**
   * Reads single link with format {@code <uri>http://somelink</uri>}.
   * @param reader
   * @param entitySet
   * @return link as string object
   * @throws EntityProviderException
   */
  public String readLink(final XMLStreamReader reader, final EdmEntitySet entitySet) throws EntityProviderException {
    try {
      reader.next();
      return readLink(reader);
    } catch (final XMLStreamException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass().getSimpleName()), e);
    }
  }

  private String readLink(final XMLStreamReader reader) throws XMLStreamException {
    return readTag(reader, Edm.NAMESPACE_D_2007_08, FormatXml.D_URI);
  }

  private String readTag(final XMLStreamReader reader, final String namespaceURI, final String localName) throws XMLStreamException {
    reader.require(XMLStreamConstants.START_ELEMENT, namespaceURI, localName);

    reader.next();
    reader.require(XMLStreamConstants.CHARACTERS, null, null);
    final String result = reader.getText();

    reader.nextTag();
    reader.require(XMLStreamConstants.END_ELEMENT, namespaceURI, localName);

    return result;
  }

  /**
   * Reads multiple links with format 
   * <pre>
   * {@code
   * <links>
   *  <uri>http://somelink</uri>
   *  <uri>http://anotherLink</uri>
   *  <uri>http://somelink/yetAnotherLink</uri>
   * </links>
   * }
   * </pre>
   * @param reader
   * @param entitySet
   * @return list of string based links
   * @throws EntityProviderException
   */
  public List<String> readLinks(final XMLStreamReader reader, final EdmEntitySet entitySet) throws EntityProviderException {
    try {
      List<String> links = new ArrayList<String>();

      reader.next();
      reader.require(XMLStreamConstants.START_ELEMENT, Edm.NAMESPACE_D_2007_08, FormatXml.D_LINKS);

      reader.nextTag();
      while (!reader.isEndElement()) {
        if (reader.getLocalName().equals(FormatXml.M_COUNT)) {
          readTag(reader, Edm.NAMESPACE_M_2007_08, FormatXml.M_COUNT);
        } else {
          final String link = readLink(reader);
          links.add(link);
        }
        reader.nextTag();
      }

      reader.require(XMLStreamConstants.END_ELEMENT, Edm.NAMESPACE_D_2007_08, FormatXml.D_LINKS);

      return links;
    } catch (final XMLStreamException e) {
      throw new EntityProviderException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass().getSimpleName()), e);
    }
  }
}
