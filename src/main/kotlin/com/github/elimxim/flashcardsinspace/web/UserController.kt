package com.github.elimxim.flashcardsinspace.web

import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.security.normalize
import com.github.elimxim.flashcardsinspace.service.UserService
import com.github.elimxim.flashcardsinspace.util.withLoggingContext
import com.github.elimxim.flashcardsinspace.web.dto.UserDto
import com.github.elimxim.flashcardsinspace.web.dto.UserUpdateRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
) {
    @GetMapping("/me")
    fun getMe(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<UserDto> = withLoggingContext {
        val dto = userService.findByEmail(userDetails.username)
        return ResponseEntity.ok(dto)
    }

    @PutMapping()
    fun updateUser(
        @AuthenticationPrincipal user: User,
        @RequestBody request: UserUpdateRequest,
    ): ResponseEntity<UserDto> = withLoggingContext(user) {
        val dto = userService.update(user, request.normalize())
        return ResponseEntity.ok(dto)
    }
}
