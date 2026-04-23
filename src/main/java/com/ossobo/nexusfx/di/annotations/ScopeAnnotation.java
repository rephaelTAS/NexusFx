package com.ossobo.nexusfx.di.annotations;




import com.ossobo.nexusfx.di.scopes.ScopeType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ScopeAnnotation {
    ScopeType value() default ScopeType.SINGLETON;
}

