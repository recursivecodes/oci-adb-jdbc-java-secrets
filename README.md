# oci-adb-jdbc-java-secrets

* Create Table

```sql
CREATE TABLE EMPLOYEES (
    EMP_EMAIL VARCHAR2(100 BYTE) NOT NULL, 
    EMP_NAME VARCHAR2(100 BYTE),
    EMP_DEPT VARCHAR2(50 BYTE), 
    CONSTRAINT PK_EMP PRIMARY KEY ( EMP_EMAIL )
);
```

* Create Application

```bash
fn create app oci-adb-jdbc-java-app --annotation oracle.com/oci/subnetIds='["ocid1.subnet.oc1.phx..."]'
```

* Create Function

```bash
fn init --runtime java oci-adb-jdbc-java-secrets
```

* Move into fn directory

```bash
cd oci-adb-jdbc-java-secrets
```

* Create Config

```bash
fn config app oci-adb-jdbc-java-app DB_URL jdbc:oracle:thin:\@[tns_name]]\?TNS_ADMIN=/tmp/wallet 
fn config app oci-adb-jdbc-java-app DB_USER [user]
```

* Download Wallet

```bash
oci db autonomous-data-warehouse generate-wallet --autonomous-data-warehouse-id ocid1.autonomousdatabase.oc1.phx... --password Str0ngPa$$word1 --file /projects/fn/oci-adb-jdbc-java-secrets/wallet.zip
```

* Unzip wallet locally.

* Base64 encode wallet files (sample script below):

```java
mkdir /tmp/base64-wallet
for f in /wallet/*
 do
   fname=$(basename $f)
   echo $fname
   x=$(base64 -i $f -o /tmp/base64-wallet/$fname)
   #echo $f: $x
 done
```

* Create Secrets in Console for each file

* Copy the OCID of each secret and create config vars for each:

```bash
fn config app oci-adb-jdbc-java-app PASSWORD_ID ocid1.vaultsecret.oc1.iad...
fn config app oci-adb-jdbc-java-app CWALLET_ID ocid1.vaultsecret.oc1.iad...
fn config app oci-adb-jdbc-java-app EWALLET_ID ocid1.vaultsecret.oc1.iad...
fn config app oci-adb-jdbc-java-app KEYSTORE_ID ocid1.vaultsecret.oc1.iad...
fn config app oci-adb-jdbc-java-app OJDBC_ID ocid1.vaultsecret.oc1.iad...
fn config app oci-adb-jdbc-java-app SQLNET_ID ocid1.vaultsecret.oc1.iad...
fn config app oci-adb-jdbc-java-app TNSNAMES_ID ocid1.vaultsecret.oc1.iad...
fn config app oci-adb-jdbc-java-app TRUSTSTORE_ID ocid1.vaultsecret.oc1.iad...
```

* Set local env vars for testing locally:

```bash
export PASSWORD_ID=ocid1.vaultsecret.oc1.iad...
export CWALLET_ID=ocid1.vaultsecret.oc1.iad...
export EWALLET_ID=ocid1.vaultsecret.oc1.iad...
export KEYSTORE_ID=ocid1.vaultsecret.oc1.iad...
export OJDBC_ID=ocid1.vaultsecret.oc1.iad...
export SQLNET_ID=ocid1.vaultsecret.oc1.iad...
export TNSNAMES_ID=ocid1.vaultsecret.oc1.iad...
export TRUSTSTORE_ID=ocid1.vaultsecret.oc1.iad...
```

* Create Dynamic Group

```
ALL{resource.type='fnfunc', resource.compartment.id='ocid1.compartment.xxxxx'}
```

* Dynamic Group Policy

```
allow dynamic-group functions-dynamic-group to manage secret-family in tenancy 
allow dynamic-group functions-dynamic-group to manage vaults in tenancy 
allow dynamic-group functions-dynamic-group to manage keys in tenancy
```

* Deploy

```bash
fn deploy --app oci-adb-jdbc-java-secrets
```

* Invoke

```bash
fn invoke oci-adb-jdbc-java-app oci-adb-jdbc-java-secrets
```