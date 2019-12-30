package com.example.demo.controller;

import com.example.demo.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.ParallelFJImageFilter;
import com.example.demo.service.AWSService;
import redis.clients.jedis.HostAndPort;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

@RestController
@CrossOrigin
public class MainController {

	@Autowired
	AWSService awsService;

	@Autowired
	RedisService redisService;

	private HostAndPort hostAndPort;

	@GetMapping(value = "/hostAndPort/{host}/{port}")
	public ResponseEntity hostAndPortInfo(@PathVariable String host, @PathVariable Integer port) {
		if (host == null || port == null) {
			return new ResponseEntity(HttpStatus.BAD_REQUEST);
		}

		hostAndPort = new HostAndPort(host, port);
		return new ResponseEntity(HttpStatus.OK);
	}
	
	@GetMapping(value = "/blur/{id}/{name}")
	public ResponseEntity blurringImage(@PathVariable String id, @PathVariable String name) throws Exception {

		BufferedImage image = null;
		String srcFileName = null;
		URL url = null;
		try {
            url = new URL("https://blur-images.s3.us-east-1.amazonaws.com/images-" + id + "/" + name);
			image = ImageIO.read(url);
		}
		catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Usage: java TestAll <image-file>");
			return new ResponseEntity(HttpStatus.BAD_REQUEST);
		}
		catch (IIOException e) {
			System.out.println("Error reading image file " + srcFileName + " !");
			return new ResponseEntity(HttpStatus.BAD_REQUEST);
		}
		catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity(HttpStatus.BAD_REQUEST);
		}

		int w = image.getWidth();
		int h = image.getHeight();
		
		int[] srcPar = image.getRGB(0, 0, w, h, null, 0, w);
		int[] dstPar = new int[srcPar.length];

		System.out.println("Starting parallel image filter using 4 threads.");

		long startTimePar = System.currentTimeMillis();
		ParallelFJImageFilter filterParallel = new ParallelFJImageFilter(srcPar, dstPar, w, h);
		filterParallel.apply(4);
		long endTimePar = System.currentTimeMillis();

		long tParallel = endTimePar - startTimePar; 
		System.out.println("Parallel image filter took " + tParallel + " milliseconds using 4 threads.");

		if (hostAndPort != null) {
			redisService.connectAndPublish(hostAndPort, id, name);
		} else {
			return new ResponseEntity(HttpStatus.BAD_REQUEST);
		}

		BufferedImage dstImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		dstImage.setRGB(0, 0, w, h, dstPar, 0, w);
		
		String dstName = "Filtered-" + name;
		File dstFile = new File(dstName);
		ImageIO.write(dstImage, "jpg", dstFile);

		if(!awsService.checkIfS3BucketExists("blur-images")) throw new Exception("Bucket with that name doesn't exist!");
		awsService.putObjectToS3("blur-images", "blurred-" + id + "/" + name, dstFile);

		return new ResponseEntity(HttpStatus.OK);
	}
	
} 
