package com.microsoft.playwright.junit;

import com.microsoft.playwright.junit.impl.BrowserContextExtension;
import com.microsoft.playwright.junit.impl.BrowserExtension;
import com.microsoft.playwright.junit.impl.PageExtension;
import com.microsoft.playwright.junit.impl.PlaywrightExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ExtendWith({PlaywrightExtension.class, BrowserExtension.class, BrowserContextExtension.class, PageExtension.class})
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UsePlaywright {
  Class<? extends PlaywrightFactory> playwrightFactory() default DefaultPlaywrightFactory.class;

  Class<? extends BrowserFactory> browserFactory() default DefaultBrowserFactory.class;

  Class<? extends BrowserContextFactory> browserContextFactory() default DefaultBrowserContextFactory.class;

  Class<? extends ConfigFactory> configFactory() default DefaultConfigFactory.class;
}
