import axios from "axios";

const PREGAME_API = "http://localhost:8080/pregame";
const INGAME_API = "http://localhost:8080/ingame";

// ---------- PREGAME ----------
export async function getGameState(gameId, playerId) {
    const res = await axios.get(`${PREGAME_API}/game/${gameId}/state`, {
        params: { playerId },
    });
    return res.data;
}

export async function listGames() {
    const res = await axios.get(`${PREGAME_API}/games`);
    return res.data;
}

export async function getGame(gameId) {
    const res = await axios.get(`${PREGAME_API}/${gameId}`);
    return res.data;
}

export async function createGame(roomName, maxPlayers) {
    const res = await axios.post(`${PREGAME_API}/create`, { roomName, maxPlayers });
    return res.data;
}

export async function deleteGame(gameId) {
    const res = await axios.delete(`${PREGAME_API}/${gameId}/delete`);
    return res.data;
}

export async function listPlayers() {
    const res = await axios.get(`${PREGAME_API}/players`);
    return res.data;
}

export async function joinGame(gameId, playerId) {
    const res = await axios.post(`${PREGAME_API}/${gameId}/join/${playerId}`);
    return res.data;
}

export async function leaveGame(gameId, playerId) {
    const res = await axios.post(`${PREGAME_API}/${gameId}/leave/${playerId}`);
    return res.data;
}

export async function startGame(gameId) {
    const res = await axios.post(`${PREGAME_API}/${gameId}/start`);
    return res.data;
}

// ---------- INGAME ----------
export async function startRoll(gameId, playerId) {
    const res = await axios.post(`${INGAME_API}/${gameId}/startroll/${playerId}`);
    return res.data;
}

export async function reRoll(gameId, playerId) {
    const res = await axios.post(`${INGAME_API}/${gameId}/reroll/${playerId}`);
    return res.data;
}
