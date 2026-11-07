export const EASE_OUT = 'cubic-bezier(0.22, 1, 0.36, 1)'
export const EASE_IN = 'cubic-bezier(0.4, 0, 0.9, 0.4)'

const FADE_MS = 160

export interface ModalParts {
  overlay: HTMLElement
  backdrop: HTMLElement
  panel: HTMLElement
}

/** Finds the backdrop and panel inside a modal overlay element. */
export function modalParts(overlay: Element | null | undefined): ModalParts | undefined {
  if (!(overlay instanceof HTMLElement)) return undefined
  const backdrop = overlay.querySelector<HTMLElement>('.modal-backdrop')
  const panel = overlay.querySelector<HTMLElement>('.modal-panel')
  if (!backdrop || !panel) return undefined
  return { overlay, backdrop, panel }
}

export function prefersReducedMotion(): boolean {
  return matchMedia('(prefers-reduced-motion: reduce)').matches
}

/** Stops whatever is moving the element and returns where it is right now. */
export function freeze(el: HTMLElement): { transform: string; opacity: string } {
  const style = getComputedStyle(el)
  const state = { transform: style.transform, opacity: style.opacity }
  el.getAnimations().forEach((animation) => animation.cancel())
  el.style.transform = ''
  el.style.opacity = ''
  return state
}

export function play(el: HTMLElement, keyframes: Keyframe[], options: KeyframeAnimationOptions) {
  return el.animate(keyframes, { fill: 'both', ...options })
}

/** Resolves true once all animations end, false if one was interrupted. */
export async function finish(animations: Animation[]): Promise<boolean> {
  try {
    await Promise.all(animations.map((animation) => animation.finished))
    animations.forEach((animation) => animation.cancel())
    return true
  } catch {
    return false
  }
}

/** The whole modal simply fades, for users who asked for less motion. */
export function fade(overlay: HTMLElement, show: boolean): Animation[] {
  const now = freeze(overlay)
  const from = show ? 0 : now.opacity
  return [play(overlay, [{ opacity: from }, { opacity: show ? 1 : 0 }], { duration: FADE_MS })]
}
