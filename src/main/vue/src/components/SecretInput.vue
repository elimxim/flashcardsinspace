<template>
  <div class="secret-input">
    <input
      :value="modelValue"
      :type="showPassword ? 'text' : 'password'"
      @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
      v-bind="$attrs"
    />
    <button
      type="button"
      @click="toggleShowPassword">
      <font-awesome-icon :icon="showPassword ? 'fa-solid fa-eye-slash' : 'fa-solid fa-eye'"/>
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

defineProps<{
  modelValue: string | undefined,
  placeholder: string,
}>()

defineEmits(['update:modelValue'])

const showPassword = ref(false)

function toggleShowPassword() {
  showPassword.value = !showPassword.value
}
</script>

<style scoped>
.secret-input {
  position: relative;
  width: 100%;
}

.secret-input input {
  width: 100%;
  box-sizing: border-box;
}

.secret-input button {
  position: absolute;
  top: 0;
  bottom: 0;
  right: 0;
  width: 3rem;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  cursor: pointer;
  color: var(--secret-toggler--color, rgba(140, 140, 140, 0.5));
}

.secret-input button:hover {
  color: var(--secret-toggler_hover--color, rgba(140, 140, 140, 0.8));
}

/* Hide the default password reveal eye icon in Microsoft Edge */
input[type="password"]::-ms-reveal {
  display: none;
}
</style>
