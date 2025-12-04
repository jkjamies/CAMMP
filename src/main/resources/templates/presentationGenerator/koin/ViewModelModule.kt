package ${PACKAGE}

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
${IMPORTS}

val viewModelModule = module {
${BINDINGS}
}
