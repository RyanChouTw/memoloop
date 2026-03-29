# MemoLoop

<p align="center">
  <img src="ic_launcher-playstore.png" width="120" alt="MemoLoop Icon"/>
</p>

<p align="center">
  <strong>每天 30 張單字卡 + 聽力故事，養成英文記憶迴圈</strong>
</p>

---

## 簡介

MemoLoop 是一款 Android 英文學習 App，結合**單字複習**與**聽力理解**兩大練習模式，搭配每日測驗、積分系統與成就挑戰，幫助使用者持續精進英文能力。

## 功能特色

### 單字複習

- 每次從選定難度的詞庫中隨機抽取 **30 張單字卡**
- 每張卡片包含英文單字、中文釋義、詞性標示與例句
- 點擊喇叭圖示可聽取 **TTS 發音**
- 四個自評按鈕決定卡片去向：
  | 按鈕 | 效果 |
  |------|------|
  | Again | 立即重新出現 |
  | Hard | 排入下一張之後 |
  | Good | 移到隊列末端 |
  | Easy | 從本次複習中移除 |

### 聽力練習

- 每次隨機抽取 **5 段英文短文故事**
- 系統透過 TTS 朗讀故事內容，畫面預設不顯示文字
- 每段故事搭配一道全英文理解選擇題
- 可展開原文對照，支援**選取文字 → 翻譯（Google 翻譯）/ 複製**
- 語速可調整：慢速（0.5x）、中速（0.75x）、正常（1x）
- 練習模式無次數限制，可反覆練習

### 三種難度等級

| 等級 | 單字詞庫 | 聽力故事 | 適合對象 |
|------|---------|---------|---------|
| Beginner | 800 字 | 100 篇 | 國中程度 |
| Advanced | 2,500 字 | 100 篇 | 高中程度 |
| Professional | 5,000+ 字 | 100 篇 | 多益考試準備 |

### 每日測驗 & 積分系統

- 測驗頁支援**單字測驗**與**聽力測驗**兩種模式切換
- 單字測驗：10 題選擇題（看中文釋義 → 選英文單字），全對才通過
- 聽力測驗：5 段故事理解題，全對才通過
- 每種測驗每天各一次機會
- 通過測驗後獲得積分，積分隨等級提升：

  | 等級 | 條件 | 積分 |
  |------|------|------|
  | 新手學員 | 完成首次複習 | 1 分 |
  | 青銅學徒 | 連續 3 天 | 3 分 |
  | 銀牌達人 | 連續 7 天 | 7 分 |
  | 黃金大師 | 連續 14 天 | 15 分 |
  | 白金傳奇 | 連續 30 天 | 30 分 |

- 積分按月累計，每月自動歸檔並重置

### 連續天數 & 成就系統

透過每日複習累積連續天數（streak），解鎖五階成就：

| 成就 | 條件 |
|------|------|
| 🥉 新手學員 | 完成第一次複習 |
| 🔶 青銅學徒 | 連續 3 天 |
| 🥈 銀牌達人 | 連續 7 天 |
| 🥇 黃金大師 | 連續 14 天 |
| 💎 白金傳奇 | 連續 30 天 |

解鎖新成就時會跳出慶祝彈窗。

### 學習紀錄

- **月曆檢視**：以圓點標示有複習的日期，快速瀏覽學習頻率
- **月度統計**：活躍天數、總複習時間、已複習單字數
- **歷史紀錄**：列出每次複習的日期與花費時間

## 畫面導覽

```
首頁（Home）
├── 設定（難度 + 語速）
├── 練習模式切換（單字 / 聽力）
├── 連續天數 & 總複習次數
└── START 按鈕
        ↓
┌─ 單字複習（Review）               ┌─ 聽力練習（Listening）
│  ├── 進度條 & 計時器               │  ├── 進度條（1/5）
│  ├── 單字卡                        │  ├── 播放按鈕 + 可展開原文
│  └── Again/Hard/Good/Easy          │  ├── 理解選擇題
│          ↓                         │  └── 選取文字 → 翻譯/複製
│  結果畫面（Result）                │          ↓
│  └── 返回首頁                      │  聽力結果畫面
└────────────────────────────────    └── 返回首頁

紀錄頁（Records）── 月曆 + 統計 + 複習歷史

測驗頁（Quiz）
├── 單字/聽力 切換
├── 月曆（今日顯示 GO 按鈕）
├── 當月積分 & 統計
└── 測驗歷史紀錄

成就頁（Awards）── 金字塔式成就展示 + 連續天數
```

**底部導覽列：** 首頁 → 紀錄 → 測驗 → 成就

## 技術架構

| 項目 | 技術 |
|------|------|
| 語言 | Kotlin |
| UI | XML Layouts + Material Design 3 |
| 架構模式 | MVVM（ViewModel + LiveData） |
| 本地資料庫 | Room（3 張表：review_sessions、quiz_results、listening_results） |
| 導覽 | Navigation Component |
| 非同步處理 | Kotlin Coroutines |
| JSON 解析 | Gson |
| View 綁定 | ViewBinding |
| 語音合成 | Android TTS（可調語速） |
| 國際化 | 英文預設 + 繁體中文（values-zh-rTW） |
| 最低版本 | Android 8.0（API 26） |
| 目標版本 | Android 15（API 35） |

### 專案結構

```
app/src/main/
├── java/com/memoloop/app/
│   ├── MainActivity.kt
│   ├── data/
│   │   ├── db/          # Room 資料庫、DAO（ReviewSession / QuizResult / ListeningResult）
│   │   ├── model/       # Word、DifficultyLevel、SpeechSpeed、ListeningStory 等
│   │   └── repository/  # WordRepository、SessionRepository、ScoreRepository、ListeningRepository
│   └── ui/
│       ├── home/        # 首頁（練習模式切換 + 設定）
│       ├── review/      # 單字複習 & 結果畫面
│       ├── listening/   # 聽力練習 & 結果畫面
│       ├── quiz/        # 測驗頁（單字/聽力切換、日曆、紀錄）
│       ├── history/     # 學習紀錄
│       └── prize/       # 成就系統
├── res/
│   ├── raw/             # 單字詞庫 JSON + 聽力故事 JSON（各 3 難度 × 100 篇）
│   ├── layout/          # 畫面佈局
│   ├── navigation/      # 導覽圖
│   ├── values/          # 字串（英文）、顏色、主題
│   └── values-zh-rTW/   # 繁體中文翻譯
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
git clone https://github.com/RyanChouTw/memoloop.git

# 2. 用 Android Studio 開啟專案

# 3. 同步 Gradle 依賴

# 4. 連接 Android 裝置或啟動模擬器，點擊 Run
```

## 授權條款

本專案僅供學習與個人使用。
