package mx.itesm.beneficiojuventud.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.EmailTextField
import mx.itesm.beneficiojuventud.components.MainButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import mx.itesm.beneficiojuventud.components.AltLoginButton
import mx.itesm.beneficiojuventud.components.GradientDivider
import mx.itesm.beneficiojuventud.components.PasswordTextField

@Composable
fun Login(nav: NavHostController, modifier: Modifier = Modifier) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .padding(top = 85.dp)
        ) {
            // Logo arriba a la izquierda
            Box(modifier = modifier.padding(horizontal = 30.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.logo_beneficio_joven),
                    contentDescription = "",
                    modifier = Modifier.size(50.dp)
                )
            }

            // Título
            Text(
                "Inicia Sesión",
                style = TextStyle(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF4B4C7E), Color(0xFF008D96))
                    ),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black
                ),
                modifier = modifier.padding(24.dp, 18.dp, 20.dp, 14.dp)
            )

            // Subtítulo
            Text(
                "Por favor, inicie sesión en su cuenta",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF616161)
                ),
                textAlign = TextAlign.Start,
                modifier = modifier.padding(horizontal = 24.dp)
            )

            // Label correo
            Text(
                "Correo Electrónico",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF7D7A7A)
                ),
                modifier = modifier.padding(24.dp, 24.dp, 24.dp, 8.dp)
            )

            // Campo correo (tu componente)
            EmailTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier
                    .padding(horizontal = 24.dp)
            )

            // Label contraseña
            Text(
                "Contraseña",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF7D7A7A)
                ),
                modifier = modifier.padding(24.dp, 20.dp, 24.dp, 8.dp)
            )


            PasswordTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            // Recuérdame / ¿Olvidaste tu contraseña?
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it }
                    )
                    Text(
                        "Recuérdame",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF7D7A7A)
                        )
                    )
                }
                TextButton(onClick = { /* TODO: recuperar contraseña */ }) {
                    Text(
                        "¿Olvidaste tu contraseña?",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF7D7A7A)
                        )
                    )
                }
            }

            // Botón primario (gradiente) — usa tu MainButton
            MainButton(
                text = "Inicia Sesión",
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // TODO: login con email/password
            }

            GradientDivider(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            // Social buttons
            AltLoginButton(
                text = "Continuar con Google",
                icon = painterResource(id = R.drawable.logo_google),
                contentDescription = "Continuar con Google",
                onClick = { /* TODO: login Google */ },
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            )

            AltLoginButton(
                text = "Continuar con Facebook",
                icon = painterResource(id = R.drawable.logo_facebook),
                contentDescription = "Continuar con Facebook",
                onClick = { /* TODO: login Facebook */ },
                modifier = Modifier
                    .padding(horizontal = 24.dp)
            )

            // ¿No tienes cuenta? Regístrate
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "¿No tienes cuenta?  ",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7D7A7A)
                    )
                )
                TextButton(onClick = { nav.navigate(Screens.Register.route) }) {
                    Text(
                        "Regístrate",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF008D96)
                        )
                    )
                }
            }
        }
    }
}

/* =======================================================================
 *  COMPONENTES PRIVADOS
 * ======================================================================= */

@Composable
private fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                if (isVisible)
                    Icon(Icons.Outlined.VisibilityOff, contentDescription = "Ocultar contraseña")
                else
                    Icon(Icons.Outlined.Visibility, contentDescription = "Mostrar contraseña")
            }
        },
        placeholder = {
            Text(
                text = "************",
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            )
        },
        textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF2F2F2F)),
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = Color(0xFFE0E0E0),
            unfocusedIndicatorColor = Color(0xFFE0E0E0),
            cursorColor = Color(0xFF008D96),
            focusedLeadingIconColor = Color(0xFF7D7A7A),
            unfocusedLeadingIconColor = Color(0xFF7D7A7A),
            focusedTrailingIconColor = Color(0xFF7D7A7A),
            unfocusedTrailingIconColor = Color(0xFF7D7A7A),
            focusedPlaceholderColor = Color(0xFF7D7A7A),
            unfocusedPlaceholderColor = Color(0xFF7D7A7A),
        )
    )
}

@Composable
private fun DividerWithDot(
    brush: Brush,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(brush)
        )
        Box(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(0xFFBDBDBD))
        )
        Divider(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(brush)
        )
    }
}
