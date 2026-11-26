/**
 * Packs widgets into a grid in their given order, choosing for each widget one
 * of the sizes it allows so that the rows end up as full as possible.
 */

export interface WidgetSizing {
  id: string
  /** Allowed column spans, the first one is the preferred size */
  colSpans: number[]
  /** Allowed row spans, the first one is the preferred size */
  rowSpans: number[]
  /** How many positions earlier than given it may go, when that closes a gap (default 0) */
  maxMoveUp?: number
}

export interface WidgetPlacement {
  id: string
  /** 1-based grid lines, ready for grid-column / grid-row */
  col: number
  row: number
  colSpan: number
  rowSpan: number
}

export interface GridLayout {
  cols: number
  rows: number
  /** Empty cells inside the used rows */
  holes: number
  /** Empty cells other than those ending the last row: unlike these, they read as gaps */
  innerHoles: number
  /** Widgets moved back from their place in the order to fill an earlier hole */
  backfilled: number
  /** Positions the widgets allowing it moved up in the order (see maxMoveUp) */
  movedUp: number
  placements: WidgetPlacement[]
}

interface Size {
  colSpan: number
  rowSpan: number
  /** Whether it differs from the widget's preferred size */
  adjusted: boolean
}

function sizeOptions(widget: WidgetSizing, cols: number): Size[] {
  const options: Size[] = []
  widget.colSpans.forEach((colSpan, colIndex) => {
    if (colSpan > cols) return
    widget.rowSpans.forEach((rowSpan, rowIndex) => {
      options.push({ colSpan, rowSpan, adjusted: colIndex > 0 || rowIndex > 0 })
    })
  })
  if (options.length === 0) {
    // Wider than the grid in every size: squeeze it into the full width
    options.push({ colSpan: cols, rowSpan: widget.rowSpans[0] ?? 1, adjusted: true })
  }
  return options
}

/**
 * Mirrors CSS grid's auto-placement. Sparse: each widget goes into the first
 * slot that fits after the previous one, so the order is never broken. Dense:
 * a widget may instead go back into an earlier hole it fits.
 */
function place(widgets: WidgetSizing[], sizes: Size[], cols: number, dense: boolean): GridLayout {
  const occupied: boolean[][] = []
  const isFree = (row: number, col: number) => !occupied[row]?.[col]
  const fits = (row: number, col: number, size: Size) => {
    if (col + size.colSpan > cols) return false
    for (let r = row; r < row + size.rowSpan; r++) {
      for (let c = col; c < col + size.colSpan; c++) {
        if (!isFree(r, c)) return false
      }
    }
    return true
  }

  const firstFit = (fromRow: number, fromCol: number, size: Size): [number, number] => {
    let row = fromRow
    let col = fromCol
    while (!fits(row, col, size)) {
      col++
      if (col >= cols) {
        col = 0
        row++
      }
    }
    return [row, col]
  }

  const placements: WidgetPlacement[] = []
  let cursorRow = 0
  let cursorCol = 0
  let backfilled = 0

  widgets.forEach((widget, index) => {
    const size = sizes[index]
    let [row, col] = firstFit(cursorRow, cursorCol, size)
    if (dense) {
      const [holeRow, holeCol] = firstFit(0, 0, size)
      if (holeRow < row || (holeRow === row && holeCol < col)) {
        ;[row, col] = [holeRow, holeCol]
        backfilled++
      }
    }
    for (let r = row; r < row + size.rowSpan; r++) {
      occupied[r] ??= []
      for (let c = col; c < col + size.colSpan; c++) {
        occupied[r][c] = true
      }
    }
    placements.push({
      id: widget.id,
      col: col + 1,
      row: row + 1,
      colSpan: size.colSpan,
      rowSpan: size.rowSpan,
    })
    // A backfilled widget leaves the cursor where it was, so the rest keep their order
    if (row === cursorRow && col >= cursorCol) {
      cursorCol = col + size.colSpan
    } else if (row > cursorRow) {
      cursorRow = row
      cursorCol = col + size.colSpan
    }
  })

  const rows = occupied.length
  let filled = 0
  for (const row of occupied) {
    filled += row.filter(Boolean).length
  }
  const holes = rows * cols - filled
  const lastRow = occupied[rows - 1] ?? []
  let trailingHoles = 0
  for (let c = cols - 1; c >= 0 && !lastRow[c]; c--) trailingHoles++

  return {
    cols,
    rows,
    holes,
    innerHoles: holes - trailingHoles,
    backfilled,
    movedUp: 0,
    placements,
  }
}

/** Lower is better, compared in order */
type Score = number[]

function isBetter(candidate: Score, best: Score): boolean {
  for (let i = 0; i < candidate.length; i++) {
    if (candidate[i] !== best[i]) return candidate[i] < best[i]
  }
  return false
}

/**
 * The given order plus every order where widgets allowing it (maxMoveUp) go
 * up to that many positions earlier, each with the total positions moved
 */
function ordersWithMovesUp(widgets: WidgetSizing[]): { order: WidgetSizing[]; movedUp: number }[] {
  let orders = [{ order: widgets, movedUp: 0 }]
  for (const widget of widgets) {
    const maxMoveUp = widget.maxMoveUp ?? 0
    if (maxMoveUp <= 0) continue
    orders = orders.flatMap(({ order, movedUp }) => {
      const index = order.indexOf(widget)
      const variants = [{ order, movedUp }]
      for (let move = 1; move <= Math.min(maxMoveUp, index); move++) {
        const moved = order.filter((other) => other !== widget)
        moved.splice(index - move, 0, widget)
        variants.push({ order: moved, movedUp: movedUp + move })
      }
      return variants
    })
  }
  return orders
}

/**
 * Tries every combination of the allowed sizes, in the given order and in the
 * orders moving widgets up as far as they allow, each with and without
 * backfilling. Keeps the one with the fewest holes (inner ones first), then
 * the fewest widgets moved back, then the fewest positions moved up, then the
 * fewest rows, then the fewest resized: widgets move only when resizing can't
 * close a gap.
 */
export function packWidgets(widgets: WidgetSizing[], cols: number): GridLayout {
  let best: GridLayout | null = null
  let bestScore: Score = []

  for (const { order, movedUp } of ordersWithMovesUp(widgets)) {
    const options = order.map((widget) => sizeOptions(widget, cols))
    const sizes: Size[] = []
    const tryAll = (index: number) => {
      if (index === order.length) {
        const adjusted = sizes.filter((size) => size.adjusted).length
        for (const dense of [false, true]) {
          const layout = { ...place(order, sizes, cols, dense), movedUp }
          const score = [
            layout.innerHoles,
            layout.holes,
            layout.backfilled,
            layout.movedUp,
            layout.rows,
            adjusted,
          ]
          if (!best || isBetter(score, bestScore)) {
            best = layout
            bestScore = score
          }
        }
        return
      }
      for (const option of options[index]) {
        sizes[index] = option
        tryAll(index + 1)
      }
    }
    tryAll(0)
  }

  return (
    best ?? { cols, rows: 0, holes: 0, innerHoles: 0, backfilled: 0, movedUp: 0, placements: [] }
  )
}

/** Whether layout a has fewer holes than b, inner ones first */
function hasFewerHoles(a: GridLayout, b: GridLayout): boolean {
  if (a.innerHoles !== b.innerHoles) return a.innerHoles < b.innerHoles
  return a.holes < b.holes
}

export interface GridMetrics {
  minCellWidth: number
  gap: number
  /**
   * How much wider than at the most cells that fit the cells may become when
   * falling back to fewer cells to avoid holes, e.g. 1.3 for 30%
   */
  maxCellGrowth: number
}

/** The most cells of at least minCellWidth that fit into the given width */
export function maxCells(width: number, minCellWidth: number, gap: number): number {
  return Math.max(1, Math.floor((width + gap) / (minCellWidth + gap)))
}

/**
 * Packs the widgets into the most cells per row that fit and leave no holes;
 * fewer cells just make them wider, up to maxCellGrowth. Past that, holes are
 * preferred over bigger widgets and the layout with the fewest holes wins.
 */
export function layoutWidgets(
  widgets: WidgetSizing[],
  width: number,
  metrics: GridMetrics,
): GridLayout {
  const { minCellWidth, gap, maxCellGrowth } = metrics
  const cellWidth = (cells: number) => (width - (cells - 1) * gap) / cells
  const most = maxCells(width, minCellWidth, gap)
  let best: GridLayout | null = null
  for (let cells = most; cells >= 1; cells--) {
    if (cells < most && cellWidth(cells) > cellWidth(most) * maxCellGrowth) break
    const layout = packWidgets(widgets, cells)
    if (layout.holes === 0) return layout
    if (!best || hasFewerHoles(layout, best)) best = layout
  }
  return best ?? packWidgets(widgets, 1)
}
