<p align="center">
  <img src="./logo.png" alt="OpenSheets Companion Logo" height="80" />
</p>

<p align="center">
  App Android para captura automática de notificações bancárias e integração com o OpenSheets.
</p>

> **Requer o OpenSheets instalado.** Este app é um complemento que captura notificações e envia para sua instância do [OpenSheets](https://github.com/felipegcoutinho/opensheets-app).

[![Android](https://img.shields.io/badge/Android-12+-3DDC84?style=flat-square&logo=android)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9-7F52FF?style=flat-square&logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?style=flat-square&logo=jetpack-compose)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-CC_BY--NC--SA_4.0-orange?style=flat-square&logo=creative-commons)](LICENSE)

---

## 📖 Índice

- [Sobre o Projeto](#-sobre-o-projeto)
- [Features](#-features)
- [Bancos Suportados](#-bancos-suportados)
- [Tech Stack](#-tech-stack)
- [Instalação](#-instalação)
- [Configuração](#-configuração)
- [Arquitetura](#-arquitetura)
- [Desenvolvimento](#-desenvolvimento)
- [Contribuindo](#-contribuindo)

---

## 🎯 Sobre o Projeto

**OpenSheets Companion** é o app Android oficial do ecossistema OpenSheets. Ele captura automaticamente notificações de transações dos seus apps de banco e fintech, extrai as informações relevantes (valor, descrição) e envia para a **Caixa de Entrada** do OpenSheets como pré-lançamentos.

### Como funciona

1. O app escuta notificações dos apps de banco configurados
2. Quando detecta uma transação (Pix recebido, compra no cartão, etc.), extrai os dados
3. Envia automaticamente para sua instância do OpenSheets via API
4. As transações aparecem na "Caixa de Entrada" para você revisar e aprovar

### Por que usar

- **Economia de tempo:** Não precisa digitar cada transação manualmente
- **Precisão:** Valores e descrições são capturados diretamente da notificação
- **Controle:** Você ainda revisa e aprova antes de virar um lançamento oficial
- **Privacidade:** Seus dados ficam no SEU servidor, não em nuvens de terceiros

---

## ✨ Features

### 📱 Captura Inteligente

- Escuta notificações em tempo real
- Filtra apenas apps de banco configurados
- Extrai valor e descrição automaticamente
- Detecta tipo de transação (Pix, cartão, transferência)

### 🔄 Sincronização

- Envio automático para o OpenSheets
- Sincronização em segundo plano via WorkManager
- Retry automático em caso de falha de conexão
- Fila de notificações pendentes

### 🔐 Segurança

- Autenticação via token de API
- Token armazenado com EncryptedSharedPreferences
- Comunicação HTTPS com o servidor
- Sem coleta de dados por terceiros

### 📊 Histórico

- Visualização de notificações capturadas
- Status de sincronização (pendente, sincronizado, falha)
- Filtros por status
- Exclusão de notificações indesejadas

### ⚙️ Configuração

- Setup guiado de conexão com servidor
- Seleção de apps para monitorar
- Gatilhos de captura personalizáveis
- Tema claro/escuro (segue sistema)

---

## 🛠️ Tech Stack

| Componente | Tecnologia |
|------------|------------|
| **Linguagem** | Kotlin 1.9 |
| **Min SDK** | Android 12 (API 31) |
| **Target SDK** | Android 15 (API 35) |
| **UI** | Jetpack Compose + Material 3 |
| **Arquitetura** | MVVM + Clean Architecture |
| **DI** | Hilt |
| **Database** | Room |
| **Network** | Retrofit + OkHttp |
| **Async** | Coroutines + Flow |
| **Background** | WorkManager |
| **Segurança** | EncryptedSharedPreferences |
| **Build** | Gradle Kotlin DSL |

---

## 📲 Instalação

### Download

Baixe a última versão do APK na página de [Releases](https://github.com/felipegcoutinho/opensheets-companion/releases).

### Requisitos

- Android 12 ou superior
- Instância do OpenSheets configurada e acessível
- Token de API gerado no OpenSheets

### Instalação Manual

1. Baixe o arquivo `opensheets-companion-vX.X.X.apk`
2. No Android, habilite "Instalar apps de fontes desconhecidas" para seu navegador/gerenciador de arquivos
3. Abra o APK e instale
4. Siga o assistente de configuração

---

## ⚙️ Configuração

### 1. Gerar Token no OpenSheets

1. Acesse sua instância do OpenSheets
2. Vá em **Ajustes → OpenSheets Companion**
3. Clique em **Gerar Token**
4. Copie o token gerado (ele só é mostrado uma vez!)

### 2. Configurar o App

1. Abra o OpenSheets Companion
2. Insira a URL do seu servidor (ex: `https://opensheets.exemplo.com`)
3. Cole o token de API
4. Clique em **Conectar**

### 3. Permissões

O app solicitará permissão de **Acesso a Notificações**:

1. Toque em **Conceder Permissão**
2. Encontre "OpenSheets Companion" na lista
3. Ative a permissão

### 4. Selecionar Apps

Por padrão, os principais apps de banco já vêm configurados. Você pode ajustar em **Configurações → Apps Monitorados**.

---

## 🏗️ Arquitetura

### Estrutura do Projeto

```
app/src/main/java/br/com/opensheets/companion/
├── OpenSheetsApp.kt              # Application class (Hilt)
├── di/                           # Módulos de Injeção de Dependência
│   ├── AppModule.kt
│   ├── DatabaseModule.kt
│   └── NetworkModule.kt
├── data/
│   ├── local/                    # Room Database
│   │   ├── AppDatabase.kt
│   │   ├── dao/
│   │   └── entities/
│   ├── remote/                   # Retrofit API
│   │   ├── api/
│   │   └── dto/
│   └── repository/               # Repositórios
├── domain/
│   ├── model/                    # Modelos de domínio
│   └── repository/               # Interfaces
├── service/
│   ├── NotificationListenerService.kt  # Captura de notificações
│   └── SyncWorker.kt                   # Sincronização em background
├── ui/
│   ├── theme/                    # Material 3 Theme
│   ├── navigation/               # Navigation Compose
│   └── screens/
│       ├── setup/                # Tela de configuração inicial
│       ├── home/                 # Tela principal
│       ├── settings/             # Configurações
│       ├── history/              # Histórico
│       └── logs/                 # Logs de sincronização
└── util/                         # Utilitários
```

### Fluxo de Dados

```
┌─────────────────────────────────────────────────────────────┐
│                    CAPTURA DE NOTIFICAÇÃO                    │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. NotificationListenerService                             │
│     ├── Recebe notificação do sistema                      │
│     ├── Filtra por packageName (apps configurados)         │
│     └── Extrai título e texto                              │
│                            ↓                                │
│  2. NotificationParser                                      │
│     ├── Identifica tipo de transação                       │
│     ├── Extrai valor (regex)                               │
│     └── Extrai descrição/nome                              │
│                            ↓                                │
│  3. NotificationRepository                                  │
│     ├── Salva no Room (local)                              │
│     └── Status: PENDING                                     │
│                            ↓                                │
│  4. SyncWorker (WorkManager)                               │
│     ├── Busca notificações PENDING                         │
│     ├── Envia para API do OpenSheets                       │
│     └── Atualiza status: SYNCED ou FAILED                  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Comunicação com OpenSheets

```
┌─────────────────┐         HTTPS          ┌─────────────────┐
│                 │ ───────────────────────▶│                 │
│   Companion     │   POST /api/inbox      │   OpenSheets    │
│   (Android)     │   Authorization: os_*  │   (Server)      │
│                 │◀─────────────────────── │                 │
└─────────────────┘        200 OK          └─────────────────┘
```

**Endpoints utilizados:**

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/health` | Verifica conectividade |
| POST | `/api/inbox` | Envia notificação única |
| POST | `/api/inbox/batch` | Envia múltiplas notificações |

---

## 💻 Desenvolvimento

### Pré-requisitos

- Android Studio Hedgehog (2023.1.1) ou superior
- JDK 17
- Android SDK 35

### Setup

1. Clone o repositório
   ```bash
   git clone https://github.com/felipegcoutinho/opensheets-companion.git
   cd opensheets-companion
   ```

2. Abra no Android Studio
   ```
   File → Open → selecionar diretório
   ```

3. Sincronize o Gradle
   ```
   O Android Studio fará automaticamente
   ```

4. Execute no emulador ou dispositivo
   ```
   Run → Run 'app'
   ```

### Build Release

```bash
./gradlew assembleRelease
```

O APK será gerado em `app/build/outputs/apk/release/`.

### Estrutura de Branches

- `master` - Versão estável
- `develop` - Desenvolvimento ativo
- `feature/*` - Novas features
- `fix/*` - Correções de bugs

---

## 🤝 Contribuindo

Contribuições são bem-vindas!

### Como contribuir

1. **Fork** o projeto
2. **Clone** seu fork
   ```bash
   git clone https://github.com/seu-usuario/opensheets-companion.git
   ```
3. **Crie uma branch** para sua feature
   ```bash
   git checkout -b feature/minha-feature
   ```
4. **Commit** suas mudanças
   ```bash
   git commit -m 'feat: adiciona suporte ao banco X'
   ```
5. **Push** para a branch
   ```bash
   git push origin feature/minha-feature
   ```
6. Abra um **Pull Request**

### Adicionando Suporte a Novo Banco

Para adicionar suporte a um novo banco, você precisa:

1. Identificar o `packageName` do app
2. Criar regras de parsing em `NotificationParser`
3. Adicionar à lista de apps suportados
4. Testar com notificações reais

---

## 📄 Licença

Este projeto está licenciado sob a **Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International** (CC BY-NC-SA 4.0).

[![License: CC BY-NC-SA 4.0](https://img.shields.io/badge/License-CC_BY--NC--SA_4.0-lightgrey.svg)](https://creativecommons.org/licenses/by-nc-sa/4.0/)

---

## 🔗 Links

- **OpenSheets (Web App):** [github.com/felipegcoutinho/opensheets-app](https://github.com/felipegcoutinho/opensheets-app)
- **Releases:** [github.com/felipegcoutinho/opensheets-companion/releases](https://github.com/felipegcoutinho/opensheets-companion/releases)
- **Issues:** [github.com/felipegcoutinho/opensheets-companion/issues](https://github.com/felipegcoutinho/opensheets-companion/issues)

---

## 📞 Contato

**Desenvolvido por:** Felipe Coutinho
**GitHub:** [@felipegcoutinho](https://github.com/felipegcoutinho)

---

<div align="center">

**Parte do ecossistema [OpenSheets](https://github.com/felipegcoutinho/opensheets-app)**

Desenvolvido com ❤️ para a comunidade open source

</div>
