# ProjetoSD
Sistema Distribuido à base de sockets em Java para Aviso de Catástrofes

<h2>Como executar este projeto:</h2>

- 1º criar uma diretoria "bin" na diretoria mãe do projeto "/ProjetoSD"

- 2º abrir a linha de comandos (powershell pode nao funcionar) e escrever o seguinte

    > cd your_path_to_directory/ProjetoSD/src

- 3º - executar cada um dos componentes

    *MainServer*

    > javac -d ../bin -cp ../resources/gson-2.8.5 server/*.java server/utils/*.java

    > java -cp ../resources/gson-2.8.5;../bin; server.MainServer

    *Middle-Client*

    > javac -d ../bin -cp ../resources/gson-2.8.5 middle_client/*.java middle_client/utils/*.java

    > java -cp ../resources/gson-2.8.5;../bin; middle_client.MiddleClient <\main_server_ip> <\main_server_port>

    *End-Client*

    > javac -d ../bin -cp ../resources/gson-2.8.5 client/*.java

    > java -cp ../resources/gson-2.8.5;../bin; client.EndClient
