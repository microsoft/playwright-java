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

import com.microsoft.playwright.options.VirtualCredential;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.microsoft.playwright.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.*;

public class TestBrowserContextWebAuthn extends TestBase {
  private static final String B64URL_TO_BYTES_JS =
    "  const b64UrlToBytes = s => {\n" +
    "    let str = s.replace(/-/g, '+').replace(/_/g, '/');\n" +
    "    while (str.length % 4)\n" +
    "      str += '=';\n" +
    "    const bin = atob(str);\n" +
    "    const u8 = new Uint8Array(bin.length);\n" +
    "    for (let i = 0; i < bin.length; i++)\n" +
    "      u8[i] = bin.charCodeAt(i);\n" +
    "    return u8;\n" +
    "  };\n";

  @Test
  void shouldNotInterceptNavigatorCredentialsWithoutInstall() {
    // Seed a credential, but do not install the interceptor.
    context.credentials().create("localhost");
    page.navigate(server.EMPTY_PAGE);

    Object intercepted = page.evaluate("() => globalThis.__pwWebAuthnInstalled === true");
    assertEquals(false, intercepted);
  }

  @Test
  void shouldSeedKnownCredentialAndAuthenticate() {
    // This is the easiest way to create credentials. In practice, this
    // probably comes from environment.
    VirtualCredential known;
    try (BrowserContext source = browser.newContext()) {
      known = source.credentials().create("localhost");
    }

    // A fresh context imports the known credential and signs in with it.
    context.credentials().create(known.rpId, new Credentials.CreateOptions()
      .setId(known.id)
      .setUserHandle(known.userHandle)
      .setPrivateKey(known.privateKey)
      .setPublicKey(known.publicKey));
    context.credentials().install();
    page.navigate(server.EMPTY_PAGE);

    Map<String, Object> result = (Map<String, Object>) page.evaluate(
      "async ({ rpId, credentialId }) => {\n" +
      B64URL_TO_BYTES_JS +
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
      "}", mapOf("rpId", "localhost", "credentialId", known.id));

    assertEquals(known.id, result.get("id"));
    assertEquals("public-key", result.get("type"));
    assertEquals(true, result.get("hasClientData"));
    assertEquals(true, result.get("hasAuthData"));
    assertEquals(true, result.get("hasSignature"));
    // UP (0x01) | UV (0x04) = 0x05
    assertEquals(0x05, ((Number) result.get("authDataFlags")).intValue() & 0x05);

    // After the credential is deleted, the page can no longer authenticate with it.
    context.credentials().delete(known.id);
    assertEquals(0, context.credentials().get().size());

    Object error = page.evaluate(
      "async ({ rpId, credentialId }) => {\n" +
      B64URL_TO_BYTES_JS +
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
      "}", mapOf("rpId", "localhost", "credentialId", known.id));
    assertEquals("NotAllowedError", error);
  }

  @Test
  void shouldCapturePageCreatedCredentialAndReuseItInAnotherContext() {
    // Setup context: the app registers a passkey via navigator.credentials.create().
    String createdId;
    VirtualCredential captured;
    try (BrowserContext setupContext = browser.newContext()) {
      setupContext.credentials().install();
      Page setupPage = setupContext.newPage();
      setupPage.navigate(server.EMPTY_PAGE);

      createdId = (String) setupPage.evaluate(
        "async ({ rpId }) => {\n" +
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
        "}", mapOf("rpId", "localhost"));

      List<VirtualCredential> credentials = setupContext.credentials().get(
        new Credentials.GetOptions().setRpId("localhost"));
      assertEquals(1, credentials.size());
      captured = credentials.get(0);
      assertEquals(createdId, captured.id);
      assertTrue(captured.privateKey.matches("^[A-Za-z0-9_-]+$"), captured.privateKey);
      assertTrue(captured.publicKey.matches("^[A-Za-z0-9_-]+$"), captured.publicKey);
    }

    // Reuse the captured passkey in a fresh context and sign in with it.
    context.credentials().create(captured.rpId, new Credentials.CreateOptions()
      .setId(captured.id)
      .setUserHandle(captured.userHandle)
      .setPrivateKey(captured.privateKey)
      .setPublicKey(captured.publicKey));
    context.credentials().install();
    page.navigate(server.EMPTY_PAGE);

    Object gotId = page.evaluate(
      "async ({ rpId }) => {\n" +
      "  const challenge = crypto.getRandomValues(new Uint8Array(32));\n" +
      "  // No allowCredentials — relies on the re-seeded credential being discoverable.\n" +
      "  const cred = await navigator.credentials.get({\n" +
      "    publicKey: { challenge, rpId, userVerification: 'preferred' },\n" +
      "  });\n" +
      "  return cred.id;\n" +
      "}", mapOf("rpId", "localhost"));

    assertEquals(createdId, gotId);
  }
}
