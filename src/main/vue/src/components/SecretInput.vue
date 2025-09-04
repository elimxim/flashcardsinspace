<template>
  <div class="secret-input">
    <input
      class="input secret-input__input transition--border-color"
      :value="modelValue"
      :type="showPassword ? 'text' : 'password'"
      @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
      v-bind="$attrs"
      :placeholder="placeholder"
    />
    <button
      type="button"
      class="button secret-input__button transition--color"
      ref="showPasswordButton"
      tabindex="-1"
      @click="toggleShowPassword">
      <font-awesome-icon :icon="showPassword ? 'fa-solid fa-eye-slash' : 'fa-solid fa-eye'"/>
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

defineOptions({
  inheritAttrs: false
})

defineProps<{
  modelValue: string | undefined,
  placeholder: string,
}>()

defineEmits(['update:modelValue'])

const showPassword = ref(false)
const showPasswordButton = ref<HTMLButtonElement>()

function toggleShowPassword() {
  showPassword.value = !showPassword.value
  showPasswordButton.value?.blur()
}
</script>

<style scoped>
.secret-input {
  position: relative;
  width: 100%;
}

.secret-input__input {
  width: 100%;
}

.secret-input__button {
  position: absolute;
  top: 0;
  bottom: 0;
  right: 0;
  width: 3rem;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  color: var(--secret-input__button--color, rgba(140, 140, 140, 0.5));
}

.secret-input__button:hover {
  color: var(--secret-input__button--hover--color, rgba(140, 140, 140, 0.8));
}

</style>
