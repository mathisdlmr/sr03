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

## CI/CD

Pour des raisons de maintenance du code, 
* Le Dockerfile permet de s'assurer que le backend java arrive bien à se build et à tourner
* Prettierc et EsLint ont été setup sur le frontend pour automatiquement mettre en forme le code et vérifier les erreurs de syntaxe

En parallèle, une CI GitLab a été setup pour, à chaque push, essayer de construire l'image Docker du backend, faire tourner EsLint sur le frontend, et essayer de build le frontend.
On peut ainsi normaliser le code en ligne et s'assurer que le code sur le repo est fonctionnel

Pour aller plus loin
* On pourrait intégrer Prettierc à la CI en faisant en sorte que la CI pousse ses changements de mise en forme sur le repo (sinon l'étape tournerait dans le vent)
* On pourrait mettre l'image docker sur un registry GitLab
* On pourrait mettre le frontend buildé en artifcat GitLab
* On pourrait ajouter du CD en déployant le projet directement sur un Docker Swarm ou Kubernetes

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

### Quelques mots supplémentaires sur les attaques XSS

Pour compléter prévenir les attaques XSS de toute part, il aurait fallut ajouter à la configuration de Spring Security : 
```java
  .headers(headers -> headers
      .contentSecurityPolicy(policy -> policy.policyDirectives(API_CSP))
      .xssProtection(xss -> xss.headerValue(ENABLED_MODE_BLOCK))
  )
```
Cela aurait permis d'activer la protection contre les attaques XSS en définissant certaines policies que l'on accepterait, et en refusant tous les autres scripts. (https://www.baeldung.com/spring-prevent-xss)

Cependant cela aurait demandé de définir des Content Security Policy (CSP) qui auraient été particulièrement verbeuses à rédiger pour accepter des styles et gestionnaire d'évènements (comme `confirm()`) en inline, des scripts et CSS de MetroUI, des fonts de GoogleAPI, etc. (https://www.baeldung.com/spring-security-csp)

Au vu des protections déjà fournies par Thymeleaf et React nous ne les avons donc pas implémentées, mais pour un projet plus mature cela aurait pu être pertinent.

## Détails sur le Frontend

Le frontend comporte le features suivantes : 
* Voir ses chats, gérer les membres (en inviter ou en supprimer), et rejoindre les chats en question
* Voir les chats où l'on a été invité, avec possibilité de les rejoindre ou de les quitter
* Créer un nouveau chat avec un datetime de début et une durée en minutes
* Sur un Chat
  * Envoie de messages
  * Envoie de photos (<5Mo)
  * Envoie des fichiers (<5Mo)
  * Envoie de messages vocaux
  * Pour les admin et créateur du chat, la possibilité de kick une personne du chat
  * Historique du Chat en RAM, lorsque l'on rejoint le chat on récupère alors tout l'historique
* Pouvoir modifier son avatar avec une photo (<1Mo)