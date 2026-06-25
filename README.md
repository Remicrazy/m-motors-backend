# M-Motors — Backend API

API REST développée avec **Spring Boot 3.4 (Java 21)** pour la plateforme M-Motors de vente et location longue durée de véhicules d'occasion.

## Stack technique

| Technologie | Usage |
|---|---|
| Java 21 + Spring Boot 3.4 | Framework backend |
| Spring Security + JWT (JJWT) | Authentification stateless |
| Spring Data JPA + Hibernate | Accès base de données |
| PostgreSQL | Base de données relationnelle |
| Cloudinary | Stockage des pièces justificatives |
| SpringDoc OpenAPI (Swagger) | Documentation API auto-générée |
| JUnit 5 + Mockito | Tests unitaires (≥80% couverture) |
| Railway | Hébergement cloud |

## Liens

| Description | Lien |
|---|---|
| Repository Git (backend) | https://github.com/Remicrazy/m-motors-backend |
| Repository Git (frontend) | https://github.com/Remicrazy/m-motors-frontend |
| Application déployée | *(URL Railway après déploiement)* |
| Swagger API docs | `{BASE_URL}/api/docs` |
| Login admin | admin@mmotors.fr / Admin123! |
| Login client test | client@test.fr / Client123! |

## Architecture

```
src/main/java/com/mmotors/
├── MMotorsApplication.java       # Point d'entrée
├── config/
│   ├── SecurityConfig.java       # Spring Security + CORS + JWT filter
│   └── CloudinaryConfig.java     # Configuration upload fichiers
├── entity/                       # Entités JPA (User, Vehicle, Dossier, Document, LldOption)
├── repository/                   # Interfaces Spring Data JPA
├── service/                      # Logique métier (AuthService, VehicleService, DossierService, DocumentService)
├── controller/                   # Endpoints REST
├── security/                     # JwtService + JwtAuthFilter + UserDetailsServiceImpl
├── dto/                          # Objets de transfert (RegisterRequest, LoginRequest, AuthResponse)
└── exception/                    # Gestion centralisée des erreurs (GlobalExceptionHandler)
```

## Endpoints principaux

| Méthode | URL | Accès | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Créer un compte client |
| POST | `/api/auth/login` | Public | Connexion + token JWT |
| GET | `/api/auth/me` | Connecté | Profil utilisateur |
| GET | `/api/vehicles` | Public | Liste véhicules + filtres |
| GET | `/api/vehicles/{id}` | Public | Fiche détaillée |
| POST | `/api/vehicles` | Admin | Ajouter un véhicule |
| PATCH | `/api/vehicles/{id}/toggle-type` | Admin | Basculer achat ↔ location |
| POST | `/api/dossiers` | Client | Déposer un dossier |
| GET | `/api/dossiers/mes-dossiers` | Client | Mes dossiers |
| GET | `/api/dossiers` | Admin | Tous les dossiers |
| PATCH | `/api/dossiers/{id}/statut` | Admin | Valider / refuser |
| POST | `/api/documents/upload/{id}` | Client | Upload pièce justificative |

## Lancer en local

### Prérequis
- Java 21
- Maven 4+
- PostgreSQL 15+ (base `mmotors` créée)

### Configuration locale

Créer `src/main/resources/application-local.properties` (gitignore) :

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mmotors
spring.datasource.username=postgres
spring.datasource.password=votre_mot_de_passe

cloudinary.cloud-name=votre_cloud_name
cloudinary.api-key=votre_api_key
cloudinary.api-secret=votre_api_secret
```

### Démarrer

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

L'API sera disponible sur `http://localhost:8080`  
Swagger UI : `http://localhost:8080/api/docs`

## Variables d'environnement Railway (production)

| Variable | Description |
|---|---|
| `DB_HOST` | Hôte PostgreSQL Railway |
| `DB_PORT` | Port PostgreSQL (5432) |
| `DB_NAME` | Nom de la base |
| `DB_USERNAME` | Utilisateur DB |
| `DB_PASSWORD` | Mot de passe DB |
| `JWT_SECRET` | Clé secrète JWT (min. 32 caractères) |
| `CLOUDINARY_CLOUD_NAME` | Cloud name Cloudinary |
| `CLOUDINARY_API_KEY` | API key Cloudinary |
| `CLOUDINARY_API_SECRET` | API secret Cloudinary |
| `FRONTEND_URL` | URL du frontend Vercel (CORS) |

## Tests

```bash
mvn test
```

Couverture ciblée : **≥ 80%** (JUnit 5 + Mockito + H2 in-memory)
