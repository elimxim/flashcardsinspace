<template>
  <div
    :class="[
      'page',
      'page--bg--light',
      'flex-column',
      'padding-auto',
      'user-page',
      'scrollbar-hidden',
      'user-page--theme',
    ]"
  >
    <h2>Welcome aboard, {{ user?.name ?? 'Unknown' }}!</h2>
    <div>{{ randomDayPhrase }}</div>
    <DeferredLoading :wait-for="userInfoAwait">
      <UserInfo :user="user"/>
      <template #fallback>
        <UserInfoSkeleton/>
      </template>
    </DeferredLoading>
  </div>
  <SpaceToast/>
</template>


<script setup lang="ts">
import DeferredLoading from '@/components/DeferredLoading.vue'
import UserInfo from '@/components/UserInfo.vue'
import UserInfoSkeleton from '@/components/UserInfoSkeleton.vue'
import SpaceToast from '@/components/SpaceToast.vue'
import { useAuthStore } from '@/stores/auth-store.ts'
import { useLanguageStore } from '@/stores/language-store.ts'
import { storeToRefs } from 'pinia'
import { computed } from 'vue'
import { waitUntilStoreLoaded } from '@/utils/store-loading.ts'

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

</script>

<style scoped>
.user-page--theme {
  --u-page--icon--color: #6369d5;
}

.user-page {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.user-page h2 {
  margin: 0;
  padding: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

</style>
