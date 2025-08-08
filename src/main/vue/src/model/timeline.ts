export interface Timeline {
  id: number
  startedAt: string
  status: string
}

export interface Chronoday {
  id: number
  chronodate: string
  seqNumber: number
  status: string
  stages: string[]
}

