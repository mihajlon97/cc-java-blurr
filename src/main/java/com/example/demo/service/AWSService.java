package com.example.demo.service;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;

@Service
@PropertySource("classpath:application.properties")
public class AWSService {
	
	private AWSCredentials credentials;
	
	private AmazonS3 s3Client;
	
	public AWSService(@Value("${aws.accessKey}") String accessKey, @Value("${aws.secretKey}") String secretKey) {
		credentials = new BasicAWSCredentials(accessKey, secretKey);
		s3Client = AmazonS3ClientBuilder
				  .standard()
				  .withCredentials(new AWSStaticCredentialsProvider(credentials))
				  .withRegion(Regions.EU_CENTRAL_1)
				  .build();
	}
	
	public boolean checkIfS3BucketExists(String s3BucketName) {
		return s3Client.doesBucketExist(s3BucketName);
	}
	
	public boolean checkIfS3BucketObjectExists(String s3BucketName, String objectKey) {
		return s3Client.doesObjectExist(s3BucketName, objectKey);
	}
	
	public void putObjectToS3(String s3BucketName, String objectKey, File file) {
		s3Client.putObject(new PutObjectRequest(s3BucketName, objectKey, file));
	}
}
