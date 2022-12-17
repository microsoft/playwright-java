/*
 * Copyright (c) Microsoft Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright.options;

public class SecurityDetails {
  /**
   * Common Name component of the Issuer field. from the certificate. This should only be used for informational purposes.
   * Optional.
   */
  public String issuer;
  /**
   * The specific TLS protocol used. (e.g. {@code TLS 1.3}). Optional.
   */
  public String protocol;
  /**
   * Common Name component of the Subject field from the certificate. This should only be used for informational purposes.
   * Optional.
   */
  public String subjectName;
  /**
   * Unix timestamp (in seconds) specifying when this cert becomes valid. Optional.
   */
  public Double validFrom;
  /**
   * Unix timestamp (in seconds) specifying when this cert becomes invalid. Optional.
   */
  public Double validTo;

}