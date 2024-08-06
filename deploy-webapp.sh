#!/bin/bash

AZURE_RESOURCE_GROUP=`az group list --query "[?contains(name, 'rg-tomcatonazure-')].{name:name}" -o tsv`
ACR_NAME=`az acr list --query "[?starts_with(name, 'crtomcat') && ends_with(name, '001')].[name]" -o tsv`
ACR_REPOSITORY_NAME_SEASAR=seasar
APPSERVICE_PLAN_NAME=`az appservice plan list --query "[?contains(name, 'plan-tomcatonazure')].{Name:name}" -o json | jq -r '.[].Name'`
ACA_NAME=`az containerapp list --query "[?contains(name, 'ca-tomcatonazure')].name" -o tsv`
ACE_NAME=`az containerapp env list --query "[?contains(name, 'env-tomcatonazure')].name" -o tsv`
SUBSCRIPTION_ID=`az account show --query id -o tsv`

echo "========================================"
echo "login ACR"
echo "========================================"
ACR_USER=`az acr credential show --name ${ACR_NAME} --resource-group ${AZURE_RESOURCE_GROUP} --query username -o tsv`
ACR_PASS=`az acr credential show --name ${ACR_NAME} --resource-group ${AZURE_RESOURCE_GROUP} --query "passwords[0].value" -o tsv`
az acr login -n ${ACR_NAME} --username ${ACR_USER} --password ${ACR_PASS}

echo "========================================"
echo "webapp build and push (Seasar)"
echo "========================================"
cd ./app/1.seasar
az acr build -t ${ACR_NAME}.azurecr.io/${ACR_REPOSITORY_NAME_SEASAR}:latest --platform linux/x86_64 .
az acr push ${ACR_NAME}.azurecr.io/${ACR_REPOSITORY_NAME_SEASAR}:latest

echo "========================================"
echo "Creating App Service (Seasar)"
echo "========================================"
az webapp create --resource-group ${AZURE_RESOURCE_GROUP} --plan ${APPSERVICE_PLAN_NAME} --name app-seasarstruts-${SUBSCRIPTION_ID} --container-image-name ${ACR_NAME}.azurecr.io/${ACR_REPOSITORY_NAME_SEASAR}:latest --container-registry-password ${ACR_PASS} --container-registry-user ${ACR_USER}

echo "========================================"
echo "Creating WebJobs outputs"
echo "========================================"
cd ../2.scheduled-tasks && chmod +x create-zip.sh && ./create-zip.sh

APPSERVICE_WEBAPP_NAME=`az webapp list --query "[?contains(name, 'app-tomcatonazure')].[name]" -o tsv`
az webapp config appsettings set --resource-group ${AZURE_RESOURCE_GROUP} --name ${APPSERVICE_WEBAPP_NAME} --settings WEBSITES_PORT=8080
az webapp config appsettings set --resource-group ${AZURE_RESOURCE_GROUP} --name ${APPSERVICE_WEBAPP_NAME} --settings WEBSITES_ENABLE_APP_SERVICE_STORAGE=true

echo "========================================"
echo "Restart WebApp"
echo "========================================"
az webapp restart -g ${AZURE_RESOURCE_GROUP} -n ${APPSERVICE_WEBAPP_NAME}