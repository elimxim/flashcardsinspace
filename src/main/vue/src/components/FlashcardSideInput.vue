<template>
  <div
    class="side"
    :class="{ 'side--fullscreen': expanded }"
  >
    <TextInput
      ref="textInputRef"
      v-model="text"
      v-model:expanded="expanded"
      :invalid="invalid"
      :placeholder="placeholder"
      expandable
    />
    <ErrorText
      :when="maxLengthInvalid"
      text="Your text has its own gravity! Maximum 512 characters."
    />
    <VoiceRecorder v-model="audio"/>
  </div>
</template>

<script setup lang="ts">
import TextInput from '@/components/TextInput.vue'
import ErrorText from '@/components/ErrorText.vue'
import VoiceRecorder from '@/components/VoiceRecorder.vue'
import { useVuelidate } from '@vuelidate/core'
import { maxLength, required } from '@vuelidate/validators'
import { ref, computed, watch, onBeforeUnmount } from 'vue'

defineProps<{
  placeholder: string
}>()

const text = defineModel<string>('text', { required: true })
const audio = defineModel<Blob | undefined>('audio')

const $v = useVuelidate({
    text: { required, maxLength: maxLength(512) }
  },
  { text }
)

const invalid = computed(() => $v.value.text.$errors.length > 0)
const maxLengthInvalid = computed(() =>
  $v.value.text.$errors.some(e => e.$validator === 'maxLength')
)

watch(text, () => {
  if (invalid.value) {
    $v.value.text.$reset()
  }
})

const expanded = ref(false)
const textInputRef = ref<InstanceType<typeof TextInput>>()

function focus() {
  textInputRef.value?.focus()
}

function validate() {
  $v.value.$touch()
}

function resetState() {
  $v.value.$reset()
  text.value = ''
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape') {
    expanded.value = false
    e.preventDefault()
    e.stopImmediatePropagation()
  }
}

defineExpose({ focus, validate, resetState, invalid })

watch(expanded, (isExpanded) => {
  if (isExpanded) {
    window.addEventListener('keydown', onKeydown, { capture: true })
  } else {
    window.removeEventListener('keydown', onKeydown, { capture: true })
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', onKeydown, { capture: true })
})

</script>

<style scoped>
.side {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-height: 0;
  min-width: 0;
}

.side--fullscreen {
  position: fixed;
  top: var(--navbar-height);
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 950;
  padding: 8px;
  background: #fff;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
</style>
