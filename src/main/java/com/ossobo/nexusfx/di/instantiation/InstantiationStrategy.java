package com.ossobo.nexusfx.di.instantiation;

import com.ossobo.nexusfx.di.scanner.models.BeanDefinition;

public interface InstantiationStrategy {

    Object instantiate(BeanDefinition definition) throws Exception;
    boolean canHandle(BeanDefinition definition);
}