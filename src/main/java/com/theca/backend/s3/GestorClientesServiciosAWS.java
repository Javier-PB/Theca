/**
 * Descripción: clase para gestionar los servicios AWS.
 * 
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 17 may 2026
 * 
 */

package com.theca.backend.s3;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

public class GestorClientesServiciosAWS {
 
	public static final Region REGION_AWS = Region.US_EAST_1;
	 
	private static String awsProfile;
	private static boolean useProfile = false;
	private static String accessKeyId;
	private static String secretAccessKey;
	private static String sessionToken;
	 
	public static void initProfile(String profile) {
		awsProfile = profile;
	    useProfile = true;
	}
	 
	public static void initCredentials(String accessKey, String secretKey) {
	    accessKeyId = accessKey;
	    secretAccessKey = secretKey;
	    useProfile = false;
	}
	 
	public static void initCredentials(String accessKey, String secretKey, String sessionToken) {
	    accessKeyId = accessKey;
	    secretAccessKey = secretKey;
	    sessionToken = sessionToken;
	    useProfile = false;
	 }
	 
	private GestorClientesServiciosAWS() {}
	 
	public static S3Presigner getClientePrefirmadorBucketS3() {
	    S3Presigner.Builder builder = S3Presigner.builder().region(REGION_AWS);
	    
	    if (useProfile && awsProfile != null) {
	        builder.credentialsProvider(ProfileCredentialsProvider.create(awsProfile));
	    } else if (accessKeyId != null && secretAccessKey != null) {
	        if (sessionToken != null && !sessionToken.isEmpty()) {
	            AwsSessionCredentials sessionCreds = AwsSessionCredentials.create(accessKeyId, secretAccessKey, sessionToken);
	            builder.credentialsProvider(StaticCredentialsProvider.create(sessionCreds));
	        } else {
	            AwsBasicCredentials basicCreds = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
	            builder.credentialsProvider(StaticCredentialsProvider.create(basicCreds));
	        }
	    } else {
	    	throw new IllegalStateException("No AWS credentials configured");
	    }
	     
	    return builder.build();
	 }
 
}
