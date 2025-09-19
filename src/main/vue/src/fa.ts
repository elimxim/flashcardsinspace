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
} from '@fortawesome/free-solid-svg-icons'
import {
  faEye as faEyeRegular,
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
        faEyeRegular,
    )
}
