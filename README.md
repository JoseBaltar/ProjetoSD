# Projeto Sistemas Distribuídos - 2019/2020
Sistema Distribuido à base de sockets em Java para Aviso de Catástrofes

<h2>Como executar este projeto:</h2>

- 1º criar uma diretoria "bin" na diretoria mãe do projeto "/ProjetoSD"

- 2º abrir a linha de comandos (powershell pode nao funcionar) e escrever o seguinte

    > cd your_path_to_directory/ProjetoSD/src

- 3º - executar cada um dos componentes

    *MainServer*

    > javac -d ../bin -cp ../resources/gson-2.8.5.jar server/*.java server/utils/*.java server/protocols/*.java server/models/*.java

    > java -cp ../resources/gson-2.8.5.jar;../bin; server.MainServer

    *Middle-Client*

    > javac -d ../bin -cp ../resources/gson-2.8.5.jar middle_client/*.java middle_client/utils/*.java middle_client/protocols/*.java middle_client/models/*.java

    > java -cp ../resources/gson-2.8.5.jar;../bin; middle_client.MiddleClient

    *End-Client*

    > javac -d ../bin -cp ../resources/gson-2.8.5.jar client/*.java

    > java -cp ../resources/gson-2.8.5.jar;../bin; client.EndClient
