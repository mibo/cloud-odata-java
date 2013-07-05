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
package com.sap.core.odata.core.uri.expression;

import com.sap.core.odata.api.exception.ODataMessageException;
import com.sap.core.odata.api.uri.expression.ExpressionParserException;
import com.sap.core.odata.api.uri.expression.OrderByExpression;

/**
 * Interface which defines a method for parsing a $orderby expression to allow different parser implementations
 * <p>
 * The current expression parser supports expressions as defined in the OData specification 2.0 with the following restrictions
 *   - the methods "cast", "isof" and "replace" are not supported
 *   
 * The expression parser can be used with providing an Entity Data Model (EDM) an without providing it.
 *  <p>When a EDM is provided the expression parser will be as strict as possible. That means:
 *  <li>All properties used in the expression must be defined inside the EDM</li>
 *  <li>The types of EDM properties will be checked against the lists of allowed type per method, binary- and unary operator</li>
 *  </p>
 *  <p>If no EDM is provided the expression parser performs a lax validation
 *  <li>The properties used in the expression are not looked up inside the EDM and the type of the expression node representing the 
 *      property will be "null"</li>
 *  <li>Expression node with EDM-types which are "null" are not considered during the parameter type validation, to the return type of the parent expression node will
 *  also become "null"</li>
 *  
 * @author SAP AG
 */
public interface OrderByParser {
  /**
   * Parses a $orderby expression string and creates an $orderby expression tree
   * @param orderByExpression
   *   The $orderby expression string ( for example "name asc" ) to be parsed
   * @return
   *    The $orderby expression tree
   * @throws ExpressionParserException
   *   Exception thrown due to errors while parsing the $orderby expression string 
   * @throws ODataMessageException
   *   Used for extensibility
   */
  abstract OrderByExpression parseOrderByString(String orderByExpression) throws ExpressionParserException, ODataMessageException;
}
