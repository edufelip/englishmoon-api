package com.englishmoon.infra.web.errors

import java.util.UUID

class UserNotFoundException(id: UUID) : RuntimeException("User $id not found")
