export const deviceType = () => {
  if (typeof window === 'undefined') return { isMobile: false, isTablet: false, isDesktop: false }

  const userAgent = navigator.userAgent.toLowerCase()
  const maxPoints = navigator.maxTouchPoints || 0

  const isTablet =
    /(tablet|ipad|playbook|silk)|(android(?!.*mobi))/i.test(userAgent) ||
    (userAgent.includes("macintosh") && maxPoints > 1) // modern iPads

  const isMobile = /mobile|iphone|ipod|android|blackberry|iemobile/i.test(userAgent) && !isTablet

  const isDesktop = !isMobile && !isTablet

  return {
    isMobile,
    isTablet,
    isDesktop
  }
}

export const deviceInteraction = () => {
  if (typeof window === 'undefined') return { isTouch: false, isHover: false, isAnyHover: false }

  // Physical hardware check
  const maxPoints = navigator.maxTouchPoints || 0
  const hasTouchHardware = maxPoints > 0

  return {
    // Primary input is a finger or coarse pointer
    isTouch: window.matchMedia('(pointer: coarse)').matches,
    // Primary input is a finger and no hover capability
    isTouchOnly: window.matchMedia('(hover: none)').matches,
    // Device supports hover (mouse/trackpad)
    isHover: window.matchMedia('(hover: hover)').matches,
    // Any secondary input supports hover (e.g., mouse plugged into tablet)
    isAnyHover: window.matchMedia('(any-hover: hover)').matches,
    // Hardware capability
    hasTouch: hasTouchHardware
  }
}

export const UXConfig = () => {
  const { isTouch, isTouchOnly, isHover, hasTouch } = deviceInteraction()
  const { isMobile, isTablet, isDesktop } = deviceType()

  return {
    // Whether the user can see the animation of a tap
    showAnimationOnTap: !isTouch,
    // Whether the user can see the tooltip
    showTooltips: isHover,
    // Whether the user can see the navigation buttons
    showNavButtons: isDesktop || isHover || isTablet,
    // Whether the user can swipe
    canSwipe: hasTouch,
    // Whether the user can see the swipe hints
    showSwipeHints: hasTouch,
    // Whether the user should see the learning stages widget expanded
    expandLearningStagesWidget: isTouchOnly,
    // Assume mobile/tablet browsers will block autoplay until a user gesture
    hasStrictAudio: isMobile || isTablet,
  }
}
