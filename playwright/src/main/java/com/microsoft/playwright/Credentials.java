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

import com.microsoft.playwright.options.*;
import java.util.*;

/**
 * {@code Credentials} is a virtual WebAuthn authenticator scoped to a {@code BrowserContext}. It lets tests register
 * passkeys and answer {@code navigator.credentials.create()} / {@code navigator.credentials.get()} ceremonies in the page,
 * without a real authenticator or hardware security key.
 *
 * <p> There are two common ways to use it:
 *
 * <p> <strong>Usage: seed a known credential</strong>
 * <pre>{@code
 * BrowserContext context = browser.newContext();
 *
 * // A passkey your backend already provisioned for a test user.
 * context.credentials().create("example.com", new Credentials.CreateOptions()
 *     .setId(knownCredentialId) // base64url
 *     .setUserHandle(knownUserHandle) // base64url
 *     .setPrivateKey(knownPrivateKey) // base64url PKCS#8 (DER)
 *     .setPublicKey(knownPublicKey)); // base64url SPKI (DER)
 * context.credentials().install();
 *
 * Page page = context.newPage();
 * page.navigate("https://example.com/login");
 * // The page's navigator.credentials.get() is answered with the seeded passkey.
 * }</pre>
 *
 * <p> <strong>Usage: capture a passkey, then reuse it</strong>
 * <pre>{@code
 * // setup test: let the app register a passkey, then save it.
 * BrowserContext context = browser.newContext();
 * context.credentials().install();
 *
 * Page page = context.newPage();
 * page.navigate("https://example.com/register");
 * page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Create a passkey")).click();
 *
 * // Read back the passkey the page registered — it includes the private key.
 * VirtualCredential credential = context.credentials().get(
 *     new Credentials.GetOptions().setRpId("example.com")).get(0);
 * Files.writeString(Paths.get("playwright/.auth/passkey.json"), new Gson().toJson(credential));
 * }</pre>
 * <pre>{@code
 * // later test: seed the captured passkey so the app starts already enrolled.
 * VirtualCredential credential = new Gson().fromJson(
 *     Files.readString(Paths.get("playwright/.auth/passkey.json")), VirtualCredential.class);
 * BrowserContext context = browser.newContext();
 * context.credentials().create(credential.rpId, new Credentials.CreateOptions()
 *     .setId(credential.id)
 *     .setUserHandle(credential.userHandle)
 *     .setPrivateKey(credential.privateKey)
 *     .setPublicKey(credential.publicKey));
 * context.credentials().install();
 *
 * Page page = context.newPage();
 * page.navigate("https://example.com/login");
 * // navigator.credentials.get() resolves the captured passkey — already signed in.
 * }</pre>
 *
 * <p> <strong>Defaults</strong>
 */
public interface Credentials {
  class CreateOptions {
    /**
     * Base64url-encoded credential id. Auto-generated if omitted.
     */
    public String id;
    /**
     * Base64url-encoded PKCS#8 (DER) private key. Auto-generated if omitted.
     */
    public String privateKey;
    /**
     * Base64url-encoded SPKI (DER) public key. Auto-generated if omitted.
     */
    public String publicKey;
    /**
     * Base64url-encoded user handle. Auto-generated if omitted.
     */
    public String userHandle;

    /**
     * Base64url-encoded credential id. Auto-generated if omitted.
     */
    public CreateOptions setId(String id) {
      this.id = id;
      return this;
    }
    /**
     * Base64url-encoded PKCS#8 (DER) private key. Auto-generated if omitted.
     */
    public CreateOptions setPrivateKey(String privateKey) {
      this.privateKey = privateKey;
      return this;
    }
    /**
     * Base64url-encoded SPKI (DER) public key. Auto-generated if omitted.
     */
    public CreateOptions setPublicKey(String publicKey) {
      this.publicKey = publicKey;
      return this;
    }
    /**
     * Base64url-encoded user handle. Auto-generated if omitted.
     */
    public CreateOptions setUserHandle(String userHandle) {
      this.userHandle = userHandle;
      return this;
    }
  }
  class GetOptions {
    /**
     * Only return the credential with this base64url-encoded id.
     */
    public String id;
    /**
     * Only return credentials for this relying party id.
     */
    public String rpId;

    /**
     * Only return the credential with this base64url-encoded id.
     */
    public GetOptions setId(String id) {
      this.id = id;
      return this;
    }
    /**
     * Only return credentials for this relying party id.
     */
    public GetOptions setRpId(String rpId) {
      this.rpId = rpId;
      return this;
    }
  }
  /**
   * Installs the virtual WebAuthn authenticator into the context, overriding {@code navigator.credentials.create()} and
   * {@code navigator.credentials.get()} in all current and future pages. Call this before the page first touches {@code
   * navigator.credentials}.
   *
   * <p> Required: until {@link com.microsoft.playwright.Credentials#install Credentials.install()} is called, no interception is
   * in place and the page sees the platform's native (or absent) WebAuthn behaviour. Seeding credentials with {@link
   * com.microsoft.playwright.Credentials#create Credentials.create()} without installing populates the authenticator, but
   * the page will never see those credentials.
   *
   * @since v1.61
   */
  void install();
  /**
   * Seeds a virtual WebAuthn credential and returns it.
   *
   * <p> With only {@code rpId}, generates a fresh **ECDSA P-256** keypair, credential id and user handle. The seeded credential
   * is discoverable (resident), so the page can resolve it from both username-then-passkey and usernameless passkey flows.
   * The returned object carries the private and public keys, so it can be persisted to disk and re-seeded in a later test.
   *
   * <p> To **import a known credential**, supply all four of {@code id}, {@code userHandle}, {@code privateKey} and {@code
   * publicKey} together.
   *
   * <p> Call {@link com.microsoft.playwright.Credentials#install Credentials.install()} before navigating to a page that uses
   * WebAuthn.
   *
   * @param rpId Relying party id (typically the site's effective domain).
   * @since v1.61
   */
  default VirtualCredential create(String rpId) {
    return create(rpId, null);
  }
  /**
   * Seeds a virtual WebAuthn credential and returns it.
   *
   * <p> With only {@code rpId}, generates a fresh **ECDSA P-256** keypair, credential id and user handle. The seeded credential
   * is discoverable (resident), so the page can resolve it from both username-then-passkey and usernameless passkey flows.
   * The returned object carries the private and public keys, so it can be persisted to disk and re-seeded in a later test.
   *
   * <p> To **import a known credential**, supply all four of {@code id}, {@code userHandle}, {@code privateKey} and {@code
   * publicKey} together.
   *
   * <p> Call {@link com.microsoft.playwright.Credentials#install Credentials.install()} before navigating to a page that uses
   * WebAuthn.
   *
   * @param rpId Relying party id (typically the site's effective domain).
   * @since v1.61
   */
  VirtualCredential create(String rpId, CreateOptions options);
  /**
   * Removes a credential from the authenticator by its id. Works for any credential currently held — both those seeded with
   * {@link com.microsoft.playwright.Credentials#create Credentials.create()} and those the page registered itself by calling
   * {@code navigator.credentials.create()}.
   *
   * @param id Base64url-encoded credential id.
   * @since v1.61
   */
  void delete(String id);
  /**
   * Returns every credential currently held by the authenticator, optionally filtered by {@code rpId} or {@code id}. This
   * includes both credentials seeded with {@link com.microsoft.playwright.Credentials#create Credentials.create()} and
   * credentials the page registered itself by calling {@code navigator.credentials.create()}.
   *
   * <p> Each returned credential includes its private and public keys, so a passkey the app just registered can be saved and
   * re-seeded into a later test with {@link com.microsoft.playwright.Credentials#create Credentials.create()} — see the
   * second example in the class overview.
   *
   * @since v1.61
   */
  default List<VirtualCredential> get() {
    return get(null);
  }
  /**
   * Returns every credential currently held by the authenticator, optionally filtered by {@code rpId} or {@code id}. This
   * includes both credentials seeded with {@link com.microsoft.playwright.Credentials#create Credentials.create()} and
   * credentials the page registered itself by calling {@code navigator.credentials.create()}.
   *
   * <p> Each returned credential includes its private and public keys, so a passkey the app just registered can be saved and
   * re-seeded into a later test with {@link com.microsoft.playwright.Credentials#create Credentials.create()} — see the
   * second example in the class overview.
   *
   * @since v1.61
   */
  List<VirtualCredential> get(GetOptions options);
}

