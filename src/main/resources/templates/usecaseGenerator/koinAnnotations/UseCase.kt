package ${PACKAGE}

import org.koin.core.annotation.Single
${IMPORTS}

@Single
class ${USECASE_NAME}(
    ${CONSTRUCTOR_PARAMS}
) {
    suspend operator fun invoke() { }
}
