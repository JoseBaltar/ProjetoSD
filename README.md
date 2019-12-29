# ProjetoSD
Sistema Distribuido à base de sockets em Java para Aviso de Catástrofes

Como executar este projeto:

1º - criar uma diretoria "bin" na diretoria mãe do projeto "/ProjetoSD"

2º - abrir a linha de comandos e escrever o seguinte

> cd your_path_to_directory/ProjetoSD/src

3º - executar cada um dos componentes, Server, Middle-Client e Final-Client

> javac -d bin src/server/*.java src/server/utils/*.java
> java -cp bin server.Server

> javac -d bin src/middle_client/*.java src/middle_client/utils/*.java
> java -cp bin middle_client.CivilProtection

> javac -d bin src/client/*.java src/client/utils/*.java
> java -cp bin client.Citizen