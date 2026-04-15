# Primo's Advanced Recipe Guide

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.10-green.svg)](https://minecraft.net)
[![Fabric API](https://img.shields.io/badge/Fabric%20API-0.138.4-blue.svg)](https://fabricmc.net)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A client-side Minecraft Fabric mod that revolutionizes the crafting experience with an advanced search-based recipe guide.

## ✨ Features

- 🔍 **Real-time Item Search** - Type to instantly filter craftable items
- 📋 **Recipe Preview** - View 3x3 crafting grid with ingredients
- ✅ **Craftability Detection** - Green checkmark when you have materials, yellow warning when missing
- 🖱️ **One-Click Crafting** - "Craft Here" button opens crafting table with recipe pre-filled
- 🔄 **Multi-Recipe Support** - Cycle through alternative recipes with arrows
- 🎨 **Vanilla-Style UI** - Seamlessly blends with Minecraft's aesthetic
- ⌨️ **Intuitive Controls** - ESC to navigate back, manual search bar focus

## 📸 Screenshots

*Search Bar above crafting table*
*Item search results grid*
*Recipe view with craftability status*

## 🚀 Installation

### Requirements
- Minecraft 1.21.10
- Fabric Loader 0.18.4 or higher
- Fabric API 0.138.4 or higher

### Steps
1. Install [Fabric Loader](https://fabricmc.net/use/)
2. Download the latest release from [GitHub Releases](../../releases)
3. Place the `.jar` file in your `mods` folder
4. Launch Minecraft with Fabric profile

## 🎮 Usage

1. **Open a crafting table** in Survival mode (with vanilla recipe book disabled)
2. **Click the search bar** above the GUI
3. **Type to search** - Results appear instantly as you type
4. **Click an item** to view its recipe
5. **Check craftability** - Green message if you have materials, yellow if missing
6. **Click "Craft Here"** (when available) to open crafting table with recipe pre-filled

### Keybinds
| Key | Action |
|-----|--------|
| `ESC` | Go back / Close |
| `Mouse Click` | Focus search bar (manual only) |

## 🛠️ Development

### Setup
```bash
git clone https://github.com/MuteMask/primos-advanced-recipe-guide.git
cd primos-advanced-recipe-guide
./gradlew build
