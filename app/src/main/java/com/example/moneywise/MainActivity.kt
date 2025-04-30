package com.example.moneywise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.moneywise.ui.theme.MoneyWiseTheme
import com.example.moneywise.ui.theme.IncomeColor
import com.example.moneywise.ui.theme.ExpenseColor
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
//import androidx.compose.ui.text.input.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoneyWiseApp()
        }
    }
}

// Data classes
data class Transaction(
    val id: UUID = UUID.randomUUID(),
    val type: TransactionType,
    val category: String,
    val amount: Double,
    val description: String,
    val date: Date = Date()
)

enum class TransactionType {
    INCOME, EXPENSE
}

// Predefined categories
val incomeCategories = listOf("Gaji", "Bonus Kerja", "Hasil Jualan", "Lainnya")
val expenseCategories = listOf("Makanan", "Transportasi", "Belanja", "Listrik & Rumah", "Lainnya")

// App state
class MoneyWiseState {
    var transactions by mutableStateOf(listOf<Transaction>())
        private set

    val totalIncome: Double
        get() = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }

    val totalExpense: Double
        get() = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

    val balance: Double
        get() = totalIncome - totalExpense

    val incomeByCategory: Map<String, Double>
        get() = transactions
            .filter { it.type == TransactionType.INCOME }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

    val expenseByCategory: Map<String, Double>
        get() = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

    fun addTransaction(transaction: Transaction) {
        transactions = transactions + transaction
    }
}

@Composable
fun MoneyWiseApp() {
    val appState = remember { MoneyWiseState() }
    val navController = rememberNavController()

    MoneyWiseTheme {
        NavHost(navController = navController, startDestination = "home") {
            composable("home") {
                HomeScreen(appState, navController)
            }
            composable("add_transaction") {
                AddTransactionScreen(appState, navController)
            }
            composable("transactions") {
                TransactionsScreen(appState, navController)
            }
            composable("analysis") {
                AnalysisScreen(appState, navController)
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeScreen(state: MoneyWiseState, navController: NavController) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Money Wise") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_transaction") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Transaksi")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ringkasan Keuangan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("Total Pemasukan")
                            Text(
                                text = currencyFormatter.format(state.totalIncome),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Pengeluaran")
                            Text(
                                text = currencyFormatter.format(state.totalExpense),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF44336)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Saldo")
                    Text(
                        text = currencyFormatter.format(state.balance),
                        fontWeight = FontWeight.Bold,
                        color = if (state.balance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }
            }

            // Pie Chart Visualization
            if (state.transactions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Ringkasan Transaksi",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Improved pie chart visualization
                val totalValue = state.totalIncome + state.totalExpense
                val incomeRatio = if (totalValue > 0) state.totalIncome / totalValue else 0.0
                val expenseRatio = if (totalValue > 0) state.totalExpense / totalValue else 0.0

                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Use our improved DonutChart
                    DonutChart(
                        values = mapOf(
                            "Income" to state.totalIncome,
                            "Expense" to state.totalExpense
                        ),
                        colors = listOf(IncomeColor, ExpenseColor),
                        modifier = Modifier.fillMaxSize()
                    )

                    // Center label
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Saldo",
                            fontSize = 12.sp
                        )
                        Text(
                            text = currencyFormatter.format(state.balance),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (state.balance >= 0) IncomeColor else ExpenseColor
                        )
                    }
                }

                // Legend
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color(0xFF4CAF50), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pemasukan")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color(0xFFF44336), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pengeluaran")
                    }
                }
            }

            // Recent transactions
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Transaksi Terbaru",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (state.transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada transaksi",
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(state.transactions.sortedByDescending { it.date }.take(5)) { transaction ->
                        TransactionItem(transaction)
                    }
                }
            }
        }
    }
}

@Composable
fun SimplePieChart(slices: List<PieChartSlice>) {
    if (slices.isEmpty() || slices.all { it.value == 0f }) {
        // Empty chart
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .clip(CircleShape)
                .background(Color.LightGray)
        )
        return
    }

    // Calculate total and proportions
    val total = slices.sumOf { it.value.toDouble() }
    var startAngle = 0f

    // Draw chart
    Box(modifier = Modifier.aspectRatio(1f)) {
        slices.forEach { slice ->
            val sweepAngle = (slice.value / total.toFloat()) * 360f

            // Draw arc only if it has a value
            if (slice.value > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawPieSlice(
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            color = slice.color
                        )
                )
            }

            startAngle += sweepAngle
        }

        // Draw inner circle for donut chart effect
        Box(
            modifier = Modifier
                .fillMaxSize(0.6f)
                .align(Alignment.Center)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background)
        )
    }
}

// Extension function to draw pie slice
fun Modifier.drawPieSlice(startAngle: Float, sweepAngle: Float, color: Color): Modifier {
    // Custom drawing would require Canvas in real implementation
    // For simplicity, we'll use a workaround with Box and background
    return this.clip(CircleShape).background(color)
}

@Composable
fun TransactionItem(transaction: Transaction) {
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        color = if (transaction.type == TransactionType.INCOME)
                            Color(0xFF4CAF50).copy(alpha = 0.2f)
                        else
                            Color(0xFFF44336).copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = transaction.category.first().toString(),
                    color = if (transaction.type == TransactionType.INCOME)
                        Color(0xFF4CAF50)
                    else
                        Color(0xFFF44336),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Transaction details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.category,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = transaction.description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = dateFormatter.format(transaction.date),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Amount
            Text(
                text = (if (transaction.type == TransactionType.INCOME) "+" else "-") +
                        currencyFormatter.format(transaction.amount),
                fontWeight = FontWeight.Bold,
                color = if (transaction.type == TransactionType.INCOME)
                    Color(0xFF4CAF50)
                else
                    Color(0xFFF44336)
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AddTransactionScreen(state: MoneyWiseState, navController: NavController) {
    var transactionType by remember { mutableStateOf(TransactionType.INCOME) }
    var category by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val categories = if (transactionType == TransactionType.INCOME) incomeCategories else expenseCategories

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Transaksi") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Transaction type selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { transactionType = TransactionType.INCOME },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (transactionType == TransactionType.INCOME)
                            Color(0xFF4CAF50)
                        else
                            Color.Gray.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Pemasukan")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { transactionType = TransactionType.EXPENSE },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (transactionType == TransactionType.EXPENSE)
                            Color(0xFFF44336)
                        else
                            Color.Gray.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Pengeluaran")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category dropdown
            var expandedCategory by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedCategory,
                onExpandedChange = { expandedCategory = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kategori") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expandedCategory,
                    onDismissRequest = { expandedCategory = false }
                ) {
                    categories.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                category = option
                                expandedCategory = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount
            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) amount = it },
                label = { Text("Nominal") },
//                keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
//                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
//                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Deskripsi (opsional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save button
            Button(
                onClick = {
                    // Validate input
                    if (category.isNotEmpty() && amount.isNotEmpty()) {
                        val amountValue = amount.toDoubleOrNull() ?: 0.0
                        if (amountValue > 0) {
                            // Add transaction
                            state.addTransaction(
                                Transaction(
                                    type = transactionType,
                                    category = category,
                                    amount = amountValue,
                                    description = description.ifEmpty { category }
                                )
                            )
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Simpan Transaksi")
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TransactionsScreen(state: MoneyWiseState, navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daftar Transaksi") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        if (state.transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Belum ada transaksi")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                items(state.transactions.sortedByDescending { it.date }) { transaction ->
                    TransactionItem(transaction)
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AnalysisScreen(state: MoneyWiseState, navController: NavController) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analisis Keuangan") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Summary card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Pemasukan")
                        Text(
                            text = currencyFormatter.format(state.totalIncome),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Pengeluaran")
                        Text(
                            text = currencyFormatter.format(state.totalExpense),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF44336)
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Saldo")
                        Text(
                            text = currencyFormatter.format(state.balance),
                            fontWeight = FontWeight.Bold,
                            color = if (state.balance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                    }
                }
            }

            // Category analysis
            if (state.transactions.isNotEmpty()) {
                // Income categories
                if (state.incomeByCategory.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Distribusi Pemasukan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        items(state.incomeByCategory.entries.toList().sortedByDescending { it.value }) { entry ->
                            val percentage = if (state.totalIncome > 0)
                                (entry.value / state.totalIncome * 100).toInt()
                            else 0

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = entry.key,
                                    modifier = Modifier.width(100.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(24.dp)
                                        .background(
                                            color = Color.LightGray,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(percentage / 100f)
                                            .background(
                                                color = Color(0xFF4CAF50),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                    )
                                }
                                Text(
                                    text = currencyFormatter.format(entry.value),
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.width(100.dp)
                                )
                            }
                        }
                    }
                }

                // Expense categories
                if (state.expenseByCategory.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Distribusi Pengeluaran",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        items(state.expenseByCategory.entries.toList().sortedByDescending { it.value }) { entry ->
                            val percentage = if (state.totalExpense > 0)
                                (entry.value / state.totalExpense * 100).toInt()
                            else 0

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = entry.key,
                                    modifier = Modifier.width(100.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(24.dp)
                                        .background(
                                            color = Color.LightGray,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(percentage / 100f)
                                            .background(
                                                color = Color(0xFFF44336),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                    )
                                }
                                Text(
                                    text = currencyFormatter.format(entry.value),
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.width(100.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tambahkan transaksi untuk melihat analisis",
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf(
        Triple("Beranda", Icons.Default.Home, "home"),
        Triple("Transaksi", Icons.Default.List, "transactions"),
        Triple("Analisis", Icons.Default.BarChart, "analysis")
    )

    NavigationBar {
        items.forEachIndexed { index, (title, icon, route) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = title) },
                label = { Text(title) },
                selected = selectedItem == index,
                onClick = {
                    selectedItem = index
                    navController.navigate(route) {
                        popUpTo("home") {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}