package codes.recursive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.auth.ResourcePrincipalAuthenticationDetailsProvider;
import com.oracle.bmc.secrets.SecretsClient;
import com.oracle.bmc.secrets.model.Base64SecretBundleContentDetails;
import com.oracle.bmc.secrets.requests.GetSecretBundleRequest;
import com.oracle.bmc.secrets.responses.GetSecretBundleResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WalletSecretFunction {

    private final File walletDir = new File("/tmp", "wallet");
    private final String dbUser = System.getenv().get("DB_USER");
    private final String dbUrl = System.getenv().get("DB_URL");
    private final String dbPasswordOcid = System.getenv().get("PASSWORD_ID");
    private String dbPassword;
    private BasicAuthenticationDetailsProvider provider = null;
    private SecretsClient secretsClient = null;

    private final Map<String, String> walletFiles = Map.of(
            "cwallet.sso",  System.getenv().get("CWALLET_ID"),
            "ewallet.p12",  System.getenv().get("EWALLET_ID"),
            "keystore.jks",  System.getenv().get("KEYSTORE_ID"),
            "ojdbc.properties",  System.getenv().get("OJDBC_ID"),
            "sqlnet.ora",  System.getenv().get("SQLNET_ID"),
            "tnsnames.ora",  System.getenv().get("TNSNAMES_ID"),
            "truststore.jks", System.getenv().get("TRUSTSTORE_ID")
    );

    public WalletSecretFunction() {
        System.out.println("Setting up secrets client");
        String version = System.getenv("OCI_RESOURCE_PRINCIPAL_VERSION");
        if( version != null ) {
            provider = ResourcePrincipalAuthenticationDetailsProvider.builder().build();
        }
        else {
            try {
                provider = new ConfigFileAuthenticationDetailsProvider("~/.oci/config", "DEFAULT");
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        secretsClient = new SecretsClient(provider);
        dbPassword = new String(getSecret(dbPasswordOcid));
        System.out.println("Secrets client set up");
    }

    public List handleRequest() throws SQLException, JsonProcessingException {
        System.setProperty("oracle.jdbc.fanEnabled", "false");
        if( !walletDir.exists() ) {
            System.out.println("Creating wallet...");
            createWallet(walletDir);
            System.out.println("Wallet created!");
        }

        DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
        Connection conn = DriverManager.getConnection(dbUrl,dbUser,dbPassword);
        Statement statement = conn.createStatement();

        ResultSet resultSet = statement.executeQuery("select * from employees");
        List<HashMap<String, Object>> recordList = convertResultSetToList(resultSet);
        System.out.println( new ObjectMapper().writeValueAsString(recordList) );
        conn.close();
        return recordList;
    }

    private List<HashMap<String,Object>> convertResultSetToList(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        List<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();
        while (rs.next()) {
            HashMap<String,Object> row = new HashMap<String, Object>(columns);
            for(int i=1; i<=columns; ++i) {
                row.put(md.getColumnName(i),rs.getObject(i));
            }
            list.add(row);
        }
        return list;
    }

    private void createWallet(File walletDir) {
        walletDir.mkdirs();
        for (String key : walletFiles.keySet()) {
            try {
                writeWalletFile(key);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeWalletFile(String key) throws IOException {
        String secretOcid = walletFiles.get(key);
        byte[] secretValueDecoded = getSecret(secretOcid);
        try {
            File walletFile = new File(walletDir + "/" + key);
            FileUtils.writeByteArrayToFile(walletFile, secretValueDecoded);
            System.out.println("Stored wallet file: " + walletFile.getAbsolutePath());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] getSecret(String secretOcid) {
        GetSecretBundleRequest getSecretBundleRequest = GetSecretBundleRequest
                .builder()
                .secretId(secretOcid)
                .stage(GetSecretBundleRequest.Stage.Current)
                .build();
        GetSecretBundleResponse getSecretBundleResponse = secretsClient
                .getSecretBundle(getSecretBundleRequest);
        Base64SecretBundleContentDetails base64SecretBundleContentDetails =
                (Base64SecretBundleContentDetails) getSecretBundleResponse.
                        getSecretBundle().getSecretBundleContent();
        byte[] secretValueDecoded = Base64.decodeBase64(base64SecretBundleContentDetails.getContent());
        return secretValueDecoded;
    }
}