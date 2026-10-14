import { describe, expect, it } from 'vitest'
import { findExactDuplicateFlashcard, findDuplicates } from '@/core-logic/flashcard-logic.ts'
import { Flashcard } from '@/model/flashcard.ts'

describe('findDuplicates', () => {
  it('should return empty set when no duplicates', () => {
    const candidates = [
      { frontSide: 'A', backSide: 'B' },
      { frontSide: 'C', backSide: 'D' },
    ]
    const existing = [makeFlashcard(1, 'X', 'Y')]

    const result = findDuplicates(candidates, existing)

    expect(result.size).toBe(0)
  })

  it('should detect duplicate matching existing flashcard', () => {
    const candidates = [
      { frontSide: 'Hello', backSide: 'World' },
      { frontSide: 'Foo', backSide: 'Bar' },
    ]
    const existing = [makeFlashcard(1, 'Hello', 'World')]

    const result = findDuplicates(candidates, existing)

    expect(result).toContain(0)
    expect(result).not.toContain(1)
  })

  it('should detect duplicate case-insensitively', () => {
    const candidates = [{ frontSide: 'hello', backSide: 'world' }]
    const existing = [makeFlashcard(1, 'HELLO', 'WORLD')]

    const result = findDuplicates(candidates, existing)

    expect(result).toContain(0)
  })

  it('should detect intra-batch duplicates', () => {
    const candidates = [
      { frontSide: 'A', backSide: 'B' },
      { frontSide: 'A', backSide: 'B' },
    ]
    const existing: Flashcard[] = []

    const result = findDuplicates(candidates, existing)

    expect(result).not.toContain(0)
    expect(result).toContain(1)
  })

  it('should NOT flag partial match as duplicate', () => {
    const candidates = [{ frontSide: 'Hello', backSide: 'Earth' }]
    const existing = [makeFlashcard(1, 'Hello', 'World')]

    const result = findDuplicates(candidates, existing)

    expect(result.size).toBe(0)
  })

  it('should detect duplicate with leading/trailing whitespace in existing', () => {
    const candidates = [{ frontSide: 'Hello', backSide: 'World' }]
    const existing = [makeFlashcard(1, '  Hello  ', '  World  ')]

    const result = findDuplicates(candidates, existing)

    expect(result).toContain(0)
  })
})

describe('findExactDuplicateFlashcard', () => {
  it('should return the flashcard with matching sides', () => {
    const existing = [makeFlashcard(1, 'Hello', 'World'), makeFlashcard(2, 'Foo', 'Bar')]

    const result = findExactDuplicateFlashcard({ frontSide: 'Foo', backSide: 'Bar' }, existing)

    expect(result?.id).toBe(2)
  })

  it('should ignore case and surrounding or repeated whitespace', () => {
    const existing = [makeFlashcard(1, 'Good  morning', 'Bonjour')]

    const result = findExactDuplicateFlashcard(
      { frontSide: '  good morning ', backSide: 'BONJOUR\n' },
      existing,
    )

    expect(result?.id).toBe(1)
  })

  it('should NOT match when only one side is the same', () => {
    const existing = [makeFlashcard(1, 'Hello', 'World')]

    const result = findExactDuplicateFlashcard({ frontSide: 'Hello', backSide: 'Earth' }, existing)

    expect(result).toBeUndefined()
  })

  it('should NOT match when a side has no text', () => {
    const existing = [makeFlashcard(1, 'Hello', ''), makeFlashcard(2, '', '')]

    expect(findExactDuplicateFlashcard({ frontSide: 'Hello', backSide: '' }, existing)).toBeUndefined()
    expect(
      findExactDuplicateFlashcard({ frontSide: '  ', backSide: undefined }, existing),
    ).toBeUndefined()
  })
})

function makeFlashcard(id: number, frontSide: string, backSide: string): Flashcard {
  return {
    id,
    frontSide,
    backSide,
    stage: 'S1',
    timesReviewed: 0,
    reviewHistory: { history: [] },
    creationDate: '2026-04-13',
  }
}
