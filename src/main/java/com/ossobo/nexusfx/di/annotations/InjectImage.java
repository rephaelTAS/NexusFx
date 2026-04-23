package com.ossobo.nexusfx.di.annotations;

import java.lang.annotation.*;

/**
 * @InjectImage v1.0 - Injeta uma Image.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InjectImage {
    String value();
}
