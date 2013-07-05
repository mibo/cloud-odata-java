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
package com.sap.core.odata.core.ep.producer;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;

import com.sap.core.odata.api.ODataServiceVersion;
import com.sap.core.odata.api.edm.EdmSimpleTypeKind;
import com.sap.core.odata.api.edm.provider.AnnotationAttribute;
import com.sap.core.odata.api.edm.provider.AnnotationElement;
import com.sap.core.odata.api.edm.provider.DataServices;
import com.sap.core.odata.api.edm.provider.EntityType;
import com.sap.core.odata.api.edm.provider.Key;
import com.sap.core.odata.api.edm.provider.Property;
import com.sap.core.odata.api.edm.provider.PropertyRef;
import com.sap.core.odata.api.edm.provider.Schema;
import com.sap.core.odata.api.edm.provider.SimpleProperty;
import com.sap.core.odata.core.ep.AbstractXmlProducerTestHelper;
import com.sap.core.odata.core.ep.util.CircleStreamBuffer;
import com.sap.core.odata.testutil.helper.StringHelper;

public class XmlMetadataProducerTest extends AbstractXmlProducerTestHelper {

  private XMLOutputFactory xmlStreamWriterFactory;

  public XmlMetadataProducerTest(final StreamWriterImplType type) {
    super(type);
  }

  @Before
  public void before() {
    xmlStreamWriterFactory = XMLOutputFactory.newInstance();
  }

  @Test
  public void writeValidMetadata() throws Exception {
    List<Schema> schemas = new ArrayList<Schema>();

    List<AnnotationElement> annotationElements = new ArrayList<AnnotationElement>();
    annotationElements.add(new AnnotationElement().setName("test").setText("hallo"));
    Schema schema = new Schema().setAnnotationElements(annotationElements);
    schema.setNamespace("http://namespace.com");
    schemas.add(schema);

    DataServices data = new DataServices().setSchemas(schemas).setDataServiceVersion(ODataServiceVersion.V20);
    OutputStreamWriter writer = null;
    CircleStreamBuffer csb = new CircleStreamBuffer();
    writer = new OutputStreamWriter(csb.getOutputStream(), "UTF-8");
    XMLStreamWriter xmlStreamWriter = xmlStreamWriterFactory.createXMLStreamWriter(writer);
    XmlMetadataProducer.writeMetadata(data, xmlStreamWriter, null);

    Map<String, String> prefixMap = new HashMap<String, String>();
    prefixMap.put("edmx", "http://schemas.microsoft.com/ado/2007/06/edmx");
    prefixMap.put("a", "http://schemas.microsoft.com/ado/2008/09/edm");

    NamespaceContext ctx = new SimpleNamespaceContext(prefixMap);
    XMLUnit.setXpathNamespaceContext(ctx);

    String metadata = StringHelper.inputStreamToString(csb.getInputStream());
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:test", metadata);
  }

  @Test
  public void writeValidMetadata2() throws Exception {
    List<Schema> schemas = new ArrayList<Schema>();

    List<AnnotationElement> childElements = new ArrayList<AnnotationElement>();
    childElements.add(new AnnotationElement().setName("schemaElementTest2").setText("text2").setNamespace("namespace1"));

    List<AnnotationAttribute> elementAttributes = new ArrayList<AnnotationAttribute>();
    elementAttributes.add(new AnnotationAttribute().setName("rel").setText("self"));
    elementAttributes.add(new AnnotationAttribute().setName("href").setText("http://google.com").setPrefix("pre").setNamespace("namespaceForAnno"));

    List<AnnotationElement> element3List = new ArrayList<AnnotationElement>();
    element3List.add(new AnnotationElement().setName("schemaElementTest4").setText("text4").setAttributes(elementAttributes));
    childElements.add(new AnnotationElement().setName("schemaElementTest3").setText("text3").setPrefix("prefix").setNamespace("namespace2").setChildElements(element3List));

    List<AnnotationElement> schemaElements = new ArrayList<AnnotationElement>();
    schemaElements.add(new AnnotationElement().setName("schemaElementTest1").setText("text1").setChildElements(childElements));

    schemaElements.add(new AnnotationElement().setName("test"));
    Schema schema = new Schema().setAnnotationElements(schemaElements);
    schema.setNamespace("http://namespace.com");
    schemas.add(schema);

    DataServices data = new DataServices().setSchemas(schemas).setDataServiceVersion(ODataServiceVersion.V20);
    OutputStreamWriter writer = null;
    CircleStreamBuffer csb = new CircleStreamBuffer();
    writer = new OutputStreamWriter(csb.getOutputStream(), "UTF-8");
    XMLStreamWriter xmlStreamWriter = xmlStreamWriterFactory.createXMLStreamWriter(writer);
    XmlMetadataProducer.writeMetadata(data, xmlStreamWriter, null);

    Map<String, String> prefixMap = new HashMap<String, String>();
    prefixMap.put("edmx", "http://schemas.microsoft.com/ado/2007/06/edmx");
    prefixMap.put("a", "http://schemas.microsoft.com/ado/2008/09/edm");
    prefixMap.put("b", "namespace1");
    prefixMap.put("prefix", "namespace2");
    prefixMap.put("pre", "namespaceForAnno");

    NamespaceContext ctx = new SimpleNamespaceContext(prefixMap);
    XMLUnit.setXpathNamespaceContext(ctx);

    String metadata = StringHelper.inputStreamToString(csb.getInputStream());
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:test", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:schemaElementTest1", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:schemaElementTest1/b:schemaElementTest2", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:schemaElementTest1/prefix:schemaElementTest3", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:schemaElementTest1/prefix:schemaElementTest3/a:schemaElementTest4[@rel=\"self\" and @pre:href=\"http://google.com\"]", metadata);

  }

  @Test
  public void writeValidMetadata3() throws Exception {
    List<Schema> schemas = new ArrayList<Schema>();

    List<AnnotationElement> annotationElements = new ArrayList<AnnotationElement>();
    annotationElements.add(new AnnotationElement().setName("test").setText("hallo)"));
    Schema schema = new Schema().setAnnotationElements(annotationElements);
    schema.setNamespace("http://namespace.com");
    schemas.add(schema);

    List<PropertyRef> keys = new ArrayList<PropertyRef>();
    keys.add(new PropertyRef().setName("Id"));
    Key key = new Key().setKeys(keys);
    List<Property> properties = new ArrayList<Property>();
    properties.add(new SimpleProperty().setName("Id").setType(EdmSimpleTypeKind.String));
    EntityType entityType = new EntityType().setName("testType").setKey(key).setProperties(properties);
    List<EntityType> entityTypes = new ArrayList<EntityType>();
    entityTypes.add(entityType);
    schema.setEntityTypes(entityTypes);

    DataServices data = new DataServices().setSchemas(schemas).setDataServiceVersion(ODataServiceVersion.V20);
    OutputStreamWriter writer = null;
    CircleStreamBuffer csb = new CircleStreamBuffer();
    writer = new OutputStreamWriter(csb.getOutputStream(), "UTF-8");
    XMLStreamWriter xmlStreamWriter = xmlStreamWriterFactory.createXMLStreamWriter(writer);
    XmlMetadataProducer.writeMetadata(data, xmlStreamWriter, null);

    Map<String, String> prefixMap = new HashMap<String, String>();
    prefixMap.put("edmx", "http://schemas.microsoft.com/ado/2007/06/edmx");
    prefixMap.put("a", "http://schemas.microsoft.com/ado/2008/09/edm");

    NamespaceContext ctx = new SimpleNamespaceContext(prefixMap);
    XMLUnit.setXpathNamespaceContext(ctx);

    String metadata = StringHelper.inputStreamToString(csb.getInputStream());
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/a:test", metadata);
  }

  //Elements with namespace and attributes without namespace
  @Test
  public void writeValidMetadata4() throws Exception {

    List<Schema> schemas = new ArrayList<Schema>();

    List<AnnotationAttribute> attributesElement1 = new ArrayList<AnnotationAttribute>();
    attributesElement1.add(new AnnotationAttribute().setName("rel").setText("self"));
    attributesElement1.add(new AnnotationAttribute().setName("href").setText("link"));

    List<AnnotationElement> schemaElements = new ArrayList<AnnotationElement>();
    schemaElements.add(new AnnotationElement().setName("schemaElementTest1").setPrefix("atom").setNamespace("http://www.w3.org/2005/Atom").setAttributes(attributesElement1));
    schemaElements.add(new AnnotationElement().setName("schemaElementTest2").setPrefix("atom").setNamespace("http://www.w3.org/2005/Atom").setAttributes(attributesElement1));

    Schema schema = new Schema().setAnnotationElements(schemaElements);
    schema.setNamespace("http://namespace.com");
    schemas.add(schema);

    DataServices data = new DataServices().setSchemas(schemas).setDataServiceVersion(ODataServiceVersion.V20);
    OutputStreamWriter writer = null;
    CircleStreamBuffer csb = new CircleStreamBuffer();
    writer = new OutputStreamWriter(csb.getOutputStream(), "UTF-8");
    XMLStreamWriter xmlStreamWriter = xmlStreamWriterFactory.createXMLStreamWriter(writer);
    XmlMetadataProducer.writeMetadata(data, xmlStreamWriter, null);
    String metadata = StringHelper.inputStreamToString(csb.getInputStream());

    Map<String, String> prefixMap = new HashMap<String, String>();
    prefixMap.put("edmx", "http://schemas.microsoft.com/ado/2007/06/edmx");
    prefixMap.put("a", "http://schemas.microsoft.com/ado/2008/09/edm");
    prefixMap.put("atom", "http://www.w3.org/2005/Atom");

    NamespaceContext ctx = new SimpleNamespaceContext(prefixMap);
    XMLUnit.setXpathNamespaceContext(ctx);

    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/atom:schemaElementTest1", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/atom:schemaElementTest2", metadata);
  }

  //Element with namespace and attributes with same namespace
  @Test
  public void writeValidMetadata5() throws Exception {

    List<Schema> schemas = new ArrayList<Schema>();

    List<AnnotationAttribute> attributesElement1 = new ArrayList<AnnotationAttribute>();
    attributesElement1.add(new AnnotationAttribute().setName("rel").setText("self").setPrefix("atom").setNamespace("http://www.w3.org/2005/Atom"));
    attributesElement1.add(new AnnotationAttribute().setName("href").setText("link").setPrefix("atom").setNamespace("http://www.w3.org/2005/Atom"));

    List<AnnotationElement> schemaElements = new ArrayList<AnnotationElement>();
    schemaElements.add(new AnnotationElement().setName("schemaElementTest1").setPrefix("atom").setNamespace("http://www.w3.org/2005/Atom").setAttributes(attributesElement1));
    schemaElements.add(new AnnotationElement().setName("schemaElementTest2").setPrefix("atom").setNamespace("http://www.w3.org/2005/Atom").setAttributes(attributesElement1));

    Schema schema = new Schema().setAnnotationElements(schemaElements);
    schema.setNamespace("http://namespace.com");
    schemas.add(schema);

    DataServices data = new DataServices().setSchemas(schemas).setDataServiceVersion(ODataServiceVersion.V20);
    OutputStreamWriter writer = null;
    CircleStreamBuffer csb = new CircleStreamBuffer();
    writer = new OutputStreamWriter(csb.getOutputStream(), "UTF-8");
    XMLStreamWriter xmlStreamWriter = xmlStreamWriterFactory.createXMLStreamWriter(writer);
    XmlMetadataProducer.writeMetadata(data, xmlStreamWriter, null);
    String metadata = StringHelper.inputStreamToString(csb.getInputStream());

    Map<String, String> prefixMap = new HashMap<String, String>();
    prefixMap.put("edmx", "http://schemas.microsoft.com/ado/2007/06/edmx");
    prefixMap.put("a", "http://schemas.microsoft.com/ado/2008/09/edm");
    prefixMap.put("atom", "http://www.w3.org/2005/Atom");

    NamespaceContext ctx = new SimpleNamespaceContext(prefixMap);
    XMLUnit.setXpathNamespaceContext(ctx);

    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/atom:schemaElementTest1", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/atom:schemaElementTest2", metadata);
  }

  //Element with namespace childelements with same namespace
  @Test
  public void writeValidMetadata6() throws Exception {

    List<Schema> schemas = new ArrayList<Schema>();

    List<AnnotationAttribute> attributesElement1 = new ArrayList<AnnotationAttribute>();
    attributesElement1.add(new AnnotationAttribute().setName("rel").setText("self").setPrefix("atom").setNamespace("http://www.w3.org/2005/Atom"));
    attributesElement1.add(new AnnotationAttribute().setName("href").setText("link").setPrefix("atom").setNamespace("http://www.w3.org/2005/Atom"));

    List<AnnotationElement> elementElements = new ArrayList<AnnotationElement>();
    elementElements.add(new AnnotationElement().setName("schemaElementTest2").setPrefix("atom").setNamespace("http://www.w3.org/2005/Atom").setAttributes(attributesElement1));
    elementElements.add(new AnnotationElement().setName("schemaElementTest3").setPrefix("atom").setNamespace("http://www.w3.org/2005/Atom").setAttributes(attributesElement1));

    List<AnnotationElement> schemaElements = new ArrayList<AnnotationElement>();
    schemaElements.add(new AnnotationElement().setName("schemaElementTest1").setPrefix("atom").setNamespace("http://www.w3.org/2005/Atom").setAttributes(attributesElement1).setChildElements(elementElements));

    Schema schema = new Schema().setAnnotationElements(schemaElements);
    schema.setNamespace("http://namespace.com");
    schemas.add(schema);

    DataServices data = new DataServices().setSchemas(schemas).setDataServiceVersion(ODataServiceVersion.V20);
    OutputStreamWriter writer = null;
    CircleStreamBuffer csb = new CircleStreamBuffer();
    writer = new OutputStreamWriter(csb.getOutputStream(), "UTF-8");
    XMLStreamWriter xmlStreamWriter = xmlStreamWriterFactory.createXMLStreamWriter(writer);
    XmlMetadataProducer.writeMetadata(data, xmlStreamWriter, null);
    String metadata = StringHelper.inputStreamToString(csb.getInputStream());

    Map<String, String> prefixMap = new HashMap<String, String>();
    prefixMap.put("edmx", "http://schemas.microsoft.com/ado/2007/06/edmx");
    prefixMap.put("a", "http://schemas.microsoft.com/ado/2008/09/edm");
    prefixMap.put("atom", "http://www.w3.org/2005/Atom");

    NamespaceContext ctx = new SimpleNamespaceContext(prefixMap);
    XMLUnit.setXpathNamespaceContext(ctx);

    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/atom:schemaElementTest1", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/atom:schemaElementTest1/atom:schemaElementTest2", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/atom:schemaElementTest1/atom:schemaElementTest3", metadata);
  }

  //If no name for an AnnotationAttribute is set this has to result in an Exception
  @Test(expected = Exception.class)
  public void writeInvalidMetadata() throws Exception {
    disableLogging(this.getClass());
    List<Schema> schemas = new ArrayList<Schema>();

    List<AnnotationElement> annotationElements = new ArrayList<AnnotationElement>();
    annotationElements.add(new AnnotationElement().setText("hallo"));
    Schema schema = new Schema().setAnnotationElements(annotationElements);
    schema.setNamespace("http://namespace.com");
    schemas.add(schema);

    DataServices data = new DataServices().setSchemas(schemas).setDataServiceVersion(ODataServiceVersion.V20);
    OutputStreamWriter writer = null;
    CircleStreamBuffer csb = new CircleStreamBuffer();
    writer = new OutputStreamWriter(csb.getOutputStream(), "UTF-8");
    XMLStreamWriter xmlStreamWriter = xmlStreamWriterFactory.createXMLStreamWriter(writer);
    XmlMetadataProducer.writeMetadata(data, xmlStreamWriter, null);
  }

  //Element with predefined namespace
  @Test
  public void writeWithPredefinedNamespaces() throws Exception {
    //prepare
    List<Schema> schemas = new ArrayList<Schema>();

    List<AnnotationAttribute> attributesElement1 = new ArrayList<AnnotationAttribute>();
    attributesElement1.add(new AnnotationAttribute().setName("rel").setText("self").setPrefix("sap").setNamespace("http://www.sap.com/Protocols/SAPData"));
    attributesElement1.add(new AnnotationAttribute().setName("href").setText("link").setPrefix("sap").setNamespace("http://www.sap.com/Protocols/SAPData"));

    List<AnnotationElement> elementElements = new ArrayList<AnnotationElement>();
    elementElements.add(new AnnotationElement().setName("schemaElementTest2").setPrefix("sap").setNamespace("http://www.sap.com/Protocols/SAPData").setAttributes(attributesElement1));
    elementElements.add(new AnnotationElement().setName("schemaElementTest3").setPrefix("sap").setNamespace("http://www.sap.com/Protocols/SAPData").setAttributes(attributesElement1));

    List<AnnotationElement> schemaElements = new ArrayList<AnnotationElement>();
    schemaElements.add(new AnnotationElement().setName("schemaElementTest1").setPrefix("sap").setNamespace("http://www.sap.com/Protocols/SAPData").setAttributes(attributesElement1).setChildElements(elementElements));

    Schema schema = new Schema().setAnnotationElements(schemaElements);
    schema.setNamespace("http://namespace.com");
    schemas.add(schema);

    //Execute
    Map<String, String> predefinedNamespaces = new HashMap<String, String>();
    predefinedNamespaces.put("sap", "http://www.sap.com/Protocols/SAPData");
    DataServices data = new DataServices().setSchemas(schemas).setDataServiceVersion(ODataServiceVersion.V20);
    OutputStreamWriter writer = null;
    CircleStreamBuffer csb = new CircleStreamBuffer();
    writer = new OutputStreamWriter(csb.getOutputStream(), "UTF-8");
    XMLStreamWriter xmlStreamWriter = xmlStreamWriterFactory.createXMLStreamWriter(writer);
    XmlMetadataProducer.writeMetadata(data, xmlStreamWriter, predefinedNamespaces);
    String metadata = StringHelper.inputStreamToString(csb.getInputStream());

    //Verify
    Map<String, String> prefixMap = new HashMap<String, String>();
    prefixMap.put("edmx", "http://schemas.microsoft.com/ado/2007/06/edmx");
    prefixMap.put("a", "http://schemas.microsoft.com/ado/2008/09/edm");
    prefixMap.put("sap", "http://www.sap.com/Protocols/SAPData");

    NamespaceContext ctx = new SimpleNamespaceContext(prefixMap);
    XMLUnit.setXpathNamespaceContext(ctx);

    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/sap:schemaElementTest1", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/sap:schemaElementTest1/sap:schemaElementTest2", metadata);
    assertXpathExists("/edmx:Edmx/edmx:DataServices/a:Schema/sap:schemaElementTest1/sap:schemaElementTest3", metadata);
  }
}
