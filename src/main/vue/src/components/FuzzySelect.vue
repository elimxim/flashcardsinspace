<template>
  <div
    ref="fuzzyContainer"
    class="fuzzy-select fuzzy-select--theme select-none"
    tabindex="-1"
    @focusout="handleFocusOut"
    @keydown="handleKeydown"
  >
    <SmartInput
      v-if="!isOpen"
      type="text"
      :value="selectedOptionLabel"
      :placeholder="optionPlaceholder"
      :invalid="invalid"
      v-bind="$attrs"
      readonly
      @click="openDropdown"
    />
    <SmartInput
      v-if="isOpen"
      ref="searchInput"
      v-model="searchQuery"
      type="text"
      :placeholder="searchPlaceholder"
      :invalid="invalid"
      v-bind="$attrs"
    />
    <ul v-if="isOpen" ref="dropDownList" class="drop-down">
      <template v-if="filteredOptions.length > 0">
        <li
          v-for="(option, index) in filteredOptions"
          :key="index"
          :class="{ 'highlighted': index === highlightedIndex }"
          @click.stop="selectOption(option)"
          @mouseover="highlightedIndex = index">
          {{ optionLabel(option) }}
        </li>
      </template>
      <li v-else>
        No signal from this language
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts" generic="T extends object">
import SmartInput from '@/components/SmartInput.vue'
import { ref, computed, nextTick, watch } from 'vue'

const model = defineModel<T>({ default: null })

const props = withDefaults(defineProps<{
  options: T[]
  optionLabel: (option: T) => string
  optionPlaceholder?: string
  searchPlaceholder?: string
  invalid?: boolean
}>(), {
  optionPlaceholder: 'Select an option',
  searchPlaceholder: 'Search..',
  invalid: false,
})

const fuzzyContainer = ref<HTMLElement>()
const searchInput = ref<InstanceType<typeof SmartInput>>()
const dropDownList = ref<HTMLUListElement>()

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
  if (!model.value) return ''
  return optionLabel(model.value)
})

async function openDropdown() {
  if (isOpen.value) return
  isOpen.value = true
  await nextTick()
  searchInput.value?.focus()
}

function selectOption(option: T) {
  model.value = option
  searchQuery.value = ''
  closeDropdown()
}

function closeDropdown() {
  isOpen.value = false
  highlightedIndex.value = -1
  fuzzyContainer.value?.blur()
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
  if (fuzzyContainer.value && !fuzzyContainer.value.contains(event.relatedTarget as Node)) {
    closeDropdown()
  }
}

async function scrollHighlightedOptionIntoView() {
  if (highlightedIndex.value === -1) return
  await nextTick().then(() => {
    const highlightedElement = dropDownList.value?.children[highlightedIndex.value]
    highlightedElement?.scrollIntoView({ block: 'nearest' })
  })
}

</script>

<style scoped>
.fuzzy-select--theme {
  --drop-down--color: var(--fuzzy-select--drop-down--color, rgba(0, 0, 0, 0.8));
  --drop-down--border-color: var(--fuzzy-select--drop-down--border-color, rgba(205, 205, 205, 0.8));
  --drop-down--bg-color--hover: var(--fuzzy-select--drop-down--bg-color--hover, rgba(213, 213, 213, 0.5));
  --scrollbar--color: var(--fuzzy-select--scrollbar--color, rgba(170, 170, 170, 0.6));
  --scrollbar--color--hover: var(--fuzzy-select--scrollbar--color--hover, rgba(170, 170, 170, 0.8));
}

.fuzzy-select {
  position: relative;
  width: 100%;
  border: none;
  outline: none;
}

.drop-down {
  position: absolute;
  top: 99%;
  left: 2px;
  right: -4px;
  border: 1px solid var(--drop-down--border-color);
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

.drop-down:focus {
  outline: none;
  border-width: 2px;
}

.drop-down li {
  padding: 10px;
  cursor: pointer;
  color: var(--drop-down--color);
  transition: background-color 0.1s ease-in-out;
}

.drop-down li:hover,
.drop-down li.highlighted {
  background-color: var(--drop-down--bg-color--hover);
}

.drop-down::-webkit-scrollbar {
  width: 8px;
}

.drop-down::-webkit-scrollbar-track {
  background: transparent;
}

.drop-down::-webkit-scrollbar-thumb {
  background-color: var(--scrollbar--color);
  border-radius: 4px;
}

@media (hover: hover) {
  .drop-down::-webkit-scrollbar-thumb:hover {
    background-color: var(--scrollbar--color--hover);
  }
}
</style>
