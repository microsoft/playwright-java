package com.microsoft.playwright.impl.junit;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(MultiBrowserExtension.class)
@TestTemplate
public @interface MultiBrowser {

  String[] browsers() default {"chromium", "firefox", "webkit"};
}
