const TRIGGER_SELECTOR = 'button, [role="button"], a, label'
const MAX_AGE_MS = 1500

let lastPressed: { el: Element; time: number } | undefined
let tracking = false

export function trackModalOrigins() {
  if (tracking) return
  tracking = true
  // Capture phase: AwesomeButton stops click propagation.
  document.addEventListener('pointerdown', (event) => remember(event.target), true)
  document.addEventListener(
    'keydown',
    (event) => {
      if (event.key === 'Enter' || event.key === ' ') remember(document.activeElement)
    },
    true,
  )
}

function remember(target: EventTarget | null) {
  if (!(target instanceof Element)) return
  lastPressed = { el: target.closest(TRIGGER_SELECTOR) ?? target, time: performance.now() }
}

export function takeModalOrigin(): Element | undefined {
  const pressed = lastPressed
  lastPressed = undefined
  if (!pressed || performance.now() - pressed.time > MAX_AGE_MS) return undefined
  return pressed.el
}

/** The element's box if it's still on screen, so a modal can animate to it. */
export function visibleBox(el: Element | undefined): DOMRect | undefined {
  if (!el?.isConnected) return undefined
  const rect = el.getBoundingClientRect()
  if (rect.width === 0 || rect.height === 0) return undefined
  const offscreen =
    rect.bottom < 0 || rect.right < 0 || rect.top > innerHeight || rect.left > innerWidth
  return offscreen ? undefined : rect
}
