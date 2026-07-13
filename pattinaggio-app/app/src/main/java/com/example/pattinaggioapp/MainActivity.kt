package com.example.pattinaggioapp

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// --- 1. ROOM DATABASE (Entità, DAO, Database) ---

@Entity(tableName = "esibizioni")
data class Esibizione(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nomePattinatore: String,
    val societa: String,
    val categoria: String,
    val punteggioTecnico: Double,
    val punteggioArtistico: Double
) {
    val punteggioTotale: Double
        get() = punteggioTecnico + punteggioArtistico
}

@Dao
interface EsibizioneDao {
    // Ottieni tutte le esibizioni di una specifica categoria, ordinate per punteggio totale decrescente
    @Query("SELECT * FROM esibizioni WHERE categoria = :categoria ORDER BY (punteggioTecnico + punteggioArtistico) DESC")
    fun getClassificaPerCategoria(categoria: String): Flow<List<Esibizione>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserisciEsibizione(esibizione: Esibizione)

    @Query("SELECT DISTINCT categoria FROM esibizioni ORDER BY categoria ASC")
    fun getCategorie(): Flow<List<String>>
}

@Database(entities = [Esibizione::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun esibizioneDao(): EsibizioneDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pattinaggio_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// --- 2. VIEWMODEL ---

class PattinaggioViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).esibizioneDao()

    // Categoria attualmente selezionata per la visualizzazione della classifica
    private val _categoriaSelezionata = MutableStateFlow("Singolo Femminile")
    val categoriaSelezionata: StateFlow<String> = _categoriaSelezionata.asStateFlow()

    // Flusso della classifica in base alla categoria selezionata
    var classificaCorrente: Flow<List<Esibizione>> = dao.getClassificaPerCategoria(_categoriaSelezionata.value)
        private set

    // Flusso delle categorie esistenti nel DB
    val categorieDisponibili: Flow<List<String>> = dao.getCategorie()

    fun impostaCategoria(categoria: String) {
        _categoriaSelezionata.value = categoria
        classificaCorrente = dao.getClassificaPerCategoria(categoria)
    }

    fun aggiungiEsibizione(
        nome: String, societa: String, categoria: String,
        tecStr: String, artStr: String
    ) {
        val tec = tecStr.toDoubleOrNull() ?: 0.0
        val art = artStr.toDoubleOrNull() ?: 0.0

        if (nome.isNotBlank() && categoria.isNotBlank()) {
            viewModelScope.launch {
                dao.inserisciEsibizione(
                    Esibizione(
                        nomePattinatore = nome,
                        societa = societa,
                        categoria = categoria,
                        punteggioTecnico = tec,
                        punteggioArtistico = art
                    )
                )
            }
        }
    }
}

// --- 3. ACTIVITY PRINCIPALE ---

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppPattinaggioCompleta()
                }
            }
        }
    }
}

// --- 4. INTERFACCIA UTENTE (COMPOSE) ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPattinaggioCompleta(viewModel: PattinaggioViewModel = viewModel()) {
    var nome by remember { mutableStateOf("") }
    var societa by remember { mutableStateOf("") }
    var categoriaInput by remember { mutableStateOf("Singolo Femminile") }
    var tecStr by remember { mutableStateOf("") }
    var artStr by remember { mutableStateOf("") }

    // Osserva la classifica
    val classifica by viewModel.classificaCorrente.collectAsState(initial = emptyList())
    // Osserva la categoria selezionata
    val catSelezionata by viewModel.categoriaSelezionata.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Gara Pattinaggio Artistico",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- FORM DI INSERIMENTO ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Nuova Esibizione", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = nome, onValueChange = { nome = it },
                    label = { Text("Nome / Coppia / Gruppo") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = societa, onValueChange = { societa = it },
                    label = { Text("Società") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = categoriaInput, onValueChange = { categoriaInput = it },
                    label = { Text("Categoria (es. Singolo, Coppia, Quartetti)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tecStr, onValueChange = { tecStr = it },
                        label = { Text("Tecnico") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = artStr, onValueChange = { artStr = it },
                        label = { Text("Artistico") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.aggiungiEsibizione(nome, societa, categoriaInput, tecStr, artStr)
                        // Auto-seleziona la categoria appena inserita per vederla in classifica
                        viewModel.impostaCategoria(categoriaInput)

                        // Resetta alcuni campi
                        nome = ""
                        tecStr = ""
                        artStr = ""
                        // Mantieni societa e categoriaInput per comodità se si inseriscono atleti della stessa squadra
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Salva ed Inserisci in Classifica")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- ZONA CLASSIFICA ---
        Text(text = "Classifica Categoria:", style = MaterialTheme.typography.titleLarge)

        // Campo per filtrare la classifica per categoria
        OutlinedTextField(
            value = catSelezionata,
            onValueChange = {
                viewModel.impostaCategoria(it)
            },
            label = { Text("Filtra per Categoria") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            itemsIndexed(classifica) { index, esibizione ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (index == 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${index + 1}° - ${esibizione.nomePattinatore}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = String.format("%.2f", esibizione.punteggioTotale),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "Società: ${esibizione.societa}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Tec: ${esibizione.punteggioTecnico}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "Art: ${esibizione.punteggioArtistico}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
