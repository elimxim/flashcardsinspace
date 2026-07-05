<template>
  <div
    :class="[
      'page',
      'page--bg--light',
      'flex-column',
      'padding-auto',
      'scrollbar-hidden',
      'user-page',
      'user-page--theme',
    ]"
  >
    <div class="user-layout">
      <div class="user-main">
        <h2 class="select-text">Welcome aboard, {{ user?.name ?? 'Unknown' }}!</h2>
        <div class="select-text">{{ randomDayPhrase }}</div>
        <DeferredLoading :wait-for="userInfoAwait">
          <UserInfo :user="user"/>
          <template #fallback>
            <UserInfoSkeleton/>
          </template>
        </DeferredLoading>
      </div>
      <div ref="mascotWrapper" class="user-mascot-wrapper">
        <div class="user-mascot-orbit" :style="mascotOrbitStyle">
          <div class="user-mascot-float">
            <SvgImage
              path="assets/mascots"
              name="Stacy.svg"
              :width="240"
              alt="Stacy the space cow"
              class="user-mascot"
              non-interactive
            />
          </div>
        </div>
      </div>
    </div>
  </div>
  <SpaceToast/>
</template>


<script setup lang="ts">
import DeferredLoading from '@/components/DeferredLoading.vue'
import UserInfo from '@/components/UserInfo.vue'
import UserInfoSkeleton from '@/components/UserInfoSkeleton.vue'
import SpaceToast from '@/components/SpaceToast.vue'
import SvgImage from '@/components/SvgImage.vue'
import { useAuthStore } from '@/stores/auth-store.ts'
import { useLanguageStore } from '@/stores/language-store.ts'
import { storeToRefs } from 'pinia'
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { waitUntilStoreLoaded } from '@/utils/store-loading.ts'
import { deviceInteraction, } from '@/utils/device-utils.ts'

const { isAnyHover } = deviceInteraction()

const authStore = useAuthStore()
const languageStore = useLanguageStore()

const { user } = storeToRefs(authStore)

const userInfoAwait = () => waitUntilStoreLoaded(languageStore)

const daysSinceRegistration = computed(() => {
  if (!user.value?.registeredAt) {
    return 0
  }

  const timeDifference = new Date().getTime() - user.value.registeredAt.getTime()
  return Math.floor(timeDifference / (1000 * 60 * 60 * 24))
})

const daysPhrases = [
  (days: number) => `Floating in zero gravity for ${days} days (and somehow still haven't lost your keys)`,
  (days: number) => `You've been floating with us for ${days} days - that's ${days} days without doing laundry!`,
  (days: number) => `Floating majestically for ${days} days (like a graceful space walrus)`,
  (days: number) => `You've survived ${days} days without a towel - impressive for a space traveler`,
  (days: number) => `${days} days in space - congratulations, you're now 0.00001% closer to Mars`,
  (days: number) => `${days} days in space - your Earth insurance probably doesn't cover this`,
]

const randomDayPhrase = computed(() => {
  const randomIndex = Math.floor(Math.random() * daysPhrases.length)
  return daysPhrases[randomIndex](daysSinceRegistration.value)
})

const mascotWrapper = ref<HTMLElement | null>(null)
const mascotOffsetX = ref(0)
const mascotOffsetY = ref(0)

const escapeRadius = 140
const maxEscapeDistance = 200

const mascotOrbitStyle = computed(() => ({
  transform: `translate(${mascotOffsetX.value}px, ${mascotOffsetY.value}px)`,
}))

let rafId = 0
let pendingEvent: PointerEvent | null = null

function updateMascotOffset() {
  rafId = 0
  const event = pendingEvent
  pendingEvent = null
  const wrapper = mascotWrapper.value
  if (!event || !wrapper) return

  // Measure from the stationary wrapper so the resting center stays stable
  // while the orbit is mid-transition (measuring the moving orbit self-oscillates).
  const rect = wrapper.getBoundingClientRect()
  const centerX = rect.left + rect.width / 2
  const centerY = rect.top + rect.height / 2
  const dx = event.clientX - centerX
  const dy = event.clientY - centerY
  const distance = Math.hypot(dx, dy)

  if (distance >= escapeRadius || distance === 0) {
    mascotOffsetX.value = 0
    mascotOffsetY.value = 0
    return
  }

  const strength = (1 - distance / escapeRadius) * maxEscapeDistance
  mascotOffsetX.value = -(dx / distance) * strength
  mascotOffsetY.value = -(dy / distance) * strength
}

function onPointerMove(event: PointerEvent) {
  pendingEvent = event
  if (rafId === 0) {
    rafId = requestAnimationFrame(updateMascotOffset)
  }
}

onMounted(() => {
  if (isAnyHover) window.addEventListener('pointermove', onPointerMove, { passive: true })
})

onBeforeUnmount(() => {
  if (isAnyHover) window.removeEventListener('pointermove', onPointerMove)
  if (rafId !== 0) cancelAnimationFrame(rafId)
})

</script>

<style scoped>
.user-page--theme {
  --u-page--icon--color: #6369d5;
}

.user-page {
  gap: 10px;
}

.user-page h2 {
  margin: 0;
  padding: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-layout {
  display: flex;
  flex-direction: row;
  align-items: stretch;
  gap: 40px;
}

.user-main {
  flex: 0 0 600px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.user-mascot-wrapper {
  flex: 1 1 auto;
  min-width: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-mascot-orbit {
  transition: transform 0.5s cubic-bezier(0.22, 1, 0.36, 1);
  will-change: transform;
}

.user-mascot-float {
  animation: mascotFloat 6s ease-in-out infinite;
  transform-origin: center;
}

@keyframes mascotFloat {
  0% {
    transform: translateY(0) rotate(-2deg);
  }
  50% {
    transform: translateY(-16px) rotate(2deg);
  }
  100% {
    transform: translateY(0) rotate(-2deg);
  }
}

@media (max-width: 900px) {
  .user-layout {
    flex-direction: column;
    align-items: stretch;
    gap: 20px;
  }

  .user-main {
    flex: 0 0 auto;
  }

  .user-mascot-wrapper {
    flex: 0 0 auto;
    max-width: 600px;
  }
}

</style>
