package com.anahoret.imagilabsapi.security

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class AppPasswordEncoder : PasswordEncoder, BCryptPasswordEncoder()
