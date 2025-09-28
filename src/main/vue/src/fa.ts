import { library } from '@fortawesome/fontawesome-svg-core'
import {
  faAngleLeft,
  faAngleRight,
  faBox,
  faCalendarDays,
  faCircleCheck,
  faCircleExclamation,
  faCircleInfo,
  faCircleQuestion,
  faCircleUser,
  faEye,
  faEyeSlash,
  faGear,
  faGlobe,
  faPenToSquare,
  faRectangleList,
  faTriangleExclamation,
  faXmark,
  faArrowsSpin,
  faCircleXmark,
} from '@fortawesome/free-solid-svg-icons'
import {
  faEye as faRegularEye,
  faCircle as faRegularCircle,
  faCircleXmark as faRegularCircleXmark,
} from '@fortawesome/free-regular-svg-icons'

export function applyFaIcons() {
  library.add(
    faGear,
    faBox,
    faRectangleList,
    faCalendarDays,
    faTriangleExclamation,
    faCircleExclamation,
    faCircleInfo,
    faCircleCheck,
    faCircleQuestion,
    faGlobe,
    faXmark,
    faPenToSquare,
    faAngleLeft,
    faAngleRight,
    faCircleUser,
    faEye,
    faEyeSlash,
    faArrowsSpin,
    faCircleXmark,

    faRegularEye,
    faRegularCircle,
    faRegularCircleXmark,
  )
}
