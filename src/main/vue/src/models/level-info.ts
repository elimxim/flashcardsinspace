import type { ComputedRef } from 'vue';

export interface LevelInfo {
  name: string,
  count: ComputedRef<number>,
}
