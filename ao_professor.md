Olá, professor

Abaixo, deixo a apresentação do projeto, bem como as instruções de como executar o programa e os testes.

# Repositório
Link do github com os commits: 
- [https://github.com/luk3rr/MOINEX/tree/submit](https://github.com/luk3rr/MOINEX/tree/submit)

# Trello
Todos os cards utilizados para guiar o desenvolvimento do programa podem ser encontrados em:
- [https://trello.com/invite/b/67158d8dc213299ad1b589b3/ATTI27622f75fd4eea43275edeee7969a1eaFA7B5940/moinex](https://trello.com/invite/b/67158d8dc213299ad1b589b3/ATTI27622f75fd4eea43275edeee7969a1eaFA7B5940/moinex)

# Pastas
## docs
A pasta docs contém todos os diagramas utilizados durando o desenvolvimento do projeto, além dos slides das apresentações das Sprints 1 e 2

## img
Contém algumas screenshots das telas do programa e os ícones do Moinex.

## scripts
Nesta pasta você encontrará os scripts de instalação e desinstalação do Moinex, além de outros scripts auxiliares que foram escritos por mim para realizar tarefas específicas.

## src
Pasta padrão de projetos java, contendo as subpastas ```main```, com a implementação do Moinex, e ```test```, com todos os testes desenvolvidos para assegurar o funcionamento correto dos componentes.

## test_db
Caso deseje executar o programa, criei um arquivo `.db` com dados falsos de transações, o qual pode ser encontrado na pasta `test_db`. Mova esse arquivo para o diretório `~/.moinex/data` com o nome de `moinex.db` após a instalação do Moinex.

Como há dados de vários meses, a visualização dos gráficos temporais ficará melhor, sem a necessidade de que você crie várias transações para avaliar as funcionalidades.

# Dependências
Para compilar o programa é necessário ter o [Maven](https://maven.apache.org/) e o Java 21 instalados.

# Execução dos testes
Os testes podem ser executados com o comando do Maven ```mvn test```. Ao todo, foram implementados 118 testes.

# Execução do programa
O Moinex foi desenvolvido para sistemas operacionais GNU/Linux.

Para executar o Moinex, é necessário instalá-lo executando o script ```scripts/install.sh```. 

Mais especificamente, o script `scripts/install.sh` faz:

1. **Compilação do Projeto**:  
   - O script utiliza o Maven para compilar o código-fonte do Moinex e gerar o arquivo `.jar` necessário para execução.
   - O arquivo compilado (`Moinex-1.0-SNAPSHOT.jar`) é movido para o diretório `~/.moinex/bin`.

2. **Criação de Diretórios Necessários**:  
   O script cria os seguintes diretórios no sistema de arquivos:
   - **`~/.moinex`**:  
     Diretório principal, que armazenará o script `run.sh`, utilizado para facilitar a execução do binário.

   - **`~/.moinex/bin`**:  
     Contém o arquivo executável do Moinex.

   - **`~/.moinex/data`**:  
     Local onde será armazenado o arquivo SQLite3 do banco de dados do Moinex.

   - **`~/.local/state/moinex`**:  
     Diretório usado para salvar informações de estado da aplicação, especificamente os logs de execução.

   - **`~/.local/share/icons`**:  
     Contém os ícones do Moinex em diferentes resoluções (`32x32`, `64x64`, `128x128`, `256x256`), utilizados para representar o programa na interface gráfica do sistema.

   - **`~/.local/share/applications`**:  
     Armazena o arquivo `.desktop` do Moinex, que é usado para integrar o programa ao menu de aplicativos do ambiente gráfico.

3. **Movimentação de Arquivos**:  
   - Copia os ícones do programa (`moinex-icon-*.png`) para o diretório `~/.local/share/icons`, garantindo que o programa tenha suporte visual na interface do sistema.
   - Move o arquivo `moinex.desktop` para `~/.local/share/applications`, registrando o Moinex no menu de aplicativos.
   - Move o script `run.sh` para `~/.moinex/bin`, permitindo iniciar o programa facilmente.

4. **Configuração Inicial**:  
   - Concede permissões de execução ao arquivo `moinex.desktop`, permitindo que ele seja usado como atalho para abrir o programa a partir do menu de aplicativos.
   - Garante que todos os diretórios criados e arquivos copiados estejam acessíveis para o usuário.

Após executar o script de instalação, o Moinex estará configurado e pronto para ser iniciado, seja pelo terminal (`~/.moinex/run.sh`) ou pelo menu de aplicativos do ambiente gráfico.

# Desinstalação
Basta executar o script `scripts/uninstall.sh` que ele irá apagar todos os arquivos e diretórios criados pelo script de instalação.

# Considerações Finais
Todas as funcionalidades das telas cujas capturas de tela estão no diretório `img/screenshots` estão implementadas e operacionais, conforme planejado nas sprints anteriores. No entanto, as funcionalidades das telas `Goals`, `Savings`, `Import` e `Settings` estão em desenvolvimento e não serão concluídas até a data limite do projeto.
