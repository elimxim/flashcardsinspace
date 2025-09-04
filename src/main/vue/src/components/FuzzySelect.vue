<template>
  <div
    class="fuzzy-select non-selectable"
    ref="selectContainer"
    tabindex="0"
    @focus="openDropdown"
    @focusout="handleFocusOut">
    <input
      v-if="!isOpen"
      type="text"
      class="input fuzzy-select__input transition--border-color"
      :value="selectedOptionLabel"
      :placeholder="optionPlaceholder"
      style="pointer-events: none;"/>
    <input
      v-if="isOpen"
      type="text"
      class="input fuzzy-select__input transition--border-color"
      ref="searchInput"
      v-model="searchQuery"
      :placeholder="searchPlaceholder"
      @keydown="handleKeydown"/>
    <ul v-if="isOpen" class="fuzzy-select__drop-down" ref="dropDownList">
      <li
        v-for="(option, index) in filteredOptions"
        :key="index"
        :class="{ 'highlighted': index === highlightedIndex }"
        @click.stop="selectOption(option)"
        @mouseover="highlightedIndex = index"
      >
        {{ optionLabel(option) }}
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts" generic="T extends object">
import { ref, computed, nextTick, watch } from 'vue'

const props = defineProps<{
  options: T[]
  modelValue: T | undefined
  optionLabel: (option: T) => string
  optionPlaceholder?: string
  searchPlaceholder?: string
}>()

const emit = defineEmits(['update:modelValue'])

const selectContainer = ref<HTMLElement>()
const searchInput = ref<HTMLInputElement>()
const dropDownList = ref<HTMLUListElement>()

const isOpen = ref(false)
const searchQuery = ref('')
const highlightedIndex = ref(-1)
const borderRadius = ref('0 4px 4px 4px')

const optionLabel = (option: T): string => {
  if (option) {
    return props.optionLabel(option)
  } else {
    return ''
  }
}

const filteredOptions = computed(() => {
  if (!searchQuery.value) return props.options
  return props.options.filter((option) =>
    optionLabel(option).toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

watch(filteredOptions, () => {
  highlightedIndex.value = -1
})

const selectedOptionLabel = computed(() => {
  if (!props.modelValue) return ''
  return optionLabel(props.modelValue)
})

async function openDropdown() {
  if (isOpen.value) return
  isOpen.value = true
  await nextTick()
  adjustDropDownStyles()
  searchInput.value?.focus()
}

function adjustDropDownStyles() {
  if (selectContainer.value) {
    const computedStyles = getComputedStyle(selectContainer.value)
    const topValue = computedStyles
      .getPropertyValue('--fuzzy-select__drop-down--right').trim()
    const radiusValue = computedStyles
      .getPropertyValue('--fuzzy-select__drop-down--border-radius').trim()
    if (topValue && radiusValue) {
      if (parseInt(topValue) === 0) {
        borderRadius.value = `0 0 ${radiusValue} ${radiusValue}`
      } else {
        borderRadius.value = `0 ${radiusValue} ${radiusValue} ${radiusValue}`
      }
    }
  }
}

function selectOption(option: T) {
  emit('update:modelValue', option)
  searchQuery.value = ''
  closeDropdown()
}

function closeDropdown() {
  isOpen.value = false
  highlightedIndex.value = -1
}

function handleKeydown(event: KeyboardEvent) {
  switch (event.key) {
    case 'ArrowDown':
      event.preventDefault()
      if (highlightedIndex.value >= filteredOptions.value.length - 1) {
        highlightedIndex.value = 0
      } else {
        highlightedIndex.value++
      }
      scrollHighlightedOptionIntoView()
      break
    case 'ArrowUp':
      event.preventDefault()
      if (highlightedIndex.value <= 0) {
        highlightedIndex.value = filteredOptions.value.length - 1
      } else {
        highlightedIndex.value--
      }
      scrollHighlightedOptionIntoView()
      break
    case 'Enter':
      event.preventDefault()
      if (highlightedIndex.value !== -1) {
        selectOption(filteredOptions.value[highlightedIndex.value])
      }
      break
    case 'Escape':
      event.stopPropagation()
      closeDropdown()
      break
  }
}

function handleFocusOut(event: FocusEvent) {
  if (selectContainer.value && !selectContainer.value.contains(event.relatedTarget as Node)) {
    closeDropdown()
  }
}

function scrollHighlightedOptionIntoView() {
  if (highlightedIndex.value === -1) return
  nextTick(() => {
    const highlightedElement = dropDownList.value?.children[highlightedIndex.value]
    highlightedElement?.scrollIntoView({ block: 'nearest' })
  })
}
</script>

<style scoped>
.fuzzy-select {
  position: relative;
  width: 100%;
  cursor: pointer;
  outline: none;
}

.fuzzy-select__input {
  border: none;
  outline: none;
  width: 100%;
  padding: 0;
}

.fuzzy-select__drop-down {
  position: absolute;
  top: 102%;
  left: 0;
  right: var(--fuzzy-select__drop-down--right, -1rem);
  border: 1px solid var(--fuzzy-select__drop-down--border-color, rgba(205, 205, 205, 0.8));
  border-radius: v-bind(borderRadius);
  list-style: none;
  padding: 0;
  margin: 0;
  max-height: 220px;
  overflow-y: auto;
  background-color: transparent;
  backdrop-filter: blur(10px);
  z-index: 1000;
}

.fuzzy-select__drop-down:focus{
  outline: none;
  border-width: 2px;
}

.fuzzy-select__drop-down li {
  padding: 10px;
  cursor: pointer;
  color: var(--fuzzy-select__drop-down--color, rgba(0, 0, 0, 0.8));
  transition: background-color 0.1s ease-in-out;
}

.fuzzy-select__drop-down li:hover,
.fuzzy-select__drop-down li.highlighted {
  background-color: var(--fuzzy-select__drop-down--hover--bg-color, rgba(213, 213, 213, 0.5));
}

.fuzzy-select__drop-down::-webkit-scrollbar {
  width: 8px;
}

.fuzzy-select__drop-down::-webkit-scrollbar-track {
  background: transparent;
}

.fuzzy-select__drop-down::-webkit-scrollbar-thumb {
  background-color: var(--fuzzy-select__drop-down__scrollbar--color, rgba(170, 170, 170, 0.6));
  border-radius: 4px;
}

.fuzzy-select__drop-down::-webkit-scrollbar-thumb:hover {
  background-color: var(--fuzzy-select__drop-down__scrollbar--hover--color, rgba(170, 170, 170, 0.8));
}
</style>
