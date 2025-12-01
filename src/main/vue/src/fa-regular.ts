import { library } from '@fortawesome/fontawesome-svg-core'
import {
  faCircle,
  faCircleQuestion,
  faEye,
  faSquare,
} from '@fortawesome/free-regular-svg-icons'

export function applyFaRegularIcons() {
  library.add(
    faCircle,
    faCircleQuestion,
    faEye,
    faSquare,
  )
}
