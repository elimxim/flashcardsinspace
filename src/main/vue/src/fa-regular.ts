import { library } from '@fortawesome/fontawesome-svg-core'
import {
  faCircle,
  faCircleQuestion,
  faCircleXmark,
  faClone,
  faEye,
  faSquare,
} from '@fortawesome/free-regular-svg-icons'

export function applyFaRegularIcons() {
  library.add(faCircle, faCircleQuestion, faCircleXmark, faClone, faEye, faSquare)
}
