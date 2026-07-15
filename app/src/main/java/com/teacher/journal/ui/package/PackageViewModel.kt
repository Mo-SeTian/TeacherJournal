package com.teacher.journal.ui.package

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teacher.journal.data.entity.CoursePackage
import com.teacher.journal.data.entity.Earning
import com.teacher.journal.data.entity.EarningType
import com.teacher.journal.data.repository.CoursePackageRepository
import com.teacher.journal.data.repository.EarningRepository
import com.teacher.journal.data.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PackagePurchaseUiState(
    val studentName: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class PackageViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val coursePackageRepository: CoursePackageRepository,
    private val earningRepository: EarningRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PackagePurchaseUiState())
    val uiState: StateFlow<PackagePurchaseUiState> = _uiState.asStateFlow()

    fun loadStudent(studentId: Long) {
        viewModelScope.launch {
            val student = studentRepository.getStudentByIdOnce(studentId)
            _uiState.update {
                it.copy(
                    studentName = student?.name ?: "",
                    isLoading = false
                )
            }
        }
    }

    fun purchasePackage(
        studentId: Long,
        sessionCount: Int,
        amount: Double,
        notes: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val pkgId = coursePackageRepository.insert(
                CoursePackage(
                    studentId = studentId,
                    sessionsPurchased = sessionCount,
                    amount = amount,
                    notes = notes
                )
            )

            earningRepository.insert(
                Earning(
                    studentId = studentId,
                    type = EarningType.PACKAGE_PURCHASE,
                    amount = amount,
                    packageId = pkgId
                )
            )

            onComplete()
        }
    }
}
