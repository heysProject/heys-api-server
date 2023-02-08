package com.api.heys.user

import com.api.heys.constants.DefaultString
import com.api.heys.constants.enums.Gender
import com.api.heys.domain.user.controller.UserController
import com.api.heys.domain.user.dto.SignUpData
import com.api.heys.domain.user.service.UserService
import com.api.heys.entity.Users
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(UserController::class)
internal class UsersControllerTest(@Autowired val mockMvc: MockMvc) {
    @MockkBean
    lateinit var userService: UserService

    private val mapper = jacksonObjectMapper()
    private val users = Users(phone = "01012341234", password = "12341234")
    private val signUpData = SignUpData(
            phone = "01012341234",
            username = "TESTER",
            password = "12341234",
            age = 29,
            gender = Gender.Male,
            interests = mutableSetOf("교육", "자기계발"),
    )
    private val adminSignUpData = SignUpData(
        phone = "01012341234",
        username = "TESTER",
        password = "12341234",
        age = 29,
        gender = Gender.Male,
    )

    @Test
    fun signUp_alreadyExistUsername_givenBadRequestStatus() {
        every { userService.signUp(signUpData, DefaultString.commonRole) } returns null

        mockMvc.perform(post("/user/signUp")
                .content(mapper.writeValueAsString(signUpData))
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().is4xxClientError)
    }

    @Test
    @WithMockUser
    fun signUp_givenSuccessStatus() {
        every { userService.signUp(signUpData, DefaultString.commonRole) } returns "token"
        every { userService.signUp(adminSignUpData, DefaultString.adminRole) } returns "token"

        mockMvc.perform(post("/user/signUp/common")
                .content(mapper.writeValueAsString(signUpData))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf().asHeader())
        )
                .andExpect(status().isOk)

        mockMvc.perform(post("/user/signUp/admin")
            .content(mapper.writeValueAsString(adminSignUpData))
            .contentType(MediaType.APPLICATION_JSON)
            .with(csrf().asHeader())
        )
            .andExpect(status().isOk)
    }
}