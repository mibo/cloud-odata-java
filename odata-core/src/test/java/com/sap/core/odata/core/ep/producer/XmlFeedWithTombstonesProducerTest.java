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

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathNotExists;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.sap.core.odata.api.ODataCallback;
import com.sap.core.odata.api.edm.EdmEntitySet;
import com.sap.core.odata.api.ep.EntityProvider;
import com.sap.core.odata.api.ep.EntityProviderException;
import com.sap.core.odata.api.ep.EntityProviderWriteProperties;
import com.sap.core.odata.api.ep.callback.TombstoneCallback;
import com.sap.core.odata.api.processor.ODataResponse;
import com.sap.core.odata.core.ep.AbstractProviderTest;
import com.sap.core.odata.testutil.helper.StringHelper;
import com.sap.core.odata.testutil.mock.MockFacade;

public class XmlFeedWithTombstonesProducerTest extends AbstractProviderTest {

  public XmlFeedWithTombstonesProducerTest(final StreamWriterImplType type) {
    super(type);
  }

  private ArrayList<Map<String, Object>> deletedRoomsData;
  private Map<String, ODataCallback> callbacks;

  @Test
  public void testOneElementDeletedEntry() throws Exception {
    initializeRoomData(2);
    initializeCallbacks();

    EntityProviderWriteProperties properties = EntityProviderWriteProperties.serviceRoot(BASE_URI).callbacks(callbacks).build();
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");

    String xmlString = execute(properties, entitySet);
    assertXpathExists("/a:feed/at:deleted-entry", xmlString);
    assertXpathEvaluatesTo("http://host:80/service/Rooms('2')", "/a:feed/at:deleted-entry/@ref", xmlString);
  }

  @Test
  public void testTwoElementsDeletedEntry() throws Exception {
    initializeRoomData(4);
    initializeCallbacks();

    EntityProviderWriteProperties properties = EntityProviderWriteProperties.serviceRoot(BASE_URI).callbacks(callbacks).build();
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");

    String xmlString = execute(properties, entitySet);
    assertXpathExists("/a:feed/at:deleted-entry", xmlString);
    assertXpathEvaluatesTo("2", "count(/a:feed/at:deleted-entry)", xmlString);
  }

  @Test
  public void nullAsCallbackResult() throws Exception {
    initializeRoomData(2);
    TombstoneCallback tombstoneCallback = new TombstoneCallbackImpl(null, null);
    callbacks = new HashMap<String, ODataCallback>();
    callbacks.put(TombstoneCallback.CALLBACK_KEY_TOMBSTONE, tombstoneCallback);

    EntityProviderWriteProperties properties = EntityProviderWriteProperties.serviceRoot(BASE_URI).callbacks(callbacks).build();
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");

    String xmlString = execute(properties, entitySet);
    assertXpathNotExists("/a:feed/at:deleted-entry", xmlString);
  }

  @Test
  public void deltaLinkPresent() throws Exception {
    initializeRoomData(2);
    initializeDeletedRoomData();
    TombstoneCallback tombstoneCallback = new TombstoneCallbackImpl(deletedRoomsData, BASE_URI.toASCIIString() + "Rooms?!deltatoken=1234");
    callbacks = new HashMap<String, ODataCallback>();
    callbacks.put(TombstoneCallback.CALLBACK_KEY_TOMBSTONE, tombstoneCallback);

    EntityProviderWriteProperties properties = EntityProviderWriteProperties.serviceRoot(BASE_URI).callbacks(callbacks).build();
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");

    String xmlString = execute(properties, entitySet);
    assertXpathExists("/a:feed/at:deleted-entry", xmlString);
    assertXpathExists("/a:feed/a:link[@rel=\"delta\" and @href=\"" + BASE_URI.toASCIIString() + "Rooms?!deltatoken=1234" + "\"]", xmlString);
  }

  @Test
  public void deltaLinkAndDataNull() throws Exception {
    initializeRoomData(2);
    initializeDeletedRoomData();
    TombstoneCallback tombstoneCallback = new TombstoneCallbackImpl(null, null);
    callbacks = new HashMap<String, ODataCallback>();
    callbacks.put(TombstoneCallback.CALLBACK_KEY_TOMBSTONE, tombstoneCallback);

    EntityProviderWriteProperties properties = EntityProviderWriteProperties.serviceRoot(BASE_URI).callbacks(callbacks).build();
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");

    String xmlString = execute(properties, entitySet);
    assertXpathNotExists("/a:feed/at:deleted-entry", xmlString);
    assertXpathNotExists("/a:feed/a:link[@rel=\"http://odata.org/delta\" and @href]", xmlString);
  }

  private String execute(final EntityProviderWriteProperties properties, final EdmEntitySet entitySet) throws EntityProviderException, IOException {
    ODataResponse response = EntityProvider.writeFeed("application/atom+xml", entitySet, roomsData, properties);
    assertNotNull(response);
    String xmlString = StringHelper.inputStreamToString((InputStream) response.getEntity());
    return xmlString;
  }

  @Test
  public void testNoElementDeletedEntry() throws Exception {
    initializeRoomData(1);
    initializeCallbacks();

    EntityProviderWriteProperties properties = EntityProviderWriteProperties.serviceRoot(BASE_URI).callbacks(callbacks).build();
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");

    String xmlString = execute(properties, entitySet);
    assertXpathNotExists("/a:feed/at:deleted-entry", xmlString);
  }

  private void initializeDeletedRoomData() {
    deletedRoomsData = new ArrayList<Map<String, Object>>();
    for (int i = 2; i <= roomsData.size(); i = i + 2) {
      HashMap<String, Object> tmp = new HashMap<String, Object>();
      tmp.put("Id", "" + i);
      tmp.put("Name", "Neu Schwanstein" + i);
      tmp.put("Seats", new Integer(20));
      tmp.put("Version", new Integer(3));
      deletedRoomsData.add(tmp);
    }

  }

  private void initializeCallbacks() {
    initializeDeletedRoomData();
    TombstoneCallback tombstoneCallback = new TombstoneCallbackImpl(deletedRoomsData, null);
    callbacks = new HashMap<String, ODataCallback>();
    callbacks.put(TombstoneCallback.CALLBACK_KEY_TOMBSTONE, tombstoneCallback);
  }
}
