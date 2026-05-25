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

## Planning

| Date    | Sujet                                                  | On en est où ? |
| ------- | ------------------------------------------------------ | -------------- |
| 03-avr  | Présentation devoir 2 + conception uml                 | X              |
| 10-avr  | Hibernate ORM + écriture des requêtes                  | X              |
| 06-mai  | Controller et Rest Controller, Thymeleaf ( Vue Admin ) | X              |
| 15-mai  | Controller et Rest Controller, Thymeleaf ( Vue Admin ) | X              |
| 22-mai  | WebSocket                                              | X              |
| 29-mai  | WebSocket                                              |                |
| 05-juin | REACT                                                  |                |
| 12-juin | REACT                                                  |                |
| 19-juin | Soutenances                                            |                |
