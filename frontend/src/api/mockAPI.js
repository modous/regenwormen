// Mock API-service
let gameState = {
    rolledDice: [],
    collectedDice: [],
    tilesOnTable: [
        { value: 21, worms: 1 },
        { value: 22, worms: 1 },
        { value: 23, worms: 1 },
        { value: 24, worms: 2 },
        { value: 25, worms: 2 },
        { value: 26, worms: 2 },
        { value: 27, worms: 2 },
        { value: 28, worms: 2 },
        { value: 29, worms: 3 },
        { value: 30, worms: 3 },
        { value: 31, worms: 3 },
        { value: 32, worms: 3 },
        { value: 33, worms: 4 },
        { value: 34, worms: 4 },
        { value: 35, worms: 4 },
        { value: 36, worms: 4 },
    ],
    players: [
        { id: 1, name: "Jij", tiles: [] },
        { id: 2, name: "Tegenstander 1", tiles: [] },
        { id: 3, name: "Tegenstander 2", tiles: [] },
    ],
    currentPlayerId: 1
};

export async function getGameState() {
    return structuredClone(gameState);
}

export async function rollDice() {
    const faces = ["SPECIAL", 1, 2, 3, 4, 5];
    const diceCount = 5;
    const rolled = Array.from({ length: diceCount }, () => {
        return faces[Math.floor(Math.random() * faces.length)];
    });
    gameState.rolledDice = rolled;
    return rolled;
}

export async function pickDie(die) {
    gameState.collectedDice.push(die);
    gameState.rolledDice = gameState.rolledDice.filter(d => d !== die);
    return { collectedDice: gameState.collectedDice, rolledDice: gameState.rolledDice };
}

export async function pickTile(tile) {
    const player = gameState.players.find(p => p.id === gameState.currentPlayerId);
    player.tiles.push(tile);
    gameState.tilesOnTable = gameState.tilesOnTable.filter(t => t.value !== tile.value);
    return { player, tilesOnTable: gameState.tilesOnTable };
}
