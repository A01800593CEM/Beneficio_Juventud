package mx.itesm.beneficiojuventud.view

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.GradientDivider

private val CardWhite = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF616161)
private val TextSecondary = Color(0xFFAEAEAE)
private val AccentColor = Color(0xFF008D96)

/**
 * Pantalla de Créditos que muestra información de los desarrolladores de la aplicación.
 * Incluye nombres, enlaces a LinkedIn y correos electrónicos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Credits(
    nav: NavHostController
) {
    val context = LocalContext.current
    val appVersion = "1.0.01"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Créditos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            // Logo de la aplicación
            Image(
                painter = painterResource(id = R.drawable.logo_beneficio_joven),
                contentDescription = "Logo Beneficio Joven",
                modifier = Modifier.size(80.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Beneficio Joven",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Versión $appVersion",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            GradientDivider(
                thickness = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Desarrollado por",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))

            // Lista de desarrolladores - Puedes agregar los que necesites
            DeveloperCard(
                name = "Alan Tomas Rodriguez Villanueva",
                linkedInUrl = "https://www.linkedin.com/in/alanrodriguezv/",
                email = "alantomasrv@gmail.com",
                onLinkedInClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/alanrodriguezv/"))
                    context.startActivity(intent)
                },
                onEmailClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:alantomasrv@gmail.com")
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // No hay aplicación de correo instalada
                    }
                }
            )

            Spacer(Modifier.height(12.dp))

            DeveloperCard(
                name = "Emilio De León Vives",
                linkedInUrl = "https://www.linkedin.com/in/edeleonv",
                email = "e.deleonv@outlook.com",
                onLinkedInClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/edeleonv"))
                    context.startActivity(intent)
                },
                onEmailClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:e.deleonv@outlook.com")
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // No hay aplicación de correo instalada
                    }
                }
            )

            Spacer(Modifier.height(12.dp))

            DeveloperCard(
                name = "Diego Carreón Aguirre",
                linkedInUrl = "https://www.linkedin.com/in/dcarreonag",
                email = "carreon21diego@gmail.com",
                onLinkedInClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/dcarreonag"))
                    context.startActivity(intent)
                },
                onEmailClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:carreon21diego@gmail.com")
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // No hay aplicación de correo instalada
                    }
                }
            )

            Spacer(Modifier.height(32.dp))

            DeveloperCard(
                name = "Iván Carrillo Zardaín",
                linkedInUrl = "http://www.linkedin.com/in/ivancz",
                email = "ivancz.com@gmail.com",
                onLinkedInClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.linkedin.com/in/ivancz"))
                    context.startActivity(intent)
                },
                onEmailClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:ivancz.com@gmail.com")
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // No hay aplicación de correo instalada
                    }
                }
            )

            Spacer(Modifier.height(32.dp))

            DeveloperCard(
                name = "Santiago Serrano",
                linkedInUrl = "https://www.linkedin.com/in/sserranom/",
                email = "ssmm.serrrano@gmail.com",
                onLinkedInClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/sserranom/"))
                    context.startActivity(intent)
                },
                onEmailClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:ssmm.serrrano@gmail.com")
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // No hay aplicación de correo instalada
                    }
                }
            )

            Spacer(Modifier.height(32.dp))

            DeveloperCard(
                name = "Erik Owen Castro Flores",
                linkedInUrl = "https://www.linkedin.com/in/erik-owen-castro-flores-245150390/",
                email = "eric.castro.data@gmail.com",
                onLinkedInClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/erik-owen-castro-flores-245150390/"))
                    context.startActivity(intent)
                },
                onEmailClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:eric.castro.data@gmail.com")
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // No hay aplicación de correo instalada
                    }
                }
            )

            Spacer(Modifier.height(32.dp))

            Text(
                text = "© 2025 Beneficio Joven\nTodos los derechos reservados",
                color = TextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

/**
 * Card que muestra la información de un desarrollador individual.
 */
@Composable
private fun DeveloperCard(
    name: String,
    linkedInUrl: String,
    email: String,
    onLinkedInClick: () -> Unit,
    onEmailClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE5E5E5), RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp)),
        color = CardWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = name,
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            // LinkedIn
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onLinkedInClick() }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Work,
                    contentDescription = "LinkedIn",
                    tint = AccentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Ver perfil en LinkedIn",
                    color = AccentColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(4.dp))

            // Email
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onEmailClick() }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Email,
                    contentDescription = "Email",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = email,
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        }
    }
}
