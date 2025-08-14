# PetSwap Backend 🐾

[![CI - Build and Test](https://github.com/Pet-Swap/Backend/actions/workflows/ci.yml/badge.svg)](https://github.com/Pet-Swap/Backend/actions/workflows/ci.yml)
[![Code Quality](https://github.com/Pet-Swap/Backend/actions/workflows/code-quality.yml/badge.svg)](https://github.com/Pet-Swap/Backend/actions/workflows/code-quality.yml)
[![Docker Build and Push](https://github.com/Pet-Swap/Backend/actions/workflows/docker.yml/badge.svg)](https://github.com/Pet-Swap/Backend/actions/workflows/docker.yml)
[![Deploy to Staging](https://github.com/Pet-Swap/Backend/actions/workflows/deploy.yml/badge.svg)](https://github.com/Pet-Swap/Backend/actions/workflows/deploy.yml)
[![Documentation](https://github.com/Pet-Swap/Backend/actions/workflows/documentation.yml/badge.svg)](https://github.com/Pet-Swap/Backend/actions/workflows/documentation.yml)
[![Security Scan](https://github.com/Pet-Swap/Backend/actions/workflows/security.yml/badge.svg)](https://github.com/Pet-Swap/Backend/actions/workflows/security.yml)

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue.svg)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-enabled-blue.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 Description

PetSwap est une plateforme de mise en relation entre propriétaires d'animaux et pet-sitters. Cette API REST permet de gérer les profils utilisateurs, les animaux, les annonces et les réservations.

## 🚀 Fonctionnalités

- **Gestion des profils** : Création et mise à jour des profils utilisateurs
- **Gestion des animaux** : Ajout et gestion des animaux de compagnie
- **Système d'annonces** : Publication et recherche d'annonces de pet-sitting
- **Système de matching** : Mise en relation entre propriétaires et pet-sitters
- **Gestion des réservations** : Système de réservation et de paiement
- **Authentification** : Sécurisation avec JWT et Auth0
- **API Documentation** : Documentation automatique avec Spring Doc

## 🛠️ Technologies

- **Java 21** - Langage de programmation
- **Spring Boot 3.5.0** - Framework principal
- **Spring Data JPA** - Persistance des données
- **Spring Security** - Authentification et autorisation
- **PostgreSQL 17** - Base de données
- **Liquibase** - Migration de base de données
- **MapStruct** - Mapping entre entités et DTOs
- **Auth0** - Authentification externe
- **Docker** - Conteneurisation
- **JUnit 5** - Tests unitaires
- **Mockito** - Mocking pour les tests
- **Jacoco** - Couverture de code

## 📦 Prérequis

- Java 21 ou supérieur
- Maven 3.9+
- PostgreSQL 17
- Docker (optionnel)

## 🔧 Installation

### Avec Docker (Recommandé)

```bash
# Cloner le repository
git clone https://github.com/USERNAME/REPOSITORY.git
cd petswap-backend

# Lancer avec Docker Compose
docker-compose up -d
```

### Installation manuelle

```bash
# Cloner le repository
git clone https://github.com/USERNAME/REPOSITORY.git
cd petswap-backend

# Configurer la base de données PostgreSQL
# Créer une base de données 'petswap_db'

# Configurer les variables d'environnement
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/petswap_db
export SPRING_DATASOURCE_USERNAME=your_username
export SPRING_DATASOURCE_PASSWORD=your_password

# Installer les dépendances et lancer l'application
./mvnw spring-boot:run
```

## 🔧 Configuration

### Variables d'environnement

| Variable | Description | Valeur par défaut |
|----------|-------------|-------------------|
| `SPRING_DATASOURCE_URL` | URL de la base de données | `jdbc:postgresql://localhost:5432/petswap_db` |
| `SPRING_DATASOURCE_USERNAME` | Nom d'utilisateur BDD | `user` |
| `SPRING_DATASOURCE_PASSWORD` | Mot de passe BDD | `secret` |
| `AUTH0_DOMAIN` | Domaine Auth0 | - |
| `AUTH0_AUDIENCE` | Audience Auth0 | - |

### Profils Spring

- `dev` : Développement local
- `test` : Tests automatisés
- `prod` : Production

## 🧪 Tests

```bash
# Exécuter tous les tests
./mvnw test

# Exécuter les tests avec couverture
./mvnw test jacoco:report

# Voir le rapport de couverture
open target/site/jacoco/index.html
```

## 📚 API Documentation

L'API est documentée automatiquement avec Spring Doc. Une fois l'application lancée :

- **Swagger UI** : http://localhost:8080/swagger-ui.html
- **OpenAPI JSON** : http://localhost:8080/v3/api-docs

## 🏗️ Architecture

```
src/
├── main/
│   ├── java/fr/petswap/backend/
│   │   ├── config/          # Configuration Spring
│   │   ├── controller/      # Contrôleurs REST
│   │   ├── dao/
│   │   │   ├── jpa/         # Entités JPA
│   │   │   └── repository/  # Repositories Spring Data
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── exception/       # Exceptions métier
│   │   ├── mapper/          # Mappers MapStruct
│   │   └── service/         # Services métier
│   └── resources/
│       ├── db/changelog/    # Scripts Liquibase
│       └── application.yaml # Configuration
└── test/                    # Tests unitaires et d'intégration
```

## 🚀 Déploiement

### GitHub Actions

Le projet utilise plusieurs workflows GitHub Actions :

- **CI/CD** : Tests automatiques sur chaque push/PR
- **Code Quality** : Analyse de la qualité du code
- **Security Scan** : Scan de sécurité automatique
- **Docker Build** : Construction et publication des images Docker
- **Documentation** : Génération automatique de la documentation
- **Deploy** : Déploiement automatique en staging

### Docker

```bash
# Construire l'image
docker build -t petswap-backend .

# Lancer le conteneur
docker run -p 8080:8080 petswap-backend
```

## 🤝 Contribution

1. Fork le projet
2. Créer une branche feature (`git checkout -b feature/AmazingFeature`)
3. Commit les changements (`git commit -m 'Add some AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

### Standards de code

- Respecter les conventions Java
- Maintenir une couverture de tests > 80%
- Documenter les nouvelles APIs
- Suivre les principes SOLID

## 📝 Changelog

Voir [CHANGELOG.md](CHANGELOG.md) pour l'historique des versions.

## 📄 License

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

## 👥 Équipe

- **Développeur Principal** - [@USERNAME](https://github.com/USERNAME)

## 🔗 Liens utiles

- [Documentation API](https://petswap-api-docs.com)
- [Issues](https://github.com/USERNAME/REPOSITORY/issues)
- [Wiki](https://github.com/USERNAME/REPOSITORY/wiki)

---

Made with ❤️ for pet lovers
