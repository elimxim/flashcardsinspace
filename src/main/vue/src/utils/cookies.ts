import Cookies from 'js-cookie'
import { Log, LogTag } from '@/utils/logger.ts'

const COOKIE_SELECTED_SET_ID = 'selectedSetId'
const COOKIE_USER_SIGNED_UP = 'userSignedUp'
const COOKIE_SIDEBAR_EXPANDED = 'sidebarExpanded'
const COOKIE_LOGGING_ENABLED = 'loggingEnabled'

function saveCookie(name: string, value: string, expires: number) {
  Cookies.set(name, value, {
    expires: expires,
    sameSite: 'Lax',
    secure: true,
    path: '/', // valid for the entire site
  })
  Log.log(LogTag.COOKIES, `${value} => ${name}`)
}

function loadCookie(name: string): string | undefined {
  const value = Cookies.get(name)
  Log.log(LogTag.COOKIES,`${name} => ${value}`)
  return value
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
function removeCookie(name: string) {
  Log.log(LogTag.COOKIES,`${name} => x_x`)
  Cookies.remove(name)
}

function loadBooleanCookie(name: string): boolean {
  const value = loadCookie(name)
  if (value) {
    return value === 'true'
  }
  return false
}

function loadNumberCookie(name: string): number | undefined {
  const value = loadCookie(name)
  if (value) {
    return parseInt(value)
  }
}

function saveUserSignedUpToCookies(value: boolean) {
  saveCookie(COOKIE_USER_SIGNED_UP, value.toString(), 30)
}

function loadUserSignedUpFromCookies(): boolean {
  return loadBooleanCookie(COOKIE_USER_SIGNED_UP)
}

function saveSelectedSetIdToCookies(value: number) {
  saveCookie(COOKIE_SELECTED_SET_ID, value.toString(), 30)
}

function loadSelectedSetIdFromCookies(): number | undefined {
  return loadNumberCookie(COOKIE_SELECTED_SET_ID)
}

function setSidebarExpandedToCookies(value: boolean) {
  saveCookie(COOKIE_SIDEBAR_EXPANDED, value.toString(), 30)
}

function loadSidebarExpandedFromCookies(): boolean {
  return loadBooleanCookie(COOKIE_SIDEBAR_EXPANDED)
}

function loadLoggingEnabledFromCookies(): boolean {
  const value = Cookies.get(COOKIE_LOGGING_ENABLED)
  if (value) {
    return value === 'true'
  }
  return false
}

function saveLoggingEnabledToCookies(value: boolean) {
  Cookies.set(COOKIE_LOGGING_ENABLED, value.toString(), {
    expires: 30,
    sameSite: 'Lax',
    secure: true,
    path: '/', // valid for the entire site
  })
}

export {
  saveUserSignedUpToCookies,
  loadUserSignedUpFromCookies,
  saveSelectedSetIdToCookies,
  loadSelectedSetIdFromCookies,
  setSidebarExpandedToCookies,
  loadSidebarExpandedFromCookies,
  loadLoggingEnabledFromCookies,
  saveLoggingEnabledToCookies,
}
