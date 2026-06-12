# Devoir 2 SR03

## Présentation du projet

L'objectif de ce projet est de créer un chat entre des utilisateurices.

Ce projet comporte deux applications : 
* Une application Java avec le framework Spring
    * Elle forme à la fois un serveur backend traitant requêtes API et requêtes de l'interface web
    * Ainsi qu'une vue web assez basique (utilisant MetroUI) fournissant un panel Admin pour notre application
* Une application React
    * Cette application fourni un frontend pour chatter via des requêtes API et websockets avec notre backend

## Lancer le projet

Pour lancer le projet, nous avons prévu un Makefile. Il suffit donc de lancer le projet avec 
```bash
make all
```
**Attention**: Le Makefile utilise Docker pour faire tourner l'application Java, et npm pour l'application React.
Vérifiez que vous avez bien Docker et node avant de lancer le projet :)
```bash
java --version
npm -v
```

Pour dire quelques mots sur le Makefile, disons simplement qu'il 
* Construit une image Docker du Backend
    * Le Dockerfile commence par copier les fichiers et les compiler avec Maven
    * Puis utilise Eclipse Temurin pour faire tourner le binaire jar construit par Maven
        * Lors de ce deuxième stage, on en profite pour créer une base de données sqlite et la seed avec le fichier database_schema.sql
* Installer les node_modules de notre app React
* Faire tourner l'image Docker du backend avec une redirection de port 8080 -> 8080
* Lancer le serveur de dev React

La raison pour cette architecture part du postulat que la majorité des devs on l'environnement nécessaire pour faire tourner React sur le pc (npm), mais que rares sont les gens qui possèdent de quoi compiler et/ou faire tourner du Java

## Détails sur le Backend

Comme mentionné précédement, le Backend est à la fois utilisé pour l'interface Admin, et à la fois pour traiter les requêtes API de React pour l'interface utilisateurice

### Interface Admin

Pour l'interface Admin
* Les views sont générées avec Thymeleaf
* L'authentification est gérée par session HTTP
* Les requêtes sont traitées par `src/main/java/fr/utc/sr03/controller/WebController.java`

D'un point de vue sécurité
* Les CORS ont été configurés
* On se prévient des attaques CSRF via les input `<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}">` dans les formulaires qui sont ensuite vérifiés par Sping Security dans le `webFilterChain` et renvoient une 401 en cas d'erreur
  * Basé sur https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html
* On se prévient des attaques XSS grâce à Thymeleaf qui gère automatiquement l'échapement, sauf pour le champ `utext` qui n'a donc jamais été utilisé
  * Basé sur https://rashidi.github.io/spring-boot-tutorials/main/web-thymeleaf-xss.html
* On se prévient des injections SQL graĉe à la construction de requêtes SQL dans les repository JPA via Injection de variables positionnelles échappées et non via concaténation
  * Basé sur https://stackoverflow.com/questions/73617743/is-springboot-data-jpa-repository-safe-against-sql-injection

### Interface Utilisateurice

Pour l'interface utilisateurice
* L'interface est générées par Vite
* Les authentifications sont gérées via des couples d'AT/RT
  * Quand on se connecte on récupère un access token et un refresh token (dont les TTL peuvent être configurés dans le `application.properties`)
  * Chaque fois que le frontend fait une requête, il met son access token dans le Header "Authorization" au format `Bearer <access_token>`
  * Quand notre access_token expire, on fait une demande de refresh de notre access token en envoyant notres refresh token au serveur
  * Quand notre refresh_token expire, on doit se reconnecter
  * Côté Backend cette configuration se trouve dans `src/main/java/fr/utc/sr03/security/`
  * Côté Frontend
    * On se connecte sur la page `frontend/src/pages/LoginPage.jsx`
    * On réalise les requêtes de login avec `frontend/src/api/authApi.js`
    * On créé un contexte avec `frontend/src/contexts/AuthContext.jsx`
    * On utilise le contexte avec `frontend/src/hooks/useAuth.js`
    * On normalise nos requêtes API avec `frontend/src/api/apiClient.js`
    * Puis on définit la liste de nos requêtes API dans `frontend/src/api/apiCalls.js`
* Les requêtes sont traités par `src/main/java/fr/utc/sr03/controller/ApiController.java`

D'un point de vue de sécurité
* On se prévient des attaques CSRF grâce à l'utilisation des JWT (voir ici)[https://medium.com/@gunawardena.buddika/jwts-csrf-tokens-465e5d4f91cf]
  * L'idée est simplement que le token est stocké en localStorage et attaché en header Authorization, et qu'une application tierce ne peut forcer notre navigateur à l'injecter dans des requêtes qu'il aurait construit -> seule notre application peut récupérer ce token et authentifier ses requêtes avec
* On se prévient des attaques XSS grâce à React qui gère automatiquement l'échappement
  * Sauf dans le cas de l'innerHtml (https://stackoverflow.com/questions/33644499/what-does-it-mean-when-they-say-react-is-xss-protected), mais cet élément n'a pas été utilisé 
* On se prévient des injections SQL de la même manière que pour l'interface Admin

## TODO 