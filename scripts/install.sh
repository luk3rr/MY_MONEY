#!/usr/bin/env sh

# Filename: install.sh
# Created on: November  9, 2024
# Author: Lucas Araújo <araujolucas@dcc.ufmg.br>

print_success() {
    echo -e "\033[0;32m$1\033[0m"
}

print_error() {
    echo -e "\033[0;31m$1\033[0m"
}

# Criar diretórios
if sudo mkdir -p "/opt/moinex"; then
    print_success ">> Diretório /opt/moinex criado"
else
    print_error "Erro ao criar o diretório /opt/moinex"
    exit 1
fi

if mkdir -p "$HOME/.moinex/data"; then
    print_success ">> Diretório $HOME/.moinex/data criado"
else
    print_error "Erro ao criar o diretório $HOME/.moinex/data"
    exit 1
fi

if mkdir -p "$HOME/.local/state/moinex"; then
    print_success ">> Diretório $HOME/.local/state/moinex criado"
else
    print_error "Erro ao criar o diretório $HOME/.local/state/moinex"
    exit 1
fi

if mkdir -p "$HOME/.local/share/icons"; then
    print_success ">> Diretório $HOME/.local/share/icons criado"
else
    print_error "Erro ao criar o diretório $HOME/.local/share/icons"
    exit 1
fi

# Copiar ícones
if cp img/icons/moinex-icon-256.png "$HOME/.local/share/icons/moinex-icon.png"; then
    print_success ">> Ícone 256x256 copiado"
else
    print_error "Erro ao copiar o ícone 256x256"
    exit 1
fi

if cp img/icons/moinex-icon-128.png "$HOME/.local/share/icons/moinex-icon-128.png"; then
    print_success ">> Ícone 128x128 copiado"
else
    print_error "Erro ao copiar o ícone 128x128"
    exit 1
fi

if cp img/icons/moinex-icon-64.png "$HOME/.local/share/icons/moinex-icon-64.png"; then
    print_success ">> Ícone 64x64 copiado"
else
    print_error "Erro ao copiar o ícone 64x64"
    exit 1
fi

if cp img/icons/moinex-icon-32.png "$HOME/.local/share/icons/moinex-icon-32.png"; then
    print_success ">> Ícone 32x32 copiado"
else
    print_error "Erro ao copiar o ícone 32x32"
    exit 1
fi

# Copiar arquivo .desktop
if cp docs/moinex.desktop "$HOME/.local/share/applications/moinex.desktop"; then
    print_success ">> Arquivo .desktop copiado"
else
    print_error "Erro ao copiar o arquivo .desktop"
    exit 1
fi

chmod +x "$HOME/.local/share/applications/moinex.desktop"

print_success ">> Permissões de execução concedidas ao arquivo .desktop"

# Construir o JAR
if mvn clean package; then
    print_success ">> JAR criado com sucesso"
else
    print_error "Erro ao criar o JAR"
    exit 1
fi

# Copiar o JAR para /opt
if sudo cp target/Moinex-1.0-SNAPSHOT.jar /opt/moinex/moinex.jar; then
    print_success ">> JAR copiado para /opt/moinex"
else
    print_error "Erro ao copiar o JAR para /opt/moinex"
    exit 1
fi

print_success ">> Instalação concluída com sucesso. Aproveite o Moinex!"
