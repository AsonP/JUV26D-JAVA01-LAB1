# 📚 Bibliotekshanteraren

Ett konsolbaserat bibliotekssystem skrivet i Java, byggt som labbuppgift.
Applikationen hanterar böcker, medlemmar och utlåning via en textbaserad meny.

---

## Innehåll

- [Funktionalitet](#funktionalitet)
- [Teknisk struktur](#teknisk-struktur)
- [Hur man kör projektet](#hur-man-kör-projektet)
- [Påbyggnadsdel](#påbyggnadsdel)
- [Designval och reflektion](#designval-och-reflektion)

---

## Funktionalitet

- 📖 Lägga till böcker
- 🙋 Registrera medlemmar (tilldelas automatiskt ett unikt ID)
- 🔄 Låna böcker (via titel)
- ↩️ Lämna tillbaka böcker (via titel)
- 🔍 Söka böcker på titel eller författare (skiftlägesokänsligt)
- 📋 Visa alla böcker med status (utlånad/tillgänglig, samt till vem), sorterade i bokstavsordning
- 📊 Visa medlem med flest aktiva lån

---

## Teknisk struktur

Projektet är byggt med **Maven** och består av följande klasser:

| Klass | Typ | Ansvar |
|---|---|---|
| `Book` | record | Representerar en bok (titel, författare, ISBN) |
| `Member` | klass | Representerar en medlem (namn, ID, antal aktiva lån) |
| `Loan` | record | Kopplar ihop en `Book` med en `Member` för ett aktivt lån |
| `Library` | klass | Huvudklassen — hanterar böcker, medlemmar och lån |
| `Menu` | klass | Hanterar användarinteraktion via konsolen |
| `LibraryApp` | klass | Programmets startpunkt (`main`) |
| `LibraryException` | klass | Egen exception med `ErrorType`-enum för olika feltyper |

Böcker, medlemmar och lån lagras i **arrayer** (inte `ArrayList`/`Collections`), enligt uppgiftens krav.

---

## Hur man kör projektet

1. Klona repot
2. Öppna projektet i en IDE (t.ex. IntelliJ) som Maven-projekt
3. Kör `LibraryApp.java` (innehåller `main`-metoden)

---

## Påbyggnadsdel

### 🔤 Egen sorteringsalgoritm

Menyval 6 sorterar böckerna på titel med en egen implementation av **selection sort** (`getBooksSortedByTitle()` i `Library`), istället för `Arrays.sort()`. Algoritmen går igenom den osorterade delen av arrayen, hittar det alfabetiskt tidigaste elementet, och byter plats med det till rätt position — ett steg i taget. Sorteringen görs på en kopia av `books[]`, så den ursprungliga insättningsordningen bevaras internt.

### 📈 Statistik

Menyval 7 hittar medlemmen med flest aktiva lån genom en enkel linjär genomsökning av `members[]` (`showMemberWithMostLoans()` i `Library`), utan att använda Streams eller Collections-metoder som `max()`.

### 📦 Dynamisk kapacitet

`books[]`, `members[]` och `loans[]` börjar med en liten kapacitet (`CAPACITY`). När en array blir full skapas en ny array med dubbel storlek (`length * 2`), och alla befintliga element kopieras manuellt över med en egen loop — inte `Arrays.copyOf()`. Metoderna `growBookArray()`, `growMemberArray()` och `growLoanArray()` i `Library` sköter detta. En konsekvens av lösningen är att fälten `books`, `members` och `loans` inte längre kunde vara `final`, eftersom de behöver kunna peka om till en ny array vid växning.

### ✍️ Formatering vid utskrift

Titlar, författarnamn och medlemsnamn formateras med en egen `capitalizeWords()`-metod vid utskrift (första bokstaven versal, resten gemener), utan att ändra det faktiskt lagrade värdet i `Book`/`Member`.

---

## Designval och reflektion

### Record kontra klass

**`Book` är en record** eftersom den representerar ren, oföränderlig data — en boks titel, författare och ISBN ändras aldrig efter att boken lagts till i biblioteket. Records i Java ger automatiskt `equals()`, `hashCode()` och `toString()`, vilket bland annat utnyttjas för att hitta rätt lån (`Loan`) kopplat till en specifik bok, utan att behöva skriva egen jämförelselogik.

**`Member` är en vanlig klass** eftersom en medlem har tillstånd som förändras över tid — framför allt antalet aktiva lån, som ökar och minskar när medlemmen lånar och lämnar tillbaka böcker. En record hade inte fungerat här eftersom alla fält i en record är `final` och därmed oföränderliga.

**`Loan` är en record** av samma anledning som `Book` — ett lån är egentligen bara en ögonblicksbild av "vilken bok" och "vilken medlem", och den kopplingen i sig ändras inte (om lånet avslutas tas hela `Loan`-objektet bort istället för att muteras).

### Arrayer med fast (växande) storlek

Uppgiften krävde arrayer istället för `ArrayList`/`Collections`. Grundkravet löstes med en fast kapacitet och tydlig felhantering vid full array. I påbyggnadsdelen byggdes detta ut till **dynamisk växning** — arrayerna dubblas i storlek istället för att kasta fel, vilket eliminerar den ursprungliga begränsningen helt.

För att hålla koll på vilka böcker som är utlånade och till vem användes inledningsvis en tredje parallell array (`borrowedBy[]`, synkad med `books[]` via index). Den ersattes senare av en separat `Loan[]`-array, där varje `Loan` explicit representerar kopplingen mellan en bok och en medlem. Detta gjorde koden tydligare och undvek risken att arrayerna hamnar i otakt med varandra.

### Felhantering

En enda `LibraryException`-klass med en intern `ErrorType`-enum används, istället för många separata exception-klasser (t.ex. `BookNotFoundException`, `MemberNotFoundException` osv.). Detta gav möjligheten att särskilja feltyper programmatiskt (om det skulle behövas) utan att skapa onödigt många klasser för ett projekt av den här storleken.

### Reflektion: arrayer kontra Collections Framework

Hela projektet byggdes med fasta (växande) arrayer istället för `ArrayList`/`Collections`, vilket krävde en del kod som annars hade kommit "gratis":

- **Dynamisk storlek** löstes manuellt genom att skapa nya, större arrayer och kopiera över innehållet (`growBookArray()` m.fl.). Med `ArrayList` hade detta inte behövts alls — listan växer automatiskt.
- **Borttagning** av ett lån (`removeLoanAt()` i `Library`) krävde att alla efterföljande element manuellt skiftades ett steg åt vänster för att undvika "hål" i arrayen. `ArrayList.remove()` hade gjort detta åt oss.
- **Sökning** (`findBookIndexByTitle`, `findMemberIndex` m.fl.) fick skrivas som egna linjära loopar. Med Collections Framework hade `stream().filter()` eller `indexOf()` med en egen `equals()`-implementation kunnat användas istället.
- **Sortering** (menyval 6) implementerades som en egen selection sort. Med Collections Framework hade `Collections.sort()` eller `list.sort(Comparator...)` gjort samma jobb på en rad kod.

Att bygga detta manuellt med arrayer gav en tydligare förståelse för vad Collections Framework faktiskt gör "under huven" — särskilt växning av dynamiska strukturer och hantering av "hål" vid borttagning. Nackdelen är mer kod och fler platser där buggar (t.ex. off-by-one-fel vid array-skiftning) kan smyga sig in, vilket `ArrayList` hade skyddat oss från.