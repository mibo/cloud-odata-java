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
package com.sap.core.odata.fit.ref;

import static org.junit.Assert.assertTrue;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.junit.Test;

import com.sap.core.odata.api.commons.HttpContentType;
import com.sap.core.odata.api.commons.HttpStatusCodes;

/**
 * @author SAP AG
 */
public class ContentNegotiationTest extends AbstractRefTest {

  @Test
  public void formatOverwriteAcceptHeader() throws Exception {
    final HttpResponse response = callUri("?$format=xml", HttpHeaders.ACCEPT, IMAGE_GIF, HttpStatusCodes.OK);
    checkMediaType(response, HttpContentType.APPLICATION_XML_UTF8);
  }

  @Test
  public void formatXml() throws Exception {
    final HttpResponse response = callUri("?$format=xml");
    checkMediaType(response, HttpContentType.APPLICATION_XML_UTF8);
  }

  @Test
  public void formatJson() throws Exception {
    final HttpResponse response = callUri("?$format=json");
    checkMediaType(response, HttpContentType.APPLICATION_JSON_UTF8);
  }

  @Test
  public void formatAtom() throws Exception {
    final HttpResponse response = callUri("Rooms('1')?$format=atom");
    checkMediaType(response, HttpContentType.APPLICATION_ATOM_XML_UTF8 + ";type=entry");
  }

  @Test
  public void formatNotSupported() throws Exception {
    callUri("?$format=XXXML", HttpStatusCodes.NOT_ACCEPTABLE);
  }

  @Test
  public void contentTypeMetadata() throws Exception {
    final HttpResponse response = callUri("$metadata");
    checkMediaType(response, HttpContentType.APPLICATION_XML_UTF8);
  }

  @Test
  public void contentTypeMetadataNotAccepted() throws Exception {
    callUri("$metadata", HttpHeaders.ACCEPT, IMAGE_GIF, HttpStatusCodes.NOT_ACCEPTABLE);
  }

  @Test
  public void browserAcceptHeader() throws Exception {
    final HttpResponse response = callUri("$metadata",
        HttpHeaders.ACCEPT, "text/html, application/xhtml+xml, application/xml;q=0.9, */*;q=0.8",
        HttpStatusCodes.OK);
    checkMediaType(response, HttpContentType.APPLICATION_XML_UTF8);
  }

  @Test
  public void browserIssue95() throws Exception {
    final HttpResponse response = callUri("Employees",
        HttpHeaders.ACCEPT, "application/atomsvc+xml;q=0.9, application/json;q=0.8, */*;q=0.1",
        HttpStatusCodes.OK);
    checkMediaType(response, HttpContentType.APPLICATION_JSON);
  }

  @Test
  public void contentTypeServiceDocumentWoAcceptHeader() throws Exception {
    final HttpResponse response = callUri("");
    checkMediaType(response, HttpContentType.APPLICATION_ATOM_SVC_UTF8);
    assertTrue(getBody(response).length() > 100);
  }

  @Test
  public void contentTypeServiceDocumentAtomXmlNotAccept() throws Exception {
    final HttpResponse response = callUri("", HttpHeaders.ACCEPT, HttpContentType.APPLICATION_ATOM_XML, HttpStatusCodes.NOT_ACCEPTABLE);
    checkMediaType(response, HttpContentType.APPLICATION_XML);
    String body = getBody(response);
    assertTrue(body.length() > 100);
    assertTrue(body.contains("error"));
  }

  @Test
  public void contentTypeServiceDocumentXml() throws Exception {
    final HttpResponse response = callUri("", HttpHeaders.ACCEPT, HttpContentType.APPLICATION_XML, HttpStatusCodes.OK);
    checkMediaType(response, HttpContentType.APPLICATION_XML_UTF8);
    assertTrue(getBody(response).length() > 100);
  }

  @Test
  public void contentTypeServiceDocumentAtomSvcXml() throws Exception {
    final HttpResponse response = callUri("", HttpHeaders.ACCEPT, HttpContentType.APPLICATION_ATOM_SVC, HttpStatusCodes.OK);
    checkMediaType(response, HttpContentType.APPLICATION_ATOM_SVC_UTF8);
    assertTrue(getBody(response).length() > 100);
  }

  @Test
  public void contentTypeServiceDocumentAcceptHeaders() throws Exception {
    final HttpResponse response = callUri("",
        HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
        HttpStatusCodes.OK);
    checkMediaType(response, HttpContentType.APPLICATION_XML_UTF8);
    assertTrue(getBody(response).length() > 100);
  }

  @Test
  public void requestContentTypeDifferent() throws Exception {
    String body = getBody(callUri("Rooms('1')"));
    final HttpResponse response = postUri("Rooms", body,
        HttpContentType.APPLICATION_ATOM_XML, HttpStatusCodes.CREATED);
    checkMediaType(response, HttpContentType.APPLICATION_ATOM_XML_UTF8 + ";type=entry");
    assertTrue(getBody(response).length() > 100);
  }

  @Test
  public void postWithUnsupportedContentTypeFeed() throws Exception {
    String body = getBody(callUri("Rooms('1')"));
    final HttpResponse response = postUri("Rooms", body,
        HttpContentType.APPLICATION_ATOM_XML_FEED_UTF8, HttpStatusCodes.UNSUPPORTED_MEDIA_TYPE);
    checkMediaType(response, HttpContentType.APPLICATION_XML);
    assertTrue(getBody(response).length() > 100);
  }
}
