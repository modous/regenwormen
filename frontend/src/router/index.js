import { createRouter, createWebHistory } from 'vue-router';

import StartScreen from '../views/StartScreen.vue';
import HowToPlay from '../views/HowToPlay.vue';
import Credits from '../views/Credits.vue';
import Login from '../views/Login.vue';
import Register from '../views/Register.vue';
import GameMain from '../components/Game/GameMain.vue';
import LobbyOverview from "@/views/LobbyOverview.vue";
import LobbyRoom from "@/views/LobbyRoom.vue";

const routes = [
  { path: '/', name: 'Home', component: StartScreen },
  { path: '/how-to-play', name: 'HowToPlay', component: HowToPlay },
  { path: '/credits', name: 'Credits', component: Credits },
  { path: '/login', name: 'Login', component: Login },
  { path: '/register', name: 'Register', component: Register },
    { path: '/lobbies', component: LobbyOverview },
    { path: '/lobby/:id', component: LobbyRoom },
    { path: '/game', name: 'GameMain', component: GameMain },
];

const router = createRouter({
  history: createWebHistory(), 
  routes,
});

export default router;
