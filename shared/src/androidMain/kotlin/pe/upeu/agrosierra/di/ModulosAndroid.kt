package pe.upeu.agrosierra.di

import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import pe.upeu.agrosierra.data.local.AlmacenSeguro
import pe.upeu.agrosierra.data.local.AlmacenSeguroAndroid
import pe.upeu.agrosierra.data.local.crearBaseLocalAndroid
import pe.upeu.agrosierra.data.local.ObservadorConexion
import pe.upeu.agrosierra.data.local.ObservadorConexionAndroid
import pe.upeu.agrosierra.data.remote.AgroApi
import pe.upeu.agrosierra.data.remote.TokenProvider
import pe.upeu.agrosierra.data.remote.crearClienteHttp
import pe.upeu.agrosierra.data.repository.LaborRepositoryImpl
import pe.upeu.agrosierra.data.repository.LoteRepositoryImpl
import pe.upeu.agrosierra.data.repository.SesionRepositoryImpl
import pe.upeu.agrosierra.domain.repository.LaborRepository
import pe.upeu.agrosierra.domain.repository.LoteRepository
import pe.upeu.agrosierra.domain.repository.SesionRepository
import pe.upeu.agrosierra.presentation.AppViewModel

val platformModule = module {
    single<AlmacenSeguro> { AlmacenSeguroAndroid(androidContext()) }
    single<ObservadorConexion> { ObservadorConexionAndroid(androidContext()) }
    single { crearBaseLocalAndroid(androidContext()) }
    single { TokenProvider() }
    single { crearClienteHttp(OkHttp.create()) { get<TokenProvider>().token } }
}

val redModule = module { single { AgroApi(get()) } }
val dataModule = module {
    single { SesionRepositoryImpl(get(), get(), get()) }
    single<SesionRepository> { get<SesionRepositoryImpl>() }
    single { LoteRepositoryImpl(get(), get()) }
    single<LoteRepository> { get<LoteRepositoryImpl>() }
    single { LaborRepositoryImpl(get(), get(), get()) { get<TokenProvider>().token?.let { get<SesionRepositoryImpl>().sesion.value?.usuario }.orEmpty() } }
    single<LaborRepository> { get<LaborRepositoryImpl>() }
}
val domainModule = module { }
val presentationModule = module { single { AppViewModel(get(), get(), get(), get()) } }
