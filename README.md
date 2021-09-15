# BotAPI

##### Customizable base for adventure game, served with (private) REST API usable for example in Discord Bots.

### TODOs:

- [ ] Auth so API is really private
- [x] Player
  - [x] Basic management (create, get)
  - [x] Money
  - [x] XP
- [ ] Items
  - [x] Basic info (name, description)
  - [ ] Item categories
    - [ ] Swords
    - [ ] Armor
    - Something else, maybe potions, but what will they do in this type of game?
- [x] Inventory
- [x] Support for translations
  - [x] Language translations
  - [x] Platform translations (for example money icon in Discord could be custom emote string `<:money:0123456789>` and on web it could be url `https://example.com/money.png`)
- [x] Daily bonuses
- [ ] Time limited events
  - [ ] Quests
  - [ ] Monsters
    - [ ] Random encounters
    - [ ] Fights
  - [ ] Work
    - Freezes all interactions for that time? Can be stopped? (with heavy penalty?)
- [ ] Guilds
  - [ ] Guild bonuses
  - [ ] Guild quests
  - [ ] Guild fights
