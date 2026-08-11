# Bidrag-reskontro
Applikasjon for å hente ut hva skatt har krevd inn og eventuell gjeld.

[![continuous integration](https://github.com/navikt/bidrag-skatteetaten/actions/workflows/reskontro_deploy_main.yaml/badge.svg?branch=main)](https://github.com/navikt/bidrag-skatteetaten/actions/workflows/reskontro_deploy_main.yaml)
[![release bidrag-regnskap](https://github.com/navikt/bidrag-skatteetaten/actions/workflows/reskontro_deploy_prod.yaml/badge.svg)](https://github.com/navikt/bidrag-skatteetaten/actions/workflows/reskontro_deploy_prod.yaml)


## Beskrivelse
Bidrag-reskontro er en applikasjon for å hente ut informasjon fra skattetaten, via Elin, om innkrevd og/eller innbetalt gjeld.

### Token generator
- https://azure-token-generator.intern.dev.nav.no/api/obo?aud=dev-gcp.bidrag.bidrag-reskontro-q1
- https://azure-token-generator.intern.dev.nav.no/api/obo?aud=dev-gcp.bidrag.bidrag-reskontro-q2