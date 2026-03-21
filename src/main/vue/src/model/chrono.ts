export interface Chronoday {
  id: number
  chronodate: string // "yyyy-MM-dd"
  seqNumber?: number
  status: string
  stages: string[]
}

export interface DayStreak {
  streak: number
  lastDate: string // "yyyy-MM-dd"
}
