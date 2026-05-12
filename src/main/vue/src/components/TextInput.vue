<template>
  <div
    class="text-input text-input--theme"
    :class="{ 'text-input--error': invalid }"
  >
    <div v-if="expandable" class="text-input--toolbar">
      <AwesomeButton
        :icon="expanded ? 'fa-solid fa-compress' : 'fa-solid fa-expand'"
        :on-click="toggleExpanded"
      />
    </div>
    <textarea
      ref="textArea"
      v-model="model"
      v-bind="$attrs"
      type="text"
      :placeholder="inputPlaceholder"
      :rows="rows"
      @click="emit('inputClick')"
    />
  </div>
</template>

<script setup lang="ts">
import AwesomeButton from '@/components/AwesomeButton.vue'
import { computed, ref } from 'vue'

const model = defineModel<string>({ default: '' })
const expanded = defineModel<boolean>('expanded', { default: false })

const emit = defineEmits<{
  inputClick: []
}>()

const props = withDefaults(defineProps<{
  invalid?: boolean
  placeholder?: string
  expandable?: boolean
  rows?: number
}>(), {
  invalid: false,
  placeholder: '',
  expandable: false,
  rows: 1,
})

const textArea = ref<HTMLTextAreaElement>()

const inputPlaceholder = computed(() =>
  props.invalid ? props.placeholder + '!' : props.placeholder
)

function toggleExpanded() {
  expanded.value = !expanded.value
}

function focus() {
  textArea.value?.focus()
}

function scrollIntoView() {
  textArea.value?.scrollIntoView({ block: 'nearest' })
}

defineExpose({
  focus,
  scrollIntoView,
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
  display: flex;
  flex-direction: column;
  width: 100%;
  background-color: var(--tx-inpt--bg-color);
  border: 2px solid var(--tx-inpt--border-color);
  border-radius: var(--tx-inpt--border-radius);
  transition: border-color 0.2s ease-in-out;
}

.text-input--toolbar {
  display: flex;
  justify-content: flex-end;
  padding: 2px 4px 0;
}

.text-input textarea {
  flex: 1;
  width: 100%;
  font-size: var(--tx-inpt--font-size);
  color: var(--tx-inpt--color);
  background: transparent;
  border: none;
  padding: clamp(4px, 1.5vh, 8px) clamp(4px, 1vw, 8px);
  margin: 0;
  resize: none;
  outline: none;
}

.text-input textarea::placeholder {
  color: var(--tx-inpt--placeholder--color);
}

.text-input:hover,
.text-input:focus-within {
  border-color: var(--tx-inpt--border-color--focus);
}

.text-input--error {
  border-color: var(--tx-inpt--border-color--error);
}

.text-input--error textarea::placeholder {
  font-size: var(--tx-inpt--font-size--error);
  color: var(--tx-inpt--color--error);
}

</style>
