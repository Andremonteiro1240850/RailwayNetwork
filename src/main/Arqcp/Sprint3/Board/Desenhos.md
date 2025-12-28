# 📋 DOCUMENTAÇÃO DO BOARD COMPONENT

## VISÃO GERAL

O **Board** é o componente responsável pela apresentação visual de informação no console do sistema de gestão da estação ferroviária. Recebe dados do Manager e apresenta-os com formatação ASCII art apelativa e estruturada.

---


---

## 📡 INTERFACE PÚBLICA

### Funções de Inicialização

#### `int board_init(void)`
- **Propósito**: Inicializa o componente Board
- **Chamada por**: Manager no arranque do sistema
- **Retorno**: 1 em sucesso, 0 em falha
- **Comportamento**:
    - Inicializa estado interno
    - Limpa ecrã
    - Apresenta banner de boas-vindas

#### `void board_cleanup(void)`
- **Propósito**: Limpa recursos do Board
- **Chamada por**: Manager no shutdown
- **Comportamento**: Apresenta mensagem de despedida e reset do estado

---

### Funções de Comunicação com Manager

#### `void board_send_message(const char *message)`
- **Propósito**: Enviar mensagens simples e diretas
- **Usada por**: Manager para prompts, avisos, debug
- **Exemplo de uso no Manager**:
```c
board_send_message("\nEnter instruction: ");
```

#### `void board_display_text(const char *data)`
- **Propósito**: Apresentar dados formatados vindos do Manager
- **Usada por**: Manager para apresentar resultados de comandos
- **Exemplo de uso no Manager**:
```c
manager_send_data_to_board(data); // Chama board_display_text internamente
```

---

### Funções de Atualização de Dados

#### `void board_update_sensors(int temperature, int humidity)`
- **Propósito**: Atualizar dados de sensores com formatação visual
- **Dados recebidos**:
    - `temperature`: Temperatura filtrada (Celsius)
    - `humidity`: Humidade filtrada (Percentagem)
- **Apresentação**:
```
╔══════════════════════════════════════════════════╗
║          📊 SENSOR DATA UPDATE                   ║
╠══════════════════════════════════════════════════╣
║ 🌡️  Temperature: 25°C                            ║
║ 💧 Humidity:    65%                              ║
╚══════════════════════════════════════════════════╝
```

---

### Funções Operacionais (USAC16)

#### `void updateBoard(const char *msg)`
- **Propósito**: Atualizar Board com mensagens operacionais
- **Usada em**: Atribuição de vias, mudanças de estado
- **Exemplo de uso**:
```c
updateBoard("✅ Train 12 assigned to Track 2 (YELLOW)");
```

#### `void showEmergencyStop(void)`
- **Propósito**: Apresentar alerta de paragem de emergência
- **Visual destacado** com múltiplos símbolos de alerta 🚨
- **Contexto**: Quando uma via ocupada recebe tentativa de atribuição

#### `void trackINOP(void)`
- **Propósito**: Indicar via inoperacional
- **Visual**: Caixa vermelha com símbolo de manutenção 🔧
- **Estado**: RED BLINKING (RB)

#### `void freeTrack(void)`
- **Propósito**: Indicar via libertada
- **Visual**: Caixa verde com símbolo de confirmação ✅
- **Estado**: GREEN (GE)

#### `void showDepartingTrain(void)`
- **Propósito**: Apresentar comboio em partida
- **Visual**: Animação ASCII com movimento 🚂💨
- **Contexto**: Quando um comboio parte da estação

---

### Funções de Utilidade

#### `void board_redraw(void)`
- **Propósito**: Redesenhar Board completo com estado atual
- **Apresenta**:
    - Dados de sensores atuais
    - Último evento registado
    - Estado geral do sistema

#### `void board_clear_screen(void)`
- **Propósito**: Limpar o ecrã do console
- **Implementação**: ANSI escape codes (`\033[2J\033[H`)
- **Compatibilidade**: Linux/UNIX terminals

---

## 🔄 COMUNICAÇÃO MANAGER → BOARD

### Fluxo de Dados

```
┌─────────────┐
│   MANAGER   │
└──────┬──────┘
       │
       │ Chama funções do Board
       │
       ▼
┌─────────────────────────────────┐
│  BOARD (Interface Pública)      │
│  - board_send_message()         │
│  - board_display_text()         │
│  - board_update_sensors()       │
│  - updateBoard()                │
│  - showEmergencyStop()          │
│  - trackINOP()                  │
│  - freeTrack()                  │
│  - showDepartingTrain()         │
└─────────────┬───────────────────┘
              │
              │ Processa e formata
              │
              ▼
       ┌──────────────┐
       │   CONSOLE    │
       │  (stdout)    │
       └──────────────┘
```

### Exemplo Concreto 

**Manager** executa `assignArrivingTrain()`:

```c
// 1. Manager valida dados
Train *t = findTrain(system, train_id);
Track *tr = findTrack(system, track_id);

// 2. Manager atualiza estado
tr->state = TRACK_ASSIGNED;
t->state = TRAIN_STOPPED;

// 3. Manager formata mensagem
char board_msg[100];
snprintf(board_msg, sizeof(board_msg), 
         "✅ Train %d assigned to Track %d (YELLOW)", 
         train_id, track_id);

// 4. Manager chama Board
updateBoard(board_msg);

// 5. Board apresenta visualmente
```

**Output no Board**:
```
╔══════════════════════════════════════════════════╗
║          📢 OPERATIONAL UPDATE                   ║
╠══════════════════════════════════════════════════╣
║ ✅ Train 12 assigned to Track 2 (YELLOW)         ║
╚══════════════════════════════════════════════════╝
```

---

## 🏗️ ESTRUTURAS DE DADOS

### `BoardState`

```c
typedef struct {
    int temperature;        // Temperatura atual (°C)
    int humidity;           // Humidade atual (%)
    char last_event[256];   // Último evento registado
    int initialized;        // Flag de inicialização
} BoardState;
```

**Propósito**: Manter estado interno para redesenhos consistentes

**Estado Global**: `static BoardState g_board_state`

---

## 🎨 DESIGN VISUAL

### Princípios de Design

1. **Caixas ASCII** - Uso de caracteres Unicode para bordas (`╔═╗║╚╝╠╣`)
2. **Ícones Emoji** - Símbolos visuais (`🚉🌡💧⚠🟢🟡🔴🚨🔧`)
3. **Hierarquia Visual** - Separadores e secções bem definidas
4. **Consistência** - Todas as mensagens seguem o mesmo padrão
5. **Largura Fixa** - `BOARD_DISPLAY_WIDTH = 50` para alinhamento

### Exemplo Completo de Board

```
╔══════════════════════════════════════════════════╗
║         🚉 STATION SYNOPTIC BOARD                ║
╠══════════════════════════════════════════════════╣
║ 🌡️  Temperature: 22°C                            ║
║ 💧 Humidity:    65%                              ║
╠══════════════════════════════════════════════════╣
║ ⚠️  Last Event:                                   ║
║    Train 8 assigned to Track 3 (YELLOW)         ║
╚══════════════════════════════════════════════════╝
```

---

## ⚙️ DECISÕES DE DESIGN

### 1. Estado Global vs. Parâmetros

**Decisão**: Usar estado global `g_board_state`

**Justificação**:
- Manager não precisa gerir estado do Board
- Simplifica interface (menos parâmetros)
- Board mantém contexto entre chamadas

### 2. Funções Específicas vs. Genéricas

**Decisão**: Criar funções específicas (`showEmergencyStop`, `trackINOP`, etc.)

**Justificação**:
- Alinhamento com código Manager existente
- Cada função tem visual único e adequado
- Facilita manutenção e extensão

### 3. Formatação Imediata vs. Buffering

**Decisão**: Formatação e output imediatos com `fflush(stdout)`

**Justificação**:
- Console partilhado com UI (requisito)
- Evita buffers misturados
- Output em tempo real

### 4. ASCII Art vs. Texto Simples

**Decisão**: ASCII art completo 

**Justificação**:
- Especificação explícita: "não pode ser texto simples"
- Melhora experiência visual
- Diferenciação clara de secções

### 5. Sem Alocação Dinâmica no Board

**Decisão**: Apenas arrays estáticos e stack

**Justificação**:
- Simplicidade e performance
- Evita memory leaks
- Alinhamento com RISC-V embedded systems

---


### Compilação

```bash
# Compilação nativa (teste)
gcc -Wall -Wextra -std=c99 -o board board.c

# Cross-compilation para RISC-V
riscv32-unknown-elf-gcc -march=rv32im -mabi=ilp32 \
    -Wall -Wextra -std=c99 -o board.elf board.c
```

---

## 📝 EXEMPLO DE USO

### Integração Completa

```c
#include "board.h"
#include "manager.h"

int main(void) {
    // 1. Inicializar Board
    if (!board_init()) {
        return 1;
    }
    
    // 2. Inicializar Manager
    ManagerSystem system;
    if (!manager_init(&system)) {
        board_send_message("Error: Failed to init system\n");
        return 1;
    }
    
    // 3. Loop principal do Manager
    // Manager chama funções do Board automaticamente
    manager_main_loop(&system);
    
    // 4. Cleanup
    board_cleanup();
    manager_cleanup(&system);
    
    return 0;
}
```

### Simulação de Dados

```c
// Simular atualização de sensores
board_update_sensors(25, 65);

// Simular operação de via
updateBoard("✅ Train 12 assigned to Track 2 (YELLOW)");

// Simular emergência
showEmergencyStop();

// Redesenhar estado completo
board_redraw();
```

---

---

## 🚀 CONCLUSÃO

O componente **Board** está completamente implementado e integrado com o **Manager** existente. Todas as funções esperadas estão disponíveis, a apresentação visual é apelativa e estruturada, e o código é compatível com os requisitos técnicos (RISC-V 32, Linux, C standard).

A implementação segue as melhores práticas de engenharia de software:
- **Modularidade** - Interface pública clara
- **Manutenibilidade** - Código bem comentado e estruturado
- **Robustez** - Validação de inputs, gestão de estado
- **Performance** - Sem alocação dinâmica desnecessária

O Board está pronto para integração no sistema completo! 🎉