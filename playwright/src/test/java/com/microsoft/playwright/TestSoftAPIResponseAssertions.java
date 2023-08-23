package com.microsoft.playwright;

import com.microsoft.playwright.assertions.APIResponseAssertions;
import com.microsoft.playwright.impl.APIResponseAssertionsImpl;
import com.microsoft.playwright.impl.APIResponseAssertionsImplProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static com.microsoft.playwright.Utils.createProxy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

// The only thing we want to verify in these tests is that the correct method was called
@ExtendWith(MockitoExtension.class)
public class TestSoftAPIResponseAssertions {
  @Mock
  private APIResponseAssertionsImpl apiResponseAssertionsMock;
  private APIResponseAssertionsImplProxy proxy;

  @BeforeEach
  void beforeEach() {
    proxy = createProxy(APIResponseAssertionsImplProxy.class, apiResponseAssertionsMock);
  }

  @Test
  void proxyImplementsAPIResponseAssertions() {
    assertTrue(APIResponseAssertions.class.isAssignableFrom(proxy.getClass()));
  }

  @Test
  void not() {
    proxy.not();
    verify(apiResponseAssertionsMock).not();
  }

  @Test
  void isOK() {
    assertDoesNotThrow(() -> proxy.isOK());
    verify(apiResponseAssertionsMock).isOK();
  }
}
