# Bidrag-reskontro-legacy
Applikasjon for kommunisere mot Elins ReskWS soap-grensesnitt for uthenting av informasjon om hva skatt har krevd inn og eventuell gjeld.

[![continuous integration](https://github.com/navikt/bidrag-skatteetaten/actions/workflows/reskontro-legacy_deploy_main.yaml/badge.svg?branch=main)](https://github.com/navikt/bidrag-skatteetaten/actions/workflows/reskontro-legacy_deploy_main.yaml)
[![release bidrag-regnskap](https://github.com/navikt/bidrag-skatteetaten/actions/workflows/reskontro-legacy_deploy_prod.yaml/badge.svg)](https://github.com/navikt/bidrag-skatteetaten/actions/workflows/reskontro-legacy_deploy_prod.yaml)


## Beskrivelse
Bidrag-reskontro-legacy er en applikasjon for å hente ut informasjon fra skattetaten, via Elins gammel soap grensesnitt, om innkrevd og/eller innbetalt gjeld.
Denne applikasjonen kommer til å være i bruk frem til skatt får lansert ny løsning med mer moderne grensesnitt. Informasjonen innhentet vil utleveres via bidrag-reskontro.
