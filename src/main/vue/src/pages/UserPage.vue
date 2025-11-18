<template>
  <div :class="[
    'page',
    'flex-column',
    'padding-auto',
    'user-page',
    'user-page--theme',
    'scrollbar-hidden',
  ]">
    <h2>Welcome aboard, {{ user?.name ?? 'Unknown' }}!</h2>
    <div>{{ randomDayPhrase }}</div>
    <div class="user-info">
      <div class="user-info-item">
        <AwesomeContainer icon="fa-solid fa-user-astronaut" class="user-info-icon">
          <SmartInput
            v-model="username"
            type="text"
            name="name"
            placeholder="Name"
            :invalid="usernameInvalid"
          />
        </AwesomeContainer>
        <ErrorText
          :show="usernameRegexMismatch"
          :errors="[
              {
                when: usernameRegexMismatch,
                text: 'Please use only letters, numbers, dashes, underscores, and spaces'
              },
              {
                when: usernameMaxLengthInvalid,
                text: 'This username is expanding faster than the universe! Please keep it under 64 characters'
              },
            ]"
        />
      </div>
      <div class="user-info-item">
        <AwesomeContainer icon="fa-solid fa-envelope" class="user-info-icon">
          <SmartInput
            v-model="userEmail"
            type="email"
            name="username"
            placeholder="Email"
            :invalid="userEmailInvalid"
          />
        </AwesomeContainer>
        <ErrorText
          :when="userEmailWrongFormat"
          text="This email seems to be lost in a cosmic dust cloud. Please check the format"
        />
      </div>
      <div class="user-info-item">
        <AwesomeContainer icon="fa-solid fa-globe" class="user-info-icon">
          <FuzzySelect
            id="language"
            v-model="language"
            :options="languages"
            :option-label="(lang) => lang.name"
            :invalid="languageInvalid"
            option-placeholder="Language"
            search-placeholder="Search..."
          />
        </AwesomeContainer>
      </div>
      <div class="user-info-item">
        Registered at: {{ user?.registeredAt ?? 'Unknown' }}
      </div>
      <div class="user-info-item">
        Timezone: {{ user?.timezone ?? 'Unknown' }}
      </div>
      <div class="user-controls">
        <SmartButton
          text="Logout"
          class="cancel-button"
          :on-click="logout"
          fill-width
          auto-blur
        />
        <SmartButton
          text="Save"
          class="update-button"
          :on-click="save"
          :disabled="!stateChanged || validationFailed"
          auto-blur
        />
      </div>
    </div>
  </div>
  <SpaceToast/>
</template>


<script setup lang="ts">
import AwesomeContainer from '@/components/AwesomeContainer.vue'
import SmartInput from '@/components/SmartInput.vue'
import SmartButton from '@/components/SmartButton.vue'
import FuzzySelect from '@/components/FuzzySelect.vue'
import SpaceToast from '@/components/SpaceToast.vue'
import ErrorText from '@/components/ErrorText.vue'
import { useAuthStore } from '@/stores/auth-store.ts'
import { useLanguageStore } from '@/stores/language-store.ts'
import { storeToRefs } from 'pinia'
import { computed, onMounted, ref, watch } from 'vue'
import { Language } from '@/model/language.ts'
import { useVuelidate } from '@vuelidate/core'
import { email, helpers, maxLength, required } from '@vuelidate/validators'
import { loadLanguageStore } from '@/shared/stores.ts'
import { sendUserUpdateRequest } from '@/api/api-client.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { routeNames } from '@/router'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const languageStore = useLanguageStore()
const toaster = useSpaceToaster()
const router = useRouter()

const { user } = storeToRefs(authStore)
const { languages } = storeToRefs(languageStore)

const username = ref(user.value?.name ?? 'Unknown')
const userEmail = ref(user.value?.email ?? 'Unknown')
const language = ref<Language | undefined>()

console.log('User language id: ', JSON.stringify(language.value))

const stateChanged = computed(() => {
  return username.value !== user.value?.name ||
    userEmail.value !== user.value?.email ||
    language.value?.id !== user.value?.languageId
})

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

const $v = useVuelidate({
  username: {
    required,
    maxLength: maxLength(64),
    regex: helpers.regex(/^[A-Za-z0-9 _-]+$/),
  },
  userEmail: { required, email },
  language: { required },
}, {
  username: username,
  userEmail: userEmail,
  language: language,
})

const validationFailed = computed(() => $v.value.$errors.length > 0)
const usernameInvalid = computed(() => $v.value.username.$errors.length > 0)
const usernameRegexMismatch = computed(() =>
  $v.value.username.$errors.find(v => v.$validator === 'regex') !== undefined
)
const usernameMaxLengthInvalid = computed(() =>
  $v.value.username.$errors.find(v => v.$validator === 'maxLength') !== undefined
)
const userEmailInvalid = computed(() => $v.value.userEmail.$errors.length > 0)
const userEmailWrongFormat = computed(() =>
  $v.value.userEmail.$errors.find(v => v.$validator === 'email') !== undefined
)
const languageInvalid = computed(() => $v.value.language.$errors.length > 0)

async function save() {
  console.log('Saving user info...')
  $v.value.$touch()
  if (validationFailed.value) {
    console.error('User info is invalid')
    return
  }

  await sendUserUpdateRequest(username.value, userEmail.value, language.value?.id)
    .then((response) => {
      authStore.setUser(response.data)
      console.log(`User ${response.data.id} info saved`)
    })
    .catch((error) => {
      console.error('Failed to save user info: ', error)
      toaster.bakeError(`Couldn't update user info`, error.response?.data)
    })
}

function logout() {
  router.push({ name: routeNames.logout })
}

watch(username, () => {
  if (usernameInvalid.value) {
    $v.value.username.$reset()
  }
})

watch(userEmail, () => {
  if (userEmailInvalid.value) {
    $v.value.userEmail.$reset()
  }
})

watch(language, () => {
  if (languageInvalid.value) {
    $v.value.language.$reset()
  }
})

onMounted(() => {
  loadLanguageStore()
    .then((success) => {
      if (success) {
        language.value = languageStore.getLanguage(user.value?.languageId ?? -1)
      }
    })
})

</script>

<style scoped>
.user-page--theme {
  --u-page--icon--color: #6369d5;
  --u-page--bg: var(--user-page--bg, #f5f5f5);
}

.user-page {
  gap: 10px;
  background: var(--u-page--bg);
}

.user-page h2 {
  margin: 0;
  padding: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: repeat(6, auto);
  grid-auto-flow: column;
  gap: 10px;
}

.user-info-item {
  align-items: center;
}

.user-info-icon {
  --awesome-container--icon--color: var(--u-page--icon--color);
}

.user-controls {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  margin-top: 20px;
}

</style>
