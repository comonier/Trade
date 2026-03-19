# 📦 Trade Plugin v1.1
**Secure & Advanced Trading System for Minecraft 1.21.1+**

This plugin provides a robust, cross-platform (Java/Bedrock via Geyser) trading environment. It supports physical items, **Vault (Economy)**, and **GriefPrevention (Claim Blocks)**, ensuring that long-distance trades are safe and scam-proof.

---

## 🛠 v1.1 Changelog (Hotfixes)
*   **UI Restoration:** Restored the lore on Player Skulls inside the trade menu, showing Coins and Claim Blocks in real-time.
*   **Discord Webhook:** Fixed Error 400 (Bad Request) by implementing proper JSON escaping for line breaks (\n) and special characters.
*   **Ender Eye Metadata:** Fixed the "Sync Button" to hide enchantment technical text while maintaining the glow effect (ItemFlag.HIDE_ENCHANTS).
*   **Version Consistency:** Updated all internal hooks to dynamically pull the version from plugin.yml.

---

## 🚀 Features
*   **UUID Support:** Fully compatible with Bedrock/Geyser players.
*   **Currency Trading:** Trade Vault Economy (Coins) directly in the GUI.
*   **Land Protection Trading:** Trade GriefPrevention Claim Blocks.
*   **Manual Sync System:** Integrated "Ender Eye" refresh button to prevent visual desync.
*   **Security Chest:** Received items are sent to a virtual chest to prevent inventory overflow.
*   **Discord Integration:** Logs all successful trades via Webhooks.

---

## 🎮 Commands


| Command | Description |
| :--- | :--- |
| `/trade <player>` | Sends a trade invitation to a player. |
| `/trade accept` | Accepts a pending trade invitation. |
| `/trade chest` | Opens your virtual chest to retrieve received items. |
| `/trade reload` | Reloads plugin configurations (Admin only). |
| `/trade help` | Shows the help menu. |

---

## 📖 How to Trade (Tutorial)

### Step 1: Sending an Invite
Use `/trade <playername>` to invite someone. The recipient has **30 seconds** to type `/trade accept` before the request expires.

### Step 2: Adding Items and Values
Once the GUI opens, you can:
1.  **Click items** in your inventory to move them to your trade slots.
2.  Use the **Emerald/Redstone** icons to add or remove **Coins**.
3.  Use the **Golden/Wooden Shovel** icons to add or remove **Claim Blocks**.

### Step 3: Verifying the Trade (Crucial)
Because of server-client synchronization, you should **always click the Ender Eye icon** before accepting. This will force a refresh of your screen, showing you exactly what the other player has offered and their current readiness status.

### Step 4: Accepting
*   Click the **Green Terracotta** to mark yourself as ready.
*   Your status pane will turn **Lime (Green)**.
*   The other player will see their status pane turn **Orange**, signaling they need to confirm.
*   Once both players are "Green", a **3-second countdown** begins. If anyone changes anything during this time, the trade resets for safety.

### Step 5: Retrieving Items
After a successful trade, your items are **NOT** placed directly in your inventory. Use **`/trade chest`** to collect them. This prevents items from falling on the ground if your inventory is full.

---

## 🖼 GUI Icon Legend



| Icon | Name | Function |
| :--- | :--- | :--- |
| 👁️ **Ender Eye** | **Refresh Vision** | **IMPORTANT:** Syncs the menu to show the latest changes from the other player. |
| 🟢 **Green Terracotta** | **Accept** | Marks your side of the trade as ready. |
| 🔴 **Red Glass Pane** | **Cancel** | Immediately cancels the trade and returns all items. |
| 🟢 **Emerald** | **Add Coins** | Adds +100 Coins from your balance to the trade. |
| 🔴 **Redstone** | **Remove Coins** | Removes -100 Coins from the trade back to your balance. |
| 🟡 **Golden Shovel** | **Add Blocks** | Adds +100 Claim Blocks to the trade. |
| 🟫 **Wooden Shovel** | **Remove Blocks** | Removes -100 Claim Blocks from the trade. |

### Status Indicators (Center Panes):
*   🟨 **Yellow:** Waiting for changes or initial state.
*   🟧 **Orange:** The other player has accepted; you need to verify and accept too.
*   🟩 **Lime:** You have accepted and are waiting for the other player.

---

## ⚠️ Safety Warning
> **NEVER** accept a trade without clicking the **Ender Eye (Refresh)** button first. Always verify the items and the total amount of Coins/Blocks. 
> 
> **Inventory Safety:** Always keep your `/trade chest` clear. If it reaches 27 items, you won't be able to start new trades until you withdraw your items.
