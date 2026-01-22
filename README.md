🚖 KM Calculator - Monitor de Lucro para Motoristas
O KM Calculator é uma ferramenta de código aberto desenvolvida para ajudar motoristas de aplicativo (Uber, 99 e InDrive) a calcularem instantaneamente o valor por KM rodado de uma oferta, antes mesmo de aceitá-la.

O diferencial deste projeto é a capacidade de ler os dados tanto de notificações quanto diretamente da tela do aplicativo, garantindo que o cálculo apareça mesmo quando a notificação do sistema é ocultada.

✨ Funcionalidades
Cálculo Automático: Identifica o valor (R$) e a distância (KM/m) e exibe o resultado (R$/km).

Soma de Distâncias Inteligente: Soma automaticamente o deslocamento até o passageiro e o trajeto da viagem (ex: 688m + 4.2km).

HUD Flutuante: Uma pequena bolinha sobreposta ao GPS que muda de cor conforme sua meta:

🟢 Verde: Valor ideal ou superior.

🟡 Amarelo: Valor mínimo aceitável.

🔴 Vermelho: Abaixo da meta.

Privacidade Total: O app não possui permissão de internet. Todo o processamento é feito localmente no seu celular.

🛠️ Como Instalar e Configurar
Para que o app funcione corretamente, o Android exige três permissões específicas devido à natureza da leitura de dados:

Sobreposição de Tela: Permite que a bolinha do cálculo flutue sobre o app da Uber/99.

Acesso a Notificações: Necessário para ler os dados quando o celular está com a tela bloqueada ou em outro app.

Serviço de Acessibilidade: Essencial para ler os valores diretamente dentro do app da 99/Uber quando a notificação não aparece.

Nota para usuários Xiaomi (MIUI/HyperOS): > Vá em Informações do App > Outras Permissões e ative "Exibir janelas pop-up enquanto em segundo plano". Além disso, em Economia de Bateria, selecione "Nenhuma restrição".

🚀 Tecnologias Utilizadas
Linguagem: Kotlin

Serviços: NotificationListenerService e AccessibilityService

UI: Jetpack Compose e XML Views

Regex: Expressões regulares avançadas para captura de múltiplos formatos de moeda e distância (KM e Metros).

🛡️ Segurança
Este projeto foi construído sob o princípio da transparência. Motoristas lidam com dados sensíveis, por isso:

O código é 100% aberto para auditoria.

Não há coleta de logs ou telemetria.

O serviço de acessibilidade é usado estritamente para capturar os números da oferta de corrida.

Quer contribuir?
Sinta-se à vontade para abrir uma Issue ou enviar um Pull Request com melhorias na lógica de captura ou novos layouts para o HUD!
