import Cookies from 'js-cookie'

const COOKIE_SELECTED_SET_ID = 'selectedSetId'
const COOKIE_USER_SIGNED_UP = 'userSignedUp'

export function saveUserSignedUp(value: boolean) {
  Cookies.set(COOKIE_USER_SIGNED_UP, value.toString(), {
    expires: 30, // 30 days
    sameSite: 'Lax',
    secure: true,
    path: '/', // valid for the entire site
  })
  console.log( `${value} => Cookie.${COOKIE_USER_SIGNED_UP}`)
}

export function loadUserSignedUp(): boolean {
  const value = Cookies.get(COOKIE_USER_SIGNED_UP)
  console.log(`Cookie.${COOKIE_USER_SIGNED_UP} => ${value}`)
  if (value) {
    return value === 'true'
  }
  return false
}

export function saveSelectedSetId(value: number) {
  Cookies.set(COOKIE_SELECTED_SET_ID, value.toString(), {
    expires: 30, // 30 days
    sameSite: 'Lax',
    secure: true,
    path: '/', // valid for the entire site
  })
  console.log( `${value} => Cookie.${COOKIE_SELECTED_SET_ID}`)
}

export function loadSelectedSetId(): number | undefined {
  const value = Cookies.get(COOKIE_SELECTED_SET_ID)
  console.log(`Cookie.${COOKIE_SELECTED_SET_ID} => ${value}`)
  if (value) {
    return parseInt(value)
  }
}
