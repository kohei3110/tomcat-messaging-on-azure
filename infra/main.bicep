targetScope = 'subscription'

@minLength(1)
@maxLength(64)
@description('Name of the environment that can be used as part of naming resource convention')
param environmentName string

@minLength(1)
@description('Primary location for all resources')
param location string

var resourceToken = toLower(uniqueString(subscription().id, environmentName, location))
var tags = {
  'azd-env-name': environmentName
}

var abbrs = loadJsonContent('./abbreviations.json')

resource rg 'Microsoft.Resources/resourceGroups@2022-09-01' = {
  name: 'rg-tomcatonazure-${environmentName}-${location}-001'
  location: location
  tags: tags
}

module appServicePlan 'modules/app/appserviceplan.bicep' = {
  name: '${abbrs.webServerFarms}tomcatonazure-${environmentName}-${location}-001'
  scope: resourceGroup(rg.name)
  params: {
    name: '${abbrs.webServerFarms}tomcatonazure-${environmentName}-${location}-001'
    location: location
    tags: tags
    kind: 'linux'
    sku: {
      name: 'P0v3'
      tier: 'Premium0V3'
      size: 'P0v3'
      family: 'Pv3'
      capacity: 1
    }
  }
}

module containerRegistry 'modules/core/registry.bicep' = {
  name: '${abbrs.containerRegistryRegistries}${environmentName}${location}${deployment().name}${resourceToken}001'
  scope: resourceGroup(rg.name)
  params: {
    acrName: '${abbrs.containerRegistryRegistries}tomcatonazure${environmentName}${location}001'
    location: location
    acrSku: 'Basic'
  }
}

module containerApp 'modules/app/containerapp.bicep' = {
  name: '${abbrs.appContainerApps}tomcatonazure-${environmentName}-${location}-001'
  scope: resourceGroup(rg.name)
  params: {
    containerAppEnvironmentName: '${abbrs.appContainerAppsManagedEnvironment}tomcatonazure-${environmentName}-${location}-001'
    location: location
    tags: tags
  }
}

module fileServer 'modules/core/files.bicep' = {
  name: '${abbrs.storageStorageAccounts}${resourceToken}${location}001'
  scope: resourceGroup(rg.name)
  params: {
    documentStorageName: '${abbrs.storageStorageAccounts}${resourceToken}${location}001'
    location: location
    tags: tags
  }
}
