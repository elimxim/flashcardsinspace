<template>
  <Transition name="selection-actions">
    <div v-if="open" class="selection-actions">
      <div class="selection-actions-track">
        <div class="selection-actions-panel">
          <div class="selection-actions-buttons scrollbar-hidden">
            <span
              v-for="action in actions"
              :key="action.key"
              class="selection-action"
              :class="{ 'selection-action--unavailable': action.unavailableReason !== undefined }"
              @pointerenter="hintedKey = action.key"
              @pointerdown="hintedKey = action.key"
              @pointerleave="onPointerLeave"
            >
              <SmartButton
                class="selection-action-button"
                :class="{ 'dangerous-button': action.danger }"
                :disabled="busy || selectedFlashcards.length === 0 || !!action.unavailableReason"
                :hold-time="action.danger ? CONFIRM_HOLD_TIME : 0"
                :on-click="() => run(action)"
                rounded
                auto-blur
              >
                <span class="selection-action-content">
                  <font-awesome-icon :icon="action.icon" />
                  <span>{{ action.text }}</span>
                </span>
              </SmartButton>
            </span>
          </div>
          <span class="selection-actions-hint">{{ hint }}</span>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import SmartButton from '@/components/common/SmartButton.vue'
import { type Flashcard } from '@/model/flashcard.ts'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { storeToRefs } from 'pinia'
import { computed, ref } from 'vue'
import { specialStages } from '@/core-logic/stage-logic.ts'
import { moveFlashcardsBack, removeFlashcards } from '@/core-logic/flashcard-operations.ts'
import { errorResponseData } from '@/core-logic/media-error.ts'
import { type UserApiError, userApiErrors } from '@/api/user-api-error.ts'

interface SelectionAction {
  key: string
  text: string
  icon: string
  danger?: boolean
  unavailableReason?: string
  run: () => Promise<void>
}

const CONFIRM_HOLD_TIME = 1.2

const selectedIds = defineModel<number[]>('selectedIds', { required: true })

defineProps<{
  open: boolean
}>()

const flashcardStore = useFlashcardStore()
const { flashcardSet, flashcardMap, isSuspended } = storeToRefs(flashcardStore)
const { currDay } = storeToRefs(useChronoStore())
const toaster = useSpaceToaster()

const busy = ref(false)
const hintedKey = ref<string>()

const selectedFlashcards = computed(() =>
  selectedIds.value
    .map((id) => flashcardMap.value.get(id))
    .filter((flashcard): flashcard is Flashcard => flashcard !== undefined),
)

const moveBackUnavailableReason = computed(() => {
  if (isSuspended.value) return 'The flashcard set is suspended'
  const allInOuterSpace = selectedFlashcards.value.every(
    (flashcard) => flashcard.stage === specialStages.OUTER_SPACE.name,
  )
  return allInOuterSpace ? undefined : 'Only flashcards in Outer space can be moved back'
})

const actions = computed<SelectionAction[]>(() => [
  {
    key: 'move-back',
    text: 'Move back',
    icon: 'fa-solid fa-rotate-left',
    danger: true,
    unavailableReason: moveBackUnavailableReason.value,
    run: moveBackSelected,
  },
  {
    key: 'remove',
    text: 'Remove',
    icon: 'fa-solid fa-trash',
    danger: true,
    run: removeSelected,
  },
])

const hint = computed(() => {
  const reason = actions.value.find((action) => action.key === hintedKey.value)?.unavailableReason
  if (reason) return reason
  if (selectedFlashcards.value.length === 0) return 'Select flashcards to act on them'
  return 'Hold a red button to confirm'
})

// a touch leaves the hint up after the finger lifts, so it can be read
function onPointerLeave(event: PointerEvent) {
  if (event.pointerType === 'mouse') hintedKey.value = undefined
}

async function run(action: SelectionAction) {
  busy.value = true
  try {
    await action.run()
  } finally {
    busy.value = false
  }
}

function countText(count: number): string {
  return count === 1 ? '1 flashcard' : `${count} flashcards`
}

/**
 * Runs the operation on all the selected flashcards in one transaction: on success they
 * leave the selection; on failure nothing changed, and they stay selected to retry.
 */
async function runOnSelected(
  operation: (setId: number, flashcards: Flashcard[]) => Promise<unknown>,
  doneText: string,
  failure: UserApiError,
) {
  const setId = flashcardSet.value?.id
  const flashcards = selectedFlashcards.value
  if (setId === undefined || flashcards.length === 0) return

  try {
    await operation(setId, flashcards)
  } catch (error) {
    toaster.bakeError(failure, errorResponseData(error))
    return
  }
  const doneIds = new Set(flashcards.map((flashcard) => flashcard.id))
  selectedIds.value = selectedIds.value.filter((id) => !doneIds.has(id))
  toaster.bakeSuccess('Success', `${countText(doneIds.size)} ${doneText}`, 200)
}

async function moveBackSelected() {
  const chronodate = currDay.value.chronodate
  await runOnSelected(
    (setId, flashcards) => moveFlashcardsBack(setId, flashcards, chronodate),
    'moved back',
    userApiErrors.FLASHCARD__PROGRESSION_FAILED,
  )
}

async function removeSelected() {
  await runOnSelected(
    (setId, flashcards) =>
      removeFlashcards(
        setId,
        flashcards.map((flashcard) => flashcard.id),
      ),
    'removed',
    userApiErrors.FLASHCARD__REMOVING_FAILED,
  )
}
</script>

<style scoped>
/*
 * Sliding in and out also grows and shrinks the bar's height (grid rows 0fr <-> 1fr),
 * so the grid above resizes smoothly instead of jumping
 */
.selection-actions {
  display: grid;
  grid-template-rows: 1fr;
}

.selection-actions-track {
  min-height: 0;
  overflow: hidden;
}

.selection-actions-enter-active,
.selection-actions-leave-active {
  transition: grid-template-rows 0.25s ease;
}

.selection-actions-enter-active .selection-actions-panel,
.selection-actions-leave-active .selection-actions-panel {
  transition: transform 0.25s ease;
}

.selection-actions-enter-from,
.selection-actions-leave-to {
  grid-template-rows: 0fr;
}

.selection-actions-enter-from .selection-actions-panel,
.selection-actions-leave-to .selection-actions-panel {
  transform: translateY(100%);
}

@media (prefers-reduced-motion: reduce) {
  .selection-actions-enter-active,
  .selection-actions-leave-active,
  .selection-actions-enter-active .selection-actions-panel,
  .selection-actions-leave-active .selection-actions-panel {
    transition: none;
  }
}

.selection-actions-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 12px 20px 12px;
  border-top: 1px solid rgba(47, 51, 64, 0.15);
}

.selection-actions-buttons {
  max-width: 100%;
  display: flex;
  flex-direction: row;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 2px;
}

.selection-action {
  flex-shrink: 0;
}

.selection-action--unavailable .selection-action-button {
  pointer-events: none;
}

.selection-action-button {
  --smart-button--width: fit-content;
  --smart-button--padding: 0 16px;
  --smart-button--height: 40px;
  --smart-button--title--font-size: 14px;
  --smart-button--bg--disabled: rgba(50, 51, 74, 0.15);
}

.selection-action-content {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
}

.selection-actions-hint {
  min-height: 1.2em;
  color: #6b6f7b;
  font-size: 0.8rem;
  text-align: center;
}
</style>
