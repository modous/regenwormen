# regenwormen

This template should help get you started developing with Vue 3 in Vite.

## Recommended IDE Setup

[VSCode](https://code.visualstudio.com/) + [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) (and disable Vetur).

## Customize configuration

See [Vite Configuration Reference](https://vite.dev/config/).

## Project Setup

```sh
npm install
```

### Compile and Hot-Reload for Development

```sh
npm run dev
```

### Compile and Minify for Production

```sh
npm run build
```

## API calls 

### in GameMain.vue

rollDice() Roept 
POST /ingame/{gameId}/startroll/{username} om de dobbelsteen de eerste keer te rollen 
en 
POST /ingame/{gameId}/reroll/{username} aan om dobbelstenen opnieuw te rollen.

trySelectDie(face) Roept 
POST /ingame/{gameId}/pickdice/{username} aan om een dobbelsteen te kiezen.

pickTile(tile) Roept 
POST /ingame/{gameId}/claimfrompot/{username} aan om een tegel te claimen.