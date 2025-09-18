import Cookies from 'js-cookie'

export function saveSelectedSetId(value: number) {
  Cookies.set('selectedSetId', value.toString(), {
    expires: 30, // 30 days
    sameSite: 'Lax',
    secure: true,
    path: '/', // valid for the entire site
  })
  console.log('Cookie.selectedSetId <=', value)
}

export function loadSelectedSetId(): number | undefined {
  const value = Cookies.get('selectedSetId')
  console.log('Cookie.selectedSetId =>', value)
  if (value) {
    return parseInt(value)
  }
}
