#!/usr/bin/env sh

# Filename: run.sh
# Created on: November  9, 2024
# Author: Lucas Araújo <araujolucas@dcc.ufmg.br>

MOINEX_JAVA_HOME="${MOINEX_JAVA_HOME:-/usr/lib/jvm/java-21-openjdk}"

JAR_PATH="/opt/moinex/moinex.jar"

print_error() {
    echo -e "\033[0;31m$1\033[0m"
}

if [ ! -d "$MOINEX_JAVA_HOME" ]; then
    print_error "Erro: O diretório $MOINEX_JAVA_HOME não foi encontrado. Verifique se o Java 21 está instalado."
  exit 1
fi

if [ ! -f "$JAR_PATH" ]; then
    print_error "Erro: O arquivo $JAR_PATH não foi encontrado. Verifique se o Moinex foi instalado corretamente."
  exit 1
fi

"$MOINEX_JAVA_HOME/bin/java" -jar "$JAR_PATH" "$@"
