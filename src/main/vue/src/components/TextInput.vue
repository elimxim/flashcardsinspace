<template>
  <div
    class="text-input text-input--theme"
    :class="{ 'text-input--error': invalid }"
  >
    <textarea
      ref="textArea"
      v-model="model"
      v-bind="$attrs"
      type="text"
      :placeholder="inputPlaceholder"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

const model = defineModel<string>({ default: '' })

const props = withDefaults(defineProps<{
  invalid?: boolean
  placeholder?: string
}>(), {
  invalid: false,
  placeholder: '',
})

const textArea = ref<HTMLTextAreaElement>()

const inputPlaceholder = computed(() =>
  props.invalid ? props.placeholder + '!' : props.placeholder
)

function focus() {
  textArea.value?.focus()
}

defineExpose({
  focus
})
</script>

<style scoped>
.text-input--theme {
  --tx-inpt--font-size: var(--text-input--font-size, 1rem);
  --tx-inpt--font-size--error: var(--text-input--font-size--error, 1rem);
  --tx-inpt--color: var(--text-input--color, #45454a);
  --tx-inpt--color--error: var(--text-input--color--error, #c80f0f);
  --tx-inpt--bg-color: var(--text-input--bg-color, #f9f9f9);
  --tx-inpt--border-color: var(--text-input--border-color, #b8c4d6);
  --tx-inpt--border-color--focus: var(--text-input--border-color--focus, #007bff);
  --tx-inpt--border-color--error: var(--text-input--border-color--error, #c80f0f);
  --tx-inpt--border-radius: var(--text-input--border-radius, 4px);
  --tx-inpt--placeholder--color: var(--text-input--placeholder--color, #b8c4d6);
  --tx-inpt--secret-button--color: var(--text-input--secret-button--color, #b8c4d6);
  --tx-inpt--secret-button--color--hover: var(--text-input--secret-button--color--hover, #007bff);
}

.text-input {
  flex: 1;
  position: relative;
  display: flex;
  width: 100%;
}

.text-input textarea {
  flex: 1;
  width: 100%;
  font-size: var(--tx-inpt--font-size);
  color: var(--tx-inpt--color);
  background-color: var(--tx-inpt--bg-color);
  border-color: var(--tx-inpt--border-color);
  border-radius: var(--tx-inpt--border-radius);
  padding: clamp(0.75rem, 1.5vh, 1.25rem) clamp(0.75rem, 1vw, 1.25rem);
  margin: 0;
  border-style: solid;
  border-width: 2px;
  resize: none;
  transition: border-color 0.2s ease-in-out;
}

.text-input textarea::placeholder {
  color: var(--tx-inpt--placeholder--color);
}

.text-input:hover textarea,
.text-input textarea:focus {
  outline: none;
  border-color: var(--tx-inpt--border-color--focus);
}

.text-input--error textarea {
  border-color: var(--tx-inpt--border-color--error);
}

.text-input--error textarea::placeholder {
  font-size: var(--tx-inpt--font-size--error);
  color: var(--tx-inpt--color--error);
}

</style>
