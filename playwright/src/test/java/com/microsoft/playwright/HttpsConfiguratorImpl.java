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

package com.microsoft.playwright;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;

class HttpsConfiguratorImpl extends HttpsConfigurator {

  static HttpsConfigurator create() {
    return new HttpsConfiguratorImpl(createSSLContext());
  }

  private HttpsConfiguratorImpl(SSLContext context) {
    super(context);
  }

  @Override
  public void configure(HttpsParameters params) {
    SSLContext sslContext = getSSLContext();
    SSLParameters sslParams = sslContext.getDefaultSSLParameters();
    sslParams.setNeedClientAuth(true);
    params.setNeedClientAuth(true);
    params.setSSLParameters(sslParams);
  }

  // @see http://rememberjava.com/http/2017/04/29/simple_https_server.html
  private static SSLContext createSSLContext() {
    try{
      KeyStore ks = KeyStore.getInstance("JKS");
      String password = "password";
      // Generated via
      // keytool -genkey -keyalg RSA -validity 36500 -keysize 4096 -dname cn=Playwright,ou=Playwright,o=Playwright,c=US -keystore keystore.jks -storepass password -keypass password
      ks.load(HttpsConfiguratorImpl.class.getClassLoader().getResourceAsStream("resources/keys/keystore.jks"), password.toCharArray());
      KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
      kmf.init(ks, password.toCharArray());

      TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
      tmf.init(ks);

      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
      return sslContext;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
