package ${PACKAGE}

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
${IMPORTS}

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {
${BINDINGS}
}
