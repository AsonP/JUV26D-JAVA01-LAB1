# Bibliotekshanteraren

Ett konsolbaserat bibliotekssystem skrivet i Java, byggt som labbuppgift.
Applikationen hanterar böcker, medlemmar och utlåning via en textbaserad meny.

## Funktionalitet

- Lägga till böcker
- Registrera medlemmar (tilldelas automatiskt ett unikt ID)
- Låna böcker (via titel)
- Lämna tillbaka böcker (via titel)
- Söka böcker på titel eller författare (skiftlägesokänsligt)
- Visa alla böcker med status (utlånad/tillgänglig, samt till vem)

## Teknisk struktur

Projektet är byggt med Maven och består av följande klasser:

- **`Book`** (record) — representerar en bok (titel, författare, ISBN)
- **`Member`** (klass) — representerar en medlem (namn, ID, antal aktiva lån)
- **`Loan`** (record) — kopplar ihop en `Book` med en `Member` för ett aktivt lån
- **`Library`** — huvudklassen som hanterar böcker, medlemmar och lån i arrayer med fast storlek (25 platser)
- **`Menu`** — hanterar användarinteraktion via konsolen
- **`LibraryApp`** — programmets startpunkt (main)
- **`LibraryException`** — egen exception-klass med en `ErrorType`-enum för att särskilja olika feltyper (t.ex. bok hittades inte, redan utlånad, kapacitet uppnådd)

## Hur man kör projektet

1. Klona repot
2. Öppna projektet i en IDE (t.ex. IntelliJ) som Maven-projekt
3. Kör `LibraryApp.java` (innehåller `main`-metoden)

## Designval och reflektion

### Record kontra klass

**`Book` är en record** eftersom den representerar ren, oföränderlig data — en boks titel, författare och ISBN ändras aldrig efter att boken lagts till i biblioteket. Records i Java ger automatiskt `equals()`, `hashCode()` och `toString()`, vilket vi bland annat utnyttjar för att hitta rätt lån (`Loan`) kopplat till en specifik bok, utan att behöva skriva egen jämförelselogik.

**`Member` är en vanlig klass** eftersom en medlem har tillstånd som förändras över tid — framför allt antalet aktiva lån, som ökar och minskar när medlemmen lånar och lämnar tillbaka böcker. En record hade inte fungerat här eftersom alla fält i en record är `final` och därmed oföränderliga.

**`Loan` är en record** av samma anledning som `Book` — ett lån är egentligen bara en ögonblicksbild av "vilken bok" och "vilken medlem", och den kopplingen i sig ändras inte (om lånet avslutas tas hela `Loan`-objektet bort istället för att muteras).

### Arrayer med fast storlek

Uppgiften krävde arrayer istället för `ArrayList`/`Collections`. Vi valde en kapacitet på 25 platser (rimligt för ett litet skolprojekt) och löste hanteringen av "arrayen är full" genom att kasta en egen `LibraryException` med `ErrorType.CAPACITY_EXCEEDED` innan något skrivs utanför arrayens gränser.

För att hålla koll på vilka böcker som är utlånade och till vem användes inledningsvis en tredje parallell array (`borrowedBy[]`, synkad med `books[]` via index). Vi valde senare att ersätta den med en separat `Loan[]`-array istället, där varje `Loan` explicit representerar kopplingen mellan en bok och en medlem. Detta gjorde koden tydligare och undvek risken att arrayerna hamnar i otakt med varandra.

### Felhantering

Vi valde en enda `LibraryException`-klass med en intern `ErrorType`-enum, istället för många separata exception-klasser (t.ex. `BookNotFoundException`, `MemberNotFoundException` osv.). Detta gav oss möjligheten att särskilja feltyper programmatiskt (om det skulle behövas) utan att skapa onödigt många klasser för ett projekt av den här storleken.