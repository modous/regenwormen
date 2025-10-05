import { createRouter, createWebHistory } from 'vue-router';

import StartScreen from '../components/StartScreen.vue';
import HowToPlay from '../components/HowToPlay.vue';
import Credits from '../components/Credits.vue';
import Login from '../components/Login.vue';
import Register from '../components/Register.vue';
import GameMain from '../components/Game/GameMain.vue';

const routes = [
  { path: '/', name: 'Home', component: StartScreen },
  { path: '/how-to-play', name: 'HowToPlay', component: HowToPlay },
  { path: '/credits', name: 'Credits', component: Credits },
  { path: '/login', name: 'Login', component: Login },
  { path: '/register', name: 'Register', component: Register },
    { path: '/game', name: 'GameMain', component: GameMain },
];

const router = createRouter({
  history: createWebHistory(), 
  routes,
});

export default router;
