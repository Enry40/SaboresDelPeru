class AppComidaPeruanaUITest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun muestraTextoAppOk() {
        // Repo falso para el test (no llama a red ni a Room)
        val fakeRepo = object : ComidaDataSource {
            private val _platos =
                MutableStateFlow<List<com.example.prueba_app.model.Plato>>(emptyList())

            override val platos: StateFlow<List<com.example.prueba_app.model.Plato>>
                get() = _platos

            override suspend fun inicializarDatos() {
                // no-op para el test de UI
            }
        }

        val vm = ComidaViewModel(repositorio = fakeRepo)

        rule.setContent {
            AppComidaPeruana(viewModel = vm, onExitApp = {})
        }

        rule.onNodeWithText("App Comida Peruana OK ✅")
            .assertIsDisplayed()
    }
}
