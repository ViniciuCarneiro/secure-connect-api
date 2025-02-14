# Secure Connect

Secure Connect é um serviço de autenticação e gerenciamento de usuários desenvolvido em Java com Spring Security. Ele oferece funcionalidades robustas para integração em sistemas que necessitam de autenticação e gerenciamento de usuários.

## Funcionalidades

- Emissão e validação de tokens JWT.
- Gerenciamento de permissões com roles de acesso.
- Autenticação em dois fatores (2FA) utilizando aplicativos de autenticação.
- Cadastro, atualização e exclusão de usuários.
- Redefinição de senha com envio de e-mail de recuperação.
- Confirmação de cadastro via e-mail de verificação.
- Envio de alertas para alterações de senha e novos logins.
- Armazenamento de logs para monitoramento e auditoria.

## Pré-requisitos

Antes de iniciar a instalação, certifique-se de ter os seguintes componentes instalados:

- Docker
- MongoDB
- Java
- Redis

## Instalação

1. **Clone o repositório:**

        git clone https://github.com/seu-usuario/secure-connect.git
        cd secure-connect

2. Configuração das variáveis de ambiente:

Crie um arquivo .env na raiz do projeto e defina as seguintes variáveis:


MONGODB_URI=seu_mongodb_uri
REDIS_HOST=seu_redis_host
REDIS_PORT=seu_redis_port
JWT_SECRET=sua_chave_secreta_jwt
EMAIL_HOST=seu_servidor_smtp
EMAIL_PORT=porta_smtp
EMAIL_USERNAME=seu_email
EMAIL_PASSWORD=sua_senha
Inicie o MongoDB e o Redis utilizando Docker:


    docker run --name mongodb -d -p 27017:27017 mongo
    docker run --name redis -d -p 6379:6379 redis
    Compile e execute a aplicação:
    ./mvnw spring-boot:run

Uso
Após a instalação e execução, a documentação da API estará disponível via Swagger em:

http://localhost:8080/swagger-ui.html
Utilize esta interface para explorar e interagir com as endpoints disponíveis.

Contribuição
Atualmente, não há diretrizes específicas para contribuições. Contribuições são bem-vindas e podem ser feitas através de pull requests. Sinta-se à vontade para abrir issues para relatar bugs ou sugerir melhorias.

Licença
A licença para este projeto ainda não foi definida. Considere adicionar uma licença para especificar os termos de uso e distribuição. Para mais informações sobre licenças open source, visite Choose a License.

Contato
Para dúvidas ou suporte, entre em contato através do e-mail:

viniciucarneiro@gmail.com
