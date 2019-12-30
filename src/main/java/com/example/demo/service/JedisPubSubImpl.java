package com.example.demo.service;

import com.example.demo.model.ParallelFJImageFilter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class JedisPubSubImpl extends JedisPubSub {

    private Jedis jedis;
    private AWSService awsService;

    public JedisPubSubImpl(Jedis jedis, AWSService awsService) {
        this.jedis = jedis;
        this.awsService = awsService;
    }

    // Message: {id}/{name}
    @Override
    public void onMessage(String channel, String message) {

        System.out.println("Channel " + channel + " has sent a message : " + message );
        String[] msg = message.split("[/]");
        String id = msg[0];
        String name = msg[1];

        BufferedImage image;
        String srcFileName = null;
        URL url;
        try {
            url = new URL("https://blur-images.s3.us-east-1.amazonaws.com/images-" + id + "/" + name);
            image = ImageIO.read(url);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Usage: java TestAll <image-file>");
            return;
        }
        catch (IIOException e) {
            System.out.println("Error reading image file " + srcFileName + " !");
            return;
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
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
        try {
            ImageIO.write(dstImage, "jpg", dstFile);
        } catch (IOException ex) {
            System.out.println("Error writing image file. Details: " + ex.getMessage());
            return;
        }

        if(!awsService.checkIfS3BucketExists("blur-images")) {
            System.out.println("Bucket with that name doesn't exist!");
            return;
        }

        awsService.putObjectToS3("blur-images", "blurred-" + id + "/" + name, dstFile);

        // Sending info to the Redis
        jedis.publish(channel, message);
    }

    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
        System.out.println("Client is Subscribed to channel : "+ channel);
        System.out.println("Client is Subscribed to "+ subscribedChannels + " no. of channels");
    }

    @Override
    public void onUnsubscribe(String channel, int subscribedChannels) {
        System.out.println("Client is Unsubscribed from channel : "+ channel);
        System.out.println("Client is Subscribed to "+ subscribedChannels + " no. of channels");
    }
}
