# MemoLoop

<p align="center">
  <img src="ic_launcher-playstore.png" width="120" alt="MemoLoop Icon"/>
</p>

<p align="center">
  <strong>每天 30 張單字卡，養成英文記憶迴圈</strong>
</p>

---

## 簡介

MemoLoop 是一款 Android 英文單字學習 App，採用「翻卡 + 自評」的方式幫助使用者記憶單字。每次複習隨機抽出 30 張單字卡，依據掌握程度即時調整出現順序，搭配連續天數挑戰與成就系統，讓背單字不再枯燥。

## 功能特色

### 翻卡複習

- 每次複習從選定難度的詞庫中隨機抽取 **30 張單字卡**
- 每張卡片包含英文單字、中文釋義、詞性標示與例句
- 點擊喇叭圖示可聽取 **TTS 發音**
- 四個自評按鈕決定卡片去向：
  | 按鈕 | 效果 |
  |------|------|
  | Again | 立即重新出現 |
  | Hard | 排入下一張之後 |
  | Good | 移到隊列末端 |
  | Easy | 從本次複習中移除 |

### 三種難度等級

| 等級 | 詞庫規模 | 適合對象 |
|------|---------|---------|
| Beginner | 800 字 | 國中程度 |
| Advanced | 2,500 字 | 高中程度 |
| Professional | 5,000+ 字 | 多益考試準備 |

### 連續天數 & 成就系統

透過每日複習累積連續天數（streak），解鎖五階成就：

| 成就 | 條件 |
|------|------|
| 🥉 Rookie | 完成第一次複習 |
| 🔶 Bronze Apprentice | 連續 3 天 |
| 🥈 Silver Expert | 連續 7 天 |
| 🥇 Gold Master | 連續 14 天 |
| 💎 Platinum Legend | 連續 30 天 |

解鎖新成就時會跳出慶祝彈窗。

### 學習紀錄

- **月曆檢視**：以圓點標示有複習的日期，快速瀏覽學習頻率
- **月度統計**：活躍天數、總複習時間、已複習單字數
- **歷史紀錄**：列出每次複習的日期與花費時間

## 畫面導覽

```
首頁（Home）
├── 難度選擇器
├── 連續天數 & 總複習次數
└── START 按鈕
        ↓
複習畫面（Review）
├── 進度條 & 計時器
├── 單字卡（英文 / 中文釋義 / 例句）
└── Again / Hard / Good / Easy
        ↓
結果畫面（Result）
├── 完成時間
├── 成就解鎖通知（若有）
└── 返回首頁

紀錄畫面（Records）── 月曆 + 統計 + 歷史列表

成就畫面（Awards）── 金字塔式成就展示 + 連續天數
```

## 技術架構

| 項目 | 技術 |
|------|------|
| 語言 | Kotlin |
| UI | XML Layouts + Material Design 3 |
| 架構模式 | MVVM（ViewModel + LiveData） |
| 本地資料庫 | Room |
| 導覽 | Navigation Component |
| 非同步處理 | Kotlin Coroutines |
| JSON 解析 | Gson |
| View 綁定 | ViewBinding |
| 最低版本 | Android 8.0（API 26） |
| 目標版本 | Android 15（API 35） |

### 專案結構

```
app/src/main/
├── java/com/memoloop/app/
│   ├── MainActivity.kt
│   ├── data/
│   │   ├── db/          # Room 資料庫、DAO、Entity
│   │   ├── model/       # Word、DifficultyLevel 等資料模型
│   │   └── repository/  # WordRepository、SessionRepository
│   └── ui/
│       ├── home/        # 首頁
│       ├── review/      # 複習 & 結果畫面
│       ├── history/     # 學習紀錄
│       └── prize/       # 成就系統
├── res/
│   ├── raw/             # 單字詞庫 JSON（junior / senior / toeic）
│   ├── layout/          # 畫面佈局
│   ├── navigation/      # 導覽圖
│   └── values/          # 字串、顏色、主題
└── AndroidManifest.xml
```

## 建置與執行

### 環境需求

- Android Studio Ladybug 以上
- JDK 17
- Android SDK 35

### 步驟

```bash
# 1. 複製專案
git clone https://github.com/<your-username>/memoloop.git

# 2. 用 Android Studio 開啟專案

# 3. 同步 Gradle 依賴

# 4. 連接 Android 裝置或啟動模擬器，點擊 Run
```

## 授權條款

本專案僅供學習與個人使用。
