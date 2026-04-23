package com.ossobo.nexusfx.di;

import com.ossobo.nexusfx.di.annotations.*;
import com.ossobo.nexusfx.di.exceptions.CircularDependencyException;
import com.ossobo.nexusfx.di.exceptions.DependencyNotRegisteredException;
import com.ossobo.nexusfx.di.scopes.*;
import org.reflections.Reflections;


import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Contêiner de Injeção de Dependência completo.
 * Suporta:
 * - Escopos (Singleton, Thread, customizados)
 * - Injeção por construtor, campo e método
 * - Registro lazy e eager de beans
 * - Detecção de dependências circulares
 * - Qualificadores e named beans
 * - Ciclo de vida com @PostConstruct e @PreDestroy
 * - Suporte a @Primary para resolução de ambiguidades
 * - Suporte a injeção de coleções (List/Set) de implementações
 * - Configuração baseada em classes (@Configuration com métodos @Bean)
 */
public final class DiContainer {
    private static final Logger LOGGER = Logger.getLogger(DiContainer.class.getName());
    private static volatile DiContainer INSTANCE; // Garante uma única instância do contêiner

    // Mapas para o registro e gestão de dependências
    private final Map<String, ScopeHandler> scopes = new ConcurrentHashMap<>();
    // Estratégias de instanciação por tipo (chave é a classe principal ou interface)
    private final Map<Class<?>, InstantiationStrategy<?>> strategies = new ConcurrentHashMap<>();
    // Mapeia tipos para seus qualificadores e implementações (ex: Service.class -> {"myService" -> MyServiceImpl.class})
    private final Map<Class<?>, Map<String, Class<?>>> qualifiers = new ConcurrentHashMap<>();
    // Mapeia nomes de beans para seus suppliers (usado por @Bean methods e named instances)
    private final Map<String, Supplier<Object>> namedBeans = new ConcurrentHashMap<>();
    // Listeners para o ciclo de vida das dependências
    private final List<DependencyLifecycleListener> lifecycleListeners = new CopyOnWriteArrayList<>();
    // Mapeia tipos para sua implementação primária (@Primary)
    private final Map<Class<?>, Class<?>> primaryImplementations = new ConcurrentHashMap<>();
    // Rastreia todas as implementações conhecidas para uma interface/classe (para injeção de coleções)
    private final Map<Class<?>, Set<Class<?>>> allRegisteredImplementations = new ConcurrentHashMap<>();

    // Caches para otimização de reflexão
    private final Map<Class<?>, Constructor<?>> injectableConstructorsCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<Field>> injectableFieldsCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<Method>> injectableMethodsCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<Method>> postConstructMethodsCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<Method>> preDestroyMethodsCache = new ConcurrentHashMap<>();

    // Controle de dependências circulares e varredura de classes
    private final ThreadLocal<Set<Class<?>>> resolutionStack = ThreadLocal.withInitial(HashSet::new);
    private final Reflections reflections; // Usado para escaneamento de classes

    // Construtores privados para Singleton
    private DiContainer(String basePackage) {
        // O Reflections precisa de um pacote base para escanear. Se vazio, escaneia tudo.
        this.reflections = new Reflections(basePackage);
        initializeContainer();
    }

    // Inicialização do contêiner. Deve ser chamado uma vez no início da aplicação.
    public static void initialize(String basePackage) {
        if (INSTANCE == null) {
            synchronized (DiContainer.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DiContainer(basePackage);
                }
            }
        }
    }

    // Inicialização sem pacote base (escaneia todo o classpath)
    public static void initialize() {
        initialize("");
    }

    // Obtém a instância do contêiner. Lança IllegalStateException se não inicializado.
    public static DiContainer getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("DIContainer não inicializado. Chame DIContainer.initialize() primeiro.");
        }
        return INSTANCE;
    }

    // Inicialização interna do contêiner (chamado uma vez pelo construtor)
    private void initializeContainer() {
        log(Level.INFO, "Iniciando inicialização do DIContainer...");
        registerDefaultScopes();
        scanAndRegisterComponents();
        log(Level.INFO, "DIContainer inicializado com sucesso.");
    }

    // Registra os escopos padrão (Singleton e Thread)
    private void registerDefaultScopes() {
        registerScope(ScopeType.SINGLETON.getName(), new SingletonScope());
        registerScope(ScopeType.THREAD.getName(), new ThreadScope());
        log(Level.INFO, "Escopos padrão 'singleton' e 'thread' registrados.");
    }

    // Registra um escopo customizado
    public void registerScope(String name, ScopeHandler scope) {
        Objects.requireNonNull(name, "Nome do escopo não pode ser nulo.");
        Objects.requireNonNull(scope, "Manipulador de escopo não pode ser nulo.");
        if (scopes.containsKey(name)) {
            log(Level.WARNING, "Escopo '{0}' já registrado. Sobrescrevendo.", name);
        }
        scopes.put(name, scope);
        log(Level.FINE, "Escopo '{0}' registrado.", name);
    }

    // Escaneia classes anotadas com Component, Service, Repository, Controller e Configuration
    public void scanAndRegisterComponents() {
        log(Level.INFO, "Iniciando escaneamento de componentes...");

        Set<Class<?>> componentClasses = new HashSet<>();
        // Adiciona classes com anotações de componente
        componentClasses.addAll(reflections.getTypesAnnotatedWith(Component.class));
        componentClasses.addAll(reflections.getTypesAnnotatedWith(Service.class));
        componentClasses.addAll(reflections.getTypesAnnotatedWith(Repository.class));
        componentClasses.addAll(reflections.getTypesAnnotatedWith(Controller.class));

        // Registra cada componente encontrado
        componentClasses.forEach(this::registerComponent);
        log(Level.INFO, "Componentes anotados (@Component, @Service, etc.) registrados.");

        // Processa classes de configuração (@Configuration)
        reflections.getTypesAnnotatedWith(Configuration.class)
                .forEach(this::processConfigurationClass);
        log(Level.INFO, "Classes de configuração (@Configuration) processadas.");
    }

    // Registra um componente (classe anotada)
    private void registerComponent(Class<?> componentClass) {
        // Obtém o nome do bean da anotação (se houver), caso contrário, o nome simples da classe
        String beanName = getBeanName(componentClass);
        // Obtém o nome do escopo padrão ou da anotação @Scope
        String scopeName = getDefaultScopeName(componentClass); // Alterado para getDefaultScopeName

        // Registra a classe como sua própria implementação
        registerComponentClass(componentClass, beanName, scopeName);
        log(Level.FINE, "Componente '{0}' registrado como '{1}' com escopo '{2}'.",
                componentClass.getName(), beanName, scopeName);

        // Adiciona a própria classe ao mapa de todas as implementações rastreadas
        allRegisteredImplementations.computeIfAbsent(componentClass, k -> ConcurrentHashMap.newKeySet()).add(componentClass);

        // Se a classe tem @Primary, registra-a como a implementação primária para si mesma
        if (componentClass.isAnnotationPresent(Primary.class)) {
            registerPrimaryImplementation(componentClass, componentClass);
            log(Level.FINE, "Componente '{0}' marcado como @Primary para o tipo '{1}'.",
                    componentClass.getName(), componentClass.getName());
        }

        // Registra a classe por cada interface que ela implementa
        Arrays.stream(componentClass.getInterfaces())
                .forEach(iface -> {
                    // Adiciona a implementação ao mapa de todas as implementações para a interface
                    allRegisteredImplementations.computeIfAbsent(iface, k -> ConcurrentHashMap.newKeySet()).add(componentClass);

                    // Se a classe tem @Primary, registra-a como a implementação primária para a interface
                    if (componentClass.isAnnotationPresent(Primary.class)) {
                        registerPrimaryImplementation(iface, componentClass);
                        log(Level.FINE, "Componente '{0}' marcado como @Primary para a interface '{1}'.",
                                componentClass.getName(), iface.getName());
                    }
                });
    }

    // Retorna o nome do bean de uma classe, com base em anotações como @Component, @Service, etc.
    private String getBeanName(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Component.class)) {
            String name = clazz.getAnnotation(Component.class).value();
            return name.isEmpty() ? clazz.getSimpleName() : name;
        } else if (clazz.isAnnotationPresent(Service.class)) {
            String name = clazz.getAnnotation(Service.class).value();
            return name.isEmpty() ? clazz.getSimpleName() : name;
        } else if (clazz.isAnnotationPresent(Repository.class)) {
            String name = clazz.getAnnotation(Repository.class).value();
            return name.isEmpty() ? clazz.getSimpleName() : name;
        } else if (clazz.isAnnotationPresent(Controller.class)) {
            String name = clazz.getAnnotation(Controller.class).value();
            return name.isEmpty() ? clazz.getSimpleName() : name;
        }
        return clazz.getSimpleName(); // Padrão se nenhuma anotação específica for encontrada ou nome vazio
    }

    /**
     * Retorna o nome do escopo para uma classe (do @Scope ou padrão "singleton").
     * CONVERTIDO DE ScopeType PARA String.
     */
    private String getDefaultScopeName(Class<?> clazz) {
        if (clazz.isAnnotationPresent(ScopeAnnotation.class)) {
            return clazz.getAnnotation(ScopeAnnotation.class).value().getName(); // Pega o nome da enum
        }
        return ScopeType.SINGLETON.getName(); // Padrão: "singleton"
    }

    /**
     * Retorna o nome do escopo para um método @Bean (do @Scope ou padrão "singleton").
     * CONVERTIDO DE ScopeType PARA String.
     */
    private String getDefaultScopeName(Method method) {
        if (method.isAnnotationPresent(ScopeAnnotation.class)) {
            return method.getAnnotation(ScopeAnnotation.class).value().getName(); // Pega o nome da enum
        }
        return ScopeType.SINGLETON.getName(); // Padrão: "singleton"
    }

    // Registra uma implementação @Primary para um tipo
    private void registerPrimaryImplementation(Class<?> type, Class<?> implementation) {
        if (primaryImplementations.containsKey(type) && !primaryImplementations.get(type).equals(implementation)) {
            log(Level.SEVERE, "Erro: Múltiplos beans @Primary encontrados para o tipo '{0}': '{1}' e '{2}'.",
                    type.getName(), primaryImplementations.get(type).getName(), implementation.getName());
            throw new IllegalStateException("Múltiplos beans @Primary para o tipo: " + type.getName());
        }
        primaryImplementations.put(type, implementation);
    }

    // Processa classes de configuração (@Configuration)
    private void processConfigurationClass(Class<?> configClass) {
        log(Level.INFO, "Processando classe @Configuration: '{0}'.", configClass.getName());
        // Instancia a classe de configuração (ela própria é um singleton funcional)
        Object configInstance = createInstance(configClass); // Pode ter dependências injetáveis

        // Encontra e processa métodos @Bean
        Arrays.stream(configClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Bean.class))
                .forEach(method -> registerBeanMethod(configInstance, method));
    }

    // Registra um método anotado com @Bean
    private void registerBeanMethod(Object configInstance, Method method) {
        Bean beanAnnotation = method.getAnnotation(Bean.class);
        String beanName = beanAnnotation.name().isEmpty() ? method.getName() : beanAnnotation.name();
        Class<?> beanType = method.getReturnType();
        String scopeName = getDefaultScopeName(method); // Alterado para getDefaultScopeName

        method.setAccessible(true); // Permite invocar métodos privados/protegidos

        // Modificação aqui: Usar Supplier<Object> para maior flexibilidade
        Supplier<Object> beanSupplier = () -> { // Tipo de retorno do lambda é Object
            try {
                // Resolve os parâmetros do método @Bean (injeção por método)
                Object[] args = resolveMethodParameters(method);
                Object bean = method.invoke(configInstance, args);

                // Aplica @PostConstruct e injeção de campo/método se o bean precisar
                if (bean != null) {
                    performFieldAndMethodInjection(bean);
                    invokePostConstruct(bean);
                }
                return bean;
            } catch (Exception e) {
                log(Level.SEVERE, "Falha ao criar bean do método @Bean '" + beanName + "' em '" + method.getDeclaringClass().getName() + "." + method.getName() + "'.", e);
                throw new RuntimeException("Falha ao criar bean do método @Bean " + beanName, e);
            }
        };

        // Registra o supplier do bean no escopo apropriado
        ScopeHandler scopeHandler = getScopeHandler(scopeName);

        // A linha que causou o problema de inferência de tipo
        // Para resolver, vamos fazer um cast explícito para `Class<Object>` e `Supplier<Object>`
        // para satisfazer o compilador, já que `beanType` é `Class<?>` e `beanSupplier` é `Supplier<Object>`.
        strategies.put(beanType, container -> scopeHandler.get((Class<Object>) beanType, beanSupplier)); // CORREÇÃO AQUI

        namedBeans.put(beanName, beanSupplier); // Registra também pelo nome para resolveByName

        // Adiciona o tipo retornado pelo método @Bean ao mapa de todas as implementações rastreadas
        allRegisteredImplementations.computeIfAbsent(beanType, k -> ConcurrentHashMap.newKeySet()).add(beanType);

        // Se o método @Bean tem @Primary, registra-o como a implementação primária para o seu tipo
        if (method.isAnnotationPresent(Primary.class)) {
            registerPrimaryImplementation(beanType, beanType);
            log(Level.FINE, "@Bean '{0}' marcado como @Primary para o tipo '{1}'.", beanName, beanType.getName());
        }

        log(Level.FINE, "@Bean '{0}' (tipo: '{1}', escopo: '{2}') registrado.", beanName, beanType.getName(), scopeName);
    }

    // Registra uma classe de componente com um nome e escopo específicos
    public <T> void registerComponentClass(Class<T> type, String beanName, String scope) {
        Objects.requireNonNull(type, "Tipo não pode ser nulo.");
        Objects.requireNonNull(beanName, "Nome do bean não pode ser nulo.");
        Objects.requireNonNull(scope, "Escopo não pode ser nulo.");

        ScopeHandler scopeHandler = getScopeHandler(scope);

        // A estratégia de instanciação cria uma nova instância e a gerencia pelo escopo
        strategies.put(type, container -> scopeHandler.get(type, () -> container.createInstance(type)));
        // IMPORTANT: namedBeans precisa de um supplier que chame getBean para garantir que o escopo seja respeitado
        namedBeans.put(beanName, () -> getBean(type)); // Correção aqui: usa getBean(type) para respeitar o escopo

        // Garante que o tipo seja rastreado para injeção de coleções
        allRegisteredImplementations.computeIfAbsent(type, k -> ConcurrentHashMap.newKeySet()).add(type);

        notifyLifecycleEvent(type, "REGISTERED");
        log(Level.FINE, "Componente '{0}' registrado como '{1}' com escopo '{2}'.", type.getName(), beanName, scope);
    }

    // Obtém o manipulador de escopo ou lança uma exceção
    private ScopeHandler getScopeHandler(String scopeName) {
        return Optional.ofNullable(scopes.get(scopeName))
                .orElseThrow(() -> new IllegalArgumentException("Escopo desconhecido: " + scopeName));
    }

    // Métodos de registro de dependências programaticos (alternativa às anotações)
    public <T> void register(Class<T> type, Class<? extends T> implementation) {
        register(type, implementation, getDefaultScopeName(implementation)); // Alterado para getDefaultScopeName
    }

    public <T> void register(Class<T> type, Class<? extends T> implementation, String scope) {
        Objects.requireNonNull(type, "Tipo não pode ser nulo.");
        Objects.requireNonNull(implementation, "Implementação não pode ser nula.");
        Objects.requireNonNull(scope, "Escopo não pode ser nulo.");

        ScopeHandler scopeHandler = getScopeHandler(scope);

        strategies.put(type, container -> {
            try {
                return scopeHandler.get(type, () -> container.createInstance(implementation));
            } catch (Exception e) {
                String msg = String.format("Falha ao criar instância de '%s' para o tipo '%s' com escopo '%s'.",
                        implementation.getName(), type.getName(), scope);
                log(Level.SEVERE, msg, e);
                throw new RuntimeException("Falha ao criar instância de " + type.getName(), e);
            }
        });

        // Garante que o tipo e sua implementação sejam rastreados para injeção de coleções
        allRegisteredImplementations.computeIfAbsent(type, k -> ConcurrentHashMap.newKeySet()).add(implementation);
        allRegisteredImplementations.computeIfAbsent(implementation, k -> ConcurrentHashMap.newKeySet()).add(implementation);

        notifyLifecycleEvent(type, "REGISTERED");
        log(Level.INFO, "Tipo '{0}' -> Implementação '{1}' registrada com escopo '{2}'.",
                type.getName(), implementation.getName(), scope);
    }

    // Registra uma instância pré-existente como um singleton
    public <T> void register(Class<T> type, T instance) {
        Objects.requireNonNull(type, "Tipo não pode ser nulo.");
        Objects.requireNonNull(instance, "Instância não pode ser nula.");

        ScopeHandler singletonScope = scopes.get(ScopeType.SINGLETON.getName()); // Usar enum
        singletonScope.get(type, () -> instance); // Garante que a instância seja gerenciada pelo escopo
        strategies.put(type, container -> instance); // Estratégia direta para a instância fornecida
        allRegisteredImplementations.computeIfAbsent(type, k -> ConcurrentHashMap.newKeySet()).add(instance.getClass());
        allRegisteredImplementations.computeIfAbsent(instance.getClass(), k -> ConcurrentHashMap.newKeySet()).add(instance.getClass());

        notifyLifecycleEvent(type, "REGISTERED");
        log(Level.INFO, "Instância pré-existente de '{0}' registrada como singleton.", type.getName());
    }

    // Registra um Supplier para criação lazy de um bean (sempre singleton)
    public <T> void registerLazy(Class<T> type, Supplier<T> supplier) {
        Objects.requireNonNull(type, "Tipo não pode ser nulo.");
        Objects.requireNonNull(supplier, "Supplier não pode ser nulo.");

        ScopeHandler singletonScope = scopes.get(ScopeType.SINGLETON.getName()); // Usar enum
        strategies.put(type, container -> singletonScope.get(type, supplier));
        allRegisteredImplementations.computeIfAbsent(type, k -> ConcurrentHashMap.newKeySet()).add(type);
        notifyLifecycleEvent(type, "REGISTERED");
        log(Level.INFO, "Tipo '{0}' registrado com lazy initialization (singleton).", type.getName());
    }

    // Registra uma implementação para um tipo com um qualificador específico
    public <T> void registerQualified(Class<T> type, Class<? extends T> implementation, String qualifier) {
        Objects.requireNonNull(type, "Tipo não pode ser nulo.");
        Objects.requireNonNull(implementation, "Implementação não pode ser nula.");
        Objects.requireNonNull(qualifier, "Qualificador não pode ser nulo.");

        qualifiers.computeIfAbsent(type, k -> new ConcurrentHashMap<>())
                .put(qualifier, implementation);
        // Reusa o método register para adicionar a estratégia e rastrear implementações
        register(type, implementation); // Isso registrará a implementação com o escopo padrão
        log(Level.INFO, "Tipo '{0}' registrado com implementação '{1}' e qualificador '{2}'.",
                type.getName(), implementation.getName(), qualifier);
    }

    // Resolução de dependências (API pública para obter beans)
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type) {
        return resolve(type, null);
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(String name) {
        return (T) resolveByName(name);
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type, String qualifier) {
        return resolve(type, qualifier);
    }

    // Métodos internos de resolução
    @SuppressWarnings("unchecked")
    private <T> T resolve(Class<T> type, String qualifier) {
        Objects.requireNonNull(type, "Tipo não pode ser nulo.");

        Set<Class<?>> stack = resolutionStack.get();
        if (stack.contains(type)) {
            log(Level.SEVERE, "Dependência circular detectada ao resolver: {0}. Stack: {1}",
                    type.getName(), stack);
            throw new CircularDependencyException("Dependência circular detectada para " + type.getName());
        }

        try {
            stack.add(type); // Adiciona ao stack para detecção de circularidade

            // Previne a resolução direta de coleções, que devem ser injetadas
            if (Collection.class.isAssignableFrom(type) && type.isInterface()) {
                log(Level.WARNING, "Tentativa de resolver coleção '{0}' diretamente com resolve(Class). Use @Inject em campos/construtores/métodos para injeção de coleções.", type.getName());
                throw new DependencyNotRegisteredException("Injeção de coleção deve ser tratada pelo mecanismo de injeção de campo/parâmetro, não por resolve() direto.");
            }

            Class<?> implementationToUse = null;

            if (qualifier != null && !qualifier.isEmpty()) {
                // Tenta resolver com qualificador
                Map<String, Class<?>> qualifiedImplementations = qualifiers.get(type);
                if (qualifiedImplementations != null && qualifiedImplementations.containsKey(qualifier)) {
                    implementationToUse = qualifiedImplementations.get(qualifier);
                } else {
                    log(Level.WARNING, "Qualificador '{0}' não encontrado para o tipo '{1}'.",
                            qualifier, type.getName());
                    throw new DependencyNotRegisteredException("Qualificador não encontrado: " + qualifier + " para tipo " + type.getName());
                }
            } else {
                // Sem qualificador, tenta @Primary ou única implementação
                if (primaryImplementations.containsKey(type)) {
                    implementationToUse = primaryImplementations.get(type);
                    log(Level.FINEST, "Resolvendo '{0}' usando implementação @Primary: '{1}'.",
                            type.getName(), implementationToUse.getName());
                } else {
                    Set<Class<?>> availableImplementations = allRegisteredImplementations.get(type);
                    if (availableImplementations != null && !availableImplementations.isEmpty()) {
                        // Filtra para remover a própria interface se ela foi erroneamente adicionada como implementação única
                        Set<Class<?>> concreteImplementations = availableImplementations.stream()
                                .filter(impl -> !impl.isInterface() && !Modifier.isAbstract(impl.getModifiers()))
                                .collect(Collectors.toSet());

                        if (concreteImplementations.size() == 1) {
                            implementationToUse = concreteImplementations.iterator().next();
                            log(Level.FINEST, "Resolvendo '{0}' com única implementação concreta disponível: '{1}'.",
                                    type.getName(), implementationToUse.getName());
                        } else if (concreteImplementations.size() > 1) {
                            log(Level.WARNING, "Múltiplas implementações concretas encontradas para '{0}' sem qualificador ou @Primary. Implementações: {1}",
                                    type.getName(), concreteImplementations.stream().map(Class::getName).collect(Collectors.joining(", ")));
                            throw new DependencyNotRegisteredException("Múltiplas implementações para " + type.getName() + " sem qualificador ou @Primary. Use @Qualifier ou @Primary.");
                        } else {
                            // Se não há implementações concretas registradas, mas o tipo é concreto
                            if (!type.isInterface() && !Modifier.isAbstract(type.getModifiers())) {
                                implementationToUse = type;
                            } else {
                                log(Level.SEVERE, "Nenhuma implementação concreta encontrada para o tipo '{0}'.", type.getName());
                                throw new DependencyNotRegisteredException("Nenhuma implementação concreta encontrada para " + type.getName());
                            }
                        }
                    } else {
                        // Se não há implementações rastreadas, mas o próprio tipo é concreto, tenta instanciá-lo
                        if (!type.isInterface() && !Modifier.isAbstract(type.getModifiers())) {
                            implementationToUse = type;
                        } else {
                            log(Level.SEVERE, "Nenhuma implementação ou bean registrado para o tipo: '{0}'.", type.getName());
                            throw new DependencyNotRegisteredException("Nenhuma implementação ou bean registrado para o tipo: " + type.getName());
                        }
                    }
                }
            }

            // Garante que implementationToUse não seja nulo antes de buscar a estratégia
            if (implementationToUse == null) {
                log(Level.SEVERE, "Não foi possível determinar a implementação para o tipo '{0}'.", type.getName());
                throw new DependencyNotRegisteredException("Não foi possível determinar a implementação para o tipo: " + type.getName());
            }

            // Obtém a estratégia de instanciação para a implementação escolhida
            InstantiationStrategy<T> strategy = (InstantiationStrategy<T>) strategies.get(implementationToUse);
            if (strategy == null) {
                // Caso a implementação não tenha uma estratégia direta (pode ser um @Component não explicitamente registrado para a interface)
                // Tentamos registrá-la agora como se fosse um componente
                if (!implementationToUse.isInterface() && !Modifier.isAbstract(implementationToUse.getModifiers())) {
                    log(Level.FINE, "Estratégia para '{0}' não encontrada. Tentando registrar como componente.", implementationToUse.getName());
                    registerComponent(implementationToUse); // Registra dinamicamente se ainda não estiver
                    strategy = (InstantiationStrategy<T>) strategies.get(implementationToUse); // Tenta novamente
                    if (strategy == null) {
                        throw new DependencyNotRegisteredException("Dependência não registrada e não pôde ser automaticamente registrada como componente: " + implementationToUse.getName());
                    }
                } else {
                    throw new DependencyNotRegisteredException("Dependência não registrada: " + type.getName());
                }
            }

            T instance = strategy.getInstance(this); // Obtém a instância usando a estratégia
            notifyLifecycleEvent(type, "RESOLVED");
            log(Level.FINEST, "Instância de '{0}' (qualificador: {1}) resolvida.",
                    type.getName(), qualifier != null ? qualifier : "nenhum");
            return instance;
        } finally {
            stack.remove(type); // Remove do stack ao sair
            if (stack.isEmpty()) {
                resolutionStack.remove(); // Limpa o ThreadLocal se a pilha estiver vazia
            }
        }
    }

    // Resolve um bean pelo nome
    private Object resolveByName(String name) {
        Objects.requireNonNull(name, "Nome do bean não pode ser nulo.");
        Supplier<Object> supplier = namedBeans.get(name);
        if (supplier == null) {
            throw new DependencyNotRegisteredException("Bean nomeado '" + name + "' não encontrado.");
        }
        // O supplier em namedBeans já deve chamar getBean(type) para respeitar o escopo.
        // Se o supplier foi registrado diretamente, ele deve ter a lógica de escopo.
        return supplier.get();
    }

    // Cria uma nova instância de uma classe (usada para @Component classes, não para @Bean methods)
    <T> T createInstance(Class<T> type) {
        log(Level.FINE, "Criando nova instância de '{0}'.", type.getName());
        try {
            // Encontra o construtor injetável (anotado com @Inject ou único)
            Constructor<?> injectConstructor = findInjectableConstructor(type);
            // Resolve os parâmetros do construtor
            Object[] args = resolveConstructorParameters(injectConstructor);

            injectConstructor.setAccessible(true); // Permite acesso a construtores privados/protegidos
            T instance = type.cast(injectConstructor.newInstance(args));

            // Realiza a injeção de campos e métodos
            performFieldAndMethodInjection(instance);
            // Invoca métodos anotados com @PostConstruct
            invokePostConstruct(instance);

            notifyLifecycleEvent(type, "CREATED");
            log(Level.FINE, "Instância de '{0}' criada e dependências injetadas.", type.getName());
            return instance;
        } catch (Exception e) {
            String msg = String.format("Falha ao criar instância de '%s'.", type.getName());
            log(Level.SEVERE, msg, e);
            throw new RuntimeException("Falha ao criar instância de " + type.getName(), e);
        }
    }

    // Encontra o construtor injetável para uma classe (cacheado)
    private Constructor<?> findInjectableConstructor(Class<?> type) {
        return injectableConstructorsCache.computeIfAbsent(type, t -> {
            Constructor<?>[] constructors = t.getDeclaredConstructors();
            Constructor<?> autowireCtor = null;
            for (Constructor<?> constructor : constructors) {
                if (constructor.isAnnotationPresent(Inject.class)) {
                    if (autowireCtor != null) {
                        throw new RuntimeException("Múltiplos construtores @Inject encontrados para bean: " + t.getName());
                    }
                    autowireCtor = constructor;
                }
            }
            if (autowireCtor != null) {
                log(Level.FINEST, "Construtor @Inject encontrado para '{0}'.", t.getName());
                return autowireCtor;
            }
            // Se não houver @Inject, tenta o construtor padrão (sem argumentos)
            try {
                return t.getDeclaredConstructor();
            } catch (NoSuchMethodException e) {
                // Se não há construtor padrão e há apenas um construtor, use-o
                if (constructors.length == 1) {
                    log(Level.FINE, "Nenhum construtor @Inject ou padrão. Usando o único construtor disponível para '{0}'.", t.getName());
                    return constructors[0];
                }
                log(Level.SEVERE, "Nenhum construtor adequado (com @Inject ou padrão) encontrado para '{0}'.", t.getName());
                throw new RuntimeException("Nenhum construtor adequado encontrado para " + type.getName());
            }
        });
    }

    // Resolve os parâmetros de um construtor
    private Object[] resolveConstructorParameters(Constructor<?> constructor) {
        return Arrays.stream(constructor.getParameters())
                .map(this::resolveParameter)
                .toArray();
    }

    // Resolve os parâmetros de um método (usado para métodos @Bean e @Inject methods)
    private Object[] resolveMethodParameters(Method method) {
        return Arrays.stream(method.getParameters())
                .map(this::resolveParameter)
                .toArray();
    }

    // Resolve um único parâmetro (para construtor ou método)
    private Object resolveParameter(Parameter parameter) {
        Class<?> paramType = parameter.getType();
        Type genericParamType = parameter.getParameterizedType();
        String qualifier = Optional.ofNullable(parameter.getAnnotation(Qualifier.class))
                .map(Qualifier::value)
                .orElse(null);

        // Tratamento especial para injeção de coleções (List, Set)
        if (Collection.class.isAssignableFrom(paramType)) {
            return resolveCollection(genericParamType);
        }

        // Se houver um qualificador
        if (qualifier != null && !qualifier.isEmpty()) {
            return getBean(paramType, qualifier);
        }

        // Se o parâmetro é uma interface ou tipo abstrato e não tem qualificador
        if (paramType.isInterface() || Modifier.isAbstract(paramType.getModifiers())) {
            // Tenta obter a implementação primária ou única registrada
            Set<Class<?>> implementations = allRegisteredImplementations.get(paramType);
            if (implementations != null && !implementations.isEmpty()) {
                Set<Class<?>> concreteImplementations = implementations.stream()
                        .filter(impl -> !impl.isInterface() && !Modifier.isAbstract(impl.getModifiers()))
                        .collect(Collectors.toSet());

                if (concreteImplementations.size() == 1) {
                    return getBean(concreteImplementations.iterator().next());
                } else if (concreteImplementations.size() > 1) {
                    // Tenta resolver com @Primary se houver ambiguidade
                    if (primaryImplementations.containsKey(paramType)) {
                        return getBean(primaryImplementations.get(paramType));
                    }
                    throw new DependencyNotRegisteredException(
                            "Múltiplas implementações para " + paramType.getName() + " sem qualificador ou @Primary. Implementações: " +
                                    concreteImplementations.stream().map(Class::getName).collect(Collectors.joining(", ")));
                } else {
                    throw new DependencyNotRegisteredException("Nenhuma implementação concreta encontrada para " + paramType.getName());
                }
            } else {
                // Se não há implementações registradas para a interface/abstrata
                throw new DependencyNotRegisteredException("Nenhuma implementação registrada para a interface/tipo abstrato: " + paramType.getName());
            }
        }
        // Para tipos concretos e sem qualificador, tenta obter diretamente
        return getBean(paramType);
    }

    // Resolve uma coleção (List ou Set) de implementações para um tipo genérico
    private Object resolveCollection(Type collectionType) {
        if (!(collectionType instanceof ParameterizedType)) {
            throw new IllegalArgumentException("Coleção para injeção deve ter um tipo genérico (e.g., List<MyInterface>). Tipo: " + collectionType.getTypeName());
        }

        ParameterizedType parameterizedType = (ParameterizedType) collectionType;
        Type rawType = parameterizedType.getRawType();
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

        if (actualTypeArguments.length != 1) {
            throw new IllegalArgumentException("Coleção deve ter exatamente um tipo de argumento genérico. Tipo: " + collectionType.getTypeName());
        }

        // Obtém o tipo genérico da coleção (e.g., MyInterface em List<MyInterface>)
        Class<?> genericType = (Class<?>) actualTypeArguments[0];

        Set<Class<?>> implementations = allRegisteredImplementations.get(genericType);
        if (implementations == null || implementations.isEmpty()) {
            log(Level.WARNING, "Nenhuma implementação encontrada para o tipo genérico '{0}' da coleção. Retornando coleção vazia.", genericType.getName());
            if (List.class.isAssignableFrom((Class<?>) rawType)) {
                return new ArrayList<>();
            } else if (Set.class.isAssignableFrom((Class<?>) rawType)) {
                return new HashSet<>();
            }
            return null; // Caso de tipo de coleção não suportado
        }

        // Filtra para obter apenas implementações concretas e válidas
        Collection<Object> resultCollection;
        if (List.class.isAssignableFrom((Class<?>) rawType)) {
            resultCollection = new ArrayList<>();
        } else if (Set.class.isAssignableFrom((Class<?>) rawType)) {
            resultCollection = new HashSet<>();
        } else {
            throw new IllegalArgumentException("Tipo de coleção não suportado para injeção: " + rawType.getTypeName() + ". Apenas List e Set são suportados.");
        }

        for (Class<?> implClass : implementations) {
            // Garante que é uma classe concreta e não a própria interface/tipo abstrato
            if (!implClass.isInterface() && !Modifier.isAbstract(implClass.getModifiers())) {
                try {
                    resultCollection.add(getBean(implClass)); // Resolve cada implementação
                } catch (Exception e) {
                    log(Level.WARNING, "Falha ao resolver instância de '{0}' para injeção de coleção. Ignorando esta implementação.", implClass.getName());
                }
            }
        }

        return resultCollection;
    }

    // Realiza injeção em campos (@Inject) e métodos set/init (@Inject)
    private void performFieldAndMethodInjection(Object instance) throws IllegalAccessException, InvocationTargetException {
        Class<?> clazz = instance.getClass();

        // Injeção de campo
        List<Field> fields = injectableFieldsCache.computeIfAbsent(clazz, this::findInjectableFields);
        for (Field field : fields) {
            field.setAccessible(true);
            Object dependency = resolveParameter(field.toGenericString(), field.getType(), field.getGenericType(), field.getAnnotation(Qualifier.class));
            field.set(instance, dependency);
            log(Level.FINEST, "Campo '{0}' injetado em '{1}'.", field.getName(), clazz.getName());
        }

        // Injeção de método
        List<Method> methods = injectableMethodsCache.computeIfAbsent(clazz, this::findInjectableMethods);
        for (Method method : methods) {
            method.setAccessible(true);
            Object[] args = resolveMethodParameters(method);
            method.invoke(instance, args);
            log(Level.FINEST, "Método '{0}' injetado em '{1}'.", method.getName(), clazz.getName());
        }
    }

    // Método auxiliar para resolver dependência para um campo, considerando qualificadores
    private Object resolveParameter(String debugName, Class<?> paramType, Type genericParamType, Qualifier qualifierAnnotation) {
        String qualifier = Optional.ofNullable(qualifierAnnotation).map(Qualifier::value).orElse(null);

        // Tratamento para List/Set<T> em campos
        if (Collection.class.isAssignableFrom(paramType)) {
            return resolveCollection(genericParamType);
        }

        if (qualifier != null && !qualifier.isEmpty()) {
            return getBean(paramType, qualifier);
        }

        // Lógica para interfaces/abstratas e tipos concretos sem qualificador
        if (paramType.isInterface() || Modifier.isAbstract(paramType.getModifiers())) {
            Set<Class<?>> implementations = allRegisteredImplementations.get(paramType);
            if (implementations != null && !implementations.isEmpty()) {
                Set<Class<?>> concreteImplementations = implementations.stream()
                        .filter(impl -> !impl.isInterface() && !Modifier.isAbstract(impl.getModifiers()))
                        .collect(Collectors.toSet());

                if (concreteImplementations.size() == 1) {
                    return getBean(concreteImplementations.iterator().next());
                } else if (concreteImplementations.size() > 1) {
                    if (primaryImplementations.containsKey(paramType)) {
                        return getBean(primaryImplementations.get(paramType));
                    }
                    throw new DependencyNotRegisteredException(
                            "Múltiplas implementações para " + paramType.getName() + " sem qualificador ou @Primary para injeção de campo/método: " + debugName + ". Implementações: " +
                                    concreteImplementations.stream().map(Class::getName).collect(Collectors.joining(", ")));
                } else {
                    throw new DependencyNotRegisteredException("Nenhuma implementação concreta encontrada para " + paramType.getName() + " para injeção de campo/método: " + debugName);
                }
            } else {
                throw new DependencyNotRegisteredException("Nenhuma implementação registrada para a interface/tipo abstrato: " + paramType.getName() + " para injeção de campo/método: " + debugName);
            }
        }
        return getBean(paramType); // Para tipos concretos
    }

    // Encontra campos anotados com @Inject (cacheado)
    private List<Field> findInjectableFields(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Inject.class))
                .collect(Collectors.toList());
    }

    // Encontra métodos anotados com @Inject (cacheado)
    private List<Method> findInjectableMethods(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Inject.class) && method.getParameterCount() > 0) // Métodos injetáveis devem ter parâmetros
                .collect(Collectors.toList());
    }

    // Invoca métodos anotados com @PostConstruct (cacheado)
    private void invokePostConstruct(Object instance) throws InvocationTargetException, IllegalAccessException {
        List<Method> postConstructMethods = postConstructMethodsCache.computeIfAbsent(instance.getClass(), clazz ->
                Arrays.stream(clazz.getDeclaredMethods())
                        .filter(method -> method.isAnnotationPresent(PostConstruct.class) && method.getParameterCount() == 0)
                        .collect(Collectors.toList())
        );

        for (Method method : postConstructMethods) {
            method.setAccessible(true);
            method.invoke(instance);
            log(Level.FINEST, "@PostConstruct método '{0}' invocado em '{1}'.", method.getName(), instance.getClass().getName());
        }
    }

    // Métodos para gerenciar listeners de ciclo de vida
    public void addLifecycleListener(DependencyLifecycleListener listener) {
        lifecycleListeners.add(listener);
        log(Level.FINE, "LifecycleListener adicionado: '{0}'.", listener.getClass().getName());
    }

    public void removeLifecycleListener(DependencyLifecycleListener listener) {
        lifecycleListeners.remove(listener);
        log(Level.FINE, "LifecycleListener removido: '{0}'.", listener.getClass().getName());
    }

    private void notifyLifecycleEvent(Class<?> type, String event) {
        for (DependencyLifecycleListener listener : lifecycleListeners) {
            listener.onEvent(type, event);
        }
    }

    // Método de fechamento do contêiner para invocar @PreDestroy
    public void close() {
        log(Level.INFO, "Fechando DIContainer. Invocando métodos @PreDestroy...");
        // Obtém todas as instâncias de singletons ativas
        SingletonScope singletonScope = (SingletonScope) scopes.get(ScopeType.SINGLETON.getName());
        if (singletonScope != null) {
            singletonScope.getAllInstances().values().forEach(this::invokePreDestroy); // USAR .values() para iterar sobre as instâncias
        }

        // Limpa todos os caches e mapas
        scopes.clear();
        strategies.clear();
        qualifiers.clear();
        namedBeans.clear();
        lifecycleListeners.clear();
        primaryImplementations.clear();
        allRegisteredImplementations.clear();
        injectableConstructorsCache.clear();
        injectableFieldsCache.clear();
        injectableMethodsCache.clear();
        postConstructMethodsCache.clear();
        preDestroyMethodsCache.clear();
        resolutionStack.remove(); // Limpa ThreadLocal
        INSTANCE = null; // Zera a instância singleton
        log(Level.INFO, "DIContainer fechado e recursos liberados.");
    }

    // Invoca métodos anotados com @PreDestroy (cacheado)
    private void invokePreDestroy(Object instance) {
        if (instance == null) return;

        List<Method> preDestroyMethods = preDestroyMethodsCache.computeIfAbsent(instance.getClass(), clazz ->
                Arrays.stream(clazz.getDeclaredMethods())
                        .filter(method -> method.isAnnotationPresent(PreDestroy.class) && method.getParameterCount() == 0)
                        .collect(Collectors.toList())
        );

        for (Method method : preDestroyMethods) {
            try {
                method.setAccessible(true);
                method.invoke(instance);
                log(Level.FINEST, "@PreDestroy método '{0}' invocado em '{1}'.", method.getName(), instance.getClass().getName());
            } catch (Exception e) {
                log(Level.SEVERE, "Falha ao invocar @PreDestroy método '{0}' em '{1}'.", method.getName(), instance.getClass().getName(), e);
            }
        }
    }

    // Método de log simples
    private void log(Level level, String msg, Object... params) {
        LOGGER.log(level, msg, params);
    }

    // Interface funcional para a estratégia de instanciação
    @FunctionalInterface
    private interface InstantiationStrategy<T> {
        T getInstance(DiContainer container);
    }
}