package org.banana.project.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.banana.project.R
import org.banana.project.navigation.SaleCreationScreen
import org.banana.project.presentation.login.LoginEvent
import org.banana.project.presentation.login.LoginViewModel
import org.banana.project.ui.components.RetroCard
import org.banana.project.ui.theme.Outfit
import org.banana.project.ui.theme.TechniColors

class LoginScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel: LoginViewModel = hiltViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val navigator = LocalNavigator.currentOrThrow
        val scrollState = rememberScrollState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TechniColors.Emerald)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .widthIn(max = 450.dp)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(vertical = 16.dp)
            ) {
                // Ayurami Logo Image - sized larger for retro brand presence
                Image(
                    painter = painterResource(id = R.drawable.ayurami_logo),
                    contentDescription = "Ayurami Logo",
                    modifier = Modifier
                        .height(160.dp)
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    contentScale = ContentScale.Fit
                )

                RetroCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surface, // Cream
                    borderColor = MaterialTheme.colorScheme.outline      // GoldenTitle Yellow
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ACCESO AL SISTEMA",
                            color = TechniColors.Crimson,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        if (!state.errorMessage.isNullOrEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(TechniColors.Crimson, RoundedCornerShape(8.dp))
                                    .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "⚠️", fontSize = 20.sp, modifier = Modifier.padding(end = 8.dp))
                                Text(
                                    text = state.errorMessage!!,
                                    color = TechniColors.Cream,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Usuario input
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Usuario",
                                color = TechniColors.Crimson,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            RetroTextField(
                                value = state.username,
                                onValueChange = { viewModel.onEvent(LoginEvent.UsernameChanged(it)) },
                                placeholder = "ej. admin",
                                enabled = !state.isLoading
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Contraseña input
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Contraseña",
                                color = TechniColors.Crimson,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            RetroTextField(
                                value = state.password,
                                onValueChange = { viewModel.onEvent(LoginEvent.PasswordChanged(it)) },
                                placeholder = "••••••••",
                                visualTransformation = PasswordVisualTransformation(),
                                enabled = !state.isLoading
                            )
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // Retro Styled shadow button
                        val buttonColor = if (state.isLoading) {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.outline
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .padding(bottom = 4.dp, end = 4.dp) // padding so shadow isn't clipped
                                .drawBehind {
                                    drawRoundRect(
                                        color = Color.Black,
                                        topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                                        size = this.size,
                                        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                                    )
                                }
                                .border(
                                    width = 4.dp,
                                    color = Color.Black,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    color = buttonColor,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable(enabled = !state.isLoading) {
                                    viewModel.onEvent(LoginEvent.Submit(onSuccess = {
                                        navigator.replaceAll(SaleCreationScreen())
                                    }))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (state.isLoading) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        color = TechniColors.Crimson,
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 3.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "INICIANDO SESIÓN...",
                                        color = TechniColors.Crimson,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp
                                    )
                                }
                            } else {
                                Text(
                                    text = "ENTRAR",
                                    color = TechniColors.Crimson,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Footer
                Text(
                    text = "Ayurami mobile app",
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "hecho en colombia",
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun RetroTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    enabled: Boolean = true
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        textStyle = androidx.compose.ui.text.TextStyle(
            color = Color.Black,
            fontFamily = Outfit,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        ),
        visualTransformation = visualTransformation,
        singleLine = true,
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(4.dp, Color.Black, RoundedCornerShape(16.dp))
                    .background(Color(0xFFFFFDD0), RoundedCornerShape(16.dp)) // Cream
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = TechniColors.Crimson.copy(alpha = 0.5f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
                innerTextField()
            }
        }
    )
}
