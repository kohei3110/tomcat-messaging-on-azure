#!/bin/bash

AZURE_RESOURCE_GROUP=`az group list --query "[?contains(name, 'rg-tomcatonazure-')].{name:name}" -o tsv`
ACR_NAME=`az acr list --query "[?starts_with(name, 'cr') && ends_with(name, '001')].[name]" -o tsv`
ACR_REPOSITORY_NAME_PUBLISHER=publisher
ACR_REPOSITORY_NAME_SUBSCRIBER=subscriber
APPSERVICE_PLAN_NAME=`az appservice plan list --query "[?contains(name, 'plan-tomcatonazure')].{Name:name}" -o json | jq -r '.[].Name'`
SERVICE_BUS_NAMESPACE=`az servicebus namespace list --query "[?contains(name, 'sb-') && contains(name, 'tomcatonazure')].name" -o tsv`
ACA_NAME=`az containerapp list --query "[?contains(name, 'ca-tomcatonazure')].name" -o tsv`
ACE_NAME=`az containerapp env list --query "[?contains(name, 'env-tomcatonazure')].name" -o tsv`

echo "========================================"
echo "login ACR"
echo "========================================"
ACR_USER=`az acr credential show --name ${ACR_NAME} --resource-group ${AZURE_RESOURCE_GROUP} --query username -o tsv`
ACR_PASS=`az acr credential show --name ${ACR_NAME} --resource-group ${AZURE_RESOURCE_GROUP} --query "passwords[0].value" -o tsv`
az acr login -n ${ACR_NAME} --username ${ACR_USER} --password ${ACR_PASS}

echo "========================================"
echo "webapp build and push (Publisher)"
echo "========================================"
cd ./app/publisher
docker build -t ${ACR_NAME}.azurecr.io/${ACR_REPOSITORY_NAME_PUBLISHER}:latest --platform linux/x86_64 .
docker push ${ACR_NAME}.azurecr.io/${ACR_REPOSITORY_NAME_PUBLISHER}:latest

echo "========================================"
echo "webapp build and push (Subscriber)"
echo "========================================"
cd ../subscriber
docker build -t ${ACR_NAME}.azurecr.io/${ACR_REPOSITORY_NAME_SUBSCRIBER}:latest --platform linux/x86_64 .
docker push ${ACR_NAME}.azurecr.io/${ACR_REPOSITORY_NAME_SUBSCRIBER}:latest

echo "========================================"
echo "Creating App Service (Publisher)"
echo "========================================"
az webapp create --resource-group ${AZURE_RESOURCE_GROUP} --plan ${APPSERVICE_PLAN_NAME} --name app-tomcatonazure --container-image-name ${ACR_NAME}.azurecr.io/${ACR_REPOSITORY_NAME_PUBLISHER}:latest --container-registry-password ${ACR_PASS} --container-registry-user ${ACR_USER}

echo "========================================"
echo "Creating Container Apps (Subscriber)"
echo "========================================"
az containerapp create --name ca-tomcatonazure-demo-eastus-001 --resource-group ${AZURE_RESOURCE_GROUP} --environment ${ACE_NAME} --image ${ACR_NAME}.azurecr.io/${ACR_REPOSITORY_NAME_SUBSCRIBER}:latest --registry-server ${ACR_NAME}.azurecr.io --registry-username ${ACR_USER} --registry-password ${ACR_PASS} --target-port 8080 --ingress external --query configuration.ingress.fqdn

# FIXME: 進行中のインジケータを表示する

APPSERVICE_WEBAPP_NAME=`az webapp list --query "[?contains(name, 'app-tomcatonazure')].[name]" -o tsv`
az webapp config appsettings set --resource-group ${AZURE_RESOURCE_GROUP} --name ${APPSERVICE_WEBAPP_NAME} --settings WEBSITES_PORT=8080
az webapp config appsettings set --resource-group ${AZURE_RESOURCE_GROUP} --name ${APPSERVICE_WEBAPP_NAME} --settings SERVICE_BUS_FULLY_QUALIFIED_NAMESPACE=${SERVICE_BUS_NAMESPACE}.servicebus.windows.net

# FIXME: サービスプリンシパルによるアクセス許可を設定する

# webappにデプロイ(latestを常に取得するようにしているのでAppServiceを再起動)
echo "========================================"
echo "Restart WebApp"
echo "========================================"
az webapp restart -g ${AZURE_RESOURCE_GROUP} -n ${APPSERVICE_WEBAPP_NAME}

echo "========================================"
echo "Allowing local IP address to access the SQL Database"
echo "========================================"
SQL_SERVER_NAME=`az sql server list --query "[?contains(name, 'sqldb-tomcatonazure')].{name:name}" -o tsv`
# LOCAL_PUBLIC_IP=$(curl -s ifconfig.me)
# echo "Local IP Address: $LOCAL_PUBLIC_IP"

ACA_FQDN=`az containerapp show --name ${ACA_NAME} --resource-group ${AZURE_RESOURCE_GROUP} --query properties.configuration.ingress.fqdn -o tsv`
ACA_PUBLIC_IP=$(nslookup $ACA_FQDN | awk '/^Address: / { print $2 }' | tail -n1)
echo "ACA Public IP Address: $ACA_PUBLIC_IP"

# SQL Serverのファイアウォールルールを設定
# ルール名は一意である必要があるため、現在のタイムスタンプを使用
RULE_NAME="AllowContainerAppsAccess_$(date +%s)"
az sql server firewall-rule create --resource-group ${AZURE_RESOURCE_GROUP} --server ${SQL_SERVER_NAME} --name ${RULE_NAME} --start-ip-address ${ACA_PUBLIC_IP} --end-ip-address ${ACA_PUBLIC_IP}
echo "Added firewall rule to allow local IP address: $ACA_PUBLIC_IP"