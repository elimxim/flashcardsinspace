import { defineStore } from 'pinia'
import { Log, LogTag } from '@/utils/logger.ts'

export type AppEvents = {
  'flashcard-file:selected': File
}

type EventCallback<K extends keyof AppEvents> = (payload: AppEvents[K]) => void
type AnyEventCallback = EventCallback<keyof AppEvents>

export const useEventStore = defineStore('event', () => {
  const subscribers = new Map<keyof AppEvents, Set<AnyEventCallback>>()

  function subscribe<K extends keyof AppEvents>(event: K, callback: EventCallback<K>): () => void {
    let callbacks = subscribers.get(event)

    if (!callbacks) {
      callbacks = new Set()
      subscribers.set(event, callbacks)
    }

    Log.log(LogTag.STORE, `register a subscriber for event ${event}, total: ${callbacks.size}`)
    callbacks.add(callback)

    return () => {
      Log.log(LogTag.STORE, `delete a subscriber for event ${event}, remaining: ${callbacks?.size - 1}`)
      callbacks?.delete(callback)
    }
  }

  function emit<K extends keyof AppEvents>(event: K, payload: AppEvents[K]): void {
    Log.log(LogTag.STORE, `fire the event ${event}`)
    subscribers.get(event)?.forEach(cb => cb(payload))
  }

  return {
    subscribe,
    emit,
  }
})
