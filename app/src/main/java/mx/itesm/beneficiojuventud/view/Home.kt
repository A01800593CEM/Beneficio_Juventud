package mx.itesm.beneficiojuventud.view

import mx.itesm.beneficiojuventud.viewmodel.CategoryViewModel
import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import kotlinx.coroutines.launch
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.BJBottomBar
import mx.itesm.beneficiojuventud.components.BJSearchBar
import mx.itesm.beneficiojuventud.components.CategoryPill
import mx.itesm.beneficiojuventud.components.GradientDivider
import mx.itesm.beneficiojuventud.components.PromoCarousel
import mx.itesm.beneficiojuventud.components.SectionTitle
import mx.itesm.beneficiojuventud.model.promos.Promotions
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.viewmodel.PromoViewModel
import mx.itesm.beneficiojuventud.viewmodel.UserViewModel
import mx.itesm.beneficiojuventud.viewmodel.CollabViewModel

// Insets
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.ui.text.style.TextOverflow
import mx.itesm.beneficiojuventud.components.BJTab

// Nuevos imports para foto de perfil (igual que en Profile.kt)
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.StoragePath
import java.io.File
import android.util.Log

// 👇 NUEVOS imports: diseño “Poster” con fav
import mx.itesm.beneficiojuventud.components.MerchantRowSelectable
import mx.itesm.beneficiojuventud.components.MerchantDesign
import mx.itesm.beneficiojuventud.components.iconForCategoryName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    nav: NavHostController,
    modifier: Modifier = Modifier,
    categoryViewModel: CategoryViewModel = viewModel(), // mismo VM que Onboarding
    userViewModel: UserViewModel,
    promoViewModel: PromoViewModel = viewModel(),        // VM de promos (backend)
    // VM colaboradores (backend)
    collabViewModel: CollabViewModel = viewModel()
) {
    // ▶ Suscripciones
    val user by userViewModel.userState.collectAsState()
    val categories by categoryViewModel.categories.collectAsState()
    val catLoading by categoryViewModel.loading.collectAsState()
    val catError by categoryViewModel.error.collectAsState()
    val scope = rememberCoroutineScope()

    val promoList by promoViewModel.promoListState.collectAsState()

    // lista de colaboradores del backend
    val collaborators by collabViewModel.collabListState.collectAsState()

    // ❤️ favoritos de colaboradores (para que funcione el corazón)
    val favoriteCollabs by userViewModel.favoriteCollabs.collectAsState()
    val favoriteCollabIds: Set<String> = remember(favoriteCollabs) {
        favoriteCollabs.mapNotNull { it.cognitoId?.takeIf(String::isNotBlank) }.toSet()
    }
    val cognitoId = user.cognitoId.orEmpty()
    LaunchedEffect(cognitoId) {
        if (cognitoId.isNotBlank()) userViewModel.refreshFavorites(cognitoId)
    }

    // Estado de UI
    var selectedTab by remember { mutableStateOf(BJTab.Home) }
    var search by rememberSaveable { mutableStateOf("") }
    var selectedCategoryName by rememberSaveable { mutableStateOf<String?>(null) }

    // Loading/Error locales para promos (hasta que el VM los exponga)
    var promoLoading by remember { mutableStateOf(false) }
    var promoError by remember { mutableStateOf<String?>(null) }

    // Loading/Error locales para colaboradores
    var collabLoading by remember { mutableStateOf(false) }
    var collabError by remember { mutableStateOf<String?>(null) }

    // ⬇️ Estado e imagen de perfil (igual que en Profile.kt)
    val context = LocalContext.current
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var isLoadingImage by remember { mutableStateOf(false) }

    LaunchedEffect(cognitoId) {
        if (cognitoId.isBlank()) {
            profileImageUrl = null
            isLoadingImage = false
            return@LaunchedEffect
        }
        try {
            downloadProfileImageForDisplay(
                context = context,
                userId = cognitoId,
                onSuccess = { url: String -> profileImageUrl = url },
                onError = { _: Throwable? -> /* si falla, dejamos el fallback */ },
                onLoading = { loading: Boolean -> isLoadingImage = loading }
            )
        } catch (_: Exception) {
            // Storage no configurado aún, ignoramos
        }

    }

    // ─────────────────────────────────────────────────────────────────────────────
    // PROMOS: carga inicial → favoritas intercaladas (si hay), si no → todas
    // ─────────────────────────────────────────────────────────────────────────────
    LaunchedEffect(user.categories) {
        promoError = null
        promoLoading = true
        runCatching {
            val favNames = user.categories
                ?.mapNotNull { it.name }
                ?.filter { it.isNotBlank() }
                .orEmpty()

            if (favNames.isNotEmpty()) {
                promoViewModel.getRecommendedInterleaved(favNames)
            } else {
                promoViewModel.getAllPromotions()
            }
        }.onFailure { e -> promoError = e.message ?: "Error al cargar promos" }
        promoLoading = false
    }

    // Carga inicial de colaboradores usando 1ra categoría si no hay selección
    LaunchedEffect(categories) {
        if (categories.isNotEmpty() && selectedCategoryName == null) {
            val firstCat = categories.first().name ?: return@LaunchedEffect
            collabError = null
            collabLoading = true
            runCatching { collabViewModel.getCollaboratorsByCategory(firstCat) }
                .onFailure { e -> collabError = e.message ?: "Error al cargar colaboradores" }
            collabLoading = false
        }
    }

    // Carga de promos cuando cambia el filtro de categoría
    LaunchedEffect(selectedCategoryName) {
        if (selectedCategoryName == null) return@LaunchedEffect
        promoError = null
        promoLoading = true
        runCatching { promoViewModel.getPromotionByCategory(selectedCategoryName!!) }
            .onFailure { e -> promoError = e.message ?: "Error al cargar promos" }
        promoLoading = false
    }

    // Carga de colaboradores cuando cambia el filtro de categoría
    LaunchedEffect(selectedCategoryName) {
        val cat = selectedCategoryName ?: return@LaunchedEffect
        collabError = null
        collabLoading = true
        runCatching { collabViewModel.getCollaboratorsByCategory(cat) }
            .onFailure { e -> collabError = e.message ?: "Error al cargar colaboradores" }
        collabLoading = false
    }

    // Nombre a mostrar
    val displayName = remember(user.name) {
        user.name?.trim()?.takeIf { it.isNotEmpty() }?.split(" ")?.firstOrNull() ?: "Usuario"
    }

    // Lista final para el carrusel
    val uiPromos: List<Promotions> = remember(promoList) { promoList }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
            .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        topBar = {
            TopBar(
                displayName = displayName,
                search = search,
                onSearchChange = { search = it },
                profileImageUrl = profileImageUrl,
                isLoadingImage = isLoadingImage
            )
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
    ) { innerPadding ->
        val bottomInset = WindowInsets.navigationBars
            .only(WindowInsetsSides.Bottom)
            .asPaddingValues()
            .calculateBottomPadding()
        val bottomBarHeight = 56.dp

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            contentPadding = PaddingValues(
                bottom = bottomBarHeight + bottomInset + 16.dp
            )
        ) {
            // ─── Categorías (API) ────────────────────────────────────────────────
            item {
                SectionTitle(
                    "Categorías Populares",
                    Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                when {
                    catLoading -> {
                        Row(
                            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Cargando categorías…")
                        }
                    }
                    catError != null -> {
                        Row(
                            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Error al cargar categorías",
                                color = Color(0xFFB00020),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.width(12.dp))
                            TextButton(onClick = { categoryViewModel.loadCategories() }) {
                                Text("Reintentar")
                            }
                        }
                    }
                    else -> {
                        Row(
                            Modifier
                                .padding(horizontal = 16.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            categories.forEach { c ->
                                val name = c.name ?: return@forEach
                                val icon = iconForCategoryName(name)
                                CategoryPill(
                                    icon = icon,
                                    label = name,
                                    selected = selectedCategoryName == name,
                                    onClick = {
                                        selectedCategoryName = if (selectedCategoryName == name) null else name
                                        // Refrescar inmediatamente promos + colaboradores
                                        scope.launch {
                                            // PROMOS
                                            promoError = null
                                            promoLoading = true
                                            runCatching {
                                                if (selectedCategoryName == null) {
                                                    val favNames = user.categories
                                                        ?.mapNotNull { it.name }
                                                        ?.filter { it.isNotBlank() }
                                                        .orEmpty()

                                                    if (favNames.isNotEmpty()) {
                                                        promoViewModel.getRecommendedInterleaved(favNames)
                                                    } else {
                                                        promoViewModel.getAllPromotions()
                                                    }
                                                } else {
                                                    promoViewModel.getPromotionByCategory(selectedCategoryName!!)
                                                }
                                            }.onFailure { e -> promoError = e.message ?: "Error al cargar promos" }
                                            promoLoading = false

                                            // COLABORADORES
                                            collabError = null
                                            collabLoading = true
                                            runCatching {
                                                val cat = selectedCategoryName
                                                    ?: categories.firstOrNull()?.name
                                                    ?: return@runCatching
                                                collabViewModel.getCollaboratorsByCategory(cat)
                                            }.onFailure { e -> collabError = e.message ?: "Error al cargar colaboradores" }
                                            collabLoading = false
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                if (!selectedCategoryName.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AssistChip(
                            onClick = {
                                selectedCategoryName = null
                                scope.launch {
                                    // PROMOS (favoritas intercaladas si existen, si no todas)
                                    promoError = null
                                    promoLoading = true
                                    runCatching {
                                        val favNames = user.categories
                                            ?.mapNotNull { it.name }
                                            ?.filter { it.isNotBlank() }
                                            .orEmpty()

                                        if (favNames.isNotEmpty()) {
                                            promoViewModel.getRecommendedInterleaved(favNames)
                                        } else {
                                            promoViewModel.getAllPromotions()
                                        }
                                    }.onFailure { e -> promoError = e.message ?: "Error al cargar promos" }
                                    promoLoading = false

                                    // COLABORADORES: vuelve a 1ra categoría como “default”
                                    if (categories.isNotEmpty()) {
                                        collabError = null
                                        collabLoading = true
                                        runCatching {
                                            collabViewModel.getCollaboratorsByCategory(categories.first().name!!)
                                        }.onFailure { e -> collabError = e.message ?: "Error al cargar colaboradores" }
                                        collabLoading = false
                                    }
                                }
                            },
                            label = { Text("Quitar filtro") }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Filtrando por: ${selectedCategoryName ?: ""}",
                            color = Color(0xFF8C8C8C),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f) // ⇦ ocupa el resto y corta
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
            }

            // ─── Recomendado / Filtrado ─────────────────────────────────────────
            item {
                SectionTitle(
                    if (selectedCategoryName.isNullOrBlank()) "Recomendado para ti"
                    else "Cupones ($selectedCategoryName)",
                    Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                when {
                    promoLoading -> {
                        Row(
                            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Buscando cupones…")
                        }
                    }
                    promoError != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No se pudieron cargar las promociones: $promoError",
                                color = Color(0xFF8C8C8C),
                                fontSize = 13.sp
                            )
                        }
                    }
                    uiPromos.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (selectedCategoryName.isNullOrBlank())
                                    "Por ahora no hay promociones disponibles."
                                else
                                    "No hay cupones para esta categoría.",
                                color = Color(0xFF8C8C8C),
                                fontSize = 13.sp
                            )
                        }
                    }
                    else -> {
                        // Carrusel de promos
                        PromoCarousel(
                            promos = uiPromos,
                            onItemClick = { promo ->
                                promo.promotionId?.let { id ->
                                    nav.navigate(Screens.PromoQR.createRoute(id))
                                }
                            }
                        )
                    }
                }
            }

            // ─── Ofertas Especiales (BACKEND) ───────────────────────────────────
            item {
                SectionTitle(
                    if (selectedCategoryName.isNullOrBlank())
                        "Ofertas Especiales"
                    else
                        "Comercios ($selectedCategoryName)",
                    Modifier.padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 6.dp)
                )

                when {
                    collabLoading -> {
                        Row(
                            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Cargando comercios…")
                        }
                    }
                    collabError != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No se pudieron cargar los comercios: $collabError",
                                color = Color(0xFF8C8C8C),
                                fontSize = 13.sp
                            )
                        }
                    }
                    collaborators.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No hay comercios para esta categoría.",
                                color = Color(0xFF8C8C8C),
                                fontSize = 13.sp
                            )
                        }
                    }
                    else -> {
                        // 👉 USAMOS EL OTRO DISEÑO (POSTER) con corazón
                        MerchantRowSelectable(
                            collaborators = collaborators,
                            design = MerchantDesign.Poster,
                            isFavorite = { c -> favoriteCollabIds.contains(c.cognitoId.orEmpty()) },
                            onFavoriteClick = { c ->
                                val id = c.cognitoId ?: return@MerchantRowSelectable
                                scope.launch {
                                    if (favoriteCollabIds.contains(id)) {
                                        userViewModel.unfavoriteCollaborator(id, cognitoId)
                                    } else {
                                        // si tu VM expone esta función:
                                        userViewModel.favoriteCollaborator(id, cognitoId)
                                    }
                                }
                            },
                            onItemClick = { collab ->
                                collab.cognitoId?.let { id ->
                                    nav.navigate(Screens.Business.createRoute(id))
                                }
                            }
                        )
                    }
                }
            }

            // ─── Lo nuevo ───────────────────────────────────────────────────────
            item {
                SectionTitle(
                    "Lo Nuevo",
                    Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 6.dp)
                )
                when {
                    collabLoading -> {
                        Row(
                            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Cargando comercios…")
                        }
                    }
                    collabError != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No se pudieron cargar los comercios: $collabError",
                                color = Color(0xFF8C8C8C),
                                fontSize = 13.sp
                            )
                        }
                    }
                    collaborators.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No hay comercios disponibles.",
                                color = Color(0xFF8C8C8C),
                                fontSize = 13.sp
                            )
                        }
                    }
                    else -> {
                        // 👉 También aquí, el diseño Poster
                        MerchantRowSelectable(
                            collaborators = collaborators,
                            design = MerchantDesign.Poster,
                            isFavorite = { c -> favoriteCollabIds.contains(c.cognitoId.orEmpty()) },
                            onFavoriteClick = { c ->
                                val id = c.cognitoId ?: return@MerchantRowSelectable
                                scope.launch {
                                    if (favoriteCollabIds.contains(id)) {
                                        userViewModel.unfavoriteCollaborator(id, cognitoId)
                                    } else {
                                        userViewModel.favoriteCollaborator(id, cognitoId)
                                    }
                                }
                            },
                            onItemClick = { collab ->
                                collab.cognitoId?.let { id ->
                                    nav.navigate(Screens.Business.createRoute(id))
                                }
                            }
                        )
                    }
                }
            }

            // ─── Pie ────────────────────────────────────────────────────────────
            item {
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Versión 1.0.01", color = Color(0xFFAEAEAE), fontSize = 10.sp)
                }
            }
        }
    }
}

/** TopBar segura (solo Top + Horizontal) con avatar que carga igual que Profile.kt. */
@Composable
private fun TopBar(
    displayName: String,
    search: String,
    onSearchChange: (String) -> Unit,
    profileImageUrl: String?,
    isLoadingImage: Boolean
) {
    Column(
        Modifier
            .fillMaxWidth()
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                )
            )
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
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD7F2F3)),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isLoadingImage -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF008D96)
                            )
                        }
                        profileImageUrl != null -> {
                            AsyncImage(
                                model = profileImageUrl,
                                contentDescription = "Foto de perfil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(CircleShape)
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = Color(0xFF008D96)
                            )
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        "Hola, $displayName",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = Color(0xFF4B4C7E)
                    )
                    Text(
                        "¿Listo para ahorrar?",
                        fontSize = 11.sp,
                        color = Color(0xFF8C8C8C)
                    )
                }
            }
            Icon(
                imageVector = Icons.Outlined.NotificationsNone,
                contentDescription = "Notificaciones",
                tint = Color(0xFF008D96),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(Modifier.height(10.dp))
        BJSearchBar(
            query = search,
            onQueryChange = onSearchChange,
            onSearch = { /* TODO: búsqueda global */ },
            modifier = Modifier.fillMaxWidth()
        )

        GradientDivider(
            thickness = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )
    }
}

/** Vista previa de [Home]. */
@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomePreview() {
    BeneficioJuventudTheme {
        val nav = rememberNavController()
        Home(nav = nav, userViewModel = UserViewModel())
    }
}

// ————————————————————————————————————————————————————————————————
// Helper: descarga imagen de perfil (igual que en Profile.kt)
// (Si ya lo tienes en un utils, puedes borrar esto y usar el tuyo.)
// ————————————————————————————————————————————————————————————————
private fun downloadProfileImageForDisplay(
    context: android.content.Context,
    userId: String,
    onSuccess: (String) -> Unit,
    onError: (Throwable?) -> Unit,
    onLoading: (Boolean) -> Unit
) {
    val key = StoragePath.fromString("public/profile-images/$userId.jpg") // ← CORREGIDO
    try {
        onLoading(true)
        val localFile = File(context.cacheDir, "avatar_$userId.jpg")
        Amplify.Storage.downloadFile(
            key,
            localFile,
            { result ->
                onLoading(false)
                onSuccess(result.file.absolutePath)
            },
            { error ->
                onLoading(false)
                Log.e("Home", "Error descargando avatar", error)
                onError(error)
            }
        )
    } catch (e: Exception) {
        onLoading(false)
        onError(e)
    }
}

