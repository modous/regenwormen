<script setup>
import wormImage from '../assets/regenwormSpel.png'
</script>

<template>
    <section class="start-screen">
        <section class="left-side">
           <a  class="title-link" href="#home"><h1 class="title">Regenwormen</h1></a>
           <router-link to="/how-to-play" class="start-btn">Start Game</router-link>
            <nav class="nav-links">
               <router-link to="/how-to-play" class="nav-link">How to Play</router-link>
               <router-link to="/credits" class="nav-link">Credits</router-link>
               <router-link to="/login" class="nav-link">Login</router-link>
               <router-link to="/register" class="nav-link">Register</router-link>
               <router-link to="/exit" class="nav-link">Exit</router-link>
            </nav>
        </section>

        <section class="right-side">
            <img :src="wormImage" alt="Regenwormen plaatje" class="worm-img" />
        </section>
    </section>
</template>

<style scoped>
.start-screen {
  display: block; 
  padding: 2rem;
}

.nav-links {
  display: flex;
  flex-direction: column; 
  margin-top: 2rem;
}

.nav-link {
  display: block;
  font-size: 1.5rem;
  color: #3b2c20; 
  font-weight: bold;
  text-decoration: none;
  transition: 0.3s;
  text-shadow: 2px 4px 2px rgba(255,255,255,0.6); 
  margin-bottom: 1rem;

}

.nav-link:hover {
  background-color: rgba(255, 251, 254, 0.844);
  padding-left: 0.5rem;
  border-radius: 4px;
}
.title-link {
  text-decoration: none;
}
.title {
  font-size: 3rem;
  color: black;
  margin-bottom: 4rem;
  text-shadow: 2px 2px #fff;
  transition: transform 0.5s, text-shadow 0.5s;
}
.title:hover {
  text-shadow: 2px 4px 3px rgba(0, 0, 0, 0.3);
  transform: translateY(-2px);
}

.worm-img {
  max-width: 200px;
}

.start-btn {
  display: inline-block;
  padding: 0.5rem 2rem;
  font-size: 1.2rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  background-color: #b10c96;
  color: white;
  transition: 0.3s;
  text-decoration: none;
}

.start-btn:hover {
  background-color: #770494;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  transform: translateY(-2px);
}

@media (min-width: 1200px) {
  .start-screen {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;       
  }

  .right-side {
  display: flex;
  justify-content: center;
  align-items: center;    
  flex: 1;
  padding: 2rem;
}

  .worm-img {
  width: 50%;          
  max-width: 800px;    
  height: auto;        
}
.title {
  font-size: 4rem;
}

}
</style>
