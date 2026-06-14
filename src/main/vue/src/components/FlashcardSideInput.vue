<template>
  <div
    class="side"
    :class="{ 'side--fullscreen': expandedFull }"
  >
    <TextInput
      ref="textInputRef"
      v-model="text"
      v-model:expanded-full="expandedFull"
      v-model:expanded-down="expandedDown"
      :invalid="invalid"
      :placeholder="placeholder"
      show-expand-full
      show-expand-down
    />
    <ErrorText
      :when="maxLengthInvalid"
      text="Your text has its own gravity! Maximum 512 characters."
    />
    <div class="side-media">
      <VoiceRecorder
        v-model:audio-blob="audio"
        v-model:expanded="voiceRecorderExpanded"
      />
      <PictureUploader
        v-model:picture-blob="picture"
        v-model:expanded="pictureUploaderExpanded"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import TextInput from '@/components/TextInput.vue'
import ErrorText from '@/components/ErrorText.vue'
import VoiceRecorder from '@/components/VoiceRecorder.vue'
import PictureUploader from '@/components/PictureUploader.vue'
import { useVuelidate } from '@vuelidate/core'
import { maxLength, requiredIf } from '@vuelidate/validators'
import { ref, computed, watch, onBeforeUnmount } from 'vue'

defineProps<{
  placeholder: string
}>()

const text = defineModel<string | undefined>('text', { required: true })
const audio = defineModel<Blob | undefined>('audio')
const picture = defineModel<Blob | undefined>('picture')
const expandedFull = defineModel<boolean>('expanded-full', { default: false })
const expandedDown = defineModel<boolean>('expanded-down', { default: false })

const voiceRecorderExpanded = ref<boolean>(false)
const pictureUploaderExpanded = ref<boolean>(false)

const pictureAbsent = computed(() => !picture.value)

const $v = useVuelidate({
    text: {
      requiredWhenNoPicture: requiredIf(pictureAbsent),
      maxLength: maxLength(512),
    }
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

watch(picture, () => {
  if (invalid.value) {
    $v.value.text.$reset()
  }
})

const textInputRef = ref<InstanceType<typeof TextInput>>()

function focus() {
  textInputRef.value?.focus()
}

function scrollIntoView() {
  textInputRef.value?.scrollIntoView()
}

function validate() {
  $v.value.$touch()
}

function resetState() {
  $v.value.$reset()
  text.value = ''
  audio.value = undefined
  picture.value = undefined
  expandedFull.value = false
  expandedDown.value = false
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape') {
    expandedFull.value = false
    e.preventDefault()
    e.stopImmediatePropagation()
  }
}

defineExpose({
  focus,
  scrollIntoView,
  validate,
  resetState,
  invalid,
})

watch(expandedFull, (newVal) => {
  if (newVal) {
    window.addEventListener('keydown', onKeydown, { capture: true })
  } else {
    window.removeEventListener('keydown', onKeydown, { capture: true })
  }
})

watch([voiceRecorderExpanded, pictureUploaderExpanded], ([vrNewVal, puNewVal], [vrOldVal, puOldVal]) => {
  if (vrNewVal && puNewVal && puOldVal) {
    pictureUploaderExpanded.value = false
  }
  if (puNewVal && vrNewVal && vrOldVal) {
    voiceRecorderExpanded.value = false
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

.side-media {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8px;
}
</style>
