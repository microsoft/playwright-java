/*
 * Copyright (c) Microsoft Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright.tests.library;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Credentials;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.ContextFactory;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.VirtualCredential;
import org.junit.jupiter.api.DisplayName;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/library/browsercontext-webauthn.spec.ts. */
public class TestBrowsercontextWebauthn {
  // Runs an assertion ceremony with a discoverable credential, returns the signature counter from the authenticator data.
  private static Object assertAndGetSignCount(Page page, String rpId) {
    return page.evaluate("async ({ rpId }) => {\n" +
      "  const challenge = crypto.getRandomValues(new Uint8Array(32));\n" +
      "  const cred = await navigator.credentials.get({\n" +
      "    publicKey: { challenge, rpId, userVerification: 'preferred' },\n" +
      "  });\n" +
      "  const resp = cred.response;\n" +
      "  return new DataView(resp.authenticatorData).getUint32(33);\n" +
      "}", mapOf("rpId", rpId));
  }

  // Java: stands for `context.credentials.create(credential.rpId, credential)`; the options are the fields of the credential.
  private static VirtualCredential seedCredential(BrowserContext context, VirtualCredential credential) {
    return context.credentials().create(credential.rpId, new Credentials.CreateOptions()
      .setId(credential.id)
      .setPrivateKey(credential.privateKey)
      .setPublicKey(credential.publicKey)
      .setSignCount(credential.signCount)
      .setUserHandle(credential.userHandle));
  }

  // Java: VirtualCredential has no equals(); this is its value as the plain object upstream compares.
  private static Map<String, Object> credentialValue(VirtualCredential credential, Integer signCount) {
    return mapOf("id", credential.id, "rpId", credential.rpId, "userHandle", credential.userHandle,
      "privateKey", credential.privateKey, "publicKey", credential.publicKey,
      "signCount", signCount != null ? signCount : credential.signCount);
  }

  // Java: stands for Buffer.from(value, 'base64url').
  private static byte[] fromBase64Url(String value) {
    return Base64.getUrlDecoder().decode(value);
  }

  // Java: stands for Buffer.includes(buffer).
  private static boolean includes(byte[] buffer, byte[] part) {
    for (int i = 0; i + part.length <= buffer.length; i++) {
      if (Arrays.equals(Arrays.copyOfRange(buffer, i, i + part.length), part)) {
        return true;
      }
    }
    return false;
  }

  private static final String CREATE_CREDENTIAL = "async ({ rpId }) => {\n" +
    "  const challenge = crypto.getRandomValues(new Uint8Array(32));\n" +
    "  const created = await navigator.credentials.create({\n" +
    "    publicKey: {\n" +
    "      challenge,\n" +
    "      rp: { id: rpId, name: 'Test RP' },\n" +
    "      user: { id: new Uint8Array([1, 2, 3, 4]), name: 'u', displayName: 'User' },\n" +
    "      pubKeyCredParams: [{ type: 'public-key', alg: -7 }],\n" +
    "      authenticatorSelection: { residentKey: 'required', userVerification: 'preferred' },\n" +
    "    },\n" +
    "  });\n" +
    "  return created.id;\n" +
    "}";

  private static final String GET_DISCOVERABLE_CREDENTIAL = "async ({ rpId }) => {\n" +
    "  const challenge = crypto.getRandomValues(new Uint8Array(32));\n" +
    "  // No allowCredentials — relies on the re-seeded credential being discoverable.\n" +
    "  const cred = await navigator.credentials.get({\n" +
    "    publicKey: { challenge, rpId, userVerification: 'preferred' },\n" +
    "  });\n" +
    "  return cred.id;\n" +
    "}";

  @PlaywrightTest
  @DisplayName("AU-017 should not intercept navigator.credentials without install()")
  void shouldNotInterceptNavigatorCredentialsWithoutInstall(ContextFactory contextFactory, TestServer server) {
    BrowserContext context = contextFactory.newContext();
    // Seed a credential, but do not install the interceptor.
    context.credentials().create(server.HOSTNAME);
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);

    Object intercepted = page.evaluate("() => globalThis.__pwWebAuthnInstalled === true");
    assertEquals(false, intercepted);
  }

  @PlaywrightTest
  @DisplayName("AU-018 should seed a known credential and authenticate")
  @SuppressWarnings("unchecked")
  void shouldSeedAKnownCredentialAndAuthenticate(ContextFactory contextFactory, TestServer server) {
    // This is the easiest way to create credentials. In practice, this
    // probably comes from environment.
    BrowserContext source = contextFactory.newContext();
    VirtualCredential known = source.credentials().create(server.HOSTNAME);

    // A fresh context imports the known credential and signs in with it.
    BrowserContext context = contextFactory.newContext();
    seedCredential(context, known);
    context.credentials().install();
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);

    Map<String, Object> result = (Map<String, Object>) page.evaluate("async ({ rpId, credentialId }) => {\n" +
      "  const b64UrlToBytes = (s) => {\n" +
      "    let str = s.replace(/-/g, '+').replace(/_/g, '/');\n" +
      "    while (str.length % 4)\n" +
      "      str += '=';\n" +
      "    const bin = atob(str);\n" +
      "    const u8 = new Uint8Array(bin.length);\n" +
      "    for (let i = 0; i < bin.length; i++)\n" +
      "      u8[i] = bin.charCodeAt(i);\n" +
      "    return u8;\n" +
      "  };\n" +
      "  const challenge = crypto.getRandomValues(new Uint8Array(32));\n" +
      "  const cred = await navigator.credentials.get({\n" +
      "    publicKey: {\n" +
      "      challenge,\n" +
      "      rpId,\n" +
      "      allowCredentials: [{ type: 'public-key', id: b64UrlToBytes(credentialId) }],\n" +
      "      userVerification: 'preferred',\n" +
      "    },\n" +
      "  });\n" +
      "  const resp = cred.response;\n" +
      "  return {\n" +
      "    id: cred.id,\n" +
      "    type: cred.type,\n" +
      "    hasClientData: resp.clientDataJSON.byteLength > 0,\n" +
      "    hasAuthData: resp.authenticatorData.byteLength > 0,\n" +
      "    hasSignature: resp.signature.byteLength > 0,\n" +
      "    authDataFlags: new Uint8Array(resp.authenticatorData)[32],\n" +
      "  };\n" +
      "}", mapOf("rpId", server.HOSTNAME, "credentialId", known.id));

    assertEquals(known.id, result.get("id"));
    assertEquals("public-key", result.get("type"));
    assertEquals(true, result.get("hasClientData"));
    assertEquals(true, result.get("hasAuthData"));
    assertEquals(true, result.get("hasSignature"));
    // UP (0x01) | UV (0x04) = 0x05
    assertEquals(0x05, ((Integer) result.get("authDataFlags")) & 0x05);

    // After the credential is deleted, the page can no longer authenticate with it.
    context.credentials().delete(known.id);
    assertEquals(0, context.credentials().get().size());

    Object error = page.evaluate("async ({ rpId, credentialId }) => {\n" +
      "  const b64UrlToBytes = (s) => {\n" +
      "    let str = s.replace(/-/g, '+').replace(/_/g, '/');\n" +
      "    while (str.length % 4)\n" +
      "      str += '=';\n" +
      "    const bin = atob(str);\n" +
      "    const u8 = new Uint8Array(bin.length);\n" +
      "    for (let i = 0; i < bin.length; i++)\n" +
      "      u8[i] = bin.charCodeAt(i);\n" +
      "    return u8;\n" +
      "  };\n" +
      "  const challenge = crypto.getRandomValues(new Uint8Array(32));\n" +
      "  try {\n" +
      "    await navigator.credentials.get({\n" +
      "      publicKey: {\n" +
      "        challenge,\n" +
      "        rpId,\n" +
      "        allowCredentials: [{ type: 'public-key', id: b64UrlToBytes(credentialId) }],\n" +
      "      },\n" +
      "    });\n" +
      "    return 'no-error';\n" +
      "  } catch (e) {\n" +
      "    return e.name;\n" +
      "  }\n" +
      "}", mapOf("rpId", server.HOSTNAME, "credentialId", known.id));
    assertEquals("NotAllowedError", error);
  }

  @PlaywrightTest
  @DisplayName("AU-019 should capture a page-created credential and reuse it in another context")
  void shouldCaptureAPageCreatedCredentialAndReuseItInAnotherContext(ContextFactory contextFactory, TestServer server) {
    // Setup context: the app registers a passkey via navigator.credentials.create().
    BrowserContext setupContext = contextFactory.newContext();
    setupContext.credentials().install();
    Page setupPage = setupContext.newPage();
    setupPage.navigate(server.EMPTY_PAGE);

    Object createdId = setupPage.evaluate(CREATE_CREDENTIAL, mapOf("rpId", server.HOSTNAME));

    VirtualCredential captured = setupContext.credentials().get(new Credentials.GetOptions().setRpId(server.HOSTNAME)).get(0);
    assertEquals(createdId, captured.id);
    assertTrue(Pattern.compile("^[A-Za-z0-9_-]+$").matcher(captured.privateKey).find());
    assertTrue(Pattern.compile("^[A-Za-z0-9_-]+$").matcher(captured.publicKey).find());

    // Reuse the captured passkey in a fresh context and sign in with it.
    BrowserContext context = contextFactory.newContext();
    seedCredential(context, captured);
    context.credentials().install();
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);

    Object gotId = page.evaluate(GET_DISCOVERABLE_CREDENTIAL, mapOf("rpId", server.HOSTNAME));

    assertEquals(createdId, gotId);
  }

  @PlaywrightTest
  @DisplayName("AU-020 should reuse a page-created credential via the storageState option")
  void shouldReuseAPageCreatedCredentialViaTheStorageStateOption(ContextFactory contextFactory, TestServer server) {
    // Setup context: the app registers a passkey via navigator.credentials.create().
    BrowserContext setupContext = contextFactory.newContext();
    setupContext.credentials().install();
    Page setupPage = setupContext.newPage();
    setupPage.navigate(server.EMPTY_PAGE);

    Object createdId = setupPage.evaluate(CREATE_CREDENTIAL, mapOf("rpId", server.HOSTNAME));

    // Capture the passkey as part of the storage state.
    String storageState = setupContext.storageState(new BrowserContext.StorageStateOptions().setCredentials(true));

    // A context created from the storage state has the authenticator installed and signs in with
    // the captured passkey — without calling install() again.
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions().setStorageState(storageState));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);

    Object gotId = page.evaluate(GET_DISCOVERABLE_CREDENTIAL, mapOf("rpId", server.HOSTNAME));

    assertEquals(createdId, gotId);
  }

  @PlaywrightTest
  @DisplayName("AU-021 should seed and report signCount")
  void shouldSeedAndReportSignCount(ContextFactory contextFactory, TestServer server) {
    BrowserContext context = contextFactory.newContext();
    VirtualCredential fresh = context.credentials().create("fresh.example.com");
    assertEquals(0, fresh.signCount);

    VirtualCredential seeded = context.credentials().create(server.HOSTNAME, new Credentials.CreateOptions().setSignCount(41));
    assertEquals(41, seeded.signCount);
    context.credentials().install();
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);

    // Each assertion increments the counter and reports the new value to the page.
    assertEquals(42, assertAndGetSignCount(page, server.HOSTNAME));
    assertEquals(43, assertAndGetSignCount(page, server.HOSTNAME));
    List<VirtualCredential> seededList = context.credentials().get(new Credentials.GetOptions().setId(seeded.id));
    assertEquals(1, seededList.size());
    assertEquals(credentialValue(seeded, 43), credentialValue(seededList.get(0), null));
    List<VirtualCredential> freshList = context.credentials().get(new Credentials.GetOptions().setId(fresh.id));
    assertEquals(1, freshList.size());
    assertEquals(credentialValue(fresh, null), credentialValue(freshList.get(0), null));

    // A captured credential continues from the same counter in another context.
    VirtualCredential captured = context.credentials().get(new Credentials.GetOptions().setId(seeded.id)).get(0);
    BrowserContext context2 = contextFactory.newContext();
    seedCredential(context2, captured);
    context2.credentials().install();
    Page page2 = context2.newPage();
    page2.navigate(server.EMPTY_PAGE);
    assertEquals(44, assertAndGetSignCount(page2, server.HOSTNAME));
  }

  @PlaywrightTest
  @DisplayName("AU-022 should reject invalid signCount")
  void shouldRejectInvalidSignCount(ContextFactory contextFactory) {
    BrowserContext context = contextFactory.newContext();
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> context.credentials().create("example.com", new Credentials.CreateOptions().setSignCount(-1)));
    assertTrue(error.getMessage().contains("signCount must be between 0 and 4294967295, got -1"));
    assertEquals(Collections.emptyList(), context.credentials().get());
  }

  @PlaywrightTest
  @DisplayName("AU-023 should preserve signCount via the storageState option")
  void shouldPreserveSignCountViaTheStorageStateOption(ContextFactory contextFactory, TestServer server) {
    BrowserContext setupContext = contextFactory.newContext();
    setupContext.credentials().create(server.HOSTNAME);
    setupContext.credentials().install();
    Page setupPage = setupContext.newPage();
    setupPage.navigate(server.EMPTY_PAGE);
    assertEquals(1, assertAndGetSignCount(setupPage, server.HOSTNAME));

    String storageState = setupContext.storageState(new BrowserContext.StorageStateOptions().setCredentials(true));
    VirtualCredential captured = setupContext.credentials().get().get(0);
    assertEquals(1, captured.signCount);
    // Java: the storage state is a JSON string; it is compared as parsed JSON.
    Gson gson = new Gson();
    assertEquals(
      gson.toJsonTree(mapOf("cookies", Collections.emptyList(), "origins", Collections.emptyList(), "credentials", Arrays.asList(credentialValue(captured, null)))),
      JsonParser.parseString(storageState));

    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions().setStorageState(storageState));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    assertEquals(2, assertAndGetSignCount(page, server.HOSTNAME));

    // Storage state saved by older versions has no signCount, the counter starts from zero.
    Map<String, Object> legacyCredential = new LinkedHashMap<>(credentialValue(captured, null));
    legacyCredential.remove("signCount");
    JsonObject legacyState = JsonParser.parseString(storageState).getAsJsonObject();
    JsonArray legacyCredentials = new JsonArray();
    legacyCredentials.add(gson.toJsonTree(legacyCredential));
    legacyState.add("credentials", legacyCredentials);
    String legacyStorageState = legacyState.toString();
    BrowserContext legacyContext = contextFactory.newContext(new Browser.NewContextOptions().setStorageState(legacyStorageState));
    Page legacyPage = legacyContext.newPage();
    legacyPage.navigate(server.EMPTY_PAGE);
    assertEquals(1, assertAndGetSignCount(legacyPage, server.HOSTNAME));
  }

  @PlaywrightTest
  @DisplayName("AU-024 should serialize credentials with toJSON")
  @SuppressWarnings("unchecked")
  void shouldSerializeCredentialsWithToJSON(ContextFactory contextFactory, TestServer server) {
    BrowserContext context = contextFactory.newContext();
    context.credentials().install();
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);

    Map<String, Object> result = (Map<String, Object>) page.evaluate("async ({ rpId }) => {\n" +
      "  const toB64Url = (buf) => btoa(String.fromCharCode(...new Uint8Array(buf))).replace(/\\+/g, '-').replace(/\\//g, '_').replace(/=+$/, '');\n" +
      "  const created = await navigator.credentials.create({\n" +
      "    publicKey: {\n" +
      "      challenge: crypto.getRandomValues(new Uint8Array(32)),\n" +
      "      rp: { id: rpId, name: 'Test RP' },\n" +
      "      user: { id: new Uint8Array([1, 2, 3, 4]), name: 'u', displayName: 'User' },\n" +
      "      pubKeyCredParams: [{ type: 'public-key', alg: -7 }],\n" +
      "      authenticatorSelection: { residentKey: 'required' },\n" +
      "    },\n" +
      "  });\n" +
      "  const attestation = created.response;\n" +
      "  const got = await navigator.credentials.get({\n" +
      "    publicKey: { challenge: crypto.getRandomValues(new Uint8Array(32)), rpId },\n" +
      "  });\n" +
      "  const assertion = got.response;\n" +
      "  return {\n" +
      "    registration: JSON.parse(JSON.stringify(created)),\n" +
      "    authentication: JSON.parse(JSON.stringify(got)),\n" +
      "    expected: {\n" +
      "      id: created.id,\n" +
      "      attestation: {\n" +
      "        clientDataJSON: toB64Url(attestation.clientDataJSON),\n" +
      "        authenticatorData: toB64Url(attestation.getAuthenticatorData()),\n" +
      "        publicKey: toB64Url(attestation.getPublicKey()),\n" +
      "        attestationObject: toB64Url(attestation.attestationObject),\n" +
      "      },\n" +
      "      assertion: {\n" +
      "        clientDataJSON: toB64Url(assertion.clientDataJSON),\n" +
      "        authenticatorData: toB64Url(assertion.authenticatorData),\n" +
      "        signature: toB64Url(assertion.signature),\n" +
      "        userHandle: toB64Url(assertion.userHandle),\n" +
      "      },\n" +
      "    },\n" +
      "  };\n" +
      "}", mapOf("rpId", server.HOSTNAME));
    Map<String, Object> registration = (Map<String, Object>) result.get("registration");
    Map<String, Object> authentication = (Map<String, Object>) result.get("authentication");
    Map<String, Object> expected = (Map<String, Object>) result.get("expected");

    // Java: the spread `{ ...expected.attestation, transports, publicKeyAlgorithm }` is a copied map.
    Map<String, Object> expectedRegistrationResponse = new LinkedHashMap<>((Map<String, Object>) expected.get("attestation"));
    expectedRegistrationResponse.put("transports", Arrays.asList("internal"));
    expectedRegistrationResponse.put("publicKeyAlgorithm", -7);
    assertEquals(mapOf(
      "id", expected.get("id"),
      "rawId", expected.get("id"),
      "response", expectedRegistrationResponse,
      "authenticatorAttachment", "platform",
      "clientExtensionResults", mapOf(),
      "type", "public-key"), registration);
    assertEquals(mapOf(
      "id", expected.get("id"),
      "rawId", expected.get("id"),
      "response", expected.get("assertion"),
      "authenticatorAttachment", "platform",
      "clientExtensionResults", mapOf(),
      "type", "public-key"), authentication);

    VirtualCredential credential = context.credentials().get().get(0);
    Map<String, Object> registrationResponse = (Map<String, Object>) registration.get("response");
    Map<String, Object> authenticationResponse = (Map<String, Object>) authentication.get("response");
    assertEquals(credential.publicKey, registrationResponse.get("publicKey"));
    assertEquals(credential.userHandle, authenticationResponse.get("userHandle"));
    assertEquals("webauthn.create", JsonParser.parseString(new String(fromBase64Url((String) registrationResponse.get("clientDataJSON")), StandardCharsets.UTF_8)).getAsJsonObject().get("type").getAsString());
    assertEquals("webauthn.get", JsonParser.parseString(new String(fromBase64Url((String) authenticationResponse.get("clientDataJSON")), StandardCharsets.UTF_8)).getAsJsonObject().get("type").getAsString());

    // Registration authenticator data is embedded in the attestation object and carries attested credential data (AT flag).
    byte[] authData = fromBase64Url((String) registrationResponse.get("authenticatorData"));
    assertEquals(true, includes(fromBase64Url((String) registrationResponse.get("attestationObject")), authData));
    assertEquals(0x40, authData[32] & 0x40);
  }
}
