package ${PACKAGE}

import javax.inject.Inject
${IMPORTS}

class ${USECASE_NAME} @Inject constructor(
    ${CONSTRUCTOR_PARAMS}
) {
    suspend operator fun invoke() { }
}
