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
package com.sap.core.odata.api.ep.callback;

import java.util.List;
import java.util.Map;

/**
 * Objects of this class are a container for the result of the {@link TombstoneCallback}.
 * @author SAP AG
 *
 */
public class TombstoneCallbackResult {

  private List<Map<String, Object>> deletedEntriesData;
  private String deltaLink;

  /**
   * A map representing a deleted entry <b>MUST</b> contain all properties which are part of the key for this entry.
   * <br>A map representing a deleted entry <b>MAY</b> contain the property which is mapped on SyndicationUpdated. The provided value here will result in the value of the "when" attribute of the deleted entry. 
   * @return deleted entries in the form of List{@literal <}Map{@literal <}property name, property value{@literal >}{@literal >}
   */
  public List<Map<String, Object>> getDeletedEntriesData() {
    return deletedEntriesData;
  }

  /**
   * Sets the data for all deleted entries
   * @param deletedEntriesData
   */
  public void setDeletedEntriesData(final List<Map<String, Object>> deletedEntriesData) {
    this.deletedEntriesData = deletedEntriesData;
  }

  /**
   * @return delta link as String
   */
  public String getDeltaLink() {
    return deltaLink;
  }

  /**
   * Sets the delta link to retrieve a delta. 
   * @param deltaLink
   */
  public void setDeltaLink(final String deltaLink) {
    this.deltaLink = deltaLink;
  }

}
