package com.example.loanova_android.data.mapper

import com.example.loanova_android.data.local.entity.PlafondEntity
import com.example.loanova_android.data.local.entity.UserProfileEntity
import com.example.loanova_android.data.model.dto.PlafondResponse
import com.example.loanova_android.data.model.dto.UserProfileResponse
import com.example.loanova_android.domain.model.Plafond
import java.math.BigDecimal

object DataMappers {

    // --- PLAFOND ---
    fun mapPlafondResponseToEntity(response: PlafondResponse): PlafondEntity {
        return PlafondEntity(
            id = response.id ?: 0L,
            name = response.name ?: "",
            description = response.description ?: "",
            maxAmount = response.maxAmount?.toPlainString() ?: "0",
            interestRate = response.interestRate?.toDouble() ?: 0.0,
            tenorMin = response.tenorMin ?: 0,
            tenorMax = response.tenorMax ?: 0
        )
    }

    fun mapPlafondEntityToDomain(entity: PlafondEntity): Plafond {
        return Plafond(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            maxAmount = try { BigDecimal(entity.maxAmount) } catch (e: Exception) { BigDecimal.ZERO },
            interestRate = BigDecimal.valueOf(entity.interestRate),
            tenorMin = entity.tenorMin,
            tenorMax = entity.tenorMax
        )
    }

    // --- PROFILE ---
    fun mapProfileResponseToEntity(response: UserProfileResponse): UserProfileEntity {
        return UserProfileEntity(
            userId = response.userId,
            id = response.id,
            username = response.username,
            fullName = response.fullName,
            phoneNumber = response.phoneNumber,
            userAddress = response.userAddress,
            nik = response.nik,
            birthDate = response.birthDate,
            npwpNumber = response.npwpNumber,
            ktpPhoto = response.ktpPhoto,
            profilePhoto = response.profilePhoto,
            npwpPhoto = response.npwpPhoto,
            createdAt = response.createdAt,
            updatedAt = response.updatedAt
        )
    }

    fun mapProfileEntityToResponse(entity: UserProfileEntity): UserProfileResponse {
        return UserProfileResponse(
            id = entity.id,
            userId = entity.userId,
            username = entity.username,
            fullName = entity.fullName,
            phoneNumber = entity.phoneNumber,
            userAddress = entity.userAddress,
            nik = entity.nik,
            birthDate = entity.birthDate,
            npwpNumber = entity.npwpNumber,
            ktpPhoto = entity.ktpPhoto,
            profilePhoto = entity.profilePhoto,
            npwpPhoto = entity.npwpPhoto,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}
