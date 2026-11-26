import { describe, expect, it } from 'vitest'
import {
  GridLayout,
  layoutWidgets,
  maxCells,
  packWidgets,
  WidgetSizing,
} from '@/utils/widget-grid.ts'

const square = (id: string): WidgetSizing => ({ id, colSpans: [1], rowSpans: [1] })
const stretchy = (id: string): WidgetSizing => ({ id, colSpans: [1, 2], rowSpans: [1] })

const panelWidgets: WidgetSizing[] = [
  square('F'),
  stretchy('C'),
  square('U'),
  stretchy('D'),
  { id: 'R', colSpans: [2, 3], rowSpans: [1] },
  { id: 'L', colSpans: [2, 3, 4], rowSpans: [1, 2] },
  square('A'),
  stretchy('Q'),
]

function draw(layout: GridLayout): string[] {
  const grid = Array.from({ length: layout.rows }, () => Array(layout.cols).fill('.'))
  for (const p of layout.placements) {
    for (let r = 0; r < p.rowSpan; r++) {
      for (let c = 0; c < p.colSpan; c++) {
        grid[p.row - 1 + r][p.col - 1 + c] = p.id
      }
    }
  }
  return grid.map((row) => row.join(''))
}

describe('packWidgets', () => {
  it('keeps preferred sizes when they fill the rows', () => {
    expect(draw(packWidgets(panelWidgets, 10))).toEqual(['FCUDRRLLAQ'])
  })

  it('stretches widgets to close the gaps', () => {
    expect(draw(packWidgets(panelWidgets, 3))).toEqual(['FCU', 'DRR', 'LLA', 'LLQ'])
    expect(draw(packWidgets(panelWidgets, 4))).toEqual(['FCUD', 'RRLL', 'AQLL'])
    expect(draw(packWidgets(panelWidgets, 5))).toEqual(['FCUDD', 'RRLLL', 'AQLLL'])
  })

  it('keeps the order unless a widget had to move back to close a gap', () => {
    for (let cols = 1; cols <= 16; cols++) {
      const layout = packWidgets(panelWidgets, cols)
      if (layout.backfilled > 0) continue
      const ids = layout.placements
        .slice()
        .sort((a, b) => a.row - b.row || a.col - b.col)
        .map((p) => p.id)
      expect(ids.join('')).toBe('FCUDRLAQ')
    }
  })

  it('moves a later widget back into a gap that resizing cannot close', () => {
    const widgets = [
      square('A'),
      square('B'),
      square('C'),
      { id: 'W', colSpans: [2], rowSpans: [1] },
      square('D'),
    ]

    const layout = packWidgets(widgets, 2)
    expect(draw(layout)).toEqual(['AB', 'CD', 'WW'])
    expect(layout.backfilled).toBe(1)
  })

  it('resizes rather than moves when both close the gap', () => {
    const widgets = [
      square('A'),
      stretchy('B'),
      { id: 'W', colSpans: [2], rowSpans: [1] },
      square('C'),
    ]

    const layout = packWidgets(widgets, 3)
    expect(draw(layout)).toEqual(['ABB', 'WWC'])
    expect(layout.backfilled).toBe(0)
  })

  it('moves a widget up when allowed and that closes a gap', () => {
    const widgets: WidgetSizing[] = [
      square('U'),
      square('F'),
      square('C'),
      { id: 'R', colSpans: [2], rowSpans: [1] },
      stretchy('D'),
      { id: 'L', colSpans: [2], rowSpans: [1, 2], maxMoveUp: 1 },
      square('A'),
      stretchy('Q'),
    ]

    const layout = packWidgets(widgets, 7)
    expect(draw(layout)).toEqual(['UFCRRLL', 'DDAQQLL'])
    expect(layout.movedUp).toBe(1)
  })

  it('does not move a widget up when the given order fills as well', () => {
    const widgets: WidgetSizing[] = [
      square('A'),
      stretchy('B'),
      { id: 'L', colSpans: [2], rowSpans: [1], maxMoveUp: 1 },
    ]

    const layout = packWidgets(widgets, 2)
    expect(draw(layout)).toEqual(['AB', 'LL'])
    expect(layout.movedUp).toBe(0)
  })

  it('moves an unavoidable hole to the end of the last row', () => {
    const widgets = [square('A'), { id: 'W', colSpans: [2], rowSpans: [1] }, square('B')]
    const layout = packWidgets(widgets, 3)

    expect(draw(layout)).toEqual(['AWW', 'B..'])
    expect(layout.innerHoles).toBe(0)
  })

  it('counts a hole before a widget in the last row as inner', () => {
    const widgets = [square('A'), square('B'), { id: 'L', colSpans: [2], rowSpans: [2] }]

    const layout = packWidgets(widgets, 4)
    expect(draw(layout)).toEqual(['ABLL', '..LL'])
    expect(layout.innerHoles).toBe(2)
  })

  it('reports holes when no size combination fills the rows', () => {
    expect(packWidgets([square('A'), square('B'), square('C')], 2).holes).toBe(1)
  })

  it('squeezes a widget wider than the grid into the full width', () => {
    const layout = packWidgets([{ id: 'W', colSpans: [3], rowSpans: [1] }], 2)
    expect(layout.placements[0]).toMatchObject({ col: 1, colSpan: 2 })
  })
})

describe('maxCells', () => {
  it('counts the cells and gaps that fit', () => {
    expect(maxCells(106, 106, 12)).toBe(1)
    expect(maxCells(223, 106, 12)).toBe(1)
    expect(maxCells(224, 106, 12)).toBe(2)
    expect(maxCells(0, 106, 12)).toBe(1)
  })
})

describe('layoutWidgets', () => {
  const metrics = { minCellWidth: 106, gap: 12, maxCellGrowth: Infinity }

  const sevenSquares = ['A', 'B', 'C', 'D', 'E', 'F', 'G'].map(square)
  const eightCells = 8 * 106 + 7 * 12

  it('uses fewer cells when the most that fit would leave holes', () => {
    expect(packWidgets(sevenSquares, 8).holes).toBe(1)
    expect(layoutWidgets(sevenSquares, eightCells, metrics).cols).toBe(7)
  })

  it('prefers holes over cells growing past maxCellGrowth', () => {
    const layout = layoutWidgets(sevenSquares, eightCells, { ...metrics, maxCellGrowth: 1.1 })
    expect(layout.cols).toBe(8)
    expect(layout.holes).toBe(1)
  })

  it('falls back to the fewest holes', () => {
    const layout = layoutWidgets([square('A'), square('B'), square('C')], 224, metrics)
    expect(layout.cols).toBe(1)
    expect(layout.holes).toBe(0)
  })
})
