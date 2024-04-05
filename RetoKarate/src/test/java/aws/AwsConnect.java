package aws;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.apache.commons.codec.binary.Base64;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AwsConnect {
    private static AWSCredentials getSessionCredentials() {
        /*return new BasicSessionCredentials(
                System.getProperty("accessKey"),
                System.getProperty("secretKey"),
                System.getProperty("sessionToken")
        );*/
        return new BasicAWSCredentials(System.getenv("clientid"),
                System.getenv("clientsecret")
        );
    }

    public static Map<String, Object> invokeLambda(String functionName, String region, String payload) {
        InvokeRequest lmbRequest = new InvokeRequest()
                .withFunctionName(functionName)
                .withPayload(payload)
                .withLogType("Tail");
        lmbRequest.setInvocationType(InvocationType.RequestResponse);
        AWSLambda lambda = AWSLambdaClientBuilder.standard()
                .withRegion(Regions.fromName(region))
                .withCredentials(new AWSStaticCredentialsProvider(getSessionCredentials()))
                .build();

        InvokeResult lmbResult = lambda.invoke(lmbRequest);
        String decodedLogString = new String(Base64.decodeBase64(lmbResult.getLogResult().getBytes()));
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("statusLambda", lmbResult.getStatusCode());
        result.put("log", decodedLogString);
        return result;
    }

    //LISTAR ARCHIVOS DEL S3
    public static String listS3(String bucket_name, String region) {
        System.out.format("Objects in S3 bucket %s:\n", bucket_name);
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(region))
                .withCredentials(new AWSStaticCredentialsProvider(getSessionCredentials()))
                .build();
        ListObjectsV2Result result = s3.listObjectsV2(bucket_name);
        List<S3ObjectSummary> objects = result.getObjectSummaries();

        String objeto = "";

        for (S3ObjectSummary os : objects) {
            //System.out.println("* " + os.getKey()); os.getKey -> nos trae el nombre de cada archivo//
            objeto = objeto + os.getKey();
        }

        return objeto;
    }

    //LEER UN ARCHIVO: listaUsuarios.txt
    public static boolean verifyObject(String region, String bucketname, String KeyName) throws IOException {
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(region))
                .withCredentials(new AWSStaticCredentialsProvider(getSessionCredentials()))
                .build();

        try {

            boolean exists = s3.doesObjectExist(bucketname, KeyName);
            return exists;


        } catch (AmazonServiceException ase) {
            System.out.println("Error Type:       " + ase.getErrorType());

        }
        return false;
    }


    //DESCARGAR Y CONVERTIR ARCHIVO .TXT EN JSON//
    public static String DowloandFile( String region, String bucketName, String keyName) {

        //CONEXIÓN CON EL CLIENTE S3//
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(region))
                .withCredentials(new AWSStaticCredentialsProvider(getSessionCredentials()))
                .build();

        StringBuilder fileContentBuilder = null;
        try {
            // Descargar el archivo de texto desde S3
            S3Object objectResponse = s3.getObject(bucketName, keyName);
            S3ObjectInputStream s3is = objectResponse.getObjectContent();

            String strCurrentLine;
            BufferedReader objReader;
            fileContentBuilder = new StringBuilder();

            objReader = new BufferedReader(new InputStreamReader(s3is));

            while ((strCurrentLine = objReader.readLine()) != null) {
                fileContentBuilder.append(strCurrentLine).append("\n"); //guarda el archivo en otro String

                System.out.println(strCurrentLine);
            }

            s3is.close();

            //CONVERTIR LOS BYTES EN VARIABLES STRING//
        } catch (S3Exception | IOException e) {
            System.err.println("Error al descargar o convertir el archivo.");
            e.printStackTrace();
        }
        return fileContentBuilder.toString();
    }
}




