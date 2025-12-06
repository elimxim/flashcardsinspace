import Cookies from 'js-cookie'

const COOKIE_SELECTED_SET_ID = 'selectedSetId'
const COOKIE_USER_SIGNED_UP = 'userSignedUp'

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

function saveUserSignedUp(value: boolean) {
  saveCookie(COOKIE_USER_SIGNED_UP, value.toString(), 30)
}

function loadUserSignedUp(): boolean {
  return loadBooleanCookie(COOKIE_USER_SIGNED_UP)
}

function saveSelectedSetId(value: number) {
  saveCookie(COOKIE_SELECTED_SET_ID, value.toString(), 30)
}

function loadSelectedSetId(): number | undefined {
  return loadNumberCookie(COOKIE_SELECTED_SET_ID)
}

export {
  saveUserSignedUp,
  loadUserSignedUp,
  saveSelectedSetId,
  loadSelectedSetId,
}
