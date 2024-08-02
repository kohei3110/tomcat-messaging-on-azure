param documentStorageName string
param location string = resourceGroup().location
param tags object = {}

@allowed([
  'Cool'
  'Hot'
  'Premium' ])
param accessTier string = 'Hot'
param allowBlobPublicAccess bool = true
param allowCrossTenantReplication bool = true
param allowSharedKeyAccess bool = true
param shares array = [
  {
    name: 'shares-demo'
    publicAccess: 'Blob'
  }
]
param shareDeleteRetentionPolicy object = {}
param defaultToOAuthAuthentication bool = false
@allowed([ 'AzureDnsZone', 'Standard' ])
param dnsEndpointType string = 'Standard'
param kind string = 'StorageV2'
param minimumTlsVersion string = 'TLS1_2'
param networkAcls object = {
  bypass: 'AzureServices'
  defaultAction: 'Allow'
}
@allowed([ 'Enabled', 'Disabled' ])
param publicNetworkAccess string = 'Enabled'
param sku object = { name: 'Standard_LRS' }

resource documentStorage 'Microsoft.Storage/storageAccounts@2022-05-01' = {
  name: documentStorageName
  location: location
  tags: tags
  kind: kind
  sku: sku
  properties: {
    accessTier: accessTier
    allowBlobPublicAccess: allowBlobPublicAccess
    allowCrossTenantReplication: allowCrossTenantReplication
    allowSharedKeyAccess: allowSharedKeyAccess
    defaultToOAuthAuthentication: defaultToOAuthAuthentication
    dnsEndpointType: dnsEndpointType
    minimumTlsVersion: minimumTlsVersion
    networkAcls: networkAcls
    publicNetworkAccess: publicNetworkAccess
  }

  resource fileServices 'fileServices' = if (!empty(shares)) {
    name: 'default'
    properties: {
      shareDeleteRetentionPolicy: shareDeleteRetentionPolicy
    }
    resource container 'shares' = [for container in shares: {
      name: container.name
      properties: {
        accessTier: accessTier
      }
    }]
  }
}
