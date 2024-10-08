# User Stories
## US 0001
Como um usuário, eu quero criar uma carteira para que eu possa registrar minhas transações.

### Tarefas:
- [X] Permitir criar uma nova carteira com um nome único.
- [x] Definir saldo inicial opcional.
- [X] Garantir que a carteira seja salva no banco de dados.

---

## US 0002
Como um usuário, eu quero registrar despesas e receitas efetivadas nas minhas carteiras.

### Tarefas:
- [X] Registrar despesas e receitas com valor, data e descrição.
- [X] Atualizar saldo da carteira após cada registro.

---

## US 0003
Como um usuário, eu quero registrar despesas e receitas planejadas (futuras) nas minhas carteiras.

### Tarefas:
- [x] Marcar transação como pendente ou confirmada
- [x] Confirmar transações pendentes

---

## US 0004
Como um usuário, eu quero poder remover uma receita/despesa de uma carteira.

### Tarefas:
- [X] Permitir remover uma receita ou despesa.
- [X] Atualizar saldo da conta após a remoção.

---

## US 0005
Como um usuário, eu quero deletar carteiras que não uso.

### Tarefas:
- [x] Criar método para apagar a carteira no banco de dados
- [ ] Verificar se a carteira tem transações antes de permitir a exclusão.
- [ ] Exibir mensagem de confirmação para deletar.

---

## US 0006
Como um usuário, eu quero arquivar carteiras que não uso, pois gostaria de manter o seu histórico de transações para consultas futuras.

### Tarefas:
- [X] Permitir arquivar carteiras não utilizadas mantendo o histórico de transações.

---

## US 0007
Como um usuário, eu quero definir um orçamento mensal para organizar os gastos por categoria.

### Tarefas:
- [ ] Permitir definição de limite de orçamento mensal.
- [ ] Distribuir o orçamento entre categorias.

---

## US 0008
Como um usuário, eu quero visualizar minhas últimas transações para organizar meus pagamentos.

### Tarefas:
- [X] Obter últimas transações a partir de uma data.
- [X] Obter últimas transações por quantidade de resultados na consulta.

---

## US 0009
Como um usuário, eu quero cadastrar um cartão de crédito para poder realizar pagamentos.

### Tarefas:
- [X] Definir limite de crédito do cartão.
- [X] Definir o dia de vencimento da fatura.

---

## US 0010
Como um usuário, eu quero visualizar quanto gastei/recebi em cada categoria para organizar meus gastos.

### Tarefas:
- [ ] Exibir gasto mensal por categoria.
- [ ] Exibir gasto anual por categoria.
- [ ] Exibir receita mensal por categoria.
- [ ] Exibir receita anual por categoria.

---

## US 0011
Como um usuário, eu quero apagar um cartão de crédito.

### Tarefas:
- [X] Implementar exclusão do cartão de crédito.
- [ ] Exibir contas pendentes associadas, se houver.
- [ ] Exibir mensagem de alerta de confirmação antes de apagar.

---

## US 0012
Como um usuário, eu quero transferir dinheiro entre carteiras.

### Tarefas:
- [X] Implementar funcionalidade de transferência entre carteiras.

---

## US 0013
Como um usuário, eu quero saber qual é o crédito ainda disponível em um determinado cartão de crédito.

### Tarefas:
- [X] Implementar consulta para saber o crédito disponível no cartão.

---

## US 0014
Como um usuário, eu quero registrar despesas no cartão de crédito.

### Tarefas:
- [X] Implementar registro de despesas no cartão de crédito.

---

## US 0015
Como um usuário, eu quero registrar transações recorrentes por período de dias e ter uma projeção de receitas/despesas mensal e anual

### Tarefas:
- [ ] Essas transações devem ser listada na projeção de fluxo de caixa de uma determinada carteira
