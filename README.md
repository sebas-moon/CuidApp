<div align="center">

# 🩺 CuidApp: Tu Asistente de Salud Integral 💙

[![Android Studio](https://img.shields.io/badge/Android%20Studio-3DDC84.svg?style=for-the-badge&logo=android-studio&logoColor=white)](#)
[![Java](https://img.shields.io/badge/Java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)](#)
[![Status](https://img.shields.io/badge/Status-Completado-success?style=for-the-badge)](#)

*Una aplicación médica diseñada para centralizar la información del paciente, facilitar el acceso a expedientes y monitorear la salud en tiempo real.*

</div>

---

## 📌 Resumen del Proyecto

**CuidApp** es una solución móvil nativa para Android construida con arquitectura plana (Java/XML, sin MVC/MVVM). Consta de **4 Activities** y **1 Service**. Permite navegar por distintas herramientas médicas, contactar a un cuidador o servicio de emergencia, ubicar hospitales en el mapa y mantener un monitoreo activo del paciente.

### ⚙️ Entorno de Desarrollo
- **Android SDK (Min / Target / Compile):** API 31 (Android 12) / API 36 (Android 16) / API 36
- **Lenguaje:** Java
- **Interfaz:** XML con Material Components (tema oscuro Material 3)
- **Paquete:** `com.santo_tomas.cuidapp`

### 🗂️ Estructura de pantallas

| Componente | Archivo | Función |
| :--- | :--- | :--- |
| Activity (Launcher) | `MenuPrincipalActivity` | Menú principal, mapas y navegador |
| Activity | `FichaPacienteActivity` | Datos del paciente, llamada, correo y cámara |
| Activity | `MisExpedientesActivity` | Expedientes médicos (selector de PDF) |
| Activity | `DashboardActivity` | Resumen de salud, BPM en vivo y alertas de taquicardia |
| Activity | `BaseActivity` | Clase base de las 4 pantallas |
| Service | `ServicioMonitoreo` | Hilo que simula el sensor cardíaco |
| Utilidad | `InsetsUtil` | Evita que el contenido quede bajo las barras del sistema (edge-to-edge) |

---

## 🚀 Navegación y Lógica (Los 8 Intents Implementados)

El núcleo de la aplicación utiliza **8 Intents** (3 explícitos y 5 implícitos). Cada uno está señalado con un comentario `INTENT #n` en el código fuente.

### 🎯 3 Intents Explícitos (Navegación Interna y Servicios)

| # | Origen | Destino | Método en el código | Propósito |
| :---: | :--- | :--- | :--- | :--- |
| **1** | `MenuPrincipalActivity` | `FichaPacienteActivity`, `MisExpedientesActivity`, `DashboardActivity` | `navegarA()` | Navegación desde el menú hacia el resto de las vistas. |
| **2** | `FichaPacienteActivity`, `MisExpedientesActivity`, `DashboardActivity` | `MenuPrincipalActivity` | `volverAlMenu()` | Botón de retorno global. Usa `CLEAR_TOP` + `SINGLE_TOP` para no duplicar pantallas en la pila. |
| **3** | `DashboardActivity` | `ServicioMonitoreo` | `iniciarServicioMonitoreo()` | Inicia el *Service* que aloja el Thread de monitoreo cardíaco. |

### 🌐 5 Intents Implícitos (Acciones Nativas del SO)

| # | Ubicación | Acción | Método | Funcionalidad |
| :---: | :--- | :--- | :--- | :--- |
| **4** | `FichaPacienteActivity` | `ACTION_DIAL` | `llamarEmergencia()` | Abre el marcador con el número de emergencia (131). |
| **5** | `FichaPacienteActivity` | `ACTION_SENDTO` (`mailto:`) | `enviarCorreo()` | Abre el cliente de correo para escribir al cuidador. |
| **6** | `FichaPacienteActivity` | `ACTION_IMAGE_CAPTURE` | `abrirCamara()` | Abre la cámara para tomar la foto de perfil. |
| **7** | `MenuPrincipalActivity` | `ACTION_VIEW` (`geo:`) | `abrirMapaHospitales()` | Abre la app de mapas buscando hospitales cercanos. |
| **8** | `MenuPrincipalActivity` | `ACTION_VIEW` (`https:`) | `abrirPortalMinsal()` | Abre el navegador en el portal del Ministerio de Salud. |

### ➕ Funcionalidades extra (fuera de los 8 intents de la rúbrica)

Estas dos funciones usan intents implícitos adicionales y están marcadas como `EXTRA` en el código:

| Ubicación | Acción | Funcionalidad |
| :--- | :--- | :--- |
| `DashboardActivity` | `ACTION_SEND` | Compartir el reporte de salud como texto. |
| `MisExpedientesActivity` | `ACTION_GET_CONTENT` | Seleccionar un PDF desde el explorador de archivos y mostrar su nombre. |

---

## 🛡️ Validaciones y Manejo de Errores (Criterio 2)

| Riesgo | Cómo se previene |
| :--- | :--- |
| No existe una app que atienda el intent implícito (ej. sin cliente de correo) | Cada intent implícito se lanza dentro de `try/catch (ActivityNotFoundException)` y se muestra un aviso al usuario. En el Manifest se declara `<queries>` para que Android 11+ permita resolver esas apps. |
| Vistas nulas (ID inexistente en el layout) | Se valida `findViewById() != null` antes de asignar listeners. |
| Intents o extras nulos en el `BroadcastReceiver` | Se comprueba `intent`, `getAction()` y el valor del extra antes de usarlos. |
| Dispositivo sin cámara | Se verifica `FEATURE_CAMERA_ANY` antes de abrir la cámara. |
| Cámara sin permiso concedido | Se solicita en tiempo de ejecución (Activity Result API). Si se deniega, se informa y la app no se cierra. |
| Ubicación sin permiso concedido | Se solicita en tiempo de ejecución (precisa y aproximada). Si se deniega, el mapa se abre igualmente, sin usar la posición. |
| Archivo de expediente ilegible | Se valida el resultado del selector y se muestra un aviso si no se puede leer el nombre. |

### 🔐 Permisos declarados en el Manifest

| Permiso | Uso | ¿En tiempo de ejecución? |
| :--- | :--- | :---: |
| `CAMERA` | Foto de perfil del paciente | ✅ Sí |
| `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` | Centrar la búsqueda de hospitales | ✅ Sí |
| `INTERNET` | Abrir el Portal Minsal | No (permiso normal) |

> 🔒 `allowBackup` está en `false` porque la aplicación maneja datos de salud.

---

## 📸 Capturas de Pantalla (UI/UX)

La interfaz usa un tema oscuro con recursos centralizados en `strings.xml` (textos con emojis 🩺 y 📈) y `colors.xml` (paleta de colores).

<div align="center">

| 🏠 Menú Inicial | 👤 Ficha del Paciente |
| :---: | :---: |
| <img src="URL_IMAGEN_1" width="250" alt="Captura Menu Principal"> | <img src="URL_IMAGEN_2" width="250" alt="Captura Ficha Paciente"> |

| 🩺 Mis Expedientes | 📈 Resumen de Salud |
| :---: | :---: |
| <img src="URL_IMAGEN_3" width="250" alt="Captura Mis Expedientes"> | <img src="URL_IMAGEN_4" width="250" alt="Captura Dashboard"> |

</div>

---

## 🧠 Características Técnicas Destacadas

*   **Multithreading y Servicios:** `DashboardActivity` inicia `ServicioMonitoreo`, que ejecuta un **Thread** propio. Cada 3 segundos genera un valor de BPM aleatorio (60 a 130) y, si supera los 100, simula una **taquicardia**. El hilo evita bloquear el hilo principal (UI Thread).
*   **Comunicación Service → Activity:** el servicio envía *Broadcasts* restringidos a la propia app (`setPackage`). La Activity los recibe con un `BroadcastReceiver` registrado con `RECEIVER_NOT_EXPORTED`, muestra el BPM en vivo y abre **un único** diálogo de alerta (se actualiza en vez de apilar uno nuevo cada 3 s).
*   **Ciclo de vida seguro:** el hilo se detiene con `interrupt()` y una bandera `volatile` al cerrar el Dashboard. El receptor se registra en `onResume()` y se desregistra en `onPause()`.
*   **Compatibilidad con Android 15/16:** como `targetSdk` es 36, las pantallas se dibujan de borde a borde. `InsetsUtil` aplica el espacio de las barras del sistema para que nada quede tapado.
*   **Gestión de Recursos Centralizada:** todos los textos (incluidos mensajes de error, títulos de alerta y las URI de los intents) están en `res/values/strings.xml`. Los colores están en `colors.xml` y los íconos en `res/drawable/`. No hay textos escritos directamente en el código Java.

---

## 📦 Ubicación del Ejecutable

El archivo binario pre-compilado para pruebas se encuentra en:

📂 **APK Debug:** `release/app-debug.apk`

---

## 🌳 Flujo de Trabajo (GitFlow Simplificado)

El desarrollo se gestionó respetando los lineamientos de la Carta Gantt:
1. Rama principal: `main` (versiones estables).
2. Rama de desarrollo: `feature/intents` (implementación de Intents, permisos e interfaces).

---
<div align="center">
  <i>Desarrollado para el Proyecto de Arquitectura Android - CuidApp</i> 🏥
</div>
