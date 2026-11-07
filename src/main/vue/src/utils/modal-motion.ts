export interface Box {
  left: number
  top: number
  width: number
  height: number
}

export interface MotionSample {
  y: number
  time: number
}

const MIN_SCALE = 0.04
const VELOCITY_WINDOW_MS = 100
const SHEET_DISMISS_DISTANCE = 0.4
const SHEET_DISMISS_VELOCITY = 0.5
const MIN_SLIDE_FRACTION = 0.3

/** Transform that makes a box laid out at `to` cover `from` instead. */
export function originTransform(from: Box, to: Box): string {
  const sx = Math.max(from.width / to.width, MIN_SCALE)
  const sy = Math.max(from.height / to.height, MIN_SCALE)
  const dx = from.left + from.width / 2 - (to.left + to.width / 2)
  const dy = from.top + from.height / 2 - (to.top + to.height / 2)
  return `translate(${dx}px, ${dy}px) scale(${sx}, ${sy})`
}

/** Follows the finger downwards; pulling up meets growing resistance. */
export function sheetDragOffset(delta: number): number {
  return delta >= 0 ? delta : -Math.sqrt(-delta) * 2
}

/** Pixels per millisecond over the most recent samples; positive is downwards. */
export function dragVelocity(samples: MotionSample[]): number {
  const last = samples[samples.length - 1]
  if (!last) return 0
  const first = samples.find((s) => last.time - s.time <= VELOCITY_WINDOW_MS) ?? last
  const elapsed = last.time - first.time
  return elapsed > 0 ? (last.y - first.y) / elapsed : 0
}

/** Dragged far enough that the sheet should close without waiting for release. */
export function reachedDismissDistance(offset: number, height: number): boolean {
  return offset >= height * SHEET_DISMISS_DISTANCE
}

/** Released with a quick downward flick. */
export function isDismissFlick(velocity: number): boolean {
  return velocity > SHEET_DISMISS_VELOCITY
}

/** Shortens a slide so a sheet that's already partly down doesn't crawl. */
export function remainingSlideDuration(offset: number, height: number, full: number): number {
  const remaining = height > 0 ? 1 - offset / height : 1
  return full * Math.min(Math.max(remaining, MIN_SLIDE_FRACTION), 1)
}
