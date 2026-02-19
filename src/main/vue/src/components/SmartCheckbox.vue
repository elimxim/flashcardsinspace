<template>
  <label class="smart-checkbox smart-checkbox--theme">
    <input
      v-model="model"
      type="checkbox"
      class="checkbox-input"
      v-bind="$attrs"
    />
    <span class="checkbox-icon-container">
      <font-awesome-icon
        icon="fa-solid fa-square-check"
        class="checkbox-icon checkbox-icon--checked"
        :class="{ 'is-visible': model }"
      />
      <font-awesome-icon
        icon="fa-regular fa-square"
        class="checkbox-icon checkbox-icon--unchecked"
        :class="{ 'is-visible': !model }"
      />
    </span>
    <span v-if="label || checkedLabel" class="label-container">
      <transition name="label-switch">
        <span :key="currentLabel" class="checkbox-label">
          {{ currentLabel }}
        </span>
      </transition>
    </span>
  </label>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const model = defineModel<boolean>({ default: false })

const props = withDefaults(defineProps<{
  label?: string
  checkedLabel?: string
}>(), {
  label: '',
  checkedLabel: ''
})

const currentLabel = computed(() => {
  if (model.value && props.checkedLabel) {
    return props.checkedLabel
  }
  return props.label
})
</script>

<style scoped>
.smart-checkbox--theme {
  --chkbx--size: var(--smart-checkbox--size, 20px);
  --chkbx--color-unchecked: var(--smart-checkbox--color-unchecked, #b8c4d6);
  --chkbx--color-unchecked--hover: var(--smart-checkbox--color-unchecked--hover, #007bff);
  --chkbx--color-checked: var(--smart-checkbox--color-checked, #007bff);
  --chkbx--color-checked--hover: var(--smart-checkbox--color-checked--hover, #0056b3);
  --chkbx--label--color: var(--smart-checkbox--label--color, #45454a);
  --chkbx--label--font-size: var(--smart-checkbox--label--font-size, clamp(0.9rem, 2vh, 1rem));
}

.smart-checkbox {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  user-select: none;
  position: relative;
}

.checkbox-input {
  position: absolute;
  opacity: 0;
  cursor: pointer;
  height: 0;
  width: 0;
  outline: none;
}

.checkbox-icon-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: var(--chkbx--size);
  width: var(--chkbx--size);
  flex-shrink: 0;
  position: relative;
}

.checkbox-icon {
  font-size: var(--chkbx--size);
  transition: opacity 0.2s ease, color 0.2s ease;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  opacity: 0;
  display: block;
}

.checkbox-icon.is-visible {
  opacity: 1;
}

.checkbox-icon--unchecked {
  color: var(--chkbx--color-unchecked);
}

.checkbox-icon--checked {
  color: var(--chkbx--color-checked);
}

@media (hover: hover) {
  .smart-checkbox:hover .checkbox-icon--unchecked {
    color: var(--chkbx--color-unchecked--hover);
  }

  .smart-checkbox:hover .checkbox-icon--checked {
    color: var(--chkbx--color-checked--hover);
  }
}

.checkbox-label-container {
  position: relative;
  display: flex;
  align-items: center;
  min-height: calc(var(--chkbx--label--font-size) * 1.5);
}

.checkbox-label {
  font-size: var(--chkbx--label--font-size);
  color: var(--chkbx--label--color);
  line-height: 1.5;
  display: inline-block;
  vertical-align: middle;
}

.label-switch-enter-active,
.label-switch-leave-active {
  transition: all 0.3s ease;
}

.label-switch-leave-active {
  position: absolute;
}

.label-switch-enter-from {
  opacity: 0;
  transform: translateY(-10px) scale(0.95);
}

.label-switch-leave-to {
  opacity: 0;
  transform: translateY(10px) scale(0.95);
}

.label-switch-enter-to,
.label-switch-leave-from {
  opacity: 1;
  transform: translateY(0) scale(1);
}

</style>
