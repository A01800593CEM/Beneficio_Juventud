package mx.itesm.beneficiojuventud.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.*
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.model.webhook.WebhookRepository
import mx.itesm.beneficiojuventud.model.webhook.PromotionData
import com.google.gson.Gson

/**
 * Pantalla para describir una promoción y solicitar a un servicio de IA su generación automática.
 * Envía la descripción al webhook, recibe un [PromotionData] y navega a la pantalla de edición.
 * @param nav Controlador de navegación para manejar el flujo entre pantallas.
 * @param modifier Modificador externo para ajustar el layout desde el llamador.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerarPromocionIA(
    nav: NavHostController,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(BJTab.Home) }
    var descripcionText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var promotionData by remember { mutableStateOf<PromotionData?>(null) }
    var errorMessage by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val webhookRepository = remember { WebhookRepository() }
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopBarSection(nav = nav)
        },
        bottomBar = {
            BJBottomBar(
                selected = selectedTab,
                onSelect = { tab ->
                    selectedTab = tab
                    when (tab) {
                        BJTab.Home      -> nav.navigate(Screens.Home.route)
                        BJTab.Coupons   -> nav.navigate(Screens.Coupons.route)
                        BJTab.Favorites -> nav.navigate(Screens.Favorites.route)
                        BJTab.Profile   -> nav.navigate(Screens.Profile.route)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            HeaderSection()

            DescriptionInputCard(
                descripcionText = descripcionText,
                onTextChange = { descripcionText = it }
            )

            GenerateButton(
                enabled = descripcionText.isNotBlank() && !isLoading,
                isLoading = isLoading,
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        promotionData = null
                        errorMessage = ""

                        val result = webhookRepository.enviarDescripcion(descripcionText)

                        if (result.isSuccess) {
                            promotionData = result.getOrNull()
                            // Navega a edición con los datos serializados
                            promotionData?.let { data ->
                                val promotionJson = Gson().toJson(data)
                                nav.currentBackStackEntry?.savedStateHandle?.set("promotion_data", promotionJson)
                                nav.navigate(Screens.EditPromotion.route)
                            }
                        } else {
                            errorMessage = result.exceptionOrNull()?.message ?: "Error desconocido"
                        }

                        isLoading = false
                    }
                }
            )

            // Muestra error en caso de fallo del webhook
            if (errorMessage.isNotEmpty()) {
                ErrorCard(errorMessage = errorMessage)
            }

            Spacer(Modifier.weight(1f))

            FooterText()

            Spacer(Modifier.height(16.dp))
        }
    }
}

/**
 * Cabecera con logo, botón de regreso, título y acceso a notificaciones.
 * @param nav Controlador de navegación para manejar el botón de regreso.
 */
@Composable
private fun TopBarSection(nav: NavHostController) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
    ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = R.drawable.logo_beneficio_joven),
                contentDescription = "Logo",
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BackButton(nav = nav)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Generar con IA",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = Color(0xFF616161)
                )
            }
            Icon(
                imageVector = Icons.Outlined.NotificationsNone,
                contentDescription = "Notificaciones",
                tint = Color(0xFF008D96),
                modifier = Modifier.size(26.dp)
            )
        }

        GradientDivider(
            thickness = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
    }
}

/**
 * Sección introductoria con ícono y texto guía para que el usuario describa su promoción.
 */
@Composable
private fun HeaderSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(Color(0xFF7B68EE)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = "IA",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Describe tu promoción para poder ayudarte",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2B2B2B),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Tarjeta de entrada de texto para capturar la descripción de la promoción.
 * @param descripcionText Texto actual escrito por el usuario.
 * @param onTextChange Callback invocado al actualizar el contenido.
 */
@Composable
private fun DescriptionInputCard(
    descripcionText: String,
    onTextChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            BasicTextField(
                value = descripcionText,
                onValueChange = onTextChange,
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = Color(0xFF2B2B2B),
                    lineHeight = 24.sp
                ),
                modifier = Modifier.fillMaxSize()
            ) { innerTextField ->
                if (descripcionText.isEmpty()) {
                    Text(
                        text = "Ejemplo: Quiero crear una promoción 2x1 en hamburguesas para estudiantes los martes, que sea válida todo el mes de noviembre...",
                        fontSize = 16.sp,
                        color = Color(0xFFAEAEAE),
                        lineHeight = 24.sp
                    )
                }
                innerTextField()
            }
        }
    }
}

/**
 * Botón de acción que envía la descripción al webhook y muestra progreso de carga.
 * @param enabled Indica si el botón está habilitado en función del estado del formulario.
 * @param isLoading Muestra un indicador de progreso cuando la solicitud está en curso.
 * @param onClick Acción a ejecutar al presionar el botón.
 */
@Composable
private fun GenerateButton(
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF7B68EE)
        ),
        enabled = enabled
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (isLoading) "Generando..." else "Generar Promoción",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * Tarjeta de error para mostrar mensajes de fallo al invocar el webhook.
 * @param errorMessage Mensaje descriptivo del error ocurrido.
 */
@Composable
private fun ErrorCard(errorMessage: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "❌ Error",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC62828)
            )
            Text(
                text = errorMessage,
                fontSize = 14.sp,
                color = Color(0xFF2B2B2B),
                lineHeight = 20.sp
            )
        }
    }
}

/**
 * Pie de página con la etiqueta de versión de la aplicación.
 */
@Composable
private fun FooterText() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Versión 1.0.01",
            color = Color(0xFFAEAEAE),
            fontSize = 10.sp
        )
    }
}

/**
 * Preview de la pantalla de generación de promoción para validación visual en el IDE.
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun GenerarPromocionIAPreview() {
    BeneficioJuventudTheme {
        val nav = rememberNavController()
        GenerarPromocionIA(nav = nav)
    }
}
