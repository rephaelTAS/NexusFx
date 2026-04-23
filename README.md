<div align="center">
  <h1>🚀 NexusFX Framework</h1>
  <p><strong>Framework JavaFX moderno para aplicações empresariais com Design System integrado</strong></p>

  <!-- Badges -->
  <p>
    <img src="https://img.shields.io/badge/Java-25-%23ED8B00?style=flat-square&logo=openjdk&logoColor=white" alt="Java 21">
    <img src="https://img.shields.io/badge/JavaFX-25.0.2-%23FF6B6B?style=flat-square&logo=java&logoColor=white" alt="JavaFX 21">
    <img src="https://img.shields.io/badge/License-MIT-green?style=flat-square" alt="License MIT">
    <img src="https://img.shields.io/badge/version-1.0.3-blue?style=flat-square" alt="Version">
    <img src="https://img.shields.io/badge/JPMS-modular-blueviolet?style=flat-square" alt="JPMS">
  </p>
</div>

---

## 📋 Sumário

- [Sobre o Projeto](#-sobre-o-projeto)
- [Funcionalidades](#-funcionalidades)
- [Tecnologias](#-tecnologias)
- [Instalação](#-instalação)
   - [Pré-requisitos](#pré-requisitos)
   - [Configuração do GitHub Packages](#configuração-do-github-packages)
   - [Maven](#maven)
   - [Gradle](#gradle)
- [Primeiros Passos](#-primeiros-passos)
- [Guia de Uso nos FXML](#-guia-de-uso-nos-fxml)
   - [Cards](#-cards)
   - [Botões](#-botões)
   - [Containers](#-containers--panels)
   - [Textos e Headings](#-textos-e-headings)
   - [Cores de Texto (Status)](#-cores-de-texto-status)
   - [Tabelas](#-tabelas-executions)
   - [Campos de Pesquisa](#-campos-de-pesquisa)
   - [Árvore de Diretórios](#-árvore-de-diretórios-system-managements)
   - [Divisórias](#-divisórias)
- [Design System](#-design-system)
- [Arquitetura](#-arquitetura)
- [API Reference](#-api-reference)
- [Exemplos Completos](#-exemplos-completos)
- [Contribuição](#-contribuição)
- [Licença](#-licença)
- [Contato](#-contato)

---

## 🎯 Sobre o Projeto

NexusFX é um framework empresarial construído sobre o JavaFX que oferece uma base sólida e moderna para o desenvolvimento de aplicações desktop. Ele combina um design system consistente (Neumorphic Design), injeção de dependências, classloader awareness para ambiente modular JPMS e uma arquitetura limpa para maximizar a produtividade sem comprometer a qualidade e manutenibilidade do código.

**Por que NexusFX?**
- ✅ **Produtividade**: Reduza o tempo de desenvolvimento com componentes prontos e API fluente
- ✅ **Consistência**: Design system unificado em toda aplicação com CSS classes programadas
- ✅ **Manutenibilidade**: Arquitetura limpa com ResourceRegistry único e ClassLoader-aware
- ✅ **Escalabilidade**: Pronto para projetos de qualquer tamanho, do simples ao empresarial
- ✅ **JPMS Ready**: Total suporte ao Java Platform Module System

---

## ✨ Funcionalidades

### 🎨 **UI/UX Completo com Neumorphic Design**

| Funcionalidade | Descrição |
|----------------|-----------|
| **FXML Integration** | Carregamento inteligente de views com ClassLoader próprio |
| **CSS Programado** | Design System via CSS classes (70% CSS, 30% Java) |
| **ResourceRegistry** | Ponto único de registro para views e imagens |
| **ClassLoader Awareness** | Cada recurso sabe seu ClassLoader (framework vs aplicação) |
| **LoadedView\<T\>** | Retorno tipado com métodos fluentes |

### 🔔 **Sistema de Alertas e Confirmações**

```java
// Alertas simples
NexusFX.alerts().info("Título", "Descrição", "Origem");

// Com detalhes
NexusFX.alerts().warn("Aviso", "Mensagem", "Detalhes técnicos", "Service");

// Confirmações
NexusFX.alerts().confirm("Deseja salvar?", "Confirmação", result -> {
    if (result) {
        // Usuário confirmou
    }
});

// Confirmação de perigo
NexusFX.alerts().confirmDanger("Excluir永久?", "Esta ação é irreversível", "Atenção", result -> {
    if (result) {
        // Excluir
    }
});
🖼️ Gerenciamento de Imagens
java
// Carregar em componentes
NexusFX.images().load(primaryStage, "app.logo");
NexusFX.images().load(avatarImageView, "user.avatar", 50, 50);
NexusFX.images().load(userLabel, "menu.icon", 16);

// Obter imagem diretamente
Image logo = NexusFX.images().loadImage("app.logo");

// Criar ImageView
ImageView icon = NexusFX.images().createImageView("menu.icon", 24, 24);
💉 Injeção de Dependências
java
@Controller
public class UserController {

    @Inject
    private UserService userService;
    
    @FXML
    private void handleSave() {
        userService.save(currentUser);
    }
}
🎯 Views com LoadedView<T>
java
// Sem tipo específico (controller como Object)
LoadedView<Object> loginView = NexusFX.views().loadView(ViewConstant.Main.LOGIN);
Parent root = loginView.getRoot();
Object controller = loginView.getController();

// Com tipo específico (recomendado)
LoadedView<LoginController> loginView = NexusFX.views().loadView(
    ViewConstant.Main.LOGIN, 
    LoginController.class
);
LoginController ctrl = loginView.getController();
ctrl.fazerLogin();

// Configuração fluente
loginView.configure(LoginController::iniciar)
         .addCssClass("dark-theme")
         .addCssClass("rounded");
📦 Resource Registry com ClassLoader Awareness
java
// Registro de recursos do framework (interno)
NexusFX.internalRegistry().registerView(
    "internal-dialog",
    "/com/ossobo/nexusfx/fxml/dialog.fxml",
    ViewDescriptor.ViewType.DYNAMIC,
    InternalController.class
);

// Registro de recursos da aplicação (externo)
NexusFX.externalRegistry().registerView(
    "login",
    "/fxml/login.fxml",
    ViewDescriptor.ViewType.DYNAMIC,
    LoginController.class
);

NexusFX.externalRegistry().registerImage("app.logo", "/images/logo.png");

// Registro em massa
NexusFX.externalRegistry().registerImages(
    NexusFX.appImage("user.avatar", "/images/avatar.png"),
    NexusFX.appImage("menu.icon", "/images/menu.png")
);
🎭 Diálogos Modais
java
// Diálogo simples
NexusFX.dialogs().open("user.edit", "Editar Usuário");

// Com configurador de controller
NexusFX.dialogs().openWithController(
    "user.edit", 
    "Editar",
    (UserController ctrl) -> ctrl.setUser(selectedUser)
);

// Com retorno
Optional<User> result = NexusFX.dialogs().openForResult(
    "user.select",
    "Selecionar Usuário",
    ctrl -> ctrl.loadUsers(),
    UserSelectController::getSelectedUser
);
🔍 Diagnóstico
java
// Diagnóstico completo
NexusFX.diagnostics().system();

// Diagnóstico de ClassLoaders
NexusFX.diagnoseClassLoaders();

// Diagnóstico de recursos
NexusFX.diagnoseResource("/fxml/login.fxml");
NexusFX.diagnostics().view("login");
NexusFX.diagnostics().image("app.logo");
🛠 Tecnologias
Tecnologia	Versão	Finalidade
Java	21+	Linguagem base
JavaFX	21.0.10+	UI Framework
Reflections	0.10.2	Auto-discovery
SLF4J	2.0.16	Logging
JUnit	5.10.0	Testes
📦 Instalação
Pré-requisitos
Requisito	Versão Mínima
JDK	21
JavaFX	21.0.10
Maven	3.8+
Gradle	7.0+
Configuração do GitHub Packages
Configure o token de acesso

Crie/edite ~/.m2/settings.xml:

xml
<settings>
  <servers>
    <server>
      <id>github</id>
      <username>rephaelTAS</username>
      <password>SEU_TOKEN_DE_ACESSO_PESSOAL</password>
    </server>
  </servers>
</settings>
🔑 Gerando um token:
GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)

Escopos necessários: read:packages e write:packages

Maven
Adicione ao seu pom.xml:

xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/rephaelTAS/NexusFX</url>
    </repository>
</repositories>

<dependencies>
    <!-- NexusFX Framework -->
    <dependency>
        <groupId>com.ossobo</groupId>
        <artifactId>NexusFX</artifactId>
        <version>1.0.0</version>
    </dependency>

    <!-- JavaFX (escopo provided se já tiver na aplicação) -->
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>21.0.10</version>
    </dependency>
    
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>21.0.10</version>
    </dependency>
</dependencies>
Gradle
Adicione ao seu build.gradle:

gradle
repositories {
    mavenCentral()
    maven {
        url = "https://maven.pkg.github.com/rephaelTAS/NexusFX"
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation 'com.ossobo:NexusFX:1.0.0'
    implementation 'org.openjfx:javafx-controls:21.0.10'
    implementation 'org.openjfx:javafx-fxml:21.0.10'
}
Para Gradle Kotlin DSL (build.gradle.kts):

kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/rephaelTAS/NexusFX")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("com.ossobo:NexusFX:1.0.0")
    implementation("org.openjfx:javafx-controls:21.0.10")
    implementation("org.openjfx:javafx-fxml:21.0.10")
}
🚀 Primeiros Passos
1. Inicialize o NexusFX no start() da sua Application
java
package com.minhaempresa;

import com.ossobo.nexusfx.NexusFX;
import com.ossobo.nexusfx.desingn.views.LoadedView;
import com.ossobo.nexusfx.desingn.views.config.ViewDescriptor;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 1. INICIALIZAR NEXUSFX (OBRIGATÓRIO!)
        NexusFX.initialize(primaryStage, getClass(), "com.minhaempresa");
        
        // 2. REGISTRAR RECURSOS DA APLICAÇÃO
        NexusFX.externalRegistry().registerView(
            "login",
            "/fxml/login.fxml",
            ViewDescriptor.ViewType.DYNAMIC,
            LoginController.class
        );
        
        NexusFX.externalRegistry().registerImage("app.logo", "/images/logo.png");
        
        // 3. CARREGAR VIEW PRINCIPAL
        LoadedView<LoginController> loginView = NexusFX.views().loadView(
            "login", 
            LoginController.class
        );
        
        // 4. CONFIGURAR STAGE
        Parent root = loginView.getRoot();
        primaryStage.setTitle("Sistema de Gestão");
        primaryStage.setScene(new Scene(root, 1024, 768));
        NexusFX.images().load(primaryStage, "app.logo");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
2. Crie um Controller
java
package com.minhaempresa;

import com.ossobo.nexusfx.di.annotations.Controller;
import com.ossobo.nexusfx.di.annotations.Inject;
import com.ossobo.nexusfx.NexusFX;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

@Controller
public class LoginController {

    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @Inject
    private AuthService authService;
    
    @FXML
    private void initialize() {
        // Configurações iniciais
        System.out.println("LoginController inicializado");
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        if (authService.authenticate(username, password)) {
            NexusFX.alerts().info(
                "Sucesso", 
                "Login realizado com sucesso!", 
                "Bem-vindo " + username, 
                "LoginController"
            );
            
            // Navegar para dashboard
            LoadedView<DashboardController> dashboardView = NexusFX.views().loadView(
                "dashboard", 
                DashboardController.class
            );
            
            NexusFX.applyStyles(dashboardView.getRoot(), "dashboard");
            NexusFX.primaryStage().getScene().setRoot(dashboardView.getRoot());
            
        } else {
            NexusFX.alerts().error(
                "Erro", 
                "Credenciais inválidas", 
                "Verifique usuário e senha", 
                "LoginController"
            );
        }
    }
}
3. Crie o arquivo FXML (com CSS classes do Design System)
xml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>

<VBox xmlns="http://javafx.com/javafx"
      xmlns:fx="http://javafx.com/fxml"
      fx:controller="com.minhaempresa.LoginController"
      spacing="20"
      alignment="CENTER"
      styleClass="background-main">

    <!-- Logo -->
    <VBox alignment="CENTER" spacing="10">
        <Label text="NexusFX" styleClass="heading-1"/>
        <Label text="Faça login para continuar" styleClass="body, text-secondary"/>
    </VBox>
    
    <!-- Card de Login -->
    <VBox spacing="15" 
          maxWidth="400"
          styleClass="neumorphic-card">
        
        <!-- Campo Usuário -->
        <VBox spacing="5">
            <Label text="Usuário" styleClass="body-small, text-secondary"/>
            <TextField fx:id="usernameField" 
                       promptText="Digite seu usuário"
                       styleClass="search-field"/>
        </VBox>
        
        <!-- Campo Senha -->
        <VBox spacing="5">
            <Label text="Senha" styleClass="body-small, text-secondary"/>
            <PasswordField fx:id="passwordField" 
                           promptText="••••••••"
                           styleClass="search-field"/>
        </VBox>
        
        <Region styleClass="divider" prefHeight="1"/>
        
        <!-- Botões -->
        <HBox spacing="10" alignment="CENTER_RIGHT">
            <Button text="Cancelar" styleClass="btn"/>
            <Button text="Entrar" 
                    onAction="#handleLogin"
                    styleClass="neumorphic-button"/>
        </HBox>
    </VBox>
</VBox>
🎨 Guia de Uso nos FXML
A estrutura do FXML NÃO DEVE SER MUDADA. Apenas adicione as styleClass conforme o guia abaixo para aplicar o tema Neumorphic.

🃏 Cards
CSS Class	Onde usar	Funcionalidade
metric-card	VBox, HBox, GridPane	Card com padding médio, centralizado, para métricas/números
neumorphic-card	VBox, HBox, GridPane	Card padrão com padding grande, sombra elevada
xml
<VBox styleClass="metric-card">
    <Label text="Dashboard" styleClass="heading-3"/>
    <Label text="0" styleClass="heading-1"/>
</VBox>

<VBox styleClass="neumorphic-card">
    <Label text="Product Name" styleClass="heading-4"/>
    <Label text="Short description here" styleClass="body-small"/>
</VBox>
🔘 Botões
CSS Class	Onde usar	Funcionalidade
btn	Button	Botão neumorphic padrão
button	Button	Botão neumorphic padrão (alias)
neumorphic-button	Button	Botão neumorphic padrão (alias)
xml
<Button text="Submit Now" styleClass="neumorphic-button"/>
<Button text="Cancel" styleClass="btn"/>
<Button text="View More" styleClass="button"/>
🧩 Containers / Panels
CSS Class	Onde usar	Funcionalidade
background-main	AnchorPane, BorderPane, VBox, HBox	Cor de fundo principal (cinza claro)
neumorphic-panel	VBox, HBox, GridPane, Pane	Painel elevado com sombra externa
neumorphic-inset	VBox, HBox, GridPane, Pane	Painel pressionado com sombra interna
xml
<BorderPane styleClass="background-main">
    <!-- conteúdo -->
</BorderPane>

<VBox styleClass="neumorphic-panel">
    <Label text="Projects"/>
</VBox>

<HBox styleClass="neumorphic-inset">
    <Label text="Pressed State"/>
</HBox>
📝 Textos e Headings
CSS Class	Onde usar	Funcionalidade	Tamanho	Peso
heading-1	Label, Text	Título maior	24px	Bold
heading-2	Label, Text	Título grande	20px	Bold
heading-3	Label, Text	Título médio	18px	Bold
heading-4	Label, Text	Título pequeno	16px	Bold
heading-5	Label, Text	Título muito pequeno	14px	Bold
body	Label, Text	Texto normal	13px	Normal
body-large	Label, Text	Texto grande	15px	Normal
body-small	Label, Text	Texto pequeno	12px	Normal
body-xs	Label, Text	Texto extra pequeno	11px	Normal
caption	Label, Text	Legenda	11px	Normal
link	Label, Text	Link clicável	13px	Medium
text	Label, Text	Texto padrão (alias)	13px	Normal
label	Label, Text	Label padrão (alias)	13px	Normal
xml
<Label text="Dashboard" styleClass="heading-1"/>
<Label text="All Your Workflows" styleClass="heading-4"/>
<Label text="John Doe" styleClass="heading-3"/>
<Label text="customerpop@gmail.com" styleClass="body"/>
<Label text="Short description" styleClass="body-small"/>
<Label text="View More" styleClass="link"/>
<Label text="Updated 2min ago" styleClass="caption"/>
🎨 Cores de Texto (Status)
CSS Class	Onde usar	Funcionalidade	Cor
status-new	Label, Text	Status "New"	Verde (#4CAF50)
status-updates	Label, Text	Status "Updates"	Azul (#2196F3)
status-review	Label, Text	Status "Team Review"	Laranja (#FF9800)
text-primary	Label, Text	Texto primário	Cinza escuro
text-secondary	Label, Text	Texto secundário	Cinza médio
text-muted	Label, Text	Texto suave	Cinza claro
text-email	Label, Text	Texto de email	Azul acinzentado
text-subtitle	Label, Text	Subtítulo	Cinza médio-claro
xml
<Label text="New" styleClass="status-new"/>
<Label text="Updates" styleClass="status-updates"/>
<Label text="Team Review" styleClass="status-review"/>
<Label text="Primary Text" styleClass="text-primary"/>
<Label text="Secondary Text" styleClass="text-secondary"/>
<Label text="Muted Text" styleClass="text-muted"/>
📊 Tabelas (Executions)
CSS Class	Onde usar	Funcionalidade
executions-table	TableView	Tabela com estilo neumorphic
neumorphic-table	TableView	Tabela com estilo neumorphic (alias)
table-header	TableColumn	Cabeçalho da tabela
table-row	TableRow	Linha da tabela
table-cell	TableCell	Célula da tabela
xml
<TableView styleClass="executions-table">
    <columns>
        <TableColumn text="Workflows" styleClass="table-header">
            <cellValueFactory .../>
        </TableColumn>
        <TableColumn text="Permissions" styleClass="table-header">
            <cellValueFactory .../>
        </TableColumn>
    </columns>
</TableView>
🔍 Campos de Pesquisa
CSS Class	Onde usar	Funcionalidade
search-field	TextField	Campo de texto com sombra interna
neumorphic-textfield	TextField	Campo de texto com sombra interna (alias)
search-field-focused	TextField	Campo de texto com foco ativo
xml
<TextField promptText="Search" styleClass="search-field"/>
<TextField promptText="Search documents..." styleClass="neumorphic-textfield"/>
🌳 Árvore de Diretórios (System Management's)
CSS Class	Onde usar	Funcionalidade
tree-section	Label, HBox	Seção principal da árvore (bold)
tree-item	HBox, Label	Item da árvore
tree-subitem	HBox, Label	Sub-item da árvore (identado)
tree-number	Label, HBox	Número/badge do item
xml
<VBox>
    <!-- Seção -->
    <Label text="System Management's" styleClass="tree-section"/>
    
    <!-- Item com número -->
    <HBox styleClass="tree-item">
        <Label text="2025 Update's"/>
        <Region HBox.hgrow="ALWAYS"/>
        <Label text="2" styleClass="tree-number"/>
    </HBox>
    
    <!-- Sub-item -->
    <HBox styleClass="tree-subitem">
        <Label text="Hiring Process"/>
        <Region HBox.hgrow="ALWAYS"/>
        <Label text="4" styleClass="tree-number"/>
    </HBox>
</VBox>
➖ Divisórias
CSS Class	Onde usar	Funcionalidade
divider	Region, Separator	Divisória horizontal
separator	Region, Separator	Divisória horizontal (alias)
divider-vertical	Region	Divisória vertical
xml
<Region styleClass="divider" prefHeight="1"/>
<Separator styleClass="separator"/>
<Region styleClass="divider-vertical" prefWidth="1"/>
🎨 Design System
🎨 Paleta de Cores
Categoria	Claro	Escuro	Uso
Primary	#2196F3	#90CAF9	Ações principais
Secondary	#FF9800	#FFB74D	Ações secundárias
Success	#4CAF50	#81C784	Operações bem-sucedidas
Danger	#F44336	#E57373	Erros e exclusões
Warning	#FFC107	#FFD54F	Alertas
Info	#00BCD4	#4DD0E1	Informações
Background	#F5F5F5	#2D2D2D	Fundo principal
Surface	#FFFFFF	#3D3D3D	Superfícies (cards)
📐 Tipografia
Elemento	Fonte	Tamanho	Peso	Uso
H1	System	24px	Bold	Títulos principais
H2	System	20px	SemiBold	Subtítulos
H3	System	18px	SemiBold	Títulos de seção
H4	System	16px	Bold	Títulos de card
Body	System	14px	Regular	Texto corrido
Body Small	System	12px	Regular	Texto auxiliar
Label	System	12px	Medium	Rótulos de campos
Caption	System	11px	Regular	Legendas
🧩 Componentes Padrão
Todos os componentes seguem estas convenções:

Bordas arredondadas: 3px

Padding interno: 8px 16px (botões), 16px (cards)

Sombras: Elevação consistente (2px blur, 4px offset)

Transições: 200ms ease para hover/focus

🔧 Arquitetura
text
┌─────────────────────────────────────────────────────────────┐
│                      APPLICATION LAYER                        │
│                      (Sua aplicação)                          │
├─────────────────────────────────────────────────────────────┤
│                        NEXUSFX CORE                           │
├─────────────────────────────────────────────────────────────┤
│                    DOMÍNIOS PÚBLICOS                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │    views()   │  │   images()   │  │   alerts()   │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │   dialogs()  │  │diagnostics() │  │    di()      │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
├─────────────────────────────────────────────────────────────┤
│                    REGISTRY (INTERNAL/EXTERNAL)               │
│  ┌─────────────────────────────────────────────────────┐    │
│  │           ResourceRegistry (único ponto)            │    │
│  │  ┌──────────────┐  ┌──────────────┐               │    │
│  │  │ ViewRegistry │  │ImageRegistry │               │    │
│  │  └──────────────┘  └──────────────┘               │    │
│  └─────────────────────────────────────────────────────┘    │
├─────────────────────────────────────────────────────────────┤
│                    SERVICE LAYER                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │ ViewManager  │  │ ImageService │  │SystemsAlerty │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
│  ┌──────────────┐  ┌──────────────┐                           │
│  │   Dialogs    │  │OwnerProvider │                           │
│  └──────────────┘  └──────────────┘                           │
├─────────────────────────────────────────────────────────────┤
│                      CORE LAYER                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │      DI      │  │  ClassLoader │  │   FXML       │       │
│  │   Container  │  │   Awareness  │  │   Service    │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
│  ┌──────────────┐  ┌──────────────┐                           │
│  │  Descriptors │  │    Loaded    │                           │
│  │(View/Image)  │  │     View     │                           │
│  └──────────────┘  └──────────────┘                           │
└─────────────────────────────────────────────────────────────┘
📖 API Reference
NexusFX (Ponto de Entrada Único)
Método	Retorno	Descrição
initialize(Stage, Class, String...)	void	Inicializa o framework
views()	ViewManager	Acesso ao domínio de views
images()	ImageService	Acesso ao domínio de imagens
alerts()	SystemsAlerty	Acesso ao sistema de alertas
dialogs()	DialogOrchestrator	Acesso ao sistema de diálogos
diagnostics()	DiagnosticsDomain	Acesso ao diagnóstico
internalRegistry()	InternalResourceRegistry	Registro de recursos do framework
externalRegistry()	ExternalResourceRegistry	Registro de recursos da aplicação
resources()	ResourceRegistry	Acesso direto ao registry (consultas)
stage()	Stage	Retorna o Stage principal
di()	DIContainer	Acesso ao container de DI
shutdown()	void	Finaliza o framework
LoadedView<T>
Método	Retorno	Descrição
getRoot()	Parent	Nó raiz da view
getController()	T	Controller da view
hasController()	boolean	Verifica se tem controller
configure(Consumer<T>)	LoadedView<T>	Configura o controller (fluente)
addCssClass(String)	LoadedView<T>	Adiciona CSS class
addCssClasses(String...)	LoadedView<T>	Adiciona múltiplas CSS classes
removeCssClass(String)	LoadedView<T>	Remove CSS class
refreshIfNeeded()	void	Executa refresh se controller for Refreshable
notifyShown()	void	Notifica que view foi exibida
notifyHidden()	void	Notifica que view foi ocultada
getControllerAs(Class<C>)	C	Converte controller para tipo específico
isControllerOfType(Class<?>)	boolean	Verifica tipo do controller
detachFromScene()	void	Desanexa view da cena
📊 Exemplos Completos
Dashboard com Cards e Gráficos
java
@Controller
public class DashboardController {

    @FXML
    private GridPane cardsGrid;
    
    @FXML
    private VBox chartContainer;
    
    @Inject
    private DashboardService service;
    
    @FXML
    private void initialize() {
        loadMetricCards();
        loadChart();
    }
    
    private void loadMetricCards() {
        List<Metric> metrics = service.getMetrics();
        
        for (int i = 0; i < metrics.size(); i++) {
            VBox card = new VBox(5);
            card.setAlignment(Pos.CENTER);
            card.getStyleClass().add("metric-card");
            
            Label titleLabel = new Label(metrics.get(i).getTitle());
            titleLabel.getStyleClass().addAll("body-small", "text-secondary");
            
            Label valueLabel = new Label(metrics.get(i).getValue());
            valueLabel.getStyleClass().addAll("heading-2", metrics.get(i).getColorClass());
            
            Label changeLabel = new Label(metrics.get(i).getChange());
            changeLabel.getStyleClass().addAll("body-xs", metrics.get(i).getStatusClass());
            
            card.getChildren().addAll(titleLabel, valueLabel, changeLabel);
            cardsGrid.add(card, i % 3, i / 3);
        }
    }
    
    private void loadChart() {
        // Implementação do gráfico
    }
}
Formulário com Validação
java
@Controller
public class UserFormController {

    @FXML
    private TextField nameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private ComboBox<String> roleCombo;
    
    @FXML
    private Button saveButton;
    
    @Inject
    private UserService userService;
    
    @FXML
    private void initialize() {
        setupValidation();
        roleCombo.getItems().addAll("Admin", "Manager", "User");
    }
    
    private void setupValidation() {
        // Validação em tempo real
        nameField.textProperty().addListener((obs, old, newVal) -> validateForm());
        emailField.textProperty().addListener((obs, old, newVal) -> validateForm());
        roleCombo.valueProperty().addListener((obs, old, newVal) -> validateForm());
    }
    
    private void validateForm() {
        boolean valid = !nameField.getText().trim().isEmpty()
            && !emailField.getText().trim().isEmpty()
            && emailField.getText().contains("@")
            && roleCombo.getValue() != null;
        
        saveButton.setDisable(!valid);
    }
    
    @FXML
    private void handleSave() {
        User user = new User();
        user.setName(nameField.getText());
        user.setEmail(emailField.getText());
        user.setRole(roleCombo.getValue());
        
        userService.save(user);
        
        NexusFX.alerts().success(
            "Sucesso",
            "Usuário salvo com sucesso!",
            "ID: " + user.getId(),
            "UserFormController"
        );
        
        // Fechar o diálogo
        NexusFX.dialogs().closeAll();
    }
}
🤝 Contribuição
Como contribuir
Fork o projeto

Clone seu fork:

bash
git clone https://github.com/seu-usuario/NexusFX.git
Crie uma branch:

bash
git checkout -b feature/nova-funcionalidade
Commit suas mudanças:

bash
git commit -m 'feat: adiciona nova funcionalidade'
Push para a branch:

bash
git push origin feature/nova-funcionalidade
Abra um Pull Request

Diretrizes
✅ Mantenha o código limpo e bem documentado

✅ Adicione testes para novas funcionalidades

✅ Atualize a documentação quando necessário

✅ Siga o padrão Conventional Commits

✅ Respeite o estilo de código existente

✅ Mantenha compatibilidade com versões anteriores

Padrão de Commits
text
feat: adiciona novo componente NexusCard
fix: corrige bug no carregamento de imagens
docs: atualiza documentação de instalação
style: formata código conforme padrões
refactor: reorganiza pacotes do core
test: adiciona testes para o ThemeManager
chore: atualiza dependências
📄 Licença
Distribuído sob a licença MIT. Veja o arquivo LICENSE para mais detalhes.

text
MIT License

Copyright (c) 2024 Rafael Tavares (Ossobo Tech)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files...
📞 Contato
Rafael Tavares

GitHub: @rephaelTAS

Email: rephaeltavares@gmail.com

LinkedIn: Rafael Tavares

Projeto: https://github.com/rephaelTAS/NexusFX

<div align="center"> <sub>Desenvolvido com ❤️ por <strong>Rafael Tavares</strong> | Ossobo Tech</sub> <br> <sub>© 2024 NexusFX Framework. Todos os direitos reservados.</sub> <br><br> <sub> <a href="#-sumário">⬆ Voltar ao topo</a> </sub> </div> ```