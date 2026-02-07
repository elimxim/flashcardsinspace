import { defineStore } from 'pinia'

type StopCallback = () => void

export const usePlaybackControl = defineStore('playback-control', () => {
  // state
  const registry = new Map<string, StopCallback>()
  let activePlayerId: string | null = null

  // actions
  function register(stopCallback: StopCallback): string {
    const id = Math.random().toString(36).substring(2, 15)
    registry.set(id, stopCallback)
    return id
  }

  function unregister(id: string) {
    if (activePlayerId === id) {
      activePlayerId = null
    }
    registry.delete(id)
  }

  function requestPlay(id: string) {
    if (activePlayerId && activePlayerId !== id) {
      const stopOther = registry.get(activePlayerId)
      if (stopOther) stopOther()
    }

    activePlayerId = id
  }

  function notifyStopped(id: string) {
    if (activePlayerId === id) {
      activePlayerId = null
    }
  }

  function $reset() {
    registry.clear()
    activePlayerId = null
  }

  return {
    register,
    unregister,
    requestPlay,
    notifyStopped,
    $reset,
  }
})
