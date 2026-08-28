<template>
  <button
    type="button"
    class="picture-thumb-button picture-thumb-button--theme"
    :class="{ 'picture-thumb-button--active': active }"
    title="Picture options"
    @click="onClick"
  >
    <img v-if="url" :src="url" class="picture-thumb-button-image" alt="Picture preview"/>
  </button>
</template>

<script lang="ts" setup>
import { onBeforeUnmount } from 'vue'

const url = defineModel<string | undefined>('url', { required: true })

withDefaults(defineProps<{
  active?: boolean
  onClick?: () => void
}>(), {
  active: false,
  onClick: () => {
  }
})

function revoke() {
  if (url.value) {
    URL.revokeObjectURL(url.value)
    url.value = undefined
  }
}

defineExpose({
  revoke
})

onBeforeUnmount(() => {
  revoke()
})

</script>

<style scoped>
.picture-thumb-button--theme {
  --thumb-btn--size: var(--picture-thumb-button--size, 2rem);
  --thumb-btn--ring-color: var(--picture-thumb-button--ring-color, #818181);
  --thumb-btn--ring-color--hover: var(--picture-thumb-button--ring-color--hover, #404040);
  --thumb-btn--ring-color--active: var(--picture-thumb-button--ring-color--active, #000000);
  --thumb-btn--border-radius: var(--picture-thumb-button--border-radius, 999px);
  --thumb-btn--scale-factor--on-hover: var(--awesome-button--scale-factor--on-hover, 1.1);
  --thumb-btn--scale-factor--on-active: var(--awesome-button--scale-factor--on-active, 0.9);
}

.picture-thumb-button {
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  width: var(--thumb-btn--size);
  height: var(--thumb-btn--size);
  padding: 0;
  margin: 2px;
  border: 2px solid var(--thumb-btn--ring-color);
  border-radius: var(--thumb-btn--border-radius);
  background: none;
  cursor: pointer;
  overflow: hidden;
  flex-shrink: 0;
  transform-origin: center;
  transition: all 0.2s ease-in-out;
}

.picture-thumb-button--active {
  border: 2px solid var(--thumb-btn--ring-color--active);
}

@media (hover: hover) {
  .picture-thumb-button:hover {
    border: 2px solid var(--thumb-btn--ring-color--hover);
    transform: scale(var(--thumb-btn--scale-factor--on-hover));
  }
}

.picture-thumb-button:active {
  transform: scale(var(--thumb-btn--scale-factor--on-active));
}

.picture-thumb-button-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
</style>
