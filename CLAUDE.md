# MemoLoop — Claude Code 專案規則

## 專案概要

MemoLoop 是一個 Android 英文學習 App（Kotlin, Material Design 3），透過間隔重複學習單字與聽力。
已上架 Google Play Console 內部測試軌道。

## 版本管理

**每次修正或新增功能，都必須 bump 版號。**

- `versionCode`：每次 +1（Google Play Console 以此判斷是否為新版本，重複會被拒絕上傳）
- `versionName`：patch 修正 +0.0.1，feature +0.1.0
- 位置：`app/build.gradle.kts` 的 `defaultConfig` 區塊
- 版號 bump 應包含在最終 commit 中，不可遺漏

## i18n（多國語言）

**所有使用者可見的字串一律禁止 hardcode。**

1. 英文預設字串定義在 `res/values/strings.xml`
2. 繁體中文翻譯放在 `res/values-zh-rTW/strings.xml`
3. Layout XML 使用 `@string/key_name`，Kotlin 使用 `getString(R.string.key_name)`
4. 新增任何字串時，**兩個檔案都要同步更新**
5. 完成前 diff 兩個檔案，確認 key 一致

例外（不需 zh-rTW，使用 default fallback）：
- `icon_*`（emoji 字元）
- 純格式 pattern（`time_fmt`、`percent_fmt` 等）
- `stat_placeholder*`、`time_placeholder`

## Android AlertDialog 注意事項

- `setMessage()` 和 `setItems()` 互斥 — 同時使用時 message 會佔據內容區，items 不顯示
- `setView()` 和 `setItems()` 也互斥
- 需要同時顯示說明文字和列表項目時，改用自訂 layout 或只保留其中一個

## 通知與提醒

- Android 13+（API 33）需在 runtime 請求 `POST_NOTIFICATIONS` 權限
- Android 12+（API 31）精確鬧鐘需要 `SCHEDULE_EXACT_ALARM` 權限
- `setInexactRepeating()` 在 Doze 模式下不可靠，應使用 `setExactAndAllowWhileIdle()`
- 精確鬧鐘不支援 repeat，需在 BroadcastReceiver 中排程下一次
