#!/bin/bash
kubectx nais-dev

deployment="deployment/bidrag-aktoerregister-q1"
[ "$1" == "q1" ] && deployment="deployment/bidrag-aktoerregister-q1"
echo "Henter miljøparametere fra deployment: $deployment"
kubectl exec --tty $deployment -- printenv | grep -E 'AZURE_|_URL|SCOPE|UNLEASH_'> src/test/resources/application-lokal-nais-secrets.properties