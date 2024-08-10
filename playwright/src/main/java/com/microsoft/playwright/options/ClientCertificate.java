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

import java.nio.file.Path;

public class ClientCertificate {
  /**
   * Exact origin that the certificate is valid for. Origin includes {@code https} protocol, a hostname and optionally a
   * port.
   */
  public String origin;
  /**
   * Path to the file with the certificate in PEM format.
   */
  public Path certPath;
  /**
   * Path to the file with the private key in PEM format.
   */
  public Path keyPath;
  /**
   * Path to the PFX or PKCS12 encoded private key and certificate chain.
   */
  public Path pfxPath;
  /**
   * Passphrase for the private key (PEM or PFX).
   */
  public String passphrase;

  public ClientCertificate(String origin) {
    this.origin = origin;
  }
  /**
   * Path to the file with the certificate in PEM format.
   */
  public ClientCertificate setCertPath(Path certPath) {
    this.certPath = certPath;
    return this;
  }
  /**
   * Path to the file with the private key in PEM format.
   */
  public ClientCertificate setKeyPath(Path keyPath) {
    this.keyPath = keyPath;
    return this;
  }
  /**
   * Path to the PFX or PKCS12 encoded private key and certificate chain.
   */
  public ClientCertificate setPfxPath(Path pfxPath) {
    this.pfxPath = pfxPath;
    return this;
  }
  /**
   * Passphrase for the private key (PEM or PFX).
   */
  public ClientCertificate setPassphrase(String passphrase) {
    this.passphrase = passphrase;
    return this;
  }
}