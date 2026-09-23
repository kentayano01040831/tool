# Island Compare

Galaxy A55 5G向けの商品単価比較アプリです。

## 現在の機能

- 商品名・価格・数量の入力
- 1個あたりの単価計算
- 安い商品の判定
- 1個あたりの差額表示
- Androidの画面上部に結果を表示するフローティング機能
- GitHub ActionsによるAPK自動ビルド

## APKの作成方法

1. GitHubの **Actions** を開く
2. `Build Android APK` を選択
3. `Run workflow` を押す
4. 完了後、実行結果の **Artifacts** から `island-compare-debug` をダウンロード
5. ZIPを展開し、APKをGalaxy A55にインストール

## 画面上部表示について

初回利用時はAndroidの「他のアプリの上に重ねて表示」権限が必要です。

※現在は開発版です。