package com.sap.core.odata.api.edm;

/**
 * EdmSimpleType is a primitive type as defined in the Entity Data Model (EDM).
 *
 * @author SAP AG
 *
 */
public interface EdmSimpleType extends EdmType {

  /**
   * Checks type compatibility.
   *
   * @param simpleType  the {@link EdmSimpleType} to be tested for compatibility
   * @return <code>true</code> if the provided type is compatible to this type
   */
  public boolean isCompatible(EdmSimpleType simpleType);

  /**
   * Validates literal value.
   *
   * @param value  the literal value
   * @param literalKind  the kind of literal representation of value
   * @param facets  additional constraints for parsing (optional)
   * @return <code>true</code> if the validation is successful
   * @see EdmLiteralKind
   * @see EdmFacets
   */
  public boolean validate(String value, EdmLiteralKind literalKind, EdmFacets facets);

  /**
   * Converts literal representation of value to system data type.
   *
   * @param value  the literal representation of value
   * @param literalKind  the kind of literal representation of value
   * @param facets  additional constraints for parsing (optional)
   * @return system data type as Object
   * @see EdmLiteralKind
   * @see EdmFacets
   */
  //TODO: Check Returntype + Exception Handling
  public Object valueOfString(String value, EdmLiteralKind literalKind, EdmFacets facets);

  /**
   * Converts system data type to literal representation of value.
   *
   * @param value  the Java value as Object
   * @param literalKind  the requested kind of literal representation
   * @param facets  additional constraints for formatting (optional)
   * @return literal representation as String
   * @see EdmLiteralKind
   * @see EdmFacets
   */
  //TODO: Check Signature + Exception Handling
  public String valueToString(Object value, EdmLiteralKind literalKind, EdmFacets facets);

  /**
   * Converts literal representation to URI literal representation.
   *
   * @param literal
   * @return URI literal representation as String
   */
  public String toUriLiteral(String literal);
}