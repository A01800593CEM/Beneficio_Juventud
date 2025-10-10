package mx.itesm.beneficiojuventud.view

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.BJBottomBar
import mx.itesm.beneficiojuventud.components.BackButton
import mx.itesm.beneficiojuventud.components.GradientDivider
import mx.itesm.beneficiojuventud.components.BJTab
import mx.itesm.beneficiojuventud.components.MainButton
import mx.itesm.beneficiojuventud.model.PromoTheme
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme

data class PromoDetail(
    val bannerRes: Int,
    val title: String,
    val merchant: String,
    val discountLabel: String,
    val validUntil: String,
    val description: String,
    val terms: String,
    val qrBitmap: ImageBitmap? = null,
    val qrRes: Int? = null,
    val uses: Int = 2,
    val maxUses: Int = 5,
    val theme: PromoTheme = PromoTheme.LIGHT // <- theme seleccionable (desde API)
)

@Composable
fun PromoQR(
    nav: NavHostController,
    modifier: Modifier = Modifier
) {
    val detail = PromoDetail(
        bannerRes = R.drawable.el_fuego_sagrado,
        title = "2do Café al 50%",
        merchant = "Café Origo",
        discountLabel = "50% OFF",
        validUntil = "12/30/2024",
        description = "Compra un boleto y obtén el segundo gratis para la misma función.",
        terms = "Válido solo los martes. No aplica en estrenos ni funciones 3D. Presentar cupón en taquilla.",
        qrRes = R.drawable.qr_demo,
        theme = PromoTheme.LIGHT // prueba rápida; cambia a DARK o llega por API
    )

    var selectedTab by remember { mutableStateOf(BJTab.Coupons) }
    var isRedeemed by rememberSaveable { mutableStateOf(false) }     // canjeado
    var showQrDialog by rememberSaveable { mutableStateOf(false) }   // overlay visible

    // Paletas de texto y degradado según el theme (idéntico criterio a tu carrusel)
    val theme = detail.theme
    val titleColor = if (theme == PromoTheme.LIGHT) Color(0xFFFFFFFF) else Color(0xFF505050)
    val subtitleColor = if (theme == PromoTheme.LIGHT) Color(0xFFD3D3D3) else Color(0xFF616161)
    val gradientBrush = when (theme) {
        PromoTheme.LIGHT -> Brush.horizontalGradient(
            0.00f to Color(0xFF2B2B2B).copy(alpha = 1f),
            0.15f to Color(0xFF2B2B2B).copy(alpha = .95f),
            0.30f to Color(0xFF2B2B2B).copy(alpha = .70f),
            0.45f to Color(0xFF2B2B2B).copy(alpha = .40f),
            0.60f to Color(0xFF2B2B2B).copy(alpha = .25f),
            0.75f to Color.Transparent,
            1.00f to Color.Transparent
        )
        PromoTheme.DARK -> Brush.horizontalGradient(
            0.00f to Color.White.copy(alpha = 1f),
            0.15f to Color.White.copy(alpha = .95f),
            0.30f to Color.White.copy(alpha = .70f),
            0.45f to Color.White.copy(alpha = .40f),
            0.60f to Color.White.copy(alpha = .25f),
            0.75f to Color.Transparent,
            1.00f to Color.Transparent
        )
    }

    // Envolvemos todo en un Box para poder pintar el overlay por encima con zIndex
    Box(modifier = Modifier.fillMaxSize()) {

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = { PromoQRTopBar(nav) },
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

            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 96.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ---------- BANNER ----------
                item {
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .fillMaxWidth()
                            .height(210.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Box(Modifier.fillMaxSize()) {

                            // 1) Imagen al fondo
                            Image(
                                painter = painterResource(id = detail.bannerRes),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(alpha = 0.85f) // coherente con carrusel
                            )

                            // 2) Degradado encima de la imagen, debajo del texto
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .drawWithCache {
                                        onDrawWithContent {
                                            drawContent()
                                            drawRect(brush = gradientBrush)
                                        }
                                    }
                            )

                            // 3) Badge arriba a la derecha
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF008D96))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = detail.discountLabel,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            // 4) Texto arriba del degradado
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = detail.merchant,
                                    color = subtitleColor.copy(alpha = 0.95f),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = detail.title,
                                    color = titleColor,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 28.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // ---------- INFO CARD ----------
                item { InfoCard(detail) }

                // ---------- USOS ----------
                item {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Usos ${detail.uses} / ${detail.maxUses}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF3C3C3C),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // ---------- BOTÓN “Ver QR” (abre overlay) ----------
                item {
                    Spacer(Modifier.height(8.dp))
                    MainButton(
                        text = "Ver QR",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        onClick = { showQrDialog = true }
                    )
                }

                // ---------- VERSIÓN ----------
                item {
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "Versión 1.0.12",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }

        // ---------- POP-UP CUSTOM (overlay propio) ----------
        if (showQrDialog) {
            // Scrim + tarjeta centrada
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f)
                    .background(Color.Black.copy(alpha = 0.50f))
                    .pointerInput(Unit) { detectTapGestures(onTap = { showQrDialog = false }) },
                contentAlignment = Alignment.Center
            ) {
                // Contenedor del pop-up
                Box(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .pointerInput(Unit) { detectTapGestures(onTap = { /* consume */ }) }
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(min = 300.dp, max = 420.dp)
                            .padding(horizontal = 24.dp, vertical = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        Text(
                            text = "Código de Canje",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(16.dp))

                        // Contenedor para centrar estrictamente el QR/contenido
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Crossfade(
                                targetState = isRedeemed,
                                label = "redeem_overlay_xfade"
                            ) { redeemed ->
                                if (redeemed) {
                                    RedeemedCardInner()
                                } else {
                                    QRCardInner(detail)
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { isRedeemed = true },
                                modifier = Modifier.weight(1f)
                            ) { Text("Simular escaneo") }

                            MainButton(
                                text = "Cerrar",
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                onClick = { showQrDialog = false }
                            )
                        }
                    }
                }
            }
        }
    }
}

/* -------------------- Tarjeta de información -------------------- */

@Composable
private fun InfoCard(detail: PromoDetail) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7))
    ) {
        Column(Modifier.padding(16.dp)) {

            // Descripción
            Text(
                text = "Descripción",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF454545),
                fontSize = 14.sp
            )
            Text(
                text = detail.description,
                color = Color.Gray,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Términos (con vigencia al inicio)
            Text(
                text = "Términos y condiciones",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF454545),
                fontSize = 14.sp
            )
            Text(
                text = "Vigencia: ${detail.validUntil}",
                color = Color(0xFF6B7280),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = detail.terms,
                color = Color.Gray,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/* -------------------- Contenido del QR dentro del pop-up -------------------- */

@Composable
private fun QRCardInner(detail: PromoDetail) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            // tamaño fijo y cómodo para que nada empuje al texto
            Box(
                modifier = Modifier
                    .size(220.dp)          // ligeramente más grande
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = detail.qrRes ?: R.drawable.qr_demo),
                    contentDescription = "QR",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // texto SIEMPRE debajo, con buen padding
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Presenta este código QR en ${detail.merchant} para canjear tu descuento",
            color = Color(0xFF6B7280),
            fontSize = 12.sp,
            lineHeight = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(max = 340.dp)
                .padding(horizontal = 8.dp),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RedeemedCardInner() {
    val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val strokeWidth = 2.dp.toPx()
                drawRoundRect(
                    color = Color(0xFFDFE3E6),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = strokeWidth,
                        pathEffect = dash
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
                )
            }
            .padding(vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_check_circle),
            contentDescription = null,
            tint = Color(0xFF22C55E),
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "¡Cupón Canjeado!",
            color = Color(0xFF22C55E),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Gracias por usar nuestros servicios",
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

/* -------------------- TopBar -------------------- */

@Composable
private fun PromoQRTopBar(nav: NavHostController) {
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
                    text = "Promo",
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PromoQRPreview() {
    BeneficioJuventudTheme {
        val nav = rememberNavController()
        PromoQR(nav)
    }
}
