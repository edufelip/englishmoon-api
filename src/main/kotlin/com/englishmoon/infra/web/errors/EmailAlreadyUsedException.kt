package com.englishmoon.infra.web.errors

class EmailAlreadyUsedException(email: String) : RuntimeException("Email $email already registered")
