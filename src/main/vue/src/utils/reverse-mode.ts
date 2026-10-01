import { computed, type MaybeRefOrGetter, ref, toValue } from 'vue'
import { ReviewSessionType } from '@/core-logic/review-logic.ts'
import { loadReversedReviewsFromCookies, saveReversedReviewsToCookies } from '@/utils/cookies.ts'

export const REVERSE_HOLD_TIME = 0.8

const reversedReviews = ref(new Set(loadReversedReviewsFromCookies()))

export function useReverseMode(sessionType: MaybeRefOrGetter<ReviewSessionType | undefined>) {
  const reversed = computed(() => {
    const type = toValue(sessionType)
    return type !== undefined && reversedReviews.value.has(type)
  })

  function toggle() {
    const type = toValue(sessionType)
    if (type === undefined) return
    const next = new Set(reversedReviews.value)
    if (!next.delete(type)) next.add(type)
    reversedReviews.value = next
    saveReversedReviewsToCookies([...next])
  }

  return { reversed, toggle }
}
