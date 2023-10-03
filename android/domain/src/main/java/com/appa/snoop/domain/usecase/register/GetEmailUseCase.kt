package com.appa.snoop.domain.usecase.register

import com.appa.snoop.domain.repository.RegisterRepository
import javax.inject.Inject

class GetEmailUseCase @Inject constructor(
    private val registerRepository: RegisterRepository
) {
    suspend operator fun invoke(): String {
        return registerRepository.getEmail()
    }
}