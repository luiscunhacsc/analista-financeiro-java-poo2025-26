# S&P 500 Swing

Aplicacao Java com Swing que desenha o desempenho do S&P 500 para:

- Ultimo 1 dia
- Ultimos 5 dias
- Ultimo 1 mes
- Ultimo 1 ano
- Ultimos 3 anos
- Ultimos 5 anos
- Desde o inicio dos dados disponiveis na fonte
- Sobreposicao no grafico de SMA 50, SMA 200 e Bandas de Bollinger (20,2)
- Subgraficos de MACD e RSI por baixo do grafico principal

Inclui tambem um painel lateral de indicadores tecnicos:

- SMA 20/50/200
- EMA 20
- RSI 14
- MACD (12,26,9)
- Bandas de Bollinger (20,2)
- Maximo e minimo de 52 semanas

UI/UX melhorada:

- Layout em modo dashboard com cards
- Header com controlos de periodo em estilo segmentado
- Grafico com area preenchida, grelha refinada e legenda compacta
- Painel tecnico por secoes com badges de sinal (Alta/Baixa/Neutro)

## Requisitos

- Java 17+ (testado com Java 25)
- Ligacao a internet para descarregar o CSV diario

## Executar

```powershell
mkdir out
javac -d out (Get-ChildItem -Recurse -Path .\src\main\java -Filter *.java | ForEach-Object { $_.FullName })
java -cp out com.analista.sp500.Sp500SwingApp
```

## Fonte de dados

- Stooq, simbolo `^SPX` (fecho diario): `https://stooq.com/q/d/l/?s=%5Espx&i=d`
