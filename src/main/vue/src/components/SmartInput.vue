<template>
  <div
    class="smart-input smart-input--theme"
    :class="{ 'smart-input--error': invalid }"
  >
    <textarea
      v-if="area"
      v-bind="$attrs"
      ref="textArea"
      v-model="model"
      style="transition: border-color 0.2s ease-in-out;"
      :type="inputType"
      :toast-type="$props.type"
      :placeholder="inputPlaceholder"
    />
    <input
      v-else-if="readonly"
      v-bind="$attrs"
      ref="readonlyInput"
      style="transition: border-color 0.2s ease-in-out;"
      :value="value"
      :type="inputType"
      :placeholder="inputPlaceholder"
    />
    <input
      v-else
      ref="input"
      v-model="model"
      v-bind="$attrs"
      style="transition: border-color 0.2s ease-in-out;"
      :type="inputType"
      :placeholder="inputPlaceholder"
    />
    <button
      v-if="!area && isPassword"
      ref="secretButton"
      class="secret-button"
      type="button"
      tabindex="-1"
      @click="toggleShowPassword"
    >
      <font-awesome-icon :icon="showPassword ? 'fa-solid fa-eye-slash' : 'fa-solid fa-eye'"/>
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

type Type = 'text' | 'password' | 'email'

const model = defineModel<string>({ default: '' })

const props = withDefaults(defineProps<{
  type: Type
  area?: boolean
  invalid?: boolean
  readonly?: boolean
  value?: string
  placeholder?: string
}>(), {
  area: false,
  invalid: false,
  readonly: false,
  value: '',
  placeholder: '',
})

const isPassword = computed(() => props.type === 'password')
const showPassword = ref(false)

const textArea = ref<HTMLTextAreaElement>()
const readonlyInput = ref<HTMLInputElement>()
const input = ref<HTMLInputElement>()
const secretButton = ref<HTMLButtonElement>()

const inputType = computed(() => {
  if (!props.area && isPassword.value) {
    return showPassword.value ? 'text' : 'password'
  } else {
    return props.type
  }
})

const inputPlaceholder = computed(() =>
  props.invalid ? props.placeholder + '!' : props.placeholder
)

function toggleShowPassword() {
  showPassword.value = !showPassword.value
  secretButton.value?.blur()
}

function focus() {
  if (props.area) {
    textArea.value?.focus()
  } else if (props.readonly) {
    readonlyInput.value?.focus()
  } else {
    input.value?.focus()
  }
}

defineExpose({
  focus
})

</script>

<style scoped>
.smart-input--theme {
  --inpt--font-size: var(--smart-input--font-size, clamp(0.9rem, 2vh, 1rem));
  --inpt--font-size--error: var(--smart-input--font-size--error, clamp(0.9rem, 2vh, 1rem));
  --inpt--color: var(--smart-input--color, #45454a);
  --inpt--color--error: var(--smart-input--color--error, #c80f0f);
  --inpt--bg-color: var(--smart-input--bg-color, #f9f9f9);
  --inpt--border-color: var(--smart-input--border-color, #b8c4d6);
  --inpt--border-color--focus: var(--smart-input--border-color--focus, #007bff);
  --inpt--border-color--error: var(--smart-input--border-color--error, #c80f0f);
  --inpt--border-radius: var(--smart-input--border-radius, 4px);
  --inpt--placeholder--color: var(--smart-input--placeholder--color, #b8c4d6);
  --inpt--secret-button--color: var(--smart-input--secret-button--color, #b8c4d6);
  --inpt--secret-button--color--hover: var(--smart-input--secret-button--color--hover, #007bff);
}

.smart-input {
  flex: 1;
  position: relative;
  display: flex;
  width: 100%;
  -webkit-tap-highlight-color: transparent;
}

.smart-input textarea,
.smart-input input {
  flex: 1;
  width: 100%;
  font-size: var(--inpt--font-size);
  color: var(--inpt--color);
  background-color: var(--inpt--bg-color);
  border-color: var(--inpt--border-color);
  border-radius: var(--inpt--border-radius);
  padding: clamp(0.75rem, 1.5vh, 1.25rem) clamp(0.75rem, 1vw, 1.25rem);
  margin: 0;
  border-style: solid;
  border-width: 2px;
  resize: none;
}

.smart-input textarea::placeholder,
.smart-input input::placeholder {
  color: var(--inpt--placeholder--color);
}

.smart-input:hover textarea,
.smart-input:hover input,
.smart-input textarea:focus,
.smart-input input:focus {
  outline: none;
  border-color: var(--inpt--border-color--focus);
}

.smart-input--error textarea,
.smart-input--error input {
  border-color: var(--inpt--border-color--error);
}

.smart-input--error textarea::placeholder,
.smart-input--error input::placeholder {
  font-size: var(--inpt--font-size--error);
  color: var(--inpt--color--error);
}

.secret-button {
  position: absolute;
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
  transition: color 0.2s ease-in-out;
  -webkit-tap-highlight-color: transparent;
}

@media (hover: hover) {
  .secret-button:hover,
  .secret-button:focus {
    outline: none;
    color: var(--inpt--secret-button--color--hover);
  }
}

</style>
