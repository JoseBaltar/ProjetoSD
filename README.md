# ProjetoSD
Sistema Distribuido à base de sockets em Java para Aviso de Catástrofes

Como executar este projeto:

1º - criar uma diretoria "bin" na diretoria mãe do projeto "/ProjetoSD"

2º - abrir a linha de comandos e escrever o seguinte

> cd your_path_to_directory/ProjetoSD/src

3º - executar cada um dos componentes, MainServer, Middle-Client e End-Client

> javac -d ../bin server/*.java server/utils/*.java
> java -cp ../bin server.MainServer

> javac -d ../bin middle_client/*.java middle_client/utils/*.java
> java -cp ../bin middle_client.MiddleClient <\main_server_ip> <\main_server_port>

> javac -d ../bin client/*.java
> java -cp ../bin client.EndClient