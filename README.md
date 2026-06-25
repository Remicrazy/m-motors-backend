# M-Motors — Backend API

API REST développée avec **Spring Boot 3.4 (Java 21)** pour la plateforme M-Motors de vente et location longue durée de véhicules d'occasion.

## 🔗 Liens du projet

| Description | Lien / Valeur |
|---|---|
| **Repository Git (backend)** | https://github.com/Remicrazy/m-motors-backend |
| **Repository Git (frontend)** | https://github.com/Remicrazy/m-motors-frontend |
| **Application déployée** | https://m-motors-frontend-nu.vercel.app |
| **API backend** | https://m-motors-backend-production.up.railway.app |
| **Swagger UI** | https://m-motors-backend-production.up.railway.app/api/docs |
| **Login ADMIN** | admin@mmotors.fr / Admin123! |
| **Login CLIENT** | client@test.fr / Client123! |

## Stack technique

| Technologie | Usage |
|---|---|
| Java 21 + Spring Boot 3.4 | Framework backend |
| Spring Security + JWT (JJWT 0.12) | Authentification stateless |
| Spring Data JPA + Hibernate 6 | ORM — accès base de données |
| PostgreSQL 18 | Base de données relationnelle |
| Cloudinary | Stockage des pièces justificatives |
| jackson-datatype-hibernate6 | Sérialisation des proxies Hibernate |
| SpringDoc OpenAPI 2.7 (Swagger) | Documentation API auto-générée |
| JUnit 5 + Mockito | 34 tests unitaires — BUILD SUCCESS |
| Maven 4 | Build + gestion des dépendances |
| Railway | Hébergement cloud (PaaS) |

## Architecture

```
src/main/java/com/mmotors/
├── MMotorsApplication.java
├── config/
│   ├── SecurityConfig.java      # Spring Security, CORS, JWT filter, entry point
│   ├── CloudinaryConfig.java    # Upload fichiers
│   └── DataInitializer.java     # Données de démo au démarrage
├── entity/                      # User, Vehicle, LldOption, Dossier, Document
├── repository/                  # Spring Data JPA interfaces
├── service/                     # AuthService, VehicleService, DossierService, DocumentService
├── controller/                  # Endpoints REST
├── security/                    # JwtService, JwtAuthFilter, UserDetailsServiceImpl
├── dto/                         # RegisterRequest, LoginRequest, AuthResponse
└── exception/                   # GlobalExceptionHandler (404, 409, 403, 401, 400)
```

## Endpoints principaux

| Méthode | URL | Accès | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Créer un compte client |
| POST | `/api/auth/login` | Public | Connexion + token JWT |
| GET | `/api/auth/me` | Connecté | Profil utilisateur |
| GET | `/api/vehicles` | Public | Liste véhicules + filtres (type, marque, prix, km) |
| GET | `/api/vehicles/{id}` | Public | Fiche détaillée + options LLD |
| POST | `/api/vehicles` | ADMIN | Ajouter un véhicule |
| PATCH | `/api/vehicles/{id}` | ADMIN | Modifier un véhicule |
| PATCH | `/api/vehicles/{id}/toggle-type` | ADMIN | Basculer achat ↔ location |
| POST | `/api/dossiers` | CLIENT | Déposer un dossier (achat ou LLD) |
| GET | `/api/dossiers/mes-dossiers` | CLIENT | Mes dossiers + statuts |
| GET | `/api/dossiers/{id}` | Connecté | Détail d'un dossier |
| GET | `/api/dossiers` | ADMIN | Tous les dossiers |
| PATCH | `/api/dossiers/{id}/statut` | ADMIN | Valider / refuser avec motif |
| POST | `/api/documents/upload/{id}` | CLIENT | Upload pièce justificative (PDF/JPG/PNG, 5Mo max) |

## Sécurité

- **JWT stateless** — token signé HMAC-SHA256, expiration 15 min
- **BCrypt strength 12** — hachage des mots de passe
- **RBAC** — `ROLE_CLIENT` / `ROLE_ADMIN` via Spring Security
- **CORS** — origines limitées à Vercel uniquement
- **Validation** — `@Valid` + Bean Validation sur tous les DTOs
- **Variables sensibles** — jamais committées, via Railway env vars

## Données de démo (DataInitializer)

Au démarrage, l'application crée automatiquement si inexistants :

```
Comptes : admin@mmotors.fr / Admin123!  (ADMIN)
          client@test.fr  / Client123!  (CLIENT)

Véhicules : Renault Clio V    → Achat    13 500€
            Peugeot 308       → Achat    16 900€
            Volkswagen Golf   → Achat    24 500€
            Toyota Yaris      → LLD      320€/mois
            Renault Megane    → LLD      450€/mois
```

## Tests

**34 tests — 0 échec — BUILD SUCCESS**

| Fichier | Tests | Contenu |
|---|---|---|
| `AuthServiceTest` | 5 | Inscription, login, BCrypt, erreurs |
| `AuthControllerTest` | 9 | Endpoints REST, JWT, RBAC, HTTP codes |
| `DossierServiceTest` | 11 | Création, statuts, accès, règles métier |
| `VehicleServiceTest` | 9 | Filtres, bascule type, blocage dossier actif |

```bash
mvn test -Dspring.profiles.active=test
```

Base de test : **H2 in-memory** (`application-test.properties`)

## Lancer en local

### Prérequis
- Java 21, Maven 4+, PostgreSQL 15+

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

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

API : `http://localhost:8080` | Swagger : `http://localhost:8080/api/docs`

## Variables d'environnement Railway

| Variable | Description |
|---|---|
| `DB_HOST` | `${{Postgres.PGHOST}}` |
| `DB_PORT` | `${{Postgres.PGPORT}}` |
| `DB_NAME` | `${{Postgres.PGDATABASE}}` |
| `DB_USERNAME` | `${{Postgres.PGUSER}}` |
| `DB_PASSWORD` | `${{Postgres.PGPASSWORD}}` |
| `JWT_SECRET` | Clé secrète (min. 32 caractères) |
| `CLOUDINARY_CLOUD_NAME` | Cloud name Cloudinary |
| `CLOUDINARY_API_KEY` | API key Cloudinary |
| `CLOUDINARY_API_SECRET` | API secret Cloudinary |
| `FRONTEND_URL` | `https://m-motors-frontend-nu.vercel.app` |
