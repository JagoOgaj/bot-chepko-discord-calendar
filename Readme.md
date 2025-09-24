# 📅 Chepko Calendar Bot

Un bot Discord permettant aux serveurs d’enregistrer, gérer et afficher des calendriers d’événements.  
Il supporte l’import de calendriers au format **ICS**, les notifications d’événements à venir et les alertes automatiques.

🔗 **Code source** : [bot-chepko-discord-calendar](https://github.com/JagoOgaj/bot-chepko-discord-calendar)  
Développé avec **Java + JDA**

---

## Fonctionnalités principales

- Importer un calendrier via un fichier **ICS**.
- Configurer des **alertes automatiques** pour les événements.
- Recevoir des notifications des **prochains événements** directement dans un salon Discord.
- Sauvegarder et afficher les calendriers d’un serveur.
- Gestion avancée des permissions pour les administrateurs.

---

## Commandes Slash disponibles

### Calendrier

- **`/register-calendar`**  
   Enregistre ou met à jour le calendrier du serveur à partir d’un lien ICS.
    - `ics-url` *(obligatoire)* : URL du fichier ICS
    - `calendar-name` *(obligatoire)* : Nom du calendrier


- **`/show-calendar`**  
   Affiche le calendrier actuellement utilisé sur le serveur.


- **`/save-calendar`**  
   Sauvegarde le calendrier du serveur pour l’utiliser sur le site.


- **`/unsave`**  
  ️ Supprime le calendrier sauvegardé pour ce serveur.

---

### Événements

- **`/today-event`**  
   Liste les événements restants de la journée.


- **`/next-event`**  
   Affiche les prochains événements pour une date donnée.
    - `date-of` *(obligatoire)* : Date au format `JJ/MM/AAAA`

---

### Notifications

- **`/config-alert`**  
   Configure ou modifie les paramètres des alertes.
    - `channel-id` *(obligatoire)* : Salon où envoyer les alertes
    - `minute` *(obligatoire)* : Minutes avant l’événement pour envoyer l’alerte


- **`/disable-event-alert`**  
  Désactive les alertes d’événements pour ce serveur.


- **`/enable-event-alert-update`**  
   Active les alertes de mise à jour des événements (après `/config-alert`).


- **`/disable-update-calendar-alert`**  
   Désactive les alertes liées aux mises à jour de calendrier.

---

## Permissions
- Certaines commandes (configuration, désactivation, etc.) nécessitent des **droits administrateurs**.
- Les alertes ne peuvent être configurées que par les membres ayant le rôle adéquat.

---

## Installation (auto-hébergement)
1. Clone le projet :
   ```bash
   git clone https://github.com/JagoOgaj/bot-chepko-discord-calendar.git
   ```
2. Compile et package avec Maven :
   ```bash
   mvn clean package -DskipTests
   ```
3.  Configure tes variables d’environnement :
    ```bash
    TOKEN_BOT=ton_token_discord
    URL_DB=jdbc:postgresql://host:5432/calendar_db
    USER_DB=postgres
    PASSWORD_DB=password
    ```
4. Lance le bot :
    ```bash
   java -jar target/calendar-bot.jar
   ```
   
