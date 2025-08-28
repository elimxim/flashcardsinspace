<template>
  <div
    class="fuzzy-select"
    ref="selectContainer"
    @click="toggleDropdown">
    <input
      type="text"
      v-if="!isOpen"
      :value="selectedOptionLabel"
      :placeholder="optionPlaceholder"
      style="pointer-events: none;"
    />
    <input
      type="text"
      ref="searchInput"
      v-if="isOpen"
      v-model="searchQuery"
      :placeholder="searchPlaceholder"
      @keydown="handleKeydown"
    />
    <ul v-if="isOpen" class="options-list" ref="optionsList">
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
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'

const props = defineProps<{
  options: T[]
  modelValue: T | null
  optionLabel: (option: T) => string
  optionPlaceholder?: string
  searchPlaceholder?: string
}>()

const emit = defineEmits(['update:modelValue'])

const selectContainer = ref<HTMLElement>()
const searchInput = ref<HTMLInputElement>()
const optionsList = ref<HTMLUListElement>()

const isOpen = ref(false)
const searchQuery = ref('')
const highlightedIndex = ref(-1)

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
  if (!props.modelValue) return props.optionPlaceholder
  return optionLabel(props.modelValue)
})

async function toggleDropdown() {
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    await nextTick()
    searchInput.value?.focus()
  } else {
    closeDropdown()
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

function scrollHighlightedOptionIntoView() {
  if (highlightedIndex.value === -1) return
  nextTick(() => {
    const highlightedElement = optionsList.value?.children[highlightedIndex.value]
    highlightedElement?.scrollIntoView({ block: 'nearest' })
  })
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})

function handleClickOutside(event: MouseEvent) {
  if (selectContainer.value && !selectContainer.value.contains(event.target as Node)) {
    closeDropdown()
  }
}
</script>

<style scoped>
.fuzzy-select {
  position: relative;
  width: 100%;
  cursor: pointer;
  outline: none;
  -webkit-user-select: none; /* Safari, Chrome */
  -moz-user-select: none; /* Firefox */
  -ms-user-select: none; /* Edge */
  user-select: none; /* Standard */
}

.fuzzy-select input {
  border: none;
  outline: none;
  width: 100%;
  padding: 0;
}

.fuzzy-select ul {
  position: absolute;
  top: 102%;
  left: 0;
  right: -1rem;
  border: 1px solid #ccc;
  border-radius: 0 4px 4px 4px;
  list-style: none;
  padding: 0;
  margin: 0;
  max-height: 220px;
  overflow-y: auto;
  background-color: transparent;
  backdrop-filter: blur(10px);
  z-index: 1000;
}

.fuzzy-select ul li {
  padding: 10px;
  cursor: pointer;
}

.fuzzy-select ul li:hover,
.fuzzy-select ul li.highlighted {
  background-color: rgba(170, 170, 170, 0.4);
}

.fuzzy-select ul::-webkit-scrollbar {
  width: 8px;
}

.fuzzy-select ul::-webkit-scrollbar-track {
  background: transparent;
}

.fuzzy-select ul::-webkit-scrollbar-thumb {
  background-color: rgba(170, 170, 170, 0.6);
  border-radius: 4px;
}

.fuzzy-select ul::-webkit-scrollbar-thumb:hover {
  background-color: rgba(170, 170, 170, 0.8);
}
</style>
