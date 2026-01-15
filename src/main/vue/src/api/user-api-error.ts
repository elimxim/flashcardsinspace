export interface UserApiError {
  title: string,
  message: string,
}

export const userApiErrors = {
  DATA_LOADING: {
    title: 'Could not load data from server',
    message: 'Please reload the page and try again.'
  } as UserApiError,
  AUTH_BAD_DATA: {
    title: 'Anomaly detected',
    message: 'Please check your credentials and try again.'
  } as UserApiError,
  AUTH_FAILED: {
    title: 'Authentication is not successful',
    message: 'There was a glitch in our identity scanners. Please try to establish a connection again.'
  } as UserApiError,
  VERIFICATION__CONTEXT_FAILED: {
    title: 'Could not load the verification context',
    message: 'Please reload the page and try again.'
  } as UserApiError,
  VERIFICATION__REQUEST_FAILED: {
    title: 'Could not request verification',
    message: 'Please try again later.'
  } as UserApiError,
  VERIFICATION__CODE_FAILED: {
    title: 'Could not test the verification code',
    message: 'Please try again later.'
  } as UserApiError,
  VERIFICATION__UNKNOWN_TYPE: {
    title: 'Couldn\'t complete the action',
    message: 'Please try again.'
  },
  PASSWORD_RESET__FAILED: {
    title: 'Couldn\'t reset the password',
    message: 'Please try again.'
  },
  USER__UNAUTHORIZED: {
    title: 'We couldn\'t recognize you',
    message: 'Please try again later.'
  } as UserApiError,
  USER__NOT_FOUND: {
    title: 'We have an Unidentified Flying Object...',
    message: 'Just kidding, we just don\'t recognize you. Please check your credentials or sign up to join the fleet.'
  } as UserApiError,
  USER__FORBIDDEN: {
    title: 'Mission Control has not authorized this action',
    message: 'You do not have permission to proceed.'
  } as UserApiError,
  USER__UPDATING_FAILED: {
    title: 'Could not update the user',
    message: 'Please try to repeat the action.'
  } as UserApiError,
  USER__ALREADY_EXISTS: {
    title: 'This email is already taken',
    message: 'Please use a different email.'
  } as UserApiError,
  FLASHCARD_SET__SUSPENDING_FAILED: {
    title: 'Could not suspend the flashcard set',
    message: 'Please refresh the page and repeat the action.'
  } as UserApiError,
  FLASHCARD_SET__UPDATING_FAILED: {
    title: 'Could not update the flashcard set',
    message: 'Please refresh the page and repeat the action.'
  } as UserApiError,
  FLASHCARD_SET__REMOVING_FAILED: {
    title: 'Could not remove the flashcard set',
    message: 'Please refresh the page and repeat the action.'
  } as UserApiError,
  FLASHCARD_SET__CREATION_FAILED: {
    title: 'Could not create the flashcard set',
    message: 'Please refresh the page and repeat the action.'
  } as UserApiError,
  FLASHCARD__CREATION_FAILED: {
    title: 'Could not add the flashcard',
    message: 'Please refresh the page and repeat the action.'
  } as UserApiError,
  FLASHCARD__UPDATING_FAILED: {
    title: 'Could not update the flashcard',
    message: 'Please refresh the page and repeat the action.'
  } as UserApiError,
  FLASHCARD__PROGRESSION_FAILED: {
    title: 'Could not track review progress',
    message: 'Your progress might not be saved.'
  } as UserApiError,
  FLASHCARD__REMOVING_FAILED: {
    title: 'Could not remove the flashcard',
    message: 'Please refresh the page and repeat the action.'
  } as UserApiError,
  SCHEDULE__PREV_FAILED: {
    title: 'Could not go to the previous day',
    message: 'Please refresh the page and repeat the action.'
  } as UserApiError,
  SCHEDULE__NEXT_FAILED: {
    title: 'Could not go to the next day',
    message: 'Please refresh the page and repeat the action.'
  } as UserApiError,
  SCHEDULE__UPDATING_FAILED: {
    title: 'Could not update the schedule',
    message: 'Please refresh the page and repeat the action.'
  } as UserApiError,
  AUDIO__UPLOADING_FAILED: {
    title: 'Could not upload the audio',
    message: 'Please try to repeat the action.'
  } as UserApiError,
  AUDIO__FETCHING_FAILED: {
    title: 'Could not fetch the audio',
    message: 'Please refresh the page and repeat the action.'
  } as UserApiError,
  AUDIO__REMOVAL_FAILED: {
    title: 'Could not remove the audio',
    message: 'Please refresh the page and repeat the action.'
  } as UserApiError,
  REVIEW_SESSION__FETCHING_FAILED: {
    title: 'Could not fetch an active review session',
    message: 'Please exit the current review session and try again.'
  } as UserApiError,
  REVIEW_SESSION__CREATION_FAILED: {
    title: 'Could not create a review session',
    message: 'Please try to repeat the action.'
  } as UserApiError,
  REVIEW_SESSION__UPDATING_FAILED: {
    title: 'Could not update a review session',
    message: 'Your progress might not be saved.'
  } as UserApiError,
  QUIZ_SESSION__NEXT_ROUND_FAILED: {
    title: 'Could not start a new quiz round',
    message: 'Please try to repeat the action.'
  } as UserApiError,
}
