# PAPER-LRAKE-IoT
一個適用於物聯網裝置並具備洩漏存活特性的可認證金鑰交換協定 

中文摘要 
 
適用於客戶端-伺服器端(client-server)環境的可認證金鑰交換協定(Authenticated key exchange，簡稱 AKE)是一個重要的密碼學基礎;
它可提供客戶端和伺服器端雙邊互相認證及安全通訊的功能。在物聯網 (Internet of Things，簡稱 IoT)的環境下，客戶端通常使用具備計算能力有限的物聯網裝置透過網際網路與伺服器做存取服務。
很多適合物聯網裝置的 AKE 協定(簡稱 AKE-IoT)已經被提出;這些協定允許物聯網下的客戶端與伺服器端運用他們長期和短期的秘密金鑰去建立一把共同的通訊金鑰(session key)來進行安全通訊，並達到相互認證。 
然而，最近幾年來有個名為旁路攻擊(side-channel attacks) 的新型態攻擊已經被實做出來去危害到許多傳統的密碼學機制，其中也包含 AKE-IoT，因為攻擊者可以在這些機制的運算過程中得到秘密金鑰的部分內容。因此一些能夠抵擋旁路攻擊的 AKE 協定(leakage-resilient AKE，簡稱 LRAKE)就被提出，不幸的，這些LRAKE 協定都不適用於資源有限的物聯網裝置，這是因為在客戶端需要許多昂貴的配對(pairing)運算。
因此，在本篇論文中，我們提出第一個適用於物聯網裝置的 LRAKE 協定(簡稱 LRAKE-IoT)，藉由不平衡的計算方法(unbalanced computation method)，使得在客戶端不需要配對運算。
在一般雙線性配對群模式(generic bilinear pairing group model，簡稱 GBPG model)下，此協定的安全性分析被提出，證明它在面對連續洩漏的 eCK 模型(continuous-leakage-resilient extended
Canetti–Krawczyk model，簡稱 CLR-eCK model)下的攻擊者時是安全的。
最後，在一個低運算能力的物聯網裝置(如 Raspberry PI)上進行效能分析，結果顯示出我們的協定非常適用於物聯網裝置。 
 
 
簡言之，AKE是一個重要的密碼學基礎，他能讓雙方在一個不安全的管道下，已非對稱式的方式進行互相辨認對方身分並共同建立一把通訊金鑰，再以此把鑰匙進行對稱式加解密。如此一來就不怕來不及事先跟對方約定好鑰匙或是鑰匙中途發生洩漏，但由於IoT裝置通常運算能力較弱且更容易被旁路攻擊給攻擊，因此我們的協定在降地了Client端的運算量的同時仍可以抵擋上述攻擊，並正式的證明了其安全性。此處的程式碼是基於我們的協定做出的簡易通訊系統，SERVER端跟客戶端會經由一些步驟來造出一把同的通訊金鑰，並且之後的通訊過程均會透過此把鑰匙加解密，具體實際影片可參考https://www.youtube.com/watch?v=P2ShzvSf3LE&t=11s。或是LRAKE-IoT.mp4。

我們將協定的SERVER端實作在ECLIPSE軟體上，主要程式碼為PALSERVER.java 和他的工具 PALTool.java，在實際安裝建構過程中，需導入兩個由JPBC library所提供的Jar檔案和一個預設的參數表，分別是jpbc-api-2.0.0.jar和jpbc-plaf-2.0.0.jar和參數表a.properties，將其導入後即可正常使用，如需自行產生相關底層公共參數，需再導入 algs4.jar即可自行生產不同的參數，協定內容為SERVER開啟後會自行運行，port設定為8082，本人使用了NGROK這套軟體將PORT給轉成外部網址，以模擬非本地端的Client做連線。當Client連接時會進行協定中的3個步驟後，雙方確立了彼此身分並得到了一把共同通訊金鑰，以此來對後續的對話進行加解密。

另一方面，我們的Client實作在Android studio軟體上，主要程式碼為MClient.java，和另三個AndroidManifest.xml、MainActivity.java、PUtils.java，其中AndroidManifest.xml 需在裡面新增網路權限，MainActivity.java來實做互動功能，至於PUtils.java則是工具。因為我們SERVER是使用NGROK來實現對外網址的轉發和利用，在MClient.java運行之前要重新調整成當下NGROK顯示之PORT。當然所需要的參數表a.properties需放入在assets資料夾中，另兩個JAR黨也需導入。注意一點的是我們所採用的是API26以上的開發版本，請在建立模擬器的時候需選擇相對應的機型。當Client連接時會進行協定中的3個步驟後，雙方確立了彼此身分並得到了一把共同通訊金鑰，以此來對後續的對話進行加解密。
