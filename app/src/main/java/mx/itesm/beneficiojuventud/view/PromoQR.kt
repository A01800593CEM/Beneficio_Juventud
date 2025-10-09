package mx.itesm.beneficiojuventud.view

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.collectLatest
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.*
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
    val qrRes: Int? = null
)

@Composable
fun PromoQR(
    nav: NavHostController,
    modifier: Modifier = Modifier
) {
    val detail = PromoDetail(
        bannerRes = R.drawable.el_fuego_sagrado,
        title = "Martes 2x1",
        merchant = "Cine Stelar",
        discountLabel = "50% OFF",
        validUntil = "12/30/2024",
        description = "Compra un boleto y obtén el segundo gratis para la misma función.",
        terms = "Válido solo los martes. No aplica en estrenos ni funciones 3D. Presentar cupón en taquilla.",
        qrRes = R.drawable.qr_demo
    )

    var selectedTab by remember { mutableStateOf(BJTab.Coupons) }
    var isRedeemed by rememberSaveable { mutableStateOf(false) }

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

        // 👇 LazyColumn respeta el top/bottom padding del Scaffold (no se enciman)
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding), // <- Desplaza por top y bottom del Scaffold
            contentPadding = PaddingValues(
                bottom = 96.dp // <- Solo tu extra para la BottomBar (o lo que necesites)
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ---------- BANNER ----------
            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth()
                        .height(150.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = detail.bannerRes),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = detail.title,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = detail.merchant,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // ---------- INFO CARD ----------
            item { InfoCard(detail) }

            item { Spacer(Modifier.height(20.dp)) }

            // ---------- QR / CANJEADO ----------
            item {
                Crossfade(targetState = isRedeemed, label = "redeem_xfade") { redeemed ->
                    if (redeemed) RedeemedCard() else QRCard(detail)
                }
            }

            // ---------- BOTÓN SIMULAR ESCANEO ----------
            item {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { isRedeemed = true },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Simular escaneo de QR")
                }
            }

            // ---------- NOTA + VERSIÓN ----------
            item {
                Spacer(Modifier.height(20.dp))
                Text(
                    "Versión 1.0.01",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

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
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
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
                Text(
                    text = "Válido hasta ${detail.validUntil}",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = "Descripción",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
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
            Text(
                text = "Términos y condiciones",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp
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

@Composable
private fun QRCard(detail: PromoDetail) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Código de Canje",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(12.dp))
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = detail.qrRes ?: R.drawable.qr_demo),
                    contentDescription = "QR",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Presenta este código QR en ${detail.merchant} para canjear tu descuento",
            color = Color.Gray,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun RedeemedCard() {
    // Borde punteado como en el mockup
    val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
    Column(
        Modifier
            .padding(horizontal = 16.dp)
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
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Código de Canje",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(16.dp))
        Icon(
            painter = painterResource(id = R.drawable.ic_check_circle), // pon un check verde
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