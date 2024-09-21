package br.edu.utfpr.appcontatos.ui.contact.details

import android.graphics.drawable.Icon
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.edu.utfpr.appcontatos.R
import br.edu.utfpr.appcontatos.data.Contact
import br.edu.utfpr.appcontatos.data.ContactDatasource
import br.edu.utfpr.appcontatos.data.groupByInitial
import br.edu.utfpr.appcontatos.ui.theme.AppContatosTheme
import br.edu.utfpr.appcontatos.ui.utils.composables.ContactAvatar
import br.edu.utfpr.appcontatos.ui.utils.composables.DefaultErrorContent
import br.edu.utfpr.appcontatos.ui.utils.composables.DefaultLoadingContent
import br.edu.utfpr.appcontatos.ui.utils.composables.FavoriteIconButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@Composable
fun ContactDetailsScreen(
    modifier: Modifier = Modifier,
    contactId: Int,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    onBackPressed: () -> Unit,
    onEditPressed: () -> Unit,
    onContactDeleted: () -> Unit
) {
    var isInitialComposition: Boolean by rememberSaveable {
        mutableStateOf(true)
    }
    var uiState: ContactDetailsUiState by remember {
        mutableStateOf(ContactDetailsUiState())
    }

    val loadContact: () -> Unit = {
        uiState = uiState.copy(
            isLoading = true,
            hasErrorLoading = false
        )
        coroutineScope.launch {
            delay(2000)
            val contact = ContactDatasource.instance.findById(contactId)
            uiState = if (contact == null) {
                uiState.copy(
                    isLoading = false,
                    hasErrorLoading = true
                )
            } else {
                uiState.copy(
                    isLoading = false,
                    contact = contact
                )
            }
        }
    }

    if (isInitialComposition) {
        loadContact()
        isInitialComposition = false
    }

    if (uiState.showConfirmationDialog) {
        ConfirmationDialog(
            content = "Esse contato será removido permanentemente",
            onDismiss = {
                uiState = uiState.copy( showConfirmationDialog = false )
            },
            onConfirm = {
                uiState = uiState.copy( showConfirmationDialog = false )
                ContactDatasource.instance.delete(uiState.contact)
                onContactDeleted()
            }
        )
    }

    val contentModifier: Modifier = modifier.fillMaxSize()
    if (uiState.isLoading) {
        DefaultLoadingContent(modifier = contentModifier)
    } else if (uiState.hasErrorLoading) {
        DefaultErrorContent(
            modifier = contentModifier,
            onTryAgainPressed = {}
        )
    } else {
        Scaffold(
            modifier = contentModifier,
            topBar = {
                AppBar(
                    isDeleting = false,
                    contact = uiState.contact,
                    onBackPressed = onBackPressed,
                    onDeletePressed = {
                        uiState = uiState.copy(
                            showConfirmationDialog = true
                        )
                    },
                    onEditPressed = onEditPressed,
                    onFavoritePressed = {
                        val updatedContact = uiState.contact.copy(
                            isFavorite=!uiState.contact.isFavorite
                        )
                        uiState = uiState.copy(contact = ContactDatasource.instance.save(updatedContact))
                    }
                )
            }
        ) { paddingValues ->
            ContactDetails(
                modifier = Modifier.padding(paddingValues),
                contact = uiState.contact,
                isDeleting = false,
                onEditPressed = onEditPressed
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppBar(
    modifier: Modifier = Modifier,
    isDeleting: Boolean,
    contact: Contact,
    onBackPressed: () -> Unit,
    onDeletePressed: () -> Unit,
    onEditPressed: () -> Unit,
    onFavoritePressed: () -> Unit
) {
    TopAppBar(
        title = { Text("") },
        colors = TopAppBarDefaults.topAppBarColors(
            titleContentColor = MaterialTheme.colorScheme.primary,
            actionIconContentColor = MaterialTheme.colorScheme.primary,
            navigationIconContentColor = MaterialTheme.colorScheme.primary
        ),
        navigationIcon = {
            IconButton(onClick = onBackPressed ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.voltar)
                )
            }
        },
        actions = {
            if (isDeleting) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(60.dp)
                        .padding(all = 16.dp),
                    strokeWidth = 2.dp
                )
            } else {
                IconButton(onClick = onEditPressed ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(R.string.editar)
                    )
                }
                FavoriteIconButton(
                    isFavorite = contact.isFavorite,
                    onPressed = onFavoritePressed
                )
                IconButton(onClick = onDeletePressed ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.excluir)
                    )
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun AppBarPreview() {
    AppContatosTheme {
        AppBar(
            isDeleting = false,
            contact = Contact(),
            onBackPressed = {},
            onEditPressed = {},
            onFavoritePressed = {},
            onDeletePressed = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppBarPreviewDeleting() {
    AppContatosTheme {
        AppBar(
            isDeleting = true,
            contact = Contact(),
            onBackPressed = {},
            onEditPressed = {},
            onFavoritePressed = {},
            onDeletePressed = {}
        )
    }
}

@Composable
fun ContactDetails(
    modifier: Modifier = Modifier,
    contact: Contact,
    isDeleting: Boolean,
    onEditPressed: () -> Unit
) {
    Column(
        modifier = modifier
            .padding(top = 24.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ContactAvatar(
            firstName = contact.firstName,
            lastName = contact.lastName,
            size = 150.dp,
            textStyle = MaterialTheme.typography.displayLarge
        )
        Spacer(Modifier.size(24.dp))
        Text(
            text = contact.fullName,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.size(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickAction(
                imageVector = Icons.Filled.Phone,
                text = stringResource(R.string.ligar),
                onPressed = {},
                enabled = contact.phoneNumber.isNotBlank() && !isDeleting
            )
            QuickAction(
                imageVector = Icons.Filled.Sms,
                text = stringResource(R.string.msg),
                onPressed = {},
                enabled = contact.phoneNumber.isNotBlank() && !isDeleting
            )
            QuickAction(
                imageVector = Icons.Filled.Videocam,
                text = stringResource(R.string.v_deo),
                onPressed = {},
                enabled = contact.phoneNumber.isNotBlank() && !isDeleting
            )
            QuickAction(
                imageVector = Icons.Filled.Email,
                text = stringResource(R.string.email),
                onPressed = {},
                enabled = contact.email.isNotBlank() && !isDeleting
            )
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp)
        ) {
            Text(
                modifier = Modifier.padding(all = 16.dp),
                text = "Informações de contato",
                style = MaterialTheme.typography.bodyMedium
            )
            ContactInfo(
                imageVector = Icons.Outlined.Phone,
                value = contact.phoneNumber.ifBlank {
                    "Adicionar numero de telefone"
                },
                enabled = contact.phoneNumber.isBlank() && !isDeleting,
                onPressed = {}
            )
            ContactInfo(
                imageVector = Icons.Outlined.Email,
                value = contact.email.ifBlank {
                    "Adicionar email"
                },
                enabled = contact.email.isBlank() && !isDeleting,
                onPressed = {}
            )
            Spacer(Modifier.size(8.dp))
        }
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        val formattedDateTime = contact.createdAt.format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        )
        Text(
            text = "Adicionado em $formattedDateTime",
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ContactDetailsPreview() {
    AppContatosTheme {
        ContactDetails(
            contact = Contact(
                firstName = "Jorge",
                lastName = "Rodrigues"
            ),
            isDeleting = true,
            onEditPressed = {}
        )
    }
}

@Composable
fun QuickAction(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    text: String,
    onPressed: ()-> Unit,
    enabled: Boolean
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FilledIconButton(onClick = {}) {
            Icon(
                imageVector = imageVector,
                contentDescription = text
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun ContactInfo(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    value: String,
    enabled: Boolean,
    onPressed: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(all = 16.dp)
            .fillMaxWidth()
            .clickable(
                enabled = true,
                onClick = {}
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(

            imageVector = imageVector,
            contentDescription = value
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ConfirmationDialog(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        title = title?.let {
            { Text(it) }
        },
        text = { Text(content)},
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.confirmar))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancelar))
            }
        }
    )
}

@Preview
@Composable
private fun ConfirmationDialogPreview() {
    AppContatosTheme {
        ConfirmationDialog(
            title = "Atenção",
            content = "Testando",
            onConfirm = {},
            onDismiss = {}
        )
    }
}