import * as XLSX from 'xlsx'
import type { Flashcard, FlashcardContent } from '@/model/flashcard.ts'

export function parseFlashcardsFromFile(file: File): Promise<FlashcardContent[]> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      try {
        const data = e.target?.result
        const workbook = XLSX.read(data, { type: 'array' })
        const sheet = workbook.Sheets[workbook.SheetNames[0]]
        const rows: unknown[][] = XLSX.utils.sheet_to_json(sheet, { header: 1 })
        const result: FlashcardContent[] = []
        for (const row of rows) {
          if (!Array.isArray(row) || row.length < 2) continue
          const rawFront = row[0]
          const rawBack = row[1]
          if (rawFront === null || rawFront === undefined) continue
          if (rawBack === null || rawBack === undefined) continue
          const front = String(rawFront).trim()
          const back = String(rawBack).trim()
          if (!front || !back) continue
          result.push({ frontSide: front, backSide: back })
        }
        resolve(result)
      } catch {
        reject(new Error('Failed to parse file'))
      }
    }
    reader.onerror = () => reject(new Error('Failed to read file'))
    reader.readAsArrayBuffer(file)
  })
}

export function downloadFlashcardsAsCsv(flashcards: Flashcard[], filename: string): void {
  const rows = flashcards.map(f => [f.frontSide, f.backSide])
  const sheet = XLSX.utils.aoa_to_sheet(rows)
  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, sheet, 'Flashcards')
  XLSX.writeFile(workbook, `${filename}.csv`)
}

export function downloadFlashcardsAsXlsx(flashcards: Flashcard[], filename: string): void {
  const rows = flashcards.map(f => [f.frontSide, f.backSide])
  const sheet = XLSX.utils.aoa_to_sheet(rows)
  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, sheet, 'Flashcards')
  XLSX.writeFile(workbook, `${filename}.xlsx`)
}

