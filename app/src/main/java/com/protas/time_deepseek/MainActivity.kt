package com.protas.time_deepseek

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.protas.time_deepseek.ui.MainScreen
import com.protas.time_deepseek.ui.MainViewModel

/**
 * Host de Compose: punto de entrada de la UI interactiva (Launcher).
 *
 * Conecta el ciclo de vida de la Activity con el árbol de Compose e
 * inyecta las dependencias al [MainViewModel] desde el [ServiceLocator].
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel: MainViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        MainViewModel(
                            cargarCatalogo = { ServiceLocator.pricingRepository.loadCatalog() },
                            clock = ServiceLocator.clock
                        )
                    }
                }
            )

            val state = viewModel.state.collectAsStateWithLifecycle().value
            MainScreen(state = state)
        }
    }
}
