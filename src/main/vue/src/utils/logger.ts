import { loggingEnabledCookie } from '@/utils/cookies-ref.ts'

declare global {
  interface Window {
    loggingEnabled: boolean
  }
}

Object.defineProperty(window, 'loggingEnabled', {
  get() {
    return loggingEnabledCookie.value
  },
  set(value: boolean) {
    loggingEnabledCookie.value = value
  },
  configurable: true,
  enumerable: true,
})

export enum LogTag {
  DEFAULT = 'DEFAULT',
  API = 'API', GET = 'GET', POST = 'POST', PUT = 'PUT', DELETE = 'DELETE',
  STORE = 'STORE',
  COOKIES = 'COOKIES',
  LOGIC = 'LOGIC',
  SYSTEM = 'SYSTEM',
  DEBUG = 'DEBUG',
}

export class Log {
  private static tagBgColors: Record<LogTag, string> = {
    [LogTag.DEFAULT]: '#888',
    [LogTag.API]: '#9C27B0',
    [LogTag.GET]: '#9C27B0',
    [LogTag.POST]: '#9C27B0',
    [LogTag.PUT]: '#9C27B0',
    [LogTag.DELETE]: '#9C27B0',
    [LogTag.STORE]: '#d5b227',
    [LogTag.COOKIES]: '#795548',
    [LogTag.LOGIC]: '#009669',
    [LogTag.SYSTEM]: '#3F51B5',
    [LogTag.DEBUG]: '#883f3f',
  }

  static info(message?: unknown, ...optionalParams: unknown[]) {
    this.log(LogTag.DEFAULT, message, optionalParams)
  }

  static log(tag: LogTag, message?: unknown, ...optionalParams: unknown[]) {
    if (!window.loggingEnabled) return
    console.log(`%c${tag}`, this.tagStyle(tag), message, optionalParams)
  }

  static error(tag: LogTag, message?: string, ...optionalParams: unknown[]) {
    if (!window.loggingEnabled) return
    console.error(`%c${tag}`, this.tagStyle(tag), message, optionalParams)
  }

  private static tagStyle(tag: LogTag): string {
    const common = 'color: white; padding: 2px 4px; border-radius: 2px;'
    return `${common} background-color: ${this.tagBgColors[tag]};`
  }
}
