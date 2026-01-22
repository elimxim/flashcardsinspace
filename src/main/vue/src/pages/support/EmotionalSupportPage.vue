<template>
  <div
    :class="[
      'page',
      'flex-column',
      'padding-auto',
      'scrollbar-hidden',
      'emotional-support-page',
    ]"
  >
    <div class="help-tips-tape">
      <SwipeTape
        :frames="randomizedTips"
        show-progress
        progress-theme="dark"
        :show-navigation="isHoverSupported"
        navigation-theme="dark"
        looped-tape
      >
        <template #default="{ frame }">
          <div class="help-tip">
            <p>{{ frame }}</p>
          </div>
        </template>
      </SwipeTape>
    </div>
  </div>
</template>

<script setup lang="ts">
import SwipeTape from '@/components/SwipeTape.vue'
import { computed } from 'vue'
import { isHoverSupported, shuffle } from '@/utils/utils.ts'

const helpTips = [
  `Take that one word that refuses to be memorized, and make it super memorable. ` +
  `Create a weird mnemonic, create a crazy sentence with it, ` +
  `say it to your family that doesn't speak the language and explain what it means.`,

  `Even the best astronauts forget things, try using a different approach ` +
  `like singing the word in an operatic voice, or say it while dancing, ` +
  `or even singing it in the shower.`,

  `Can't start? Chill out, eat a moon pie or another tasty snack and learn words for 5 mins.`,

  `You are not a boring person, so add some personality to your studying! ` +
  `Have a specific study spot that has some cozy things like pillows, blankets, or snacks. ` +
  `Maybe it has some cool lighting to help you get into a cool, calm, and collected mood. ` +
  `And don’t forget your unique taste in music to top it all off!`,

  `You don't need to be the superman of flashcards in space all the time. ` +
  `Set a realistic number of words you want to look at today, and when you reach it, stop. ` +
  `Here at flashcards in space, we think you are doing great no matter the number of cards ` +
  `you have in your review sessions ❤️`,

  `Feeling overwhelmed with number of words? Don't add new words for a while.`,

  `You can always take a vacation 🌴 Don't forget to enable vacation mode until you ` +
  `feel less overwhelmed and ready to continue. But please be aware that the vacation mode ` +
  `ends your streak.`,
]

const randomizedTips = computed(() => {
  const tips = [...helpTips]
  shuffle(tips)
  return tips
})

</script>

<style scoped>
.emotional-support-page {
  padding: clamp(10px, 5vw, 20px) clamp(20px, 5vw, 40px);
}

.help-tips-tape {
  height: fit-content;
}

.help-tip {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 22px;
  width: 80%;
  height: fit-content;
  padding: 16px;
  margin: 16px;
  backdrop-filter: blur(10px);
}

.help-tip p {
  font-family: var(--site-content--font-family);
  font-size: 1rem;
  font-weight: 200;
  color: #ffffff;
  text-align: center;
  margin: 0;
  padding: 0;
}

@media (max-width: 400px) {
  .help-tip {
    padding: 8px;
    margin: 8px;
  }

  .help-tip p {
    font-size: 0.9rem;
  }
}
</style>
