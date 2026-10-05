<div align="center">

# 🩺 CuidApp: Tu Asistente de Salud Integral 💙

[![Android Studio](https://img.shields.io/badge/Android%20Studio-3DDC84.svg?style=for-the-badge&logo=android-studio&logoColor=white)](#)
[![Java](https://img.shields.io/badge/Java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)](#)
[![Status](https://img.shields.io/badge/Status-Completado-success?style=for-the-badge)](#)

*Una aplicación médica diseñada para centralizar la información del paciente, facilitar el acceso a expedientes y monitorear la salud cardíaca en tiempo real.*

</div>

---

## 📌 Resumen del Proyecto

**CuidApp** es una solución móvil nativa para Android construida con arquitectura plana (Java/XML). Permite a los usuarios navegar por diversas herramientas médicas de manera fluida, contactar a profesionales, acceder a mapas de clínicas y mantener un monitoreo activo (simulado en segundo plano) de su ritmo cardíaco.

### ⚙️ Entorno de Desarrollo
- **Versión de Android SDK (Min / Target):** API 24 / API 34 *(Ajustar según tu build.gradle)*
- **Android Gradle Plugin (AGP):** 8.x.x *(Ajustar según tu build.gradle)*
- **Lenguaje Principal:** Java

---

## 🚀 Navegación y Lógica (Los 8 Intents Implementados)

El núcleo de la aplicación utiliza **8 Intents** (3 explícitos y 5 implícitos) para asegurar una integración nativa profunda con el sistema operativo Android.

### 🎯 3 Intents Explícitos (Navegación Interna y Servicios)
| Intent | Origen | Destino / Acción | Propósito |
| :--- | :--- | :--- | :--- |
| **1** | `MenuPrincipalActivity` | *Múltiples Activities* | Conectar la pestaña principal con el resto de las vistas (Dashboard, Expedientes, Ficha). |
| **2** | *Cualquier Activity* | `MenuPrincipalActivity` | Botón global de retorno al menú de inicio. |
| **3** | `DashboardActivity` | `ServicioMonitoreo.java` | Iniciar el *Background Service* que aloja el Thread de monitoreo cardíaco. |

### 🌐 5 Intents Implícitos (Acciones Nativas del SO)
| Intent | Ubicación Principal | Acción (`Intent.ACTION_...`) | Funcionalidad |
| :--- | :--- | :--- | :--- |
| **4** | `MenuPrincipalActivity` | `ACTION_VIEW` | Abrir Mapas para localizar clínicas u hospitales cercanos. |
| **5** | `MenuPrincipalActivity` | `ACTION_VIEW` | Abrir el Navegador Web para buscar información de salud. |
| **6** | `FichaPacienteActivity` | `ACTION_DIAL` | Realizar llamadas de emergencia a médicos registrados. |
| **7** | `FichaPacienteActivity` | `ACTION_SENDTO` | Enviar correos electrónicos con consultas médicas. |
| **8** | `FichaPacienteActivity` | `ACTION_IMAGE_CAPTURE` | Abrir la cámara para adjuntar fotos a la ficha del paciente. |

> ⚠️ **Nota Técnica:** Se ha implementado validación y manejo de errores (`resolveActivity`) para evitar cierres inesperados en caso de que el dispositivo no cuente con una aplicación capaz de resolver los intents implícitos. Además, se gestionan los permisos de cámara y ubicación (Manifest y Runtime).

---

## 📸 Capturas de Pantalla (UI/UX)

A continuación se muestra la interfaz de usuario, la cual integra diseño intuitivo y recursos centralizados en `strings.xml` (incluyendo emojis 🩺 y 📈).

<div align="center">
  
| 🏠 Menú Inicial | 👤 Ficha del Paciente |
| :---: | :---: |
| <img src="URL_IMAGEN_1" width="250" alt="Captura Menu Principal"> | <img src="URL_IMAGEN_2" width="250" alt="Captura Ficha Paciente"> |

| 📁 Mis Expedientes | 📈 Resumen de Salud |
| :---: | :---: |
| <img src="URL_IMAGEN_3" width="250" alt="Captura Mis Expedientes"> | <img src="URL_IMAGEN_4" width="250" alt="Captura Dashboard"> |

*(Remplaza `URL_IMAGEN_X` con la ruta de tus capturas alojadas en la carpeta de github, ej: `./screenshots/menu.png`)*
</div>

---

## 🧠 Características Técnicas Destacadas

*   **Multithreading & Servicios:** La vista de *Monitoreo Diario* (`DashboardActivity`) lanza un servicio interno. Este servicio ejecuta un **Thread** en segundo plano que simula el procesamiento de un sensor biométrico para detectar episodios de taquicardia, sin bloquear el hilo principal (UI Thread).
*   **Gestión de Recursos Centralizada:** Los textos, iconografía médica e indentación visual (colores de paleta institucional) están definidos rígidamente en `res/values/strings.xml`, `colors.xml` y la carpeta `drawable/`.
*   **Prevención de Errores (Criterio 2):** Se verifica si los datos están nulos antes de enviarlos.

---

## 📦 Ubicación del Ejecutable

Para los evaluadores y profesores, el archivo binario pre-compilado para pruebas se encuentra en la siguiente ruta dentro de este repositorio:

📂 **APK Debug:** `app/build/outputs/apk/debug/app-debug.apk`

---

## 🌳 Flujo de Trabajo (GitFlow Simplificado)

El desarrollo se gestionó respetando los lineamientos de la Carta Gantt:
1. Rama principal: `main` (Versiones estables)
2. Rama de desarrollo: `feature/intents` (Implementación de Intents e interfaces).

---
<div align="center">
  <i>Desarrollado para el Proyecto de Arquitectura Android - CuidApp</i> 🏥
</div>