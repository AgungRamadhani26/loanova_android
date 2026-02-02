package com.example.loanova_android.data.mapper

import com.example.loanova_android.data.local.entity.ApplicationHistoryEntity
import com.example.loanova_android.data.local.entity.BranchEntity
import com.example.loanova_android.data.local.entity.LoanApplicationEntity
import com.example.loanova_android.data.local.entity.PlafondEntity
import com.example.loanova_android.data.local.entity.UserProfileEntity
import com.example.loanova_android.data.model.dto.ApplicationHistoryResponse
import com.example.loanova_android.data.model.dto.BranchResponse
import com.example.loanova_android.data.model.dto.LoanApplicationResponse
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
    
    // --- LOAN APPLICATION ---
    fun mapLoanApplicationResponseToEntity(response: LoanApplicationResponse): LoanApplicationEntity {
        return LoanApplicationEntity(
            id = response.id,
            userId = response.userId,
            username = response.username,
            branchId = response.branchId,
            branchCode = response.branchCode,
            plafondId = response.plafondId,
            plafondName = response.plafondName,
            amount = response.amount.toPlainString(),
            tenor = response.tenor,
            interestRateSnapshot = response.interestRateSnapshot?.toPlainString(),
            status = response.status,
            submittedAt = response.submittedAt,
            fullNameSnapshot = response.fullNameSnapshot,
            phoneNumberSnapshot = response.phoneNumberSnapshot,
            userAddressSnapshot = response.userAddressSnapshot,
            nikSnapshot = response.nikSnapshot,
            birthDateSnapshot = response.birthDateSnapshot,
            npwpNumberSnapshot = response.npwpNumberSnapshot,
            occupation = response.occupation,
            companyName = response.companyName,
            rekeningNumber = response.rekeningNumber,
            ktpPhotoSnapshot = response.ktpPhotoSnapshot,
            npwpPhotoSnapshot = response.npwpPhotoSnapshot,
            savingBookCover = response.savingBookCover,
            payslipPhoto = response.payslipPhoto,
            latitude = response.latitude,
            longitude = response.longitude
        )
    }
    
    fun mapLoanApplicationEntityToResponse(entity: LoanApplicationEntity): LoanApplicationResponse {
        return LoanApplicationResponse(
            id = entity.id,
            userId = entity.userId,
            username = entity.username,
            branchId = entity.branchId,
            branchCode = entity.branchCode,
            plafondId = entity.plafondId,
            plafondName = entity.plafondName,
            amount = try { BigDecimal(entity.amount) } catch (e: Exception) { BigDecimal.ZERO },
            tenor = entity.tenor,
            interestRateSnapshot = entity.interestRateSnapshot?.let { 
                try { BigDecimal(it) } catch (e: Exception) { null } 
            },
            status = entity.status,
            submittedAt = entity.submittedAt,
            fullNameSnapshot = entity.fullNameSnapshot,
            phoneNumberSnapshot = entity.phoneNumberSnapshot,
            userAddressSnapshot = entity.userAddressSnapshot,
            nikSnapshot = entity.nikSnapshot,
            birthDateSnapshot = entity.birthDateSnapshot,
            npwpNumberSnapshot = entity.npwpNumberSnapshot,
            occupation = entity.occupation,
            companyName = entity.companyName,
            rekeningNumber = entity.rekeningNumber,
            ktpPhotoSnapshot = entity.ktpPhotoSnapshot,
            npwpPhotoSnapshot = entity.npwpPhotoSnapshot,
            savingBookCover = entity.savingBookCover,
            payslipPhoto = entity.payslipPhoto,
            latitude = entity.latitude,
            longitude = entity.longitude
        )
    }
    
    // --- APPLICATION HISTORY ---
    fun mapApplicationHistoryResponseToEntity(response: ApplicationHistoryResponse): ApplicationHistoryEntity {
        return ApplicationHistoryEntity(
            id = response.id,
            loanApplicationId = response.loanApplicationId,
            actionByUserId = response.actionByUserId,
            actionByUsername = response.actionByUsername,
            actionByRole = response.actionByRole,
            status = response.status,
            comment = response.comment,
            createdAt = response.createdAt
        )
    }
    
    fun mapApplicationHistoryEntityToResponse(entity: ApplicationHistoryEntity): ApplicationHistoryResponse {
        return ApplicationHistoryResponse(
            id = entity.id,
            loanApplicationId = entity.loanApplicationId,
            actionByUserId = entity.actionByUserId,
            actionByUsername = entity.actionByUsername,
            actionByRole = entity.actionByRole,
            status = entity.status,
            comment = entity.comment,
            createdAt = entity.createdAt
        )
    }
    
    // --- BRANCH ---
    fun mapBranchResponseToEntity(response: BranchResponse): BranchEntity {
        return BranchEntity(
            id = response.id,
            branchCode = response.branchCode,
            branchName = response.branchName,
            branchAddress = response.branchAddress ?: ""
        )
    }
    
    fun mapBranchEntityToResponse(entity: BranchEntity): BranchResponse {
        return BranchResponse(
            id = entity.id,
            branchCode = entity.branchCode,
            branchName = entity.branchName,
            branchAddress = entity.branchAddress
        )
    }
    
    // --- USER PLAFOND (Active Plafond) ---
    fun mapUserPlafondResponseToEntity(response: com.example.loanova_android.data.model.dto.UserPlafondResponse): com.example.loanova_android.data.local.entity.UserPlafondEntity {
        return com.example.loanova_android.data.local.entity.UserPlafondEntity(
            id = response.id,
            userId = response.userId,
            username = response.username,
            plafondId = response.plafondId,
            plafondName = response.plafondName,
            maxAmount = response.maxAmount.toPlainString(),
            remainingAmount = response.remainingAmount.toPlainString(),
            isActive = response.isActive,
            assignedAt = response.assignedAt
        )
    }
    
    fun mapUserPlafondEntityToResponse(entity: com.example.loanova_android.data.local.entity.UserPlafondEntity): com.example.loanova_android.data.model.dto.UserPlafondResponse {
        return com.example.loanova_android.data.model.dto.UserPlafondResponse(
            id = entity.id,
            userId = entity.userId,
            username = entity.username,
            plafondId = entity.plafondId,
            plafondName = entity.plafondName,
            maxAmount = try { BigDecimal(entity.maxAmount) } catch (e: Exception) { BigDecimal.ZERO },
            remainingAmount = try { BigDecimal(entity.remainingAmount) } catch (e: Exception) { BigDecimal.ZERO },
            isActive = entity.isActive,
            assignedAt = entity.assignedAt
        )
    }
}
