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
    <div class="comic-container">
      <img
        src="@/assets/mascot.svg"
        alt="Melvin"
        role="button"
        tabindex="0"
        class="mascot-img"
        @click="handleClickOnMascot"
      />
      <div class="speech-bubble">
        <p v-if="!mascotClicked">
          Hi, my name is Melvin. I am an emotional support possum.
          Don't panic! I'm here to help you. What seems to be the problem?
        </p>
        <p v-else>
          {{ mascotPhrase }}
        </p>
      </div>
    </div>
    <div class="tips-categories">
      <div
        class="tip-category tip-category--left"
        :class="{ 'tip-category--left--active': currentCategory === 'memory' }"
        @click="currentCategory = 'memory'"
      >
        Memory
      </div>
      <div
        class="tip-category tip-category--middle"
        :class="{ 'tip-category--middle--active': currentCategory === 'motivation' }"
        @click="currentCategory = 'motivation'"
      >
        Motivation
      </div>
      <div
        class="tip-category tip-category--right"
        :class="{ 'tip-category--right--active': currentCategory === 'overwhelmed' }"
        @click="currentCategory = 'overwhelmed'"
      >
        Overwhelmed
      </div>
    </div>
    <div v-if="currentCategory" class="tips-tape">
      <SwipeTape
        :frames="currentCategoryTips"
        show-progress
        progress-theme="dark"
        :show-navigation="isHoverSupported"
        navigation-theme="dark"
        looped-tape
      >
        <template #default="{ frame }">
          <div class="tip">
            <p>{{ frame }}</p>
          </div>
        </template>
      </SwipeTape>
    </div>
  </div>
</template>

<script setup lang="ts">
import SwipeTape from '@/components/SwipeTape.vue'
import { computed, ref } from 'vue'
import { isHoverSupported, shuffle } from '@/utils/utils.ts'

const mascotPhrases = [
  `I really like your shoes!`,
  `Don't tell the others... but you're my favorite space traveler.`,
  `Did you change your fur... I mean, hair? Looking fresh!`,
  `Good news, there's no traffic in orbit!`,
  `I just took a tactical nap. You should try it.`,
  `Your day streak is quite impressive!`,
  `It's lonely out here in the void, glad you stopped by.`,
  `Zero gravity is great for my back.`,
  `Feeling overwhelmed? Just play dead. Works for me.`,
  `I eat trash, but you consume knowledge. We are not the same.`,
  `If I hiss at you, it's a hiss of affection.`,
  `*Internal screaming*`,
  `I have deployed the emergency blanket. It is invisible, but it is warm.`,
  `I am currently playing dead. Feel free to join me.`,
  `Status Report: You are allowed to be a potato today. A space potato.`,
  `System overload? Have you tried screaming and then eating a snack?`,
  `*Hisses affectionately*`,
  `Look at me. Look at my weird little hands. Everything is going to be okay.`,
  `I don't know what "Anki" is, but if it made you sad, I will bite it.`,
  `The stars are exploding constantly. You're doing fine by comparison.`,
  `Gravity is heavy today. It's not your fault.`,
  `I found a shiny button. I don't know what it does. Do you want it?`,
  `Unclench your jaw. Yes, I see you.`,
  `Take a sip of water. Space is dry.`,
]

const memoryTips = [
  `Take that one word that refuses to be memorized, and make it super memorable. ` +
  `Create a weird mnemonic, create a crazy sentence with it, ` +
  `say it to your family that doesn't speak the language and explain what it means.`,

  `Even the best astronauts forget things, try using a different approach ` +
  `like singing the word in an operatic voice, or say it while dancing, ` +
  `or even singing it in the shower.`,

  `Brain refusing to cooperate? Imagine the word written in neon lights on a giant ` +
  `piece of trash floating in space. Sounds weird? Good. Weird sticks.`,

  `Try "acting out" the word with your hands. Even if it's an abstract concept. ` +
  `Flail your arms. Your brain remembers movement better than text.`,
]

const motivationTips = [
  `Can't start? Chill out, eat a moon pie or another tasty snack and learn words for 5 mins.`,

  `You are not a boring person, so add some personality to your studying! ` +
  `Have a specific study spot that has some cozy things like pillows, blankets, or snacks. ` +
  `Maybe it has some cool lighting to help you get into a cool, calm, and collected mood. ` +
  `And don’t forget your unique taste in music to top it all off!`,

  `Can't do a full session? Fine. Do exactly three cards. Just three. ` +
  `If you still hate it after three, you have my permission to go back to sleep.`,

  `Pretend you are an alien spy sent to Earth. You *need* to learn this vocabulary ` +
  `to blend in with the humans. Don't blow your cover!`,

  `I am a simple creature. I see snack, I do task. Have you tried putting a piece of chocolate ` +
  `next to the SpaceBar? One card = one bite.`,
];

const overwhelmedTips = [
  `You don't need to be the superman of flashcards in space all the time. ` +
  `Set a realistic number of words you want to look at today, and when you reach it, stop. ` +
  `Here at flashcards in space, we think you are doing great no matter the number of cards ` +
  `you have in your review sessions ❤️`,

  `Feeling overwhelmed with number of words? Don't add new words for a while.`,

  `You can always take a vacation 🌴 Don't forget to enable vacation mode until you ` +
  `feel less overwhelmed and ready to continue. But please be aware that the vacation mode ` +
  `ends your streak.`,

  `Whoa, space cowboy. Your review pile is taller than a rocket. ` +
  `Stop adding "New Cards" immediately! Throw them out the airlock until you feel better.`,

  `The number of reviews is scary? Squint your eyes until the number looks blurry. There. ` +
  `Now it's just a friendly blob. Tackle the blob for 5 minutes.`,

  `So what if you have 500 reviews? They aren't going anywhere. They will wait. ` +
  `The void is patient, and so am I. Just do what you can.`,

  `Some cards are just "leeches" sucking your energy. Suspend them! ` +
  `Banish them to the dark side of the moon! You can relearn them next year.`,
];

type TipsCategory = 'memory' | 'motivation' | 'overwhelmed'

const shuffledMemoryTips = shuffle(memoryTips)
const shuffledMotivationTips = shuffle(motivationTips)
const shuffledOverwhelmedTips = shuffle(overwhelmedTips)

const currentCategory = ref<TipsCategory>()
const mascotClicked = ref(false)
const mascotPhrase = ref('')

const currentCategoryTips = computed(() => {
  switch (currentCategory.value) {
    case 'memory':
      return shuffledMemoryTips
    case 'motivation':
      return shuffledMotivationTips
    case 'overwhelmed':
      return shuffledOverwhelmedTips
    default:
      return []
  }
})

function handleClickOnMascot() {
  mascotClicked.value = true
  const index = Math.floor(Math.random() * mascotPhrases.length)
  mascotPhrase.value = mascotPhrases[index]
}

</script>

<style scoped>
.emotional-support-page {
  padding: clamp(10px, 5vw, 20px) clamp(20px, 5vw, 40px);
  gap: 10px;
  align-items: center;
}

.comic-container {
  display: flex;
  align-items: center;
  justify-content: start;
  gap: 20px;
  width: 800px;
  margin-bottom: 40px;
}

.mascot-img {
  width: 300px;
  height: 160px;
  cursor: pointer;
}

.speech-bubble {
  position: relative;
  min-height: 100px;
  background: #ffffff;
  color: #000000;
  border: 3px solid #000000;
  border-radius: 44px;
  padding: 25px 35px;
  font-family: var(--site-content--font-family);
  box-shadow: 6px 6px 6px rgba(0, 0, 0, 0.1);
  animation: popIn 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.speech-bubble p {
  margin: 0;
  padding: 0;
  font-size: 1rem;
  line-height: 1.4;
  font-weight: 500;
}

.speech-bubble::before {
  content: "";
  position: absolute;
  left: -24px; /* Push out to left */
  top: 50%; /* Center vertically */
  transform: translateY(-50%);
  border-style: solid;
  border-width: 15px 24px 15px 0;
  border-color: transparent #000000 transparent transparent;
}

.speech-bubble::after {
  content: "";
  position: absolute;
  left: -19px; /* Slightly right of the black one */
  top: 50%;
  transform: translateY(-50%);
  border-style: solid;
  border-width: 12px 20px 12px 0;
  border-color: transparent #ffffff transparent transparent;
}

@keyframes popIn {
  0% {
    opacity: 0;
    transform: scale(0.9);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

.tips-categories {
  display: flex;
  flex-direction: row;
  justify-content: center;
  gap: 0;
  width: 100%;
  height: 44px;
  padding: 0 10px;
}

.tip-category {
  flex: 1;
  max-width: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.6);
  font-family: var(--site-content--font-family);
  font-size: 1rem;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.tip-category--left {
  border-top-left-radius: 20px;
  border-bottom-left-radius: 20px;
  border-right: none;
}

.tip-category--middle {
  border-right: none;
}

.tip-category--right {
  border-top-right-radius: 20px;
  border-bottom-right-radius: 20px;
}

.tip-category:not([class*="--active"]):hover {
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
}

.tip-category--left--active {
  color: #ffffff;
  border-color: transparent;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  box-shadow: 0 0 15px rgba(59, 130, 246, 0.5);
}

.tip-category--middle--active {
  color: #ffffff;
  border-color: transparent;
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  box-shadow: 0 0 15px rgba(34, 197, 94, 0.5);
}

.tip-category--right--active {
  color: #ffffff;
  border-color: transparent;
  background: linear-gradient(135deg, #ec4899 0%, #db2777 100%);
  box-shadow: 0 0 15px rgba(236, 72, 153, 0.5);
}

.tip-category--left--active:hover {
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%); /* Lighter Blue */
}

.tip-category--middle--active:hover {
  background: linear-gradient(135deg, #4ade80 0%, #22c55e 100%); /* Lighter Green */
}

.tip-category--right--active:hover {
  background: linear-gradient(135deg, #f472b6 0%, #ec4899 100%); /* Lighter Pink */
}

.tips-tape {
  width: auto;
  max-width: 1000px;
  height: 254px;
}

.tip {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 22px;
  width: 80%;
  height: fit-content;
  padding: 16px;
  margin: 16px;
  backdrop-filter: blur(10px);
}

.tip p {
  font-family: var(--site-content--font-family);
  font-size: 1rem;
  font-weight: 200;
  color: #ffffff;
  text-align: center;
  margin: 0;
  padding: 0;
}

@media (max-width: 1080px) {
  .tips-tape {
    max-width: 100%;
  }
}

@media (max-width: 880px) {
  .comic-container {
    width: 100%;
  }

  .speech-bubble {
    padding: 15px 20px;
  }

  .speech-bubble p {
    font-size: 0.9rem;
  }

  .tips-tape {
    max-width: 100%;
  }
}

@media (max-width: 600px) {
  .comic-container {
    width: 100%;
    flex-direction: column;
    gap: 20px;
    margin-bottom: 10px;
  }

  .mascot-img {
    order: 2;
    width: 240px;
    height: 130px;
  }

  .speech-bubble {
    order: 1;
    text-align: center;
    width: 100%;
    padding: 15px 20px;
    min-height: 80px;
  }

  .speech-bubble p {
    font-size: 0.9rem;
  }

  .speech-bubble::before,
  .speech-bubble::after {
    left: 50%;
    top: auto;
    transform: translateX(-50%);
  }

  .speech-bubble::before {
    bottom: -24px;
    border-width: 24px 15px 0 15px;
    border-color: #000000 transparent transparent transparent;
  }

  .speech-bubble::after {
    bottom: -19px;
    border-width: 20px 12px 0 12px;
    border-color: #ffffff transparent transparent transparent;
  }

  .tips-categories {
    height: 40px;
  }

  .tip-category {
    font-size: 0.8rem;
    font-weight: 400;
  }

  .tips-tape {
    max-width: 100%;
  }

  .tip {
    padding: 10px;
    margin: 10px;
  }

  .tip p {
    font-size: 0.9rem;
  }
}

</style>
