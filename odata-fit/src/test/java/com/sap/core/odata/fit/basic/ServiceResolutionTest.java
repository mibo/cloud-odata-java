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
package com.sap.core.odata.fit.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.core.odata.api.commons.HttpStatusCodes;
import com.sap.core.odata.api.edm.provider.EdmProvider;
import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.api.processor.ODataContext;
import com.sap.core.odata.api.processor.ODataResponse;
import com.sap.core.odata.api.processor.ODataSingleProcessor;
import com.sap.core.odata.api.processor.part.MetadataProcessor;
import com.sap.core.odata.api.processor.part.ServiceDocumentProcessor;
import com.sap.core.odata.api.uri.info.GetMetadataUriInfo;
import com.sap.core.odata.api.uri.info.GetServiceDocumentUriInfo;
import com.sap.core.odata.core.processor.ODataSingleProcessorService;
import com.sap.core.odata.testutil.fit.BaseTest;
import com.sap.core.odata.testutil.helper.StringHelper;
import com.sap.core.odata.testutil.server.TestServer;

/**
 * @author SAP AG
 */
public class ServiceResolutionTest extends BaseTest {

  private final HttpClient httpClient = new DefaultHttpClient();
  private final TestServer server = new TestServer();
  private ODataContext context;
  private ODataSingleProcessorService service;

  @Before
  public void before() {
    try {
      final ODataSingleProcessor processor = mock(ODataSingleProcessor.class);
      final EdmProvider provider = mock(EdmProvider.class);

      service = new ODataSingleProcessorService(provider, processor) {};
      //      FitStaticServiceFactory.setService(service);

      // science fiction (return context after setContext)
      // see http://www.planetgeek.ch/2010/07/20/mockito-answer-vs-return/

      doAnswer(new Answer<Object>() {
        @Override
        public Object answer(final InvocationOnMock invocation) throws Throwable {
          context = (ODataContext) invocation.getArguments()[0];
          return null;
        }
      }).when(processor).setContext(any(ODataContext.class));

      when(processor.getContext()).thenAnswer(new Answer<ODataContext>() {
        @Override
        public ODataContext answer(final InvocationOnMock invocation) throws Throwable {
          return context;
        }
      });

      when(((MetadataProcessor) processor).readMetadata(any(GetMetadataUriInfo.class), any(String.class))).thenReturn(ODataResponse.entity("metadata").status(HttpStatusCodes.OK).build());
      when(((ServiceDocumentProcessor) processor).readServiceDocument(any(GetServiceDocumentUriInfo.class), any(String.class))).thenReturn(ODataResponse.entity("servicedocument").status(HttpStatusCodes.OK).build());
    } catch (final ODataException e) {
      throw new RuntimeException(e);
    }
  }

  private void startServer() {
    server.startServer(service);
  }

  @After
  public void after() {
    if (server != null) {
      server.stopServer();
    }
  }

  @Test
  public void testSplit0() throws ClientProtocolException, IOException, ODataException {
    server.setPathSplit(0);
    startServer();

    final HttpGet get = new HttpGet(URI.create(server.getEndpoint().toString() + "/$metadata"));
    final HttpResponse response = httpClient.execute(get);

    assertEquals(HttpStatusCodes.OK.getStatusCode(), response.getStatusLine().getStatusCode());

    final ODataContext ctx = service.getProcessor().getContext();
    assertNotNull(ctx);

    assertTrue(ctx.getPathInfo().getPrecedingSegments().isEmpty());
    assertEquals("$metadata", ctx.getPathInfo().getODataSegments().get(0).getPath());
  }

  @Test
  public void testSplit1() throws ClientProtocolException, IOException, ODataException {
    server.setPathSplit(1);
    startServer();

    final HttpGet get = new HttpGet(URI.create(server.getEndpoint().toString() + "/aaa/$metadata"));
    final HttpResponse response = httpClient.execute(get);

    assertEquals(HttpStatusCodes.OK.getStatusCode(), response.getStatusLine().getStatusCode());

    final ODataContext ctx = service.getProcessor().getContext();
    assertNotNull(ctx);

    assertEquals("aaa", ctx.getPathInfo().getPrecedingSegments().get(0).getPath());
    assertEquals("$metadata", ctx.getPathInfo().getODataSegments().get(0).getPath());
  }

  @Test
  public void testSplit2() throws ClientProtocolException, IOException, ODataException {
    server.setPathSplit(2);
    startServer();

    final HttpGet get = new HttpGet(URI.create(server.getEndpoint().toString() + "/aaa/bbb/$metadata"));
    final HttpResponse response = httpClient.execute(get);

    assertEquals(HttpStatusCodes.OK.getStatusCode(), response.getStatusLine().getStatusCode());

    final ODataContext ctx = service.getProcessor().getContext();
    assertNotNull(ctx);

    assertEquals("aaa", ctx.getPathInfo().getPrecedingSegments().get(0).getPath());
    assertEquals("bbb", ctx.getPathInfo().getPrecedingSegments().get(1).getPath());
    assertEquals("$metadata", ctx.getPathInfo().getODataSegments().get(0).getPath());
  }

  @Test
  public void testSplitUrlToShort() throws ClientProtocolException, IOException, ODataException {
    server.setPathSplit(3);
    startServer();

    final HttpGet get = new HttpGet(URI.create(server.getEndpoint().toString() + "/aaa/$metadata"));
    final HttpResponse response = httpClient.execute(get);

    assertEquals(HttpStatusCodes.BAD_REQUEST.getStatusCode(), response.getStatusLine().getStatusCode());
  }

  @Test
  public void testSplitUrlServiceDocument() throws ClientProtocolException, IOException, ODataException {
    server.setPathSplit(1);
    startServer();

    final HttpGet get = new HttpGet(URI.create(server.getEndpoint().toString() + "/aaa/"));
    final HttpResponse response = httpClient.execute(get);

    assertEquals(HttpStatusCodes.OK.getStatusCode(), response.getStatusLine().getStatusCode());

    final ODataContext ctx = service.getProcessor().getContext();
    assertNotNull(ctx);

    assertEquals("", ctx.getPathInfo().getODataSegments().get(0).getPath());
    assertEquals("aaa", ctx.getPathInfo().getPrecedingSegments().get(0).getPath());
  }

  @Test
  public void testMatrixParameterInNonODataPath() throws ClientProtocolException, IOException, ODataException {
    server.setPathSplit(1);
    startServer();

    final HttpGet get = new HttpGet(URI.create(server.getEndpoint().toString() + "aaa;n=2/"));
    final HttpResponse response = httpClient.execute(get);

    assertEquals(HttpStatusCodes.OK.getStatusCode(), response.getStatusLine().getStatusCode());

    final ODataContext ctx = service.getProcessor().getContext();
    assertNotNull(ctx);

    assertEquals("", ctx.getPathInfo().getODataSegments().get(0).getPath());
    assertEquals("aaa", ctx.getPathInfo().getPrecedingSegments().get(0).getPath());

    assertNotNull(ctx.getPathInfo().getPrecedingSegments().get(0).getMatrixParameters());

    String key, value;
    key = ctx.getPathInfo().getPrecedingSegments().get(0).getMatrixParameters().keySet().iterator().next();
    assertEquals("n", key);
    value = ctx.getPathInfo().getPrecedingSegments().get(0).getMatrixParameters().get(key).get(0);
    assertEquals("2", value);
  }

  @Test
  public void testNoMatrixParameterInODataPath() throws ClientProtocolException, IOException, ODataException {
    server.setPathSplit(0);
    startServer();

    final HttpGet get = new HttpGet(URI.create(server.getEndpoint().toString() + "$metadata;matrix"));
    final HttpResponse response = httpClient.execute(get);

    final InputStream stream = response.getEntity().getContent();
    final String body = StringHelper.inputStreamToString(stream);

    assertTrue(body.contains("metadata"));
    assertTrue(body.contains("matrix"));
    assertEquals(HttpStatusCodes.NOT_FOUND.getStatusCode(), response.getStatusLine().getStatusCode());
  }

  @Test
  public void testBaseUriWithMatrixParameter() throws ClientProtocolException, IOException, ODataException, URISyntaxException {
    server.setPathSplit(3);
    startServer();

    final String endpoint = server.getEndpoint().toString();
    final HttpGet get = new HttpGet(URI.create(endpoint + "aaa/bbb;n=2,3;m=1/ccc/"));
    final HttpResponse response = httpClient.execute(get);

    assertEquals(HttpStatusCodes.OK.getStatusCode(), response.getStatusLine().getStatusCode());

    final ODataContext ctx = service.getProcessor().getContext();
    assertNotNull(ctx);
    assertEquals(endpoint + "aaa/bbb;n=2,3;m=1/ccc/", ctx.getPathInfo().getServiceRoot().toASCIIString());
  }

  @Test
  public void testBaseUriWithEncoding() throws ClientProtocolException, IOException, ODataException, URISyntaxException {
    server.setPathSplit(3);
    startServer();

    final URI uri = new URI(server.getEndpoint().getScheme(), null, server.getEndpoint().getHost(), server.getEndpoint().getPort(), server.getEndpoint().getPath() + "/aaa/äдержb;n=2, 3;m=1/c c/", null, null);

    final HttpGet get = new HttpGet(uri);
    final HttpResponse response = httpClient.execute(get);

    assertEquals(HttpStatusCodes.OK.getStatusCode(), response.getStatusLine().getStatusCode());

    final ODataContext context = service.getProcessor().getContext();
    assertNotNull(context);
    assertEquals(server.getEndpoint() + "aaa/%C3%A4%D0%B4%D0%B5%D1%80%D0%B6b;n=2,%203;m=1/c%20c/", context.getPathInfo().getServiceRoot().toASCIIString());
  }

}
