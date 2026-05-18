package com.ossobo.nexusfx.di.scanner;

import com.ossobo.nexusfx.di.scanner.models.BeanDefinition;
import com.ossobo.nexusfx.di.annotations.*;
import com.ossobo.nexusfx.di.scanner.models.BeanDefinition;
import com.ossobo.nexusfx.di.scanner.ComponentRegistry;
import com.ossobo.nexusfx.di.scanner.models.BeanDefinition;
import com.ossobo.nexusfx.di.scopes.enums.ScopeType;
import com.ossobo.nexusfx.di.scanner.models.BeanDefinition;
import io.github.classgraph.ClassGraph;
import com.ossobo.nexusfx.di.scanner.models.BeanDefinition;
import io.github.classgraph.ScanResult;

import com.ossobo.nexusfx.di.scanner.models.BeanDefinition;
import java.lang.annotation.Annotation;
import com.ossobo.nexusfx.di.scanner.models.BeanDefinition;
import java.lang.reflect.Method;
import com.ossobo.nexusfx.di.scanner.models.BeanDefinition;
import java.util.*;
import com.ossobo.nexusfx.di.scanner.models.BeanDefinition;
import java.util.logging.Level;
import com.ossobo.nexusfx.di.scanner.models.BeanDefinition;
import java.util.logging.Logger;

/**
 * ComponentScanner v2.0
 *
 * Responsabilidade única: descobrir classes anotadas via ClassGraph.
 *
 * Fluxo:
 * 1. ClassGraph scan → nomes de classes (NÃO carrega)
 * 2. Para cada nome → Class.forName (carrega sob demanda)
 * 3. Regista no ComponentRegistry
 *
 * Suporta:
 * - @Component, @Service, @Repository, @Controller
 * - @Configuration com métodos @Bean
 * - @ComponentScan para pacotes adicionais
 *
 * @since 2.0
 */
public final class ComponentScanner {

    private static final Logger LOGGER = Logger.getLogger(ComponentScanner.class.getName());

    private static final Set<Class<? extends Annotation>> COMPONENT_ANNOTATIONS =
            Set.of(Component.class, Service.class, Repository.class, Controller.class);

    private final ComponentRegistry registry;
    private final String[] basePackages;

    public ComponentScanner(ComponentRegistry registry, String... basePackages) {
        this.registry = registry;
        this.basePackages = (basePackages != null && basePackages.length > 0)
                ? basePackages
                : new String[]{""};
    }

    /**
     * Executa o scan completo:
     * 1. ClassGraph descobre nomes de classes
     * 2. Carrega cada classe e regista no ComponentRegistry
     */
    public void scanAndRegister() {
        LOGGER.log(Level.INFO, "Iniciando scan nos pacotes: {0}", String.join(", ", basePackages));

        try (ScanResult result = new ClassGraph()
                .enableAnnotationInfo()
                .enableClassInfo()
                .acceptPackages(basePackages)
                .scan()) {

            // === COMPONENTES ===
            Set<String> componentNames = new HashSet<>();
            for (Class<? extends Annotation> ann : COMPONENT_ANNOTATIONS) {
                componentNames.addAll(result.getClassesWithAnnotation(ann).getNames());
            }

            LOGGER.log(Level.INFO, "ClassGraph encontrou {0} componentes.", componentNames.size());

            for (String className : componentNames) {
                registerComponentByName(className);
            }

            // === @Configuration ===
            List<String> configNames = result.getClassesWithAnnotation(Configuration.class).getNames();
            LOGGER.log(Level.INFO, "ClassGraph encontrou {0} @Configuration.", configNames.size());

            for (String className : configNames) {
                processConfigurationByName(className);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Falha no scan: {0}", e.getMessage());
            throw new RuntimeException("Falha no escaneamento de componentes", e);
        }

        LOGGER.log(Level.INFO, "Scan concluído. Total de beans: {0}", registry.getBeanNames().size());
    }

    /**
     * Scan adicional para @ComponentScan.
     */
    public void scanAdditional(String... additionalPackages) {
        if (additionalPackages == null || additionalPackages.length == 0) return;

        LOGGER.log(Level.INFO, "@ComponentScan: pacotes adicionais: {0}",
                String.join(", ", additionalPackages));

        try (ScanResult result = new ClassGraph()
                .enableAnnotationInfo()
                .enableClassInfo()
                .acceptPackages(additionalPackages)
                .scan()) {

            Set<String> componentNames = new HashSet<>();
            for (Class<? extends Annotation> ann : COMPONENT_ANNOTATIONS) {
                componentNames.addAll(result.getClassesWithAnnotation(ann).getNames());
            }

            for (String className : componentNames) {
                registerComponentByName(className);
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Falha no scan adicional: {0}", e.getMessage());
        }
    }

    // ===== REGISTO A PARTIR DO NOME (ClassGraph → Class.forName) =====

    private void registerComponentByName(String className) {
        try {
            Class<?> type = Class.forName(className);
            if (isComponent(type)) {
                registerComponent(type);
            }
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.WARNING, "Classe não encontrada: {0}", className);
        }
    }

    private void processConfigurationByName(String className) {
        try {
            Class<?> type = Class.forName(className);
            if (type.isAnnotationPresent(Configuration.class)) {
                processConfigurationClass(type);
            }
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.WARNING, "@Configuration não encontrada: {0}", className);
        }
    }

    // ===== REGISTO DE COMPONENTES =====

    private boolean isComponent(Class<?> type) {
        return COMPONENT_ANNOTATIONS.stream().anyMatch(type::isAnnotationPresent);
    }

    private void registerComponent(Class<?> type) {
        String beanName = getBeanName(type);
        ScopeType scopeType = determineScope(type);

        BeanDefinition definition = new BeanDefinition(beanName, type, scopeType);
        registry.registerDefinition(definition);

        LOGGER.log(Level.FINE, "Registado: {0}", definition);
    }

    private String getBeanName(Class<?> type) {
        for (Class<? extends Annotation> ann : COMPONENT_ANNOTATIONS) {
            if (type.isAnnotationPresent(ann)) {
                try {
                    Method valueMethod = ann.getMethod("value");
                    String value = (String) valueMethod.invoke(type.getAnnotation(ann));
                    if (!value.isEmpty()) return value;
                } catch (Exception ignored) {}
            }
        }
        return Character.toLowerCase(type.getSimpleName().charAt(0))
                + type.getSimpleName().substring(1);
    }

    private ScopeType determineScope(Class<?> type) {
        if (type.isAnnotationPresent(ScopeAnnotation.class)) {
            ScopeType scopeValue = type.getAnnotation(ScopeAnnotation.class).value();
            return scopeValue;
        }
        return ScopeType.SINGLETON;
    }

    // ===== @Configuration + @Bean =====

    private void processConfigurationClass(Class<?> configClass) {
        String beanName = getConfigBeanName(configClass);
        ScopeType scopeType = determineScope(configClass);

        registry.registerDefinition(new BeanDefinition(beanName, configClass, scopeType));

        // Verifica @ComponentScan
        if (configClass.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan scan = configClass.getAnnotation(ComponentScan.class);
            scanAdditional(scan.value());
        }

        // Processa métodos @Bean
        for (Method method : configClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Bean.class)) {
                registerFactoryBean(configClass, method);
            }
        }
    }

    private String getConfigBeanName(Class<?> configClass) {
        Configuration config = configClass.getAnnotation(Configuration.class);
        String value = config.value();
        return value.isEmpty() ? getDefaultBeanName(configClass) : value;
    }

    private void registerFactoryBean(Class<?> factoryClass, Method factoryMethod) {
        Bean bean = factoryMethod.getAnnotation(Bean.class);
        String beanName = bean.name().isEmpty() ? factoryMethod.getName() : bean.name();
        Class<?> beanType = factoryMethod.getReturnType();
        ScopeType scopeType = determineScope(factoryClass);

        BeanDefinition definition = new BeanDefinition(
                beanName, beanType, scopeType, factoryClass, factoryMethod
        );
        registry.registerDefinition(definition);

        LOGGER.log(Level.FINE, "Registado @Bean: {0}", definition);
    }

    private String getDefaultBeanName(Class<?> type) {
        return Character.toLowerCase(type.getSimpleName().charAt(0))
                + type.getSimpleName().substring(1);
    }
}