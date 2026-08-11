# Bidrag-skatteetaten

Samling av applikasjoner som benyttes i kommunikasjon med Skatteetaten.

## Applikasjoner

| App | Mappe | Beskrivelse | Miljøer |
|---|---|---|---|
| bidrag-aktoerregister | [`apps/bidrag-aktoerregister`](apps/bidrag-aktoerregister) | Formidler aktører (skyldnere, mottakere og samhandlere) med kontonummer, adresseinformasjon mm. til Skatteetaten. | prod, q1, q2 |
| bidrag-regnskap | [`apps/bidrag-regnskap`](apps/bidrag-regnskap) | Krever inn bidrag, forskudd, gebyrer mm. via Skatteetaten. | prod, q1, q2 |
| bidrag-reskontro | [`apps/bidrag-reskontro`](apps/bidrag-reskontro) | Henter ut hva Skatteetaten har krevd inn og eventuell gjeld. | prod, q1, q2 |
| bidrag-elin-stub | [`apps/bidrag-elin-stub`](apps/bidrag-elin-stub) | Stubber ut Skatteetaten sine tjenester for lokal/test-bruk. | alle branches unntatt main |

Eies av **Team Bidrag**.

## Repo-struktur

- `apps/` — alle applikasjonsmoduler (Maven multi-module reactor med felles versjonering i rot-`pom.xml`)
- `.nais/` — Nais-manifester og miljøvariabler per app
- `.github/workflows/` — én deploy-fil og én test-fil per app, pluss delte reusable workflows