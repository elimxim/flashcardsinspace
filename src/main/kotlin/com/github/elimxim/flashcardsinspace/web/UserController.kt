package com.github.elimxim.flashcardsinspace.web

import com.github.elimxim.flashcardsinspace.web.dto.toDto
import com.github.elimxim.flashcardsinspace.service.UserService
import com.github.elimxim.flashcardsinspace.web.dto.UserResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
) {
    @GetMapping("/me")
    fun getMe(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<UserResponse> {
        val user = userService.findByEmail(userDetails.username)
        return ResponseEntity.ok(UserResponse(user.toDto()))
    }
}
