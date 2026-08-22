
Installation information
=======

This template repository can be directly cloned to get you started with a new
mod. Simply create a new repository cloned from this one, by following the
instructions provided by [GitHub](https://docs.github.com/en/repositories/creating-and-managing-repositories/creating-a-repository-from-a-template).

Once you have your clone, simply open the repository in the IDE of your choice. The usual recommendation for an IDE is either IntelliJ IDEA or Eclipse.

If at any point you are missing libraries in your IDE, or you've run into problems you can
run `gradlew --refresh-dependencies` to refresh the local cache. `gradlew clean` to reset everything 
{this does not affect your code} and then start the process again.

Mapping Names:
============
By default, the MDK is configured to use the official mapping names from Mojang for methods and fields 
in the Minecraft codebase. These names are covered by a specific license. All modders should be aware of this
license. For the latest license text, refer to the mapping file itself, or the reference copy here:
https://github.com/NeoForged/NeoForm/blob/main/Mojang.md

Additional Resources: 
==========
Community Documentation: https://docs.neoforged.net/  
NeoForged Discord: https://discord.neoforged.net/



ST:
==========
teammod/<br>
 ├── TeamMod.java            <-- ① MODのメインクラス<br>
 ├── client/ (クライアント側・描画関連)<br>
 │    ├── ClientPayloadHandler.java <-- ② パケット受信時の処理<br>
 │    └── InGameHudRenderer.java    <-- ③ 画面にHUDを描画する処理<br>
 ├── network/ (通信・パケット関連)<br>
 │    ├── PlayerDataPayload.java    <-- ④ 送るデータの箱（パケット定義）<br>
 │    └── ServerNetworkHandler.java <-- ⑤ サーバーからデータを送るタイマー<br>
 └── server/ (サーバー側・データ管理)<br>
      └── ServerPayloadHandler.java <-- (将来の拡張用)<br>