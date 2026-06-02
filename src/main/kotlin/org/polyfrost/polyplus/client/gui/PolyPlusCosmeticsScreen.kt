package org.polyfrost.polyplus.client.gui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.polyfrost.oneconfig.internal.ui.components.Icon
import org.polyfrost.oneconfig.internal.ui.navigation.NavigationGroup
import org.polyfrost.oneconfig.internal.ui.navigation.NavigationRoute
import org.polyfrost.oneconfig.internal.ui.themes.Accent
import org.polyfrost.oneconfig.internal.ui.themes.LocalTheme
import org.polyfrost.polyplus.client.PolyPlusClient
import org.polyfrost.polyplus.client.cosmetics.CosmeticCatalog
import org.polyfrost.polyplus.client.cosmetics.CosmeticService
import org.polyfrost.polyplus.client.network.http.responses.CosmeticDefinition
import org.polyfrost.polyplus.client.network.http.responses.CosmeticType
import org.polyfrost.polyplus.client.utils.ClientPlatform
import kotlin.math.ceil

@Serializable
data object PolyPlusCosmeticsRoute

object PolyPlusOneConfigIntegration {
    private val cosmeticsRoute = NavigationRoute(
        id = "cosmetics",
        icon = "assets/polyplus/ico/stars.svg",
        route = PolyPlusCosmeticsRoute,
    )

    @JvmStatic
    fun navigationGroups(original: List<NavigationGroup>): List<NavigationGroup> {
        if (original.any { group -> group.routes.any { it.route == PolyPlusCosmeticsRoute } }) {
            return original
        }

        return original.map { group ->
            if (group.id == "Personalization") {
                val routes = group.routes.toMutableList()
                routes.add(minOf(1, routes.size), cosmeticsRoute)
                NavigationGroup(group.id, *routes.toTypedArray())
            } else {
                group
            }
        }
    }

    @JvmStatic
    fun addRoutes(builder: NavGraphBuilder) {
        builder.polyPlusCosmeticsGraph()
    }
}

fun NavGraphBuilder.polyPlusCosmeticsGraph() {
    composable<PolyPlusCosmeticsRoute> {
        PolyPlusCosmeticsScreen()
    }
}

private enum class PolyPlusTab {
    Wardrobe,
    Browse,
}

private enum class ShopPanel {
    Preview,
    Cart,
}

private data class CosmeticUiItem(
    val id: Int,
    val type: CosmeticType,
    val name: String,
    val collection: String,
    val owned: Boolean,
    val equipped: Boolean,
) {
    val isNew: Boolean get() = id % 3 == 0
    val discounted: Boolean get() = id % 5 == 0
}

@Composable
private fun PolyPlusCosmeticsScreen() {
    var tab by remember { mutableStateOf(PolyPlusTab.Wardrobe) }
    var shopPanel by remember { mutableStateOf(ShopPanel.Preview) }
    var refreshKey by remember { mutableIntStateOf(0) }
    var status by remember { mutableStateOf<String?>(null) }
    val cart = remember { mutableStateListOf<Int>() }
    val allItems = rememberCosmeticItems(refreshKey)
    var selectedId by remember(allItems) { mutableStateOf(allItems.firstOrNull()?.id) }
    val selected = allItems.firstOrNull { it.id == selectedId } ?: allItems.firstOrNull()

    LaunchedEffect(Unit) {
        while (true) {
            delay(1_500L)
            refreshKey++
        }
    }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Toolbar(
            activeTab = tab,
            cartSize = cart.size,
            onWardrobe = {
                tab = PolyPlusTab.Wardrobe
                status = null
            },
            onBrowse = {
                tab = PolyPlusTab.Browse
                shopPanel = ShopPanel.Preview
                status = null
            },
            onCart = {
                tab = PolyPlusTab.Browse
                shopPanel = ShopPanel.Cart
            },
            onRefresh = {
                PolyPlusClient.refreshCosmetics()
                refreshKey++
                status = "Refreshing cosmetic data..."
            },
        )

        when (tab) {
            PolyPlusTab.Wardrobe -> WardrobeScreen(
                items = allItems.filter { it.owned },
                selected = selected,
                status = status,
                onSelect = { selectedId = it.id },
                onEquip = { item ->
                    status = "Equipping ${item.name}..."
                    equip(item) {
                        refreshKey++
                        status = it
                    }
                },
            )

            PolyPlusTab.Browse -> BrowseCosmeticsScreen(
                items = allItems.filter { !it.owned },
                selected = selected,
                cart = cart,
                panel = shopPanel,
                status = status,
                onSelect = {
                    selectedId = it.id
                    shopPanel = ShopPanel.Preview
                },
                onAddToCart = {
                    if (it.id !in cart) cart += it.id
                    selectedId = it.id
                    status = "${it.name} added to cart."
                },
                onBackToPreview = { shopPanel = ShopPanel.Preview },
                onCheckout = { status = "Checkout is not wired to an API yet." },
            )
        }
    }
}

@Composable
private fun Toolbar(
    activeTab: PolyPlusTab,
    cartSize: Int,
    onWardrobe: () -> Unit,
    onBrowse: () -> Unit,
    onCart: () -> Unit,
    onRefresh: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth().height(33.dp), verticalAlignment = Alignment.CenterVertically) {
        TabButton("Wardrobe", activeTab == PolyPlusTab.Wardrobe, onWardrobe)
        Spacer(Modifier.width(8.dp))
        TabButton("Browse Cosmetics", activeTab == PolyPlusTab.Browse, onBrowse)
        Spacer(Modifier.weight(1f))
        SmallButton("Refresh", iconPath = "refresh", primary = false, onClick = onRefresh)
        Spacer(Modifier.width(8.dp))
        SmallButton("Cart ($cartSize)", iconPath = "assets/polyplus/ico/shopping-cart/0.svg", primary = true, onClick = onCart)
    }
}

@Composable
private fun TabButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.height(33.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(if (selected) Accent else LocalTheme.current.chipBackground)
            .border(1.dp, LocalTheme.current.borderColor, RoundedCornerShape(7.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        GuiText(
            label,
            color = if (selected) LocalTheme.current.accentTextColor else LocalTheme.current.textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun SmallButton(
    label: String,
    iconPath: String,
    primary: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.height(33.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(if (primary) Accent else LocalTheme.current.chipBackground)
            .border(1.dp, LocalTheme.current.borderColor, RoundedCornerShape(7.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(iconPath, color = if (primary) LocalTheme.current.accentTextColor else LocalTheme.current.textColor, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(8.dp))
        GuiText(label, color = if (primary) LocalTheme.current.accentTextColor else LocalTheme.current.textColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun WardrobeScreen(
    items: List<CosmeticUiItem>,
    selected: CosmeticUiItem?,
    status: String?,
    onSelect: (CosmeticUiItem) -> Unit,
    onEquip: (CosmeticUiItem) -> Unit,
) {
    var selectedType by remember { mutableStateOf(CosmeticType.Cape) }
    val displayItems = remember(items, selectedType) {
        items.filter { it.type == selectedType }
    }

    LaunchedEffect(selectedType, displayItems) {
        if (selected?.type != selectedType) {
            displayItems.firstOrNull()?.let(onSelect)
        }
    }

    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(19.dp)) {
        CategoryRail(
            selected = selectedType,
            onSelect = { selectedType = it },
        )
        CosmeticGrid(
            items = displayItems,
            mode = CardMode.Wardrobe,
            modifier = Modifier.width(596.dp).fillMaxHeight(),
            onSelect = onSelect,
            onPrimaryAction = onEquip,
        )
        PreviewPanel(
            selected = selected?.takeIf { it.type == selectedType } ?: displayItems.firstOrNull(),
            status = status,
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
    }
}

@Composable
private fun BrowseCosmeticsScreen(
    items: List<CosmeticUiItem>,
    selected: CosmeticUiItem?,
    cart: MutableList<Int>,
    panel: ShopPanel,
    status: String?,
    onSelect: (CosmeticUiItem) -> Unit,
    onAddToCart: (CosmeticUiItem) -> Unit,
    onBackToPreview: () -> Unit,
    onCheckout: () -> Unit,
) {
    val displayItems = items
    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(25.dp)) {
        CosmeticGrid(
            items = displayItems,
            mode = CardMode.Shop(cart.toSet()),
            modifier = Modifier.width(596.dp).fillMaxHeight(),
            onSelect = onSelect,
            onPrimaryAction = onAddToCart,
        )
        Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (panel == ShopPanel.Cart) {
                SmallButton("Back to preview", iconPath = "left-arrow", primary = false, onClick = onBackToPreview)
                CartPanel(
                    items = displayItems.filter { it.id in cart },
                    cart = cart,
                    status = status,
                    modifier = Modifier.fillMaxSize(),
                    onCheckout = onCheckout,
                )
            } else {
                PreviewPanel(selected = selected ?: displayItems.firstOrNull(), status = status, modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun CategoryRail(
    selected: CosmeticType,
    onSelect: (CosmeticType) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.width(58.dp)) {
        CosmeticType.entries.forEach { type ->
            val isSelected = type == selected
            Box(
                modifier = Modifier.fillMaxWidth().height(57.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(if (isSelected) Accent else LocalTheme.current.chipBackground)
                    .border(1.dp, LocalTheme.current.borderColor, RoundedCornerShape(7.dp))
                    .clickable { onSelect(type) },
                contentAlignment = Alignment.Center,
            ) {
                GuiText(
                    type.displayName,
                    color = if (isSelected) LocalTheme.current.accentTextColor else LocalTheme.current.textColor,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private sealed interface CardMode {
    data object Wardrobe : CardMode
    data class Shop(val cart: Set<Int>) : CardMode
}

@Composable
private fun CosmeticGrid(
    items: List<CosmeticUiItem>,
    mode: CardMode,
    modifier: Modifier,
    onSelect: (CosmeticUiItem) -> Unit,
    onPrimaryAction: (CosmeticUiItem) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(19.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(items) { item ->
            CosmeticCard(
                item = item,
                mode = mode,
                onSelect = { onSelect(item) },
                onPrimaryAction = { onPrimaryAction(item) },
            )
        }
    }
}

@Composable
private fun CosmeticCard(
    item: CosmeticUiItem,
    mode: CardMode,
    onSelect: () -> Unit,
    onPrimaryAction: () -> Unit,
) {
    val inCart = mode is CardMode.Shop && item.id in mode.cart
    val border = when {
        item.equipped || (mode is CardMode.Shop && item.id % 4 == 0) -> Accent
        item.discounted -> Color(0xFF239A60)
        else -> LocalTheme.current.borderColor
    }
    val buttonColor = when {
        item.equipped || inCart -> Accent
        item.discounted -> Color(0xFF239A60)
        else -> Color(0xB3232D32)
    }

    Box(
        modifier = Modifier.size(180.dp, 258.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(cardBrush())
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .clickable(onClick = onSelect),
    ) {
        CheckerThumbnail(Modifier.offset(17.dp, 17.dp).size(144.dp))
        GuiText(item.name, color = LocalTheme.current.textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.offset(17.dp, 169.dp).width(146.dp))

        when (mode) {
            CardMode.Wardrobe -> {
                GuiText(item.collection, color = LocalTheme.current.textColorSecondary, fontSize = 12.sp, modifier = Modifier.offset(17.dp, 192.dp).width(146.dp))
            }
            is CardMode.Shop -> {
                Row(modifier = Modifier.offset(17.dp, 193.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (item.discounted) {
                        GuiText("\$2", color = Color(0xFFFF4444), fontSize = 10.sp, textDecoration = TextDecoration.LineThrough)
                        Spacer(Modifier.width(4.dp))
                        GuiText("\$1.50", color = Color(0xFF239A60), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    } else {
                        GuiText("\$1.50", color = LocalTheme.current.textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        if (mode is CardMode.Shop && (item.isNew || item.discounted)) {
            Box(
                modifier = Modifier.align(Alignment.TopCenter).size(81.dp, 21.dp)
                    .background(if (item.discounted) Color(0xFF239A60) else Accent, RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)),
                contentAlignment = Alignment.Center,
            ) {
                GuiText(if (item.discounted) "25% OFF" else "NEW", color = LocalTheme.current.accentTextColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }

        Row(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(36.dp)
                .background(buttonColor)
                .clickable(onClick = onPrimaryAction),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when (mode) {
                CardMode.Wardrobe -> {
                    GuiText(if (item.equipped) "Equipped" else "Equip", color = LocalTheme.current.accentTextColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                is CardMode.Shop -> {
                    GuiText(if (inCart) "In cart" else "Add to cart", color = LocalTheme.current.accentTextColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun PreviewPanel(
    selected: CosmeticUiItem?,
    status: String?,
    modifier: Modifier,
) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(12.dp))
            .background(cardBrush())
            .border(1.dp, LocalTheme.current.borderColor, RoundedCornerShape(12.dp)),
    ) {
        PlayerPreview(Modifier.align(Alignment.Center).size(190.dp, 330.dp))
        if (selected != null) {
            Column(Modifier.align(Alignment.TopStart).padding(18.dp)) {
                GuiText(selected.name, color = LocalTheme.current.textColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                GuiText(selected.collection, color = LocalTheme.current.textColorSecondary, fontSize = 12.sp)
            }
        }
        if (status != null) {
            GuiText(status, color = LocalTheme.current.textColorSecondary, fontSize = 12.sp, modifier = Modifier.align(Alignment.BottomStart).padding(18.dp))
        }
    }
}

@Composable
private fun CartPanel(
    items: List<CosmeticUiItem>,
    cart: MutableList<Int>,
    status: String?,
    modifier: Modifier,
    onCheckout: () -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(11.dp)) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            items(items, key = { it.id }) { item ->
                CartRow(item = item, onRemove = { cart.remove(item.id) })
            }
        }

        val cartItems = items
        val subtotal = cartItems.size * 150
        val discount = cartItems.count { it.discounted } * 50
        RowTotals("Subtotal", cents(subtotal), LocalTheme.current.textColor)
        RowTotals("Discounts", "-${cents(discount)}", Color(0xFF239A60))
        RowTotals("Total", cents((subtotal - discount).coerceAtLeast(0)), Color.White, large = true)
        if (status != null) {
            GuiText(status, color = LocalTheme.current.textColorSecondary, fontSize = 12.sp)
        }
        SmallButton(
            label = "Checkout ${items.size} items",
            iconPath = "assets/polyplus/ico/shopping-cart/0.svg",
            primary = true,
            onClick = onCheckout,
        )
    }
}

@Composable
private fun CartRow(item: CosmeticUiItem, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(81.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(cardBrush())
            .border(1.dp, LocalTheme.current.borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 13.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        CheckerThumbnail(Modifier.size(58.dp))
        Column(Modifier.weight(1f)) {
            GuiText(item.name, color = LocalTheme.current.textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            GuiText(item.collection, color = LocalTheme.current.textColorSecondary, fontSize = 12.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (item.discounted) {
                    GuiText("\$2", color = Color(0xFFFF4444), fontSize = 10.sp, textDecoration = TextDecoration.LineThrough)
                    Spacer(Modifier.width(4.dp))
                    GuiText("\$1.50", color = Color(0xFF239A60), fontSize = 14.sp)
                } else {
                    GuiText("\$1.50", color = LocalTheme.current.textColor, fontSize = 14.sp)
                }
            }
            if (item.discounted) GuiText("SAVE 25%", color = Color(0xFF239A60), fontSize = 12.sp)
            GuiText("Remove", color = Color(0xFFFF4444), fontSize = 12.sp, modifier = Modifier.clickable(onClick = onRemove))
        }
    }
}

@Composable
private fun RowTotals(label: String, value: String, color: Color, large: Boolean = false) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        GuiText(label, color = LocalTheme.current.textColor, fontSize = if (large) 18.sp else 12.sp, modifier = Modifier.weight(1f))
        GuiText(value, color = color, fontSize = if (large) 22.sp else 12.sp, fontWeight = if (large) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
private fun CheckerThumbnail(modifier: Modifier) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(5.dp))
            .border(1.dp, LocalTheme.current.borderColor, RoundedCornerShape(5.dp))
            .drawBehind {
                val cell = 9.dp.toPx()
                val cols = ceil(size.width / cell).toInt()
                val rows = ceil(size.height / cell).toInt()
                for (x in 0..cols) {
                    for (y in 0..rows) {
                        drawRect(
                            color = if ((x + y) % 2 == 0) Color(0xFF5F6568) else Color(0xFF3D4245),
                            topLeft = Offset(x * cell, y * cell),
                            size = Size(cell, cell),
                        )
                    }
                }
            },
    )
}

@Composable
private fun PlayerPreview(modifier: Modifier) {
    Box(modifier) {
        Box(Modifier.align(Alignment.TopCenter).offset(y = 8.dp).size(72.dp).background(Color(0xFFC58A5C), RoundedCornerShape(6.dp)))
        Box(Modifier.align(Alignment.TopCenter).offset(y = 8.dp).size(72.dp, 22.dp).background(Color(0xFF6B3F25), RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)))
        Box(Modifier.align(Alignment.TopCenter).offset(y = 86.dp).size(88.dp, 116.dp).background(Color(0xFF163D9E), RoundedCornerShape(8.dp)))
        Box(Modifier.align(Alignment.TopCenter).offset((-62).dp, 96.dp).size(34.dp, 112.dp).background(Color(0xFF244FB8), RoundedCornerShape(8.dp)))
        Box(Modifier.align(Alignment.TopCenter).offset(62.dp, 96.dp).size(34.dp, 112.dp).background(Color(0xFF244FB8), RoundedCornerShape(8.dp)))
        Box(Modifier.align(Alignment.TopCenter).offset((-24).dp, 204.dp).size(34.dp, 112.dp).background(Color(0xFF151A21), RoundedCornerShape(8.dp)))
        Box(Modifier.align(Alignment.TopCenter).offset(24.dp, 204.dp).size(34.dp, 112.dp).background(Color(0xFF151A21), RoundedCornerShape(8.dp)))
        Box(Modifier.align(Alignment.TopCenter).offset(y = 122.dp).size(18.dp, 94.dp).background(Color(0xFFE4BD56), RoundedCornerShape(8.dp)))
        Box(Modifier.align(Alignment.TopCenter).offset(y = 196.dp).size(112.dp, 18.dp).background(Color(0xFF7E2419), RoundedCornerShape(4.dp)))
        Box(Modifier.align(Alignment.TopCenter).offset((-24).dp, 300.dp).size(36.dp, 20.dp).background(Color(0xFF8F271C), RoundedCornerShape(5.dp)))
        Box(Modifier.align(Alignment.TopCenter).offset(24.dp, 300.dp).size(36.dp, 20.dp).background(Color(0xFF8F271C), RoundedCornerShape(5.dp)))
    }
}

@Composable
private fun GuiText(
    text: String,
    color: Color,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.Normal,
    textAlign: TextAlign = TextAlign.Start,
    textDecoration: TextDecoration? = null,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = TextStyle(
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight,
            fontFamily = LocalTheme.current.typography.family,
            textAlign = textAlign,
            textDecoration = textDecoration,
        ),
    )
}

@Composable
private fun rememberCosmeticItems(refreshKey: Int): List<CosmeticUiItem> =
    remember(refreshKey) {
        val ownedIds = CosmeticCatalog.ownedIds()
        val equipped = CosmeticCatalog.localEquipped().equipped
        val definitions = CosmeticCatalog.allDefinitions().sortedWith(compareBy<CosmeticDefinition> { it.type.ordinal }.thenBy { it.id })
        definitions.map { definition ->
            CosmeticUiItem(
                id = definition.id,
                type = definition.type,
                name = definition.name,
                collection = "${definition.type.displayName} Collection",
                owned = definition.id in ownedIds,
                equipped = equipped[definition.type] == definition.id || definition.type == CosmeticType.Emote && CosmeticCatalog.selectedEmoteId() == definition.id,
            )
        }
    }

private fun equip(item: CosmeticUiItem, onComplete: (String) -> Unit) {
    if (!item.owned) {
        onComplete("${item.name} is not in your locker.")
        return
    }

    PolyPlusClient.SCOPE.launch {
        val result = if (item.type == CosmeticType.Emote) {
            CosmeticService.equipEmote(item.id)
        } else {
            CosmeticService.equip(item.id, item.type)
        }
        ClientPlatform.runOnMain {
            onComplete(
                result.fold(
                    onSuccess = { "Equipped ${item.name}." },
                    onFailure = { "Failed to equip ${item.name}: ${it.message}" },
                ),
            )
        }
    }
}

private fun cardBrush(): Brush =
    Brush.verticalGradient(
        listOf(
            Color(0x59232D32),
            Color(0xB3232D32),
        ),
    )

private fun cents(cents: Int): String = "\$${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"
