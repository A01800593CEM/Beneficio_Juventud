package mx.itesm.beneficiojuventud.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.EmailTextField
import mx.itesm.beneficiojuventud.components.MainButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import mx.itesm.beneficiojuventud.components.PasswordTextField

@Composable
fun Register(nav: NavHostController, modifier: Modifier = Modifier) {
    var nombre by remember { mutableStateOf("") }
    var apPaterno by remember { mutableStateOf("") }
    var apMaterno by remember { mutableStateOf("") }
    var fechaNac by remember { mutableStateOf("01/Febrero/2003") } // mock
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var acceptTerms by remember { mutableStateOf(false) }

    val scroll = rememberScrollState()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(top = 85.dp, bottom = 24.dp) // bottom para que no tape el botón
        ) {
            // Logo
            Box(modifier = Modifier.padding(horizontal = 30.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.logo_beneficio_joven),
                    contentDescription = "",
                    modifier = Modifier.size(50.dp)
                )
            }

            // Título
            Text(
                "Regístrate",
                style = TextStyle(
                    brush = Brush.linearGradient(listOf(Color(0xFF4B4C7E), Color(0xFF008D96))),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black
                ),
                modifier = Modifier.padding(24.dp, 18.dp, 20.dp, 14.dp)
            )

            // Fila: Nombre / Apellido Paterno / Apellido Materno
            Label("Nombre")
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                singleLine = true,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .heightIn(min = TextFieldDefaults.MinHeight),
                shape = RoundedCornerShape(18.dp),
                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                placeholder = { Text("Nombre", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF2F2F2F)),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = textFieldColors()
            )

            Label("Apellido Paterno", top = 20.dp)
            OutlinedTextField(
                value = apPaterno,
                onValueChange = { apPaterno = it },
                singleLine = true,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .heightIn(min = TextFieldDefaults.MinHeight),
                shape = RoundedCornerShape(18.dp),
                placeholder = { Text("Apellido Paterno", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF2F2F2F)),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = textFieldColors()
            )

            Label("Apellido Materno", top = 20.dp)
            OutlinedTextField(
                value = apMaterno,
                onValueChange = { apMaterno = it },
                singleLine = true,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .heightIn(min = TextFieldDefaults.MinHeight),
                shape = RoundedCornerShape(18.dp),
                placeholder = { Text("Apellido Materno", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF2F2F2F)),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = textFieldColors()
            )

            // Fecha de Nacimiento
            Label("Fecha de Nacimiento", top = 20.dp)
            OutlinedTextField(
                value = fechaNac,
                onValueChange = { fechaNac = it },
                singleLine = true,
                readOnly = true,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .heightIn(min = TextFieldDefaults.MinHeight),
                shape = RoundedCornerShape(18.dp),
                placeholder = { Text("DD/Mes/AAAA", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                trailingIcon = {
                    IconButton(onClick = { /* TODO: DatePickerDialog */ }) {
                        Icon(Icons.Outlined.CalendarMonth, contentDescription = "Elegir fecha")
                    }
                },
                textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF2F2F2F)),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = textFieldColors()
            )

            // Número Telefónico (debajo del mail)
            Label("Número Telefónico", top = 20.dp)
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                singleLine = true,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .heightIn(min = TextFieldDefaults.MinHeight),
                shape = RoundedCornerShape(18.dp),
                leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null) },
                placeholder = { Text("55 1234 5678", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF2F2F2F)),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                colors = textFieldColors()
            )


            // Correo
            Label("Correo Electrónico", top = 20.dp)
            EmailTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.padding(horizontal = 24.dp)
            )


            // Contraseña
            Label("Contraseña", top = 20.dp)
            PasswordTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.padding(horizontal = 24.dp)
            )


            // Términos y condiciones
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth()
            ) {
                Checkbox(checked = acceptTerms, onCheckedChange = { acceptTerms = it })
                Text(
                    buildAnnotatedString {
                        append("Estoy de acuerdo con los ")
                        pushStyle(
                            SpanStyle(
                                color = Color(0xFF008D96),
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = TextDecoration.Underline
                            )
                        )
                        append("términos y condiciones")
                        pop()
                    },
                    style = TextStyle(fontSize = 14.sp, color = Color(0xFF7D7A7A))
                )
            }

            // Botón Continuar
            MainButton(
                text = "Continuar",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                // TODO: validar y registrar
            }

            // ¿Ya tienes cuenta?
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "¿Ya tienes cuenta?  ",
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF7D7A7A))
                )
                TextButton(onClick = { nav.navigate(Screens.Login.route) }) {
                    Text("Inicia Sesión", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF008D96)))
                }
            }
        }
    }
}

/* ---------- helpers de estilo ---------- */

@Composable
private fun Label(text: String, top: Dp = 0.dp) {
    Text(
        text,
        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF7D7A7A)),
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = top, bottom = 8.dp)
    )
}

@Composable
private fun textFieldColors() = TextFieldDefaults.colors(
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
