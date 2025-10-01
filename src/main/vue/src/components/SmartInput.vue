<template>
  <div
    class="smart-input smart-input--theme"
    :class="{ 'smart-input--error': invalid }"
  >
    <textarea
      v-if="area"
      v-model="model"
      :type="inputType"
      class="transition--border-color"
      :toast-type="$props.type"
      :placeholder="$props.placeholder"
      v-bind="$attrs"
    />
    <input
      v-else
      v-model="model"
      class="transition--border-color"
      :type="inputType"
      :placeholder="$props.placeholder"
      v-bind="$attrs"
    />
    <button
      v-if="!area && $props.type === 'password'"
      class="secret-button transition--color"
      type="button"
      ref="secretButton"
      @click="toggleShowPassword"
      tabindex="-1"
    >
      <font-awesome-icon :icon="showPassword ? 'fa-solid fa-eye-slash' : 'fa-solid fa-eye'"/>
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

type Type = 'text' | 'password' | 'email' | 'search' | 'image'

const model = defineModel<string>()

const props = withDefaults(defineProps<{
  type: Type
  placeholder: string
  area?: boolean
  invalid?: boolean
}>(), {
  area: false,
  invalid: false,
})

const showPassword = ref(false)
const secretButton = ref<HTMLButtonElement>()

const inputType = computed(() => {
  if (!props.area && props.type === 'password') {
    return showPassword.value ? 'text' : 'password'
  } else {
    return props.type
  }
})

function toggleShowPassword() {
  showPassword.value = !showPassword.value
  secretButton.value?.blur()
}

</script>

<style scoped>
.smart-input--theme {
  --inpt--font-family: var(--smart-input--font-family, var(--main-font-family));
  --inpt--font-size: var(--smart-input--font-size, clamp(0.9rem, 2vh, 1rem));
  --inpt--font-size--error: var(--smart-input--font-size--error, clamp(0.9rem, 2vh, 1rem));
  --inpt--color: var(--smart-input--color, #45454a);
  --inpt--color--error: var(--smart-input--color--error, #c80f0f);
  --inpt--bg-color: var(--smart-input--bg-color, #f9f9f9);
  --inpt--border-color: var(--smart-input--border-color, #b8c4d6);
  --inpt--border-color--focus: var(--smart-input--border-color--focus, #007bff);
  --inpt--border-color--error: var(--smart-input--border-color--error, #c80f0f);
  --inpt--border-radius: var(--smart-input--border-radius, 4px);
  --inpt--placeholder--font-family: var(--smart-input--placeholder--font-family, var(--main-font-family));
  --inpt--placeholder--color: var(--smart-input--placeholder--color, #b8c4d6);
  --inpt--secret-button--color: var(--smart-input--secret-button--color, #b8c4d6);
  --inpt--secret-button--color--hover: var(--smart-input--secret-button--color--hover, #007bff);
}

.smart-input {
  position: relative;
  width: 100%;
}

.smart-input textarea,
.smart-input input {
  width: 100%;
  font-family: var(--inpt--font-family);
  font-size: var(--inpt--font-size);
  color: var(--inpt--color);
  background-color: var(--inpt--bg-color);
  border-color: var(--inpt--border-color);
  border-radius: var(--inpt--border-radius);
  padding: clamp(0.75rem, 1.5vh, 1.25rem) clamp(0.75rem, 1vw, 1.25rem);
  border-style: solid;
  border-width: 2px;
  margin: 0;
  resize: none;
}

.smart-input textarea::placeholder,
.smart-input input::placeholder {
  font-family: var(--inpt--placeholder--font-family);
  color: var(--inpt--placeholder--color);
}

.smart-input:hover textarea,
.smart-input:hover input,
.smart-input textarea:focus,
.smart-input input:focus {
  outline: none;
  border-color: var(--inpt--border-color--focus);
}

.smart-input--error input {
  border-color: var(--inpt--border-color--error);
}

.smart-input--error input::placeholder {
  font-size: var(--inpt--font-size--error);
  color: var(--inpt--color--error);
}

.secret-button {
  position: absolute;
  font-family: var(--inpt--font-family);
  color: var(--inpt--secret-button--color);
  border-radius: var(--inpt--border-radius);
  border: none;
  cursor: pointer;
  top: 0;
  bottom: 0;
  right: 0;
  width: 3rem;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
}

.secret-button:hover,
.secret-button:focus {
  outline: none;
  color: var(--inpt--secret-button--color--hover);
}

</style>
