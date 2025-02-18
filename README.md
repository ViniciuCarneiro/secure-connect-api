# Secure Connect

## Visão Geral

A API **Secure Connect** é um serviço de autenticação e gerenciamento de usuários desenvolvido em Java com Spring Security. Ela oferece funcionalidades robustas para integrar sistemas que necessitam de autenticação e administração de usuários.

## Funcionalidades

- Emissão e validação de tokens JWT.
- Gerenciamento de permissões com roles de acesso.
- Autenticação em dois fatores (2FA) utilizando aplicativos de autenticação e e-mail.
- Cadastro, atualização e exclusão de usuários.
- Redefinição de senha com envio de e-mail para recuperação.
- Confirmação de cadastro via e-mail de verificação.
- Envio de alertas para alterações de senha e novos logins.

## Tecnologias Utilizadas

- **Java 17+**
- **Spring Boot**
- **Spring Security**
- **MongoDB**
- **Docker**
- **Maven**
- **Redis**

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
         SECURITY_SECRET=secret_secure-connect-api
         EMAIL_HOST=smtp.gmail.com
         EMAIL_USER=seuemail@gmail.com
         EMAIL_PASSWORD=senha_app_google
         EMAIL_PORT=587
   
   Com o arquivo criado, importar na IDE (Enviroments Variables)


## Iniciar MongoDB e Redis com Docker

1. **Inicie o MongoDB e o Redis utilizando Docker:**

   ```bash
   docker run --name mongodb -d -p 27017:27017 mongo
   docker run --name redis -d -p 6379:6379 redis

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

1. **Autenticação**  
   **Método:** `POST /auth/login`  
   Realiza a autenticação do usuário utilizando e-mail e senha.

   #### Fluxo de Retorno
   - **Caso MFA NÃO esteja ativado:**  
     Retorna um token JWT completo, concedendo acesso a todos os recursos protegidos.

   - **Caso MFA esteja ativado:**  
     Retorna um token com acesso restrito ao endpoint de verificação de OTP. Outros recursos permanecem bloqueados até que o OTP seja validado.

2. **Autenticação MFA**  
   **Método:** `POST /auth/login/verify-otp`  
   Realiza a verificação do código OTP para usuários com MFA ativo.

   > **Obs:** Utilize o token recebido no login (campo `token_otp`) e informe o código OTP gerado pelo aplicativo (ex.: Google Authenticator).

3. **Cadastro de Usuários**  
   **Método:** `POST /users/register`  
   Registra um novo usuário no sistema.  
   **Acesso restrito:** Somente usuários autenticados com token válido e role **ADMIN** podem acessar este endpoint.

   #### Fluxo de Cadastro
   - Cria o usuário na coleção `users` do MongoDB.
   - Se o campo `mfaAtivado` for `true`:
      - A API gera um QR Code (em base64) para que o usuário possa configurar a autenticação de dois fatores (ex.: Google Authenticator).
      - É enviado um e-mail para o usuário com um token de verificação (válido por 24 horas). Sem a verificação por e-mail, o acesso do usuário não será liberado.


4. **Verificação de E-mail**  
   **Método:** `GET /users/verify-email`  
   Verifica o token enviado por e-mail para ativar a conta do usuário.


5. **Consulta de Usuários**  
   **Método:** `GET /users/search`  
   Consulta os usuários cadastrados no sistema. Este endpoint pode ser utilizado para filtrar ou listar usuários com roles **ADMIN** e **STANDARD**.

   **Requisito:**  
   Um token JWT válido deve ser enviado no header da requisição.

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

---

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
