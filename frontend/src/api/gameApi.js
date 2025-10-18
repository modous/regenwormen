import axios from "axios";

const API_BASE = "http://localhost:8080/pregame"; // pas poort aan als nodig

export async function listGames() {
    const res = await axios.get(`${API_BASE}/games`);
    return res.data;
}

export async function getGame(gameId) {
    const res = await axios.get(`${API_BASE}/${gameId}`);
    return res.data;
}

export async function createGame(roomName, maxPlayers) {
    const res = await axios.post(`${API_BASE}/create`, { roomName, maxPlayers });
    return res.data;
}

export async function deleteGame(gameId) {
    const res = await axios.delete(`${API_BASE}/${gameId}/delete`);
    return res.data;
}

export async function listPlayers() {
    const res = await axios.get(`${API_BASE}/players`);
    return res.data;
}

export async function joinGame(gameId, playerId) {
    const res = await axios.post(`${API_BASE}/${gameId}/join?playerId=${playerId}`);
    return res.data;
}

export async function leaveGame(gameId, playerId) {
    const res = await axios.post(`${API_BASE}/${gameId}/leave`, playerId, {
        headers: { "Content-Type": "text/plain" }
    });
    return res.data;
}

export async function startGame(gameId) {
    const res = await axios.post(`${API_BASE}/${gameId}/start`);
    return res.data;
}
