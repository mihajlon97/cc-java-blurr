package com.example.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Message;
import com.example.demo.model.ParallelFJImageFilter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

@RestController
@CrossOrigin
public class MainController {

	@RequestMapping(value = "/blur/{id}/{name}", method = RequestMethod.GET)
	public Message mainWorldMessageEndpoint(@PathVariable String id, @PathVariable String name) throws IOException {

		BufferedImage image = null;
		String srcFileName = null;
		URL url = null;
		try {
            url = new URL("https://blurring-images.s3.eu-central-1.amazonaws.com/images-" + id + "/" + name);
			image = ImageIO.read(url);
		}
		catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Usage: java TestAll <image-file>");
			System.exit(1);
		}
		catch (IIOException e) {
			System.out.println("Error reading image file " + srcFileName + " !");
			System.exit(1);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
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

		BufferedImage dstImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		dstImage.setRGB(0, 0, w, h, dstPar, 0, w);
		
		String dstName = "Filtered-" + name;
		File dstFile = new File(dstName);
		ImageIO.write(dstImage, "jpg", dstFile);
		
		return new Message("URL:" + url, 1);
	}
	
} 
