#!/bin/bash
kubectx nais-dev

deployment="deployment/bidrag-regnskap-q2"
[ "$1" == "q2" ] && deployment="deployment/bidrag-regnskap-q2"
echo "Henter miljøparametere fra deployment: $deployment"
kubectl exec --tty $deployment -- printenv | grep -E 'AZURE_|_URL|SCOPE|UNLEASH_|MASKINPORTEN_|DB_' > src/test/resources/application-lokal-nais-secrets.properties