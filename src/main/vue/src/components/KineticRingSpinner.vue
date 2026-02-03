<template>
  <div class="kinetic-ring-spinner">
    <div class="kinetic-ring">
      <div class="ring-track"></div>
      <div class="ring-beam"></div>
    </div>
  </div>
</template>

<style scoped>
.kinetic-ring-spinner {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  width: 100%;
  background: white;
}

.kinetic-ring {
  position: relative;
  width: 160px;
  height: 160px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.ring-track {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 12px solid #f1f5f9;
}

.ring-beam {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 12px solid transparent;
  background: conic-gradient(
    from 0deg,
    transparent 0%,
    #7f42b8 15%,
    #a855f7 25%,
    transparent 35%
  ) border-box;
  -webkit-mask: linear-gradient(#fff 0 0) padding-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: destination-out;
  mask-composite: exclude;
  filter: drop-shadow(0 0 8px rgba(127, 66, 184, 0.6));
  animation: rotate 1.2s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
