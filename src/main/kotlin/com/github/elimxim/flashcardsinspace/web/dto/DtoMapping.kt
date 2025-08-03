package com.github.elimxim.flashcardsinspace.web.dto

import com.github.elimxim.flashcardsinspace.entity.Language
import com.github.elimxim.flashcardsinspace.entity.User

fun User.toDto() = UserDto(
    id = id,
    name = username,
    email = email,
    roles = roles.split(",").toList(),
    registeredAt = registeredAt.toString(),
)

fun Language.toDto() = LanguageDto(
    id = id,
    name = name,
    code = code,
)