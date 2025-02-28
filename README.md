# Secure Connect API

## Visão Geral

A API **Secure Connect** é um serviço de autenticação e gerenciamento de usuários desenvolvido em Java com Spring Security. Ela oferece funcionalidades robustas para integrar sistemas que necessitam de autenticação e administração de usuários.

## Funcionalidades

- Emissão e validação de tokens JWT.
- Gerenciamento de permissões com roles de acesso.
- Autenticação em dois fatores (2FA) utilizando aplicativos de autenticação.
- Cadastro, atualização e exclusão de usuários.
- Redefinição de senha com envio de e-mail para recuperação.
- Confirmação de cadastro via e-mail de verificação.
- Envio de alertas por e-mail.

## Tecnologias Utilizadas

- **Java 17+**
- **Spring Boot**
- **Spring Security**
- **MongoDB**
- **Docker**
- **Maven**
- **Redis**
- **Swagger**

## Pré-requisitos

Antes de iniciar o projeto, certifique-se de ter instalado:

- Java 17 ou superior
- Docker
- MongoDB
- Redis
- Senha de App do Google (Caso for usar o Gmail como provedor de emails)

## Instalação

1. **Clone o repositório:**

   ```bash
   git clone https://github.com/ViniciuCarneiro/secure-connect.git
   cd secure-connect

2. Configuração das variáveis de ambiente:

   Crie um arquivo .env na raiz do projeto e defina as seguintes variáveis:

         MONGO_DB_HOST=localhost
         MONGO_DB_PORT=27017
         REDIS_HOST=localhost
         REDIS_PORT=6379
         SECURITY_SECRET=secret_secure-connect-api
         EMAIL_HOST=smtp.gmail.com
         EMAIL_USER=seuemail@gmail.com
         EMAIL_PASSWORD=senha_app_google
         EMAIL_PORT=587
         VERIFY_EMAIIL_HOST=http://localhost:8080/api/auth/verify-email
         RESET_EMAIL_HOST=http://localhost:8080/api/auth/reset-password
         CORS_ORIGINS=api.securiity.cors.origins=http://localhost:8080,http://localhost:3000
   
   Com o arquivo criado, importar na IDE (Enviroments Variables)


## Iniciar MongoDB e Redis com Docker

1. **Inicie o MongoDB e o Redis utilizando Docker:**

   ```bash
   docker start mongodb-container
   docker start redis-container

2. Verifique se os containers foram iniciados:
   ```bash
   docker ps

## Acessar a API

A API estará disponível em: [http://localhost:8080](http://localhost:8080)

A documentação da API pode ser acessada via Swagger em: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)  
Utilize esta interface para explorar e interagir com os endpoints disponíveis.

> **Observação:**  
> Existe um usuário **admin** pré-cadastrado na coleção `users` do MongoDB, utilizado para iniciar o cadastro de novos usuários.

---

### Endpoints

## Índice

1. [Autenticação](#1-autenticação)
2. [Autenticação MFA](#2-autenticação-mfa)
3. [Cadastro de Usuários](#3-cadastro-de-usuários)
4. [Consulta de Usuários](#4-consulta-de-usuários)
5. [Verificação de E-mail](#5-verificação-de-e-mail)
6. [Reset de Senha](#6-reset-de-senha)
7. [Solicitar Recuperação de Senha](#7-solicitar-recuperação-de-senha)
8. [Atualização de Dados de Usuário](#8-atualização-de-dados-de-usuário)
9. [Exclusão de Usuário](#9-exclusão-de-usuário)

---

## 1. Autenticação

- **Método:** `POST`
- **Endpoint:** `/auth/login`
- **Descrição:**  
  Realiza a autenticação do usuário utilizando e-mail e senha.

### Fluxo de Retorno

- **Caso MFA NÃO esteja ativado:**  
  Retorna um token JWT completo, concedendo acesso a todos os recursos protegidos.

- **Caso MFA esteja ativado:**  
  Retorna um token com acesso restrito ao endpoint de verificação de OTP. Outros recursos permanecem bloqueados até que o OTP seja validado.

---

## 2. Autenticação MFA

- **Método:** `POST`
- **Endpoint:** `/auth/login/verify-otp`
- **Descrição:**  
  Realiza a verificação do código OTP para usuários com MFA ativo.

> **Observação:**  
> Utilize o token recebido no login (campo `access_token`) e informe o código OTP gerado pelo aplicativo (ex.: Google Authenticator).

---

## 3. Cadastro de Usuários

- **Método:** `POST`
- **Endpoint:** `/users/register`
- **Descrição:**  
  Registra um novo usuário no sistema.

- **Acesso Restrito:**  
  Somente usuários autenticados com token válido e role **ADMIN** podem acessar este endpoint.

### Fluxo de Cadastro

- Criação do usuário na coleção `users` do MongoDB.
- Se o campo `mfaAtivado` for `true`:
    - A API gera um QR Code (em base64) para que o usuário possa configurar a autenticação de dois fatores (ex.: Google Authenticator).
    - Um e-mail é enviado ao usuário contendo um token de verificação (válido por 24 horas). Sem a verificação por e-mail, o acesso do usuário não será liberado.

---

## 4. Consulta de Usuários

- **Método:** `GET`
- **Endpoint:** `/users/search`
- **Descrição:**  
  Consulta os usuários cadastrados no sistema, permitindo a filtragem ou listagem de usuários com roles **ADMIN** e **STANDARD**.

---

## 5. Verificação de E-mail

- **Método:** `GET`
- **Endpoint:** `/users/verify-email`
- **Descrição:**  
  Verifica o token enviado por e-mail para ativar a conta do usuário.

---

## 6. Reset de Senha

- **Método:** `POST`
- **Endpoint:** `/auth/reset-password`
- **Descrição:**  
  Permite a alteração da senha atual para uma nova senha, conforme informado pelo usuário durante o processo de recuperação.

---

## 7. Solicitar Recuperação de Senha

- **Método:** `POST`
- **Endpoint:** `/auth/forgot-password`
- **Descrição:**  
  Solicita a recuperação de senha para o usuário, enviando um e-mail de recuperação para o endereço informado.

---

## 8. Atualização de Dados de Usuário

- **Método:** `PUT`
- **Endpoint:** `/users/update`
- **Descrição:**  
  Realiza a atualização dos dados do usuário.

---

## 9. Exclusão de Usuário

- **Método:** `DELETE`
- **Endpoint:** `/users/delete`
- **Descrição:**  
  Exclui os dados do usuário do banco de dados.

---

**Requisito:**  
   Um token JWT válido deve ser enviado no header de cada requisição.

---

### Fluxo Completo de Uso

- **Autenticação:**
    1. Envie as credenciais via `POST /auth/login`.
    2. Se o usuário não possuir MFA, receba o token JWT completo.
    3. Se o usuário possuir MFA, receba um token restrito e, em seguida, envie o código OTP via `POST /auth/login/verify-otp` para obter o token completo.

- **Cadastro de Novo Usuário (Acesso Somente para ADMIN):**
    1. Envie o token de um usuário **ADMIN** e os dados do novo usuário via `POST /users/register`.
    2. Se o novo usuário ativar o MFA, ele receberá, além do cadastro, um QR Code para configuração e um e-mail com o token de verificação.

- **Ativação da Conta:**
    - O usuário deve confirmar o cadastro enviando o token recebido por e-mail através do endpoint `GET /users/verify-email` (token válido por 24 horas).

- **Consulta de Usuários:**
    - Certifique-se de enviar um token JWT válido no header da requisição.

- **Atualização de Dados do Usuário:**
    1. Envie um token JWT válido no header da requisição.
    2. Envie os dados atualizados via `PUT /users/update`.
    3. A API atualiza os dados do usuário e retorna a confirmação da alteração.

- **Exclusão de Usuário:**
    1. Certifique-se de enviar um token JWT válido no header da requisição.
    2. Envie a requisição de exclusão via `DELETE /users/delete`.
    3. A API remove os dados do usuário do banco de dados e retorna a confirmação da exclusão.

- **Solicitação de Recuperação de Senha:**
    1. Envie a requisição com o e-mail do usuário via `POST /auth/forgot-password`.
    2. A API envia um e-mail de recuperação de senha com as instruções necessárias para proceder com o reset.

- **Reset de Senha:**
    1. Após receber o e-mail, o usuário envia a nova senha juntamente com o token de recuperação via `POST /auth/reset-password`.
    2. A API atualiza a senha do usuário e confirma a alteração.


### Notas Adicionais

- **Tokens:**
   - O token gerado após o login contém informações sobre a role do usuário.
   - Para usuários com MFA, o token inicial é limitado e deve ser complementado com a verificação OTP para liberar o acesso completo.

- **MFA (Autenticação de Dois Fatores):**
   - Usuários que ativarem o MFA receberão um QR Code (em base64) durante o cadastro para configurar a autenticação de dois fatores, juntamente com um fluxo de verificação específico.

- **Verificação por E-mail:**
   - É indispensável que o usuário confirme o cadastro através do e-mail enviado. O token de verificação possui validade de 24 horas.

---

Esta documentação foi elaborada para facilitar a integração e o uso da API em diferentes sistemas e projetos.

Se tiver dúvidas ou precisar de mais informações, entre em contato!
