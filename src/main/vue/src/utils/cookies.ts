import Cookies from 'js-cookie'

const COOKIE_SELECTED_SET_ID = 'selectedSetId'
const COOKIE_USER_SIGNED_UP = 'userSignedUp'
const COOKIE_SIDEBAR_EXPANDED = 'sidebarExpanded'

function saveCookie(name: string, value: string, expires: number) {
  Cookies.set(name, value, {
    expires: expires,
    sameSite: 'Lax',
    secure: true,
    path: '/', // valid for the entire site
  })
  console.log( `${value} => Cookie.${name}`)
}

function loadCookie(name: string): string | undefined {
  const value = Cookies.get(name)
  console.log(`Cookie.${name} => ${value}`)
  return value
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

export {
  saveUserSignedUpToCookies,
  loadUserSignedUpFromCookies,
  saveSelectedSetIdToCookies,
  loadSelectedSetIdFromCookies,
  setSidebarExpandedToCookies,
  loadSidebarExpandedFromCookies,
}
