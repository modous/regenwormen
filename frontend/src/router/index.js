import { createRouter, createWebHistory } from 'vue-router';

import StartScreen from '../views/StartScreen.vue';
import HowToPlay from '../views/HowToPlay.vue';
import Credits from '../views/Credits.vue';
import Login from '../views/Login.vue';
import Register from '../views/Register.vue';
import App from '../App.vue'; // gamecomponentai

const routes = [
    { path: '/', name: 'Home', component: StartScreen },
    { path: '/how-to-play', name: 'HowToPlay', component: HowToPlay },
    { path: '/credits', name: 'Credits', component: Credits },
    { path: '/login', name: 'Login', component: Login },
    { path: '/register', name: 'Register', component: Register },
    { path: '/game', name: 'Game', component: App },
];

export default createRouter({
    history: createWebHistory(),
    routes,
});
