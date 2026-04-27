# 🕒 TimeDrop

TimeDrop es una aplicación de productividad integral y moderna para Android, diseñada para ayudarte a gestionar tu tiempo, mejorar tu concentración y organizar tus tareas diarias de forma eficiente, todo con una interfaz de usuario fluida y estética.

## ✨ Características Principales

- **Reloj Mundial**: Visualiza la hora de diferentes ciudades alrededor del mundo sobre un mapa interactivo premium con marcadores dinámicos.
- **Temporizador Pomodoro**: Aumenta tu productividad utilizando la técnica Pomodoro, con periodos ajustables de trabajo y descanso.
- **Cronómetro**: Mide el tiempo con alta precisión y lleva un registro de tu historial.
- **Notas Integradas**: Un espacio rápido y seguro para capturar tus ideas, tareas y recordatorios.
- **Calendario**: Organiza y planifica tus eventos diarios con facilidad.
- **Sonidos Ambientales**: Mejora tu concentración o relájate reproduciendo sonidos de fondo integrados (lluvia, bosque, olas).
- **Seguridad y Privacidad Biometrica**: Protege tu información personal, notas y calendario mediante autenticación biométrica (huella dactilar o reconocimiento facial).
- **Temas Dinámicos**: Diseño adaptativo con soporte para Modo Oscuro y Claro.
- **Widgets de Pantalla de Inicio**: Accede a las funciones más importantes directamente desde el inicio de tu dispositivo.

## 🚀 Tecnologías y Arquitectura

El proyecto está desarrollado utilizando las últimas tecnologías y buenas prácticas recomendadas para Android:

- **Lenguaje**: Kotlin
- **Interfaz de Usuario (UI)**: Jetpack Compose (Moderno, reactivo y con animaciones fluidas)
- **Arquitectura**: MVVM (Model-View-ViewModel) para una clara separación de responsabilidades.
- **Base de Datos Local**: Room Database para almacenar notas, eventos y usuarios.
- **Preferencias**: Preferences DataStore para configuraciones y estado de la app.
- **Navegación**: Jetpack Navigation Compose para el manejo de pantallas.
- **Seguridad**: Autenticación Biométrica mediante AndroidX Biometrics.

## 📱 Cómo usar la aplicación

1. **Autenticación**: Al iniciar, puedes registrarte o iniciar sesión. Te recomendamos activar el bloqueo biométrico en los Ajustes para proteger tu información.
2. **Navegación Fluida**: Usa la barra de navegación inferior para cambiar rápidamente entre el Home, Calendario, Pomodoro, Cronómetro y Música.
3. **Modo Concentración (Pomodoro + Sonidos)**: Dirígete a la pestaña Pomodoro, inicia el temporizador y activa los sonidos de lluvia o bosque desde el reproductor integrado para entrar en "Modo de Flujo".
4. **Organización**: Usa la sección de Notas y Calendario para mantener tu día estructurado. Puedes agregar, editar y eliminar elementos fácilmente.
5. **Personalización**: En la vista de Perfil y Ajustes, puedes cambiar el tema de la aplicación o gestionar qué datos deseas mantener privados.

## 🛠 Instalación y Configuración (Para Desarrolladores)

1. Clona este repositorio en tu máquina local:
   ```bash
   git clone https://github.com/lightKic/Timedrop.git
   ```
2. Abre el proyecto utilizando **Android Studio**.
3. Espera a que Gradle sincronice todas las dependencias del proyecto.
4. Conecta un dispositivo físico Android o inicia un Emulador (Se recomienda API nivel 26 o superior).
5. Haz clic en **Run 'app'** o presiona `Shift + F10` para compilar e instalar la aplicación en el dispositivo.
