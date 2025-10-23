<template>
  <div class="rules-container">
    <img src="../assets/Gamerules.png" alt="Game Rules" class="rules-image" />
  </div>

</template>

<style scoped>
.rules-container {
  position: fixed;                 /* blijft over de hele pagina liggen */
  top: 0;
  left: 0;
  width: 100vw;                    /* volledige schermbreedte */
  height: 100vh;                   /* volledige schermhoogte */
  background-color: rgba(200, 200, 200, 0.6); /* lichtgrijs, half doorschijnend */
  display: flex;
  justify-content: center;         /* horizontaal centreren */
  align-items: center;             /* verticaal centreren */
  z-index: 1000;                   /* bovenop andere elementen */
  backdrop-filter: blur(4px);      /* optioneel: lichte achtergrondvervaging */
}

.rules-image {
  height: 75vh;
  width: auto;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
  object-fit: contain;
}

@media (max-width: 768px) {
  .rules-image {
    height: 60vh;
  }
}

</style>
