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
package com.sap.core.odata.core.batch;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import com.sap.core.odata.api.batch.BatchException;
import com.sap.core.odata.api.batch.BatchPart;
import com.sap.core.odata.api.commons.ODataHttpMethod;
import com.sap.core.odata.api.ep.EntityProviderBatchProperties;
import com.sap.core.odata.api.exception.ODataMessageException;
import com.sap.core.odata.api.processor.ODataRequest;
import com.sap.core.odata.api.uri.PathInfo;
import com.sap.core.odata.api.uri.PathSegment;
import com.sap.core.odata.core.ODataPathSegmentImpl;
import com.sap.core.odata.core.ODataRequestImpl;
import com.sap.core.odata.core.PathInfoImpl;
import com.sap.core.odata.core.commons.ContentType;

/**
 * @author SAP AG
 */
public class BatchRequestParser {
  private static final String LF = "\n";
  private static final String REG_EX_OPTIONAL_WHITESPACE = "\\s?";
  private static final String REG_EX_ZERO_OR_MORE_WHITESPACES = "\\s*";
  private static final String ANY_CHARACTERS = ".*";

  private static final Pattern REG_EX_BLANK_LINE = Pattern.compile("(|" + REG_EX_ZERO_OR_MORE_WHITESPACES + ")");
  private static final Pattern REG_EX_HEADER = Pattern.compile("([a-zA-Z\\-]+):" + REG_EX_OPTIONAL_WHITESPACE + "(.*)" + REG_EX_ZERO_OR_MORE_WHITESPACES);
  private static final Pattern REG_EX_VERSION = Pattern.compile("(?:HTTP/[0-9]\\.[0-9])");
  private static final Pattern REG_EX_ANY_BOUNDARY_STRING = Pattern.compile("--" + ANY_CHARACTERS + REG_EX_ZERO_OR_MORE_WHITESPACES);
  private static final Pattern REG_EX_REQUEST_LINE = Pattern.compile("(GET|POST|PUT|DELETE|MERGE|PATCH)\\s(.*)\\s?" + REG_EX_VERSION + REG_EX_ZERO_OR_MORE_WHITESPACES);
  private static final Pattern REG_EX_BOUNDARY_PARAMETER = Pattern.compile(REG_EX_OPTIONAL_WHITESPACE + "boundary=(\".*\"|.*)" + REG_EX_ZERO_OR_MORE_WHITESPACES);
  private static final Pattern REG_EX_CONTENT_TYPE = Pattern.compile(REG_EX_OPTIONAL_WHITESPACE + BatchConstants.MULTIPART_MIXED);
  private static final Pattern REG_EX_QUERY_PARAMETER = Pattern.compile("((?:\\$[a-z]+)|(?:[^\\$][^=]))=([^=]+)");

  private static final String REG_EX_BOUNDARY = "([a-zA-Z0-9_\\-\\.'\\+]{1,70})|\"([a-zA-Z0-9_\\-\\.'\\+\\s\\(\\),/:=\\?]{1,69}[a-zA-Z0-9_\\-\\.'\\+\\(\\),/:=\\?])\""; // See RFC 2046
  private String baseUri;
  private PathInfo batchRequestPathInfo;
  private String contentTypeMime;
  private String boundary;
  private String currentContentId;
  private static Set<String> HTTP_CHANGESET_METHODS;
  private static Set<String> HTTP_BATCH_METHODS;

  static {
    HTTP_CHANGESET_METHODS = new HashSet<String>();
    HTTP_CHANGESET_METHODS.add("POST");
    HTTP_CHANGESET_METHODS.add("PUT");
    HTTP_CHANGESET_METHODS.add("DELETE");
    HTTP_CHANGESET_METHODS.add("MERGE");
    HTTP_CHANGESET_METHODS.add("PATCH");

    HTTP_BATCH_METHODS = new HashSet<String>();
    HTTP_BATCH_METHODS.add("GET");
  }

  public BatchRequestParser(final String contentType, final EntityProviderBatchProperties properties) {
    contentTypeMime = contentType;
    batchRequestPathInfo = properties.getPathInfo();
  }

  public List<BatchPart> parse(final InputStream in) throws BatchException {
    Scanner scanner = new Scanner(in).useDelimiter(LF);
    baseUri = getBaseUri();
    List<BatchPart> requestList;
    try {
      requestList = parseBatchRequest(scanner);
    } finally {// NOPMD (suppress DoNotThrowExceptionInFinally)
      scanner.close();
      try {
        in.close();
      } catch (IOException e) {
        throw new BatchException(ODataMessageException.COMMON, e);
      }
    }
    return requestList;
  }

  private List<BatchPart> parseBatchRequest(final Scanner scanner) throws BatchException {
    List<BatchPart> requests = new LinkedList<BatchPart>();
    if (contentTypeMime != null) {
      boundary = getBoundary(contentTypeMime);
      parsePreamble(scanner);
      String closeDelimiter = "--" + boundary + "--" + REG_EX_ZERO_OR_MORE_WHITESPACES;
      while (scanner.hasNext() && !scanner.hasNext(closeDelimiter)) {
        requests.add(parseMultipart(scanner, boundary, false));
        parseNewLine(scanner);
      }
      if (scanner.hasNext(closeDelimiter)) {
        scanner.next(closeDelimiter);
      } else {
        throw new BatchException(BatchException.MISSING_CLOSE_DELIMITER);
      }
    } else {
      throw new BatchException(BatchException.MISSING_CONTENT_TYPE);
    }
    return requests;

  }

  //The method parses additional information prior to the first boundary delimiter line
  private void parsePreamble(final Scanner scanner) {
    while (scanner.hasNext() && !scanner.hasNext(REG_EX_ANY_BOUNDARY_STRING)) {
      scanner.next();
    }
  }

  private BatchPart parseMultipart(final Scanner scanner, final String boundary, final boolean isChangeSet) throws BatchException {
    Map<String, String> mimeHeaders = new HashMap<String, String>();
    BatchPart multipart = null;
    List<ODataRequest> requests = new ArrayList<ODataRequest>();
    if (scanner.hasNext("--" + boundary + REG_EX_ZERO_OR_MORE_WHITESPACES)) {
      scanner.next();
      mimeHeaders = parseHeaders(scanner);

      String contentType = mimeHeaders.get(BatchConstants.HTTP_CONTENT_TYPE.toLowerCase());
      if (contentType == null) {
        throw new BatchException(BatchException.MISSING_CONTENT_TYPE);
      }
      if (isChangeSet) {
        if (BatchConstants.HTTP_APPLICATION_HTTP.equalsIgnoreCase(contentType)) {
          validateEncoding(mimeHeaders.get(BatchConstants.HTTP_CONTENT_TRANSFER_ENCODING.toLowerCase()));
          currentContentId = mimeHeaders.get(BatchConstants.HTTP_CONTENT_ID.toLowerCase());
          parseNewLine(scanner);// mandatory

          requests.add(parseRequest(scanner, isChangeSet));
          multipart = new BatchPartImpl(false, requests);
        } else {
          throw new BatchException(BatchException.INVALID_CONTENT_TYPE.addContent(BatchConstants.HTTP_APPLICATION_HTTP));
        }
      } else {
        if (BatchConstants.HTTP_APPLICATION_HTTP.equalsIgnoreCase(contentType)) {
          validateEncoding(mimeHeaders.get(BatchConstants.HTTP_CONTENT_TRANSFER_ENCODING.toLowerCase()));
          parseNewLine(scanner);// mandatory
          requests.add(parseRequest(scanner, isChangeSet));
          multipart = new BatchPartImpl(false, requests);
        } else if (contentType.matches(REG_EX_OPTIONAL_WHITESPACE + BatchConstants.MULTIPART_MIXED + ANY_CHARACTERS)) {
          String changeSetBoundary = getBoundary(contentType);
          if (boundary.equals(changeSetBoundary)) {
            throw new BatchException(BatchException.INVALID_CHANGESET_BOUNDARY);
          }
          List<ODataRequest> changeSetRequests = new LinkedList<ODataRequest>();
          parseNewLine(scanner);// mandatory
          Pattern changeSetCloseDelimiter = Pattern.compile("--" + changeSetBoundary + "--" + REG_EX_ZERO_OR_MORE_WHITESPACES);
          while (!scanner.hasNext(changeSetCloseDelimiter)) {
            BatchPart part = parseMultipart(scanner, changeSetBoundary, true);
            if (part.getRequests().size() == 1) {
              changeSetRequests.add(part.getRequests().get(0));
            }
          }
          scanner.next(changeSetCloseDelimiter);
          multipart = new BatchPartImpl(true, changeSetRequests);
        } else {
          throw new BatchException(BatchException.INVALID_CONTENT_TYPE.addContent(BatchConstants.MULTIPART_MIXED + " or " + BatchConstants.HTTP_APPLICATION_HTTP));
        }
      }
    } else if (scanner.hasNext(boundary + REG_EX_ZERO_OR_MORE_WHITESPACES)) {
      throw new BatchException(BatchException.INVALID_BOUNDARY);
    } else if (scanner.hasNext(REG_EX_ANY_BOUNDARY_STRING)) {
      throw new BatchException(BatchException.NO_MATCH_WITH_BOUNDARY_STRING.addContent(boundary));
    } else {
      throw new BatchException(BatchException.MISSING_BOUNDARY_DELIMITER);
    }
    return multipart;

  }

  private ODataRequest parseRequest(final Scanner scanner, final boolean isChangeSet) throws BatchException {
    ODataRequestImpl request = new ODataRequestImpl();
    if (scanner.hasNext(REG_EX_REQUEST_LINE)) {
      scanner.next(REG_EX_REQUEST_LINE);
      String method = null;
      String uri = null;
      MatchResult result = scanner.match();
      if (result.groupCount() == 2) {
        method = result.group(1);
        uri = result.group(2).trim();
      } else {
        throw new BatchException(BatchException.INVALID_REQUEST_LINE.addContent(scanner.next()));
      }
      request.setPathInfo(parseRequestUri(uri));
      request.setQueryParameters(parseQueryParameters(uri));
      if (isChangeSet) {
        if (!HTTP_CHANGESET_METHODS.contains(method)) {
          throw new BatchException(BatchException.INVALID_CHANGESET_METHOD);
        }
      } else if (!HTTP_BATCH_METHODS.contains(method)) {
        throw new BatchException(BatchException.INVALID_QUERY_OPERATION_METHOD);
      }
      request.setMethod(ODataHttpMethod.valueOf(method));
      Map<String, List<String>> headers = parseRequestHeaders(scanner);
      if (currentContentId != null) {
        List<String> headerList = new ArrayList<String>();
        headerList.add(currentContentId);
        headers.put(BatchConstants.HTTP_CONTENT_ID.toLowerCase(), headerList);
      }
      request.setRequestHeaders(headers);
      String requestContentType = request.getRequestHeaderValue(BatchConstants.HTTP_CONTENT_TYPE.toLowerCase());
      if (requestContentType != null) {
        request.setContentType(ContentType.create(requestContentType));
      }
      String requestAcceptHeaders = request.getRequestHeaderValue(BatchConstants.ACCEPT.toLowerCase());
      if (requestAcceptHeaders != null) {
        request.setAcceptHeaders(parseAcceptHeaders(requestAcceptHeaders));
      } else {
        request.setAcceptHeaders(new ArrayList<String>());
      }
      String requestAcceptLanguages = request.getRequestHeaderValue(BatchConstants.ACCEPT_LANGUAGE.toLowerCase());
      if (requestAcceptLanguages != null) {
        request.setAcceptableLanguages(parseAcceptableLanguages(requestAcceptLanguages));
      } else {
        request.setAcceptableLanguages(new ArrayList<Locale>());
      }

      parseNewLine(scanner);

      if (isChangeSet) {
        request.setBody(parseBody(scanner));
      }

    } else {
      throw new BatchException(BatchException.INVALID_REQUEST_LINE.addContent(scanner.next()));
    }
    return request;
  }

  private Map<String, List<String>> parseRequestHeaders(final Scanner scanner) throws BatchException {
    Map<String, List<String>> headers = new HashMap<String, List<String>>();
    while (scanner.hasNext() && !scanner.hasNext(REG_EX_BLANK_LINE)) {
      if (scanner.hasNext(REG_EX_HEADER)) {
        scanner.next(REG_EX_HEADER);
        MatchResult result = scanner.match();
        if (result.groupCount() == 2) {
          String headerName = result.group(1).trim().toLowerCase();
          String headerValue = result.group(2).trim().toLowerCase();
          if (headers.containsKey(headerName)) {
            headers.get(headerName).add(headerValue);
          } else {
            List<String> headerList = new ArrayList<String>();
            headerList.add(headerValue);
            headers.put(headerName, headerList);
          }
        }
      } else {
        throw new BatchException(BatchException.INVALID_HEADER.addContent(scanner.next()));
      }
    }
    return headers;
  }

  private PathInfo parseRequestUri(final String uri) throws BatchException {
    PathInfoImpl pathInfo = new PathInfoImpl();
    pathInfo.setServiceRoot(batchRequestPathInfo.getServiceRoot());
    pathInfo.setPrecedingPathSegment(batchRequestPathInfo.getPrecedingSegments());
    Scanner uriScanner = new Scanner(uri);
    Pattern regexRequestUri = Pattern.compile("(?:" + baseUri + ")?/?([^?]+)(\\?.*)?");
    if (uriScanner.hasNext("\\$[^/]/([^?]+)(?:\\?.*)?")) {
      // TODO: Content-ID reference 
      uriScanner.next("\\$[^/]/([^?]+)(?:\\?.*)?");
    } else if (uriScanner.hasNext(regexRequestUri)) {
      uriScanner.next(regexRequestUri);
      MatchResult result = uriScanner.match();
      if (result.groupCount() == 2) {
        String odataPathSegmentsAsString = result.group(1);
        String queryParametersAsString = result.group(2) != null ? result.group(2) : "";
        pathInfo.setODataPathSegment(parseODataPathSegments(odataPathSegmentsAsString));
        try {
          String requestUri = baseUri + "/" + odataPathSegmentsAsString + queryParametersAsString;
          pathInfo.setRequestUri(new URI(requestUri));
        } catch (URISyntaxException e) {
          uriScanner.close();
          throw new BatchException(BatchException.INVALID_URI, e);
        }
      } else {
        uriScanner.close();
        throw new BatchException(BatchException.INVALID_URI);
      }
    } else {
      uriScanner.close();
      throw new BatchException(BatchException.INVALID_URI);
    }
    uriScanner.close();
    return pathInfo;
  }

  private Map<String, String> parseQueryParameters(final String uri) throws BatchException {
    Scanner uriScanner = new Scanner(uri);
    Map<String, String> queryParametersMap = new HashMap<String, String>();
    Pattern regex = Pattern.compile("(?:" + baseUri + ")?/?" + "[^?]+" + "\\?(.*)");
    if (uriScanner.hasNext(regex)) {
      uriScanner.next(regex);
      MatchResult uriResult = uriScanner.match();
      if (uriResult.groupCount() == 1) {
        String queryParams = uriResult.group(1);
        Scanner queryParamsScanner = new Scanner(queryParams).useDelimiter("&");
        while (queryParamsScanner.hasNext(REG_EX_QUERY_PARAMETER)) {
          queryParamsScanner.next(REG_EX_QUERY_PARAMETER);
          MatchResult result = queryParamsScanner.match();
          if (result.groupCount() == 2) {
            String systemQueryOption = result.group(1);
            String value = result.group(2);
            queryParametersMap.put(systemQueryOption, value);
          } else {
            queryParamsScanner.close();
            throw new BatchException(BatchException.INVALID_QUERY_PARAMETER);
          }
        }
        queryParamsScanner.close();

      } else {
        uriScanner.close();
        throw new BatchException(BatchException.INVALID_URI);
      }
    }
    uriScanner.close();
    return queryParametersMap;
  }

  private List<PathSegment> parseODataPathSegments(final String odataPathSegmentsAsString) {
    Scanner pathSegmentScanner = new Scanner(odataPathSegmentsAsString).useDelimiter("/");
    List<PathSegment> odataPathSegments = new ArrayList<PathSegment>();
    while (pathSegmentScanner.hasNext()) {
      odataPathSegments.add(new ODataPathSegmentImpl(pathSegmentScanner.next(), null));
    }
    pathSegmentScanner.close();
    return odataPathSegments;
  }

  private List<String> parseAcceptHeaders(final String headerValue) throws BatchException {
    return AcceptParser.parseAcceptHeaders(headerValue);
  }

  private List<Locale> parseAcceptableLanguages(final String headerValue) throws BatchException {
    return AcceptParser.parseAcceptableLanguages(headerValue);
  }

  private InputStream parseBody(final Scanner scanner) {
    String body = null;
    InputStream requestBody;
    while (scanner.hasNext() && !scanner.hasNext(REG_EX_ANY_BOUNDARY_STRING)) {
      if (!scanner.hasNext(REG_EX_ZERO_OR_MORE_WHITESPACES)) {
        if (body == null) {
          body = scanner.next();
        } else {
          body = body + LF + scanner.next();
        }
      } else {
        scanner.next();
      }
    }
    if (body != null) {
      requestBody = new ByteArrayInputStream(body.getBytes());
    } else {
      requestBody = new ByteArrayInputStream("".getBytes());
    }
    return requestBody;
  }

  private String getBoundary(final String contentType) throws BatchException {
    Scanner contentTypeScanner = new Scanner(contentType).useDelimiter(";\\s?");
    if (contentTypeScanner.hasNext(REG_EX_CONTENT_TYPE)) {
      contentTypeScanner.next(REG_EX_CONTENT_TYPE);
    } else {
      contentTypeScanner.close();
      throw new BatchException(BatchException.INVALID_CONTENT_TYPE.addContent(BatchConstants.MULTIPART_MIXED));
    }
    if (contentTypeScanner.hasNext(REG_EX_BOUNDARY_PARAMETER)) {
      contentTypeScanner.next(REG_EX_BOUNDARY_PARAMETER);
      MatchResult result = contentTypeScanner.match();
      contentTypeScanner.close();
      if (result.groupCount() == 1 && result.group(1).trim().matches(REG_EX_BOUNDARY)) {
        return trimQuota(result.group(1).trim());
      } else {
        throw new BatchException(BatchException.INVALID_BOUNDARY);
      }
    } else {
      contentTypeScanner.close();
      throw new BatchException(BatchException.MISSING_PARAMETER_IN_CONTENT_TYPE);
    }
  }

  private void validateEncoding(final String encoding) throws BatchException {
    if (!BatchConstants.BINARY_ENCODING.equalsIgnoreCase(encoding)) {
      throw new BatchException(BatchException.INVALID_CONTENT_TRANSFER_ENCODING);
    }
  }

  private Map<String, String> parseHeaders(final Scanner scanner) throws BatchException {
    Map<String, String> headers = new HashMap<String, String>();
    while (scanner.hasNext() && !(scanner.hasNext(REG_EX_BLANK_LINE))) {
      if (scanner.hasNext(REG_EX_HEADER)) {
        scanner.next(REG_EX_HEADER);
        MatchResult result = scanner.match();
        if (result.groupCount() == 2) {
          String headerName = result.group(1).trim().toLowerCase();
          String headerValue = result.group(2).trim().toLowerCase();
          headers.put(headerName, headerValue);
        }
      } else {
        throw new BatchException(BatchException.INVALID_HEADER.addContent(scanner.next()));
      }
    }
    return headers;
  }

  private void parseNewLine(final Scanner scanner) throws BatchException {
    if (scanner.hasNext() && scanner.hasNext(REG_EX_BLANK_LINE)) {
      scanner.next();
    } else {
      throw new BatchException(BatchException.MISSING_BLANK_LINE);
    }
  }

  private String getBaseUri() throws BatchException {
    if (batchRequestPathInfo != null) {
      if (batchRequestPathInfo.getServiceRoot() != null) {
        String baseUri = batchRequestPathInfo.getServiceRoot().toASCIIString();
        if (baseUri.lastIndexOf('/') == baseUri.length() - 1) {
          baseUri = baseUri.substring(0, baseUri.length() - 1);
        }
        for (PathSegment precedingPS : batchRequestPathInfo.getPrecedingSegments()) {
          baseUri = baseUri + "/" + precedingPS.getPath();
        }
        return baseUri;
      }
    } else {
      throw new BatchException(BatchException.INVALID_PATHINFO);
    }
    return null;
  }

  private String trimQuota(String boundary) {
    if (boundary.matches("\".*\"")) {
      boundary = boundary.replace("\"", "");
    }
    boundary = boundary.replaceAll("\\)", "\\\\)");
    boundary = boundary.replaceAll("\\(", "\\\\(");
    boundary = boundary.replaceAll("\\?", "\\\\?");
    boundary = boundary.replaceAll("\\+", "\\\\+");
    return boundary;
  }
}
