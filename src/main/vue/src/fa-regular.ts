import { library } from '@fortawesome/fontawesome-svg-core'
import {
  faCircle,
  faCircleQuestion,
  faClone,
  faEye,
  faSquare,
} from '@fortawesome/free-regular-svg-icons'

export function applyFaRegularIcons() {
  library.add(
    faCircle,
    faCircleQuestion,
    faClone,
    faEye,
    faSquare,
  )
}
