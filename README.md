# LOCINTEL M-Pesa Bridge Android — V1.4.0

Application Android légère destinée à transmettre à Odoo uniquement les notifications M-Pesa contenant les marqueurs métier `Amount:`, `Reason:` et `Ref:`.

## Fonctionnement

1. Configurer l'URL HTTPS du serveur Odoo.
2. Configurer le jeton défini dans **Device Financing → Configuration passerelle M-Pesa**.
3. Autoriser l'accès Android aux notifications.
4. Lorsqu'une notification M-Pesa correspond au format attendu, l'application l'envoie vers :
   `/device_financing/mpesa/inbox`.

Le module Odoo peut ensuite extraire le shop, le numéro payeur, le nom M-Pesa, le montant, le `Reason` (numéro client) et la `Ref` (transaction unique).

## Compilation avec GitHub Actions

Le workflow `.github/workflows/build-apk.yml` compile automatiquement un APK de test lors d'un push sur `main` ou via **Actions → Build LOCINTEL M-Pesa Bridge APK → Run workflow**.

L'APK est disponible à la fin du workflow dans **Artifacts** sous le nom :
`LOCINTEL-Mpesa-Bridge-V1.4.0-debug`.

## Sécurité

- Aucune lecture générale de la boîte SMS.
- Seules les notifications qui ressemblent au format M-Pesa attendu sont transmises.
- Connexion HTTPS obligatoire.
- Authentification par jeton passerelle Odoo.
- La validation finale des paiements reste contrôlée côté Odoo.
