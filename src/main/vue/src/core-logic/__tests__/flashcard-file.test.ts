// @vitest-environment jsdom
import { describe, it, expect } from 'vitest'
import {
  parseFlashcardsFromFile,
} from '@/core-logic/flashcard-file-logic.ts'

function makeCsvFile(content: string): File {
  const bytes = new TextEncoder().encode(content)
  return new File([bytes], 'test.csv', { type: 'text/csv' })
}

describe('parseFlashcardsFromFile', () => {
  it('should parse valid CSV with two columns', async () => {
    const file = makeCsvFile('What is 2+2?,4\nCapital of France?,Paris')

    const result = await parseFlashcardsFromFile(file)

    expect(result).toHaveLength(2)
    expect(result[0]).toEqual({ frontSide: 'What is 2+2?', backSide: '4' })
    expect(result[1]).toEqual({ frontSide: 'Capital of France?', backSide: 'Paris' })
  })

  it('should skip rows with only one column', async () => {
    const file = makeCsvFile('OnlyOneColumn\nfront,back')

    const result = await parseFlashcardsFromFile(file)

    expect(result).toHaveLength(1)
    expect(result[0]).toEqual({ frontSide: 'front', backSide: 'back' })
  })

  it('should skip empty rows', async () => {
    const file = makeCsvFile('front1,back1\n\nfront2,back2')

    const result = await parseFlashcardsFromFile(file)

    expect(result).toHaveLength(2)
  })

  it('should skip rows where front or back is empty after trim', async () => {
    const file = makeCsvFile('  ,back\nfront,  \nfront,back')

    const result = await parseFlashcardsFromFile(file)

    expect(result).toHaveLength(1)
    expect(result[0]).toEqual({ frontSide: 'front', backSide: 'back' })
  })

  it('should trim whitespace from front and back', async () => {
    const file = makeCsvFile('  hello  ,  world  ')

    const result = await parseFlashcardsFromFile(file)

    expect(result).toHaveLength(1)
    expect(result[0]).toEqual({ frontSide: 'hello', backSide: 'world' })
  })
})


