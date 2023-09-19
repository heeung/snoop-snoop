package com.appa.snoop.data.repository

import com.appa.snoop.data.mapper.toDomain
import com.appa.snoop.data.model.registration.request.RegisterRequest
import com.appa.snoop.data.service.RegisterService
import com.appa.snoop.data.service.handleApi
import com.appa.snoop.domain.model.NetworkResult
import com.appa.snoop.domain.model.member.Register
import com.appa.snoop.domain.model.member.RegisterDone
import com.appa.snoop.domain.repository.RegisterRepository
import java.lang.reflect.Member
import javax.inject.Inject

class RegisterRepositoryImpl @Inject constructor(
//    private val  preferenceDatasource: PreferenceDataSource,
    private val registerService: RegisterService
) : RegisterRepository {

    override suspend fun registerMember(register: Register): NetworkResult<RegisterDone> {
        return handleApi { registerService.registerMember(register).toDomain() }
    }

}