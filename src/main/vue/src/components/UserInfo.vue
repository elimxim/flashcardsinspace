<template>
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
          readonly
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
        class="off-button"
        :on-click="logout"
        fill-width
        auto-blur
      />
      <SmartButton
        text="Save"
        class="calm-button"
        :on-click="save"
        :disabled="!stateChanged || validationFailed"
        auto-blur
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import AwesomeContainer from '@/components/AwesomeContainer.vue'
import SmartInput from '@/components/SmartInput.vue'
import SmartButton from '@/components/SmartButton.vue'
import FuzzySelect from '@/components/FuzzySelect.vue'
import ErrorText from '@/components/ErrorText.vue'
import { useAuthStore } from '@/stores/auth-store.ts'
import { useLanguageStore } from '@/stores/language-store.ts'
import { storeToRefs } from 'pinia'
import { computed, ref, watch } from 'vue'
import { Language } from '@/model/language.ts'
import { useVuelidate } from '@vuelidate/core'
import { email, helpers, maxLength, required } from '@vuelidate/validators'
import { sendUserUpdateRequest } from '@/api/api-client.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { routeNames } from '@/router'
import { useRouter } from 'vue-router'
import { Log, LogTag } from '@/utils/logger.ts'
import { userApiErrors } from '@/api/user-api-error.ts'
import { User } from '@/model/user.ts'

const props = defineProps<{
  user: User | undefined,
}>()

const authStore = useAuthStore()
const languageStore = useLanguageStore()
const toaster = useSpaceToaster()
const router = useRouter()

const { languages } = storeToRefs(languageStore)

const username = ref(props.user?.name ?? 'Unknown')
const userEmail = ref(props.user?.email ?? 'Unknown')
const language = ref<Language | undefined>(
  languageStore.getLanguage(props.user?.languageId ?? -1)
)

const stateChanged = computed(() => {
  return username.value !== props.user?.name ||
    userEmail.value !== props.user?.email ||
    language.value?.id !== props.user?.languageId
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
  Log.log(LogTag.LOGIC, 'Saving user info...')
  $v.value.$touch()
  if (validationFailed.value) {
    Log.error(LogTag.LOGIC, 'User info is invalid')
    return
  }

  await sendUserUpdateRequest(username.value, userEmail.value, language.value?.id)
    .then((response) => {
      authStore.setUser(response.data)
      Log.log(LogTag.LOGIC, `User.id=${response.data.id} info saved`)
    })
    .catch((error) => {
      Log.error(LogTag.LOGIC, 'Failed to save user info: ', error)
      toaster.bakeError(userApiErrors.USER__UPDATING_FAILED, error.response?.data)
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

</script>

<style scoped>
.user-info {
  flex: 0 0 auto;
  display: grid;
  grid-template-columns: 1fr;
  grid-template-rows: repeat(6, auto);
  grid-auto-flow: column;
  gap: 10px;
  max-width: 600px;
}

@media (max-width: 600px) {
  .user-info {
    max-width: 100%;
  }
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
  gap: 10px;
  margin-top: 20px;
}

</style>
