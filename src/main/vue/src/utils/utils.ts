/**
 * Creates a seeded pseudorandom number generator using the Mulberry32 algorithm.
 *
 * @param a - The seed value (32-bit integer)
 * @returns A function that generates the next pseudorandom number in the sequence.
 *          Each call returns a floating-point number in the range [0, 1).
 */
export function mulberry32(a: number) {
  return function () {
    let t = (a += 0x6D2B79F5)
    t = Math.imul(t ^ (t >>> 15), t | 1)
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61)
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296
  }
}
