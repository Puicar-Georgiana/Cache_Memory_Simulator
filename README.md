Simularea Funcționării Unei Memorie Cache
Descriere proiect
Acest proiect implementează un simulator interactiv de memorie cache cu interfață grafică (GUI) în Java, destinat studiului comportamentului și performanței cache-ului. Aplicația permite vizualizarea operațiilor de citire și scriere, monitorizarea statisticilor și compararea diferitelor politici de mapare, înlocuire și scriere.
Simulatorul oferă utilizatorului posibilitatea de a înțelege:
•	Tipuri de mapare: Direct-Mapped, Set-Associative, Fully Associative
•	Politici de înlocuire: FIFO, LRU, Random
•	Politici de scriere: Write-Through, Write-Back
•	Impactul dimensiunii cache-ului asupra performanței
Funcționalități principale
1.	Operații de bază
o	Citire (Read): verifică hit/miss și evidențiază vizual linia afectată
o	Scriere (Write): suport pentru Write-Through și Write-Back, marcarea liniilor dirty
o	Reset: golește cache-ul și resetează statisticile
o	Salvare statistici: export în fișiere .txt cu raport detaliat
2.	Interfața grafică
o	Zona de control: introducere adresă, butoane Read/Write/Reset/Salvare Stats, setări cache (dimensiune, mapare, politică)
o	Zona cache vizuală: tabel JTable cu coloane Index, Valid, Tag, Dirty, Date; colorare dinamică (verde hit, roz miss)
o	Zona log și statistici: afișează log de operații și indicatori de performanță (hits, misses, hit rate, miss rate, avg. access time)
3.	Monitorizarea performanței
o	Rata de cache hit și miss
o	Număr total de operații
o	Timp mediu de acces (AMAT)
Structura proiectului
•	CacheLine – Reprezintă o linie din cache, păstrează tag, date, valid, dirty, timestamp pentru LRU/FIFO
•	CacheMemory – Logica cache-ului: mapare, înlocuire, operații read/write
•	CacheCellRenderer – Evidențierea vizuală a liniilor afectate în JTable
•	CacheSimulatorGUI – GUI-ul principal și orchestratorul operațiilor
•	StatsManager – Calcularea și gestionarea statisticilor de performanță
Instalare și rulare
1.	Deschide proiectul într-un IDE Java (ex. IntelliJ IDEA, Eclipse) sau în linia de comandă.
2.	Compilează proiectul.
3.	Rulează clasa principală:
java CacheSimulatorGUI
4.	Interfața GUI se va deschide, afișând zona de control, tabelul cache și zona de log/statistici.
Testare
Simulatorul include mai multe scenarii de testare:
•	Operații de bază: citire și scriere în cache
•	Politici de înlocuire: LRU, FIFO, Random
•	Politici de scriere: Write-Through vs Write-Back
•	Tipuri de mapare: Direct, Set-Associative, Fully Associative
•	Scenarii de performanță: acces secvențial, aleator, cache mic/mare
•	Salvare log și statistici în fișiere externe
•	Edge cases: adrese invalide, cache plin, reset în timpul utilizării
Parametri configurabili
•	Dimensiune cache: 4, 8 sau 16 linii
•	Mapare: Direct, Set-Associative, Fully Associative
•	Înlocuire: LRU, FIFO, Random
•	Scriere: Write-Through, Write-Back

