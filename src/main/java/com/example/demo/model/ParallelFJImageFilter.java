package com.example.demo.model;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
 
public class ParallelFJImageFilter {
    private int[] src;
    private int[] dst;
    private int width;
    private int height;
    private final int NRSTEPS = 100;
    private CyclicBarrier sync = null;
 
    public ParallelFJImageFilter(int[] src, int[] dst, int w, int h) {
        this.src = src;
        this.dst = dst;
        this.width = w;
        this.height = h;
    }
   
    class Worker extends RecursiveAction {
        private static final long serialVersionUID = 1L;
        private int start, end, THRESHOLD, distance;
       
        Worker (int start, int end, int threshold) {
            this.start = start;
            this.end = end;
            this.distance = end - start;
            this.THRESHOLD = threshold;
        }
       
        @Override
        protected void compute() {
            if (this.distance <= this.THRESHOLD) {
                try {
                    this.filter();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            } else {
                invokeAll(
                    new Worker(this.start, (((end+start) % 2 == 0) ? (end + start) / 2 : (end + start - 1) / 2), this.THRESHOLD),
                    new Worker((((end + start) % 2 == 0) ? (end + start) / 2 : (end + start - 1) / 2), this.end, this.THRESHOLD)
                );
            }
        }
       
       
        private void filter() throws BrokenBarrierException, InterruptedException {
            int index, pixel;
            for (int steps = 0; steps < NRSTEPS; steps++) {
                for (int i = ((this.start == 0) ? 1 : this.start); i < ((this.end == height) ? height-1 : this.end); i++) {
                    for (int j = 1; j < width - 1; j++) {
                        float rt = 0, gt = 0, bt = 0;
                        for (int k = i - 1; k <= i + 1; k++) {
                            index = k * width + j - 1;
                            pixel = src[index];
                            rt += (float) ((pixel & 0x00ff0000) >> 16);
                            gt += (float) ((pixel & 0x0000ff00) >> 8);
                            bt += (float) ((pixel & 0x000000ff));
 
                            index = k * width + j;
                            pixel = src[index];
                            rt += (float) ((pixel & 0x00ff0000) >> 16);
                            gt += (float) ((pixel & 0x0000ff00) >> 8);
                            bt += (float) ((pixel & 0x000000ff));
 
                            index = k * width + j + 1;
                            pixel = src[index];
                            rt += (float) ((pixel & 0x00ff0000) >> 16);
                            gt += (float) ((pixel & 0x0000ff00) >> 8);
                            bt += (float) ((pixel & 0x000000ff));
                        }
 
                        index = i * width + j;
                        int dpixel = (0xff000000) | (((int) rt / 9) << 16) | (((int) gt / 9) << 8) | (((int) bt / 9));
                        dst[index] = dpixel;
                    }
                }
                sync.await();
            }
        }
    }
   
    public void apply(int nthreads) {      
        Runnable action = new Runnable()
        {
           @Override
           public void run()
           {
            int[] help;
            help = src;
            src = dst;
            dst = help;
           }
        };
       
        sync = new CyclicBarrier(nthreads, action);
        ForkJoinPool pool = new ForkJoinPool(nthreads);
        Worker initWorker = new Worker(0, height - 1, ((height + ((height % 2 == 0) ? 0 : 1)) / nthreads));
        pool.invoke(initWorker);
    }
}
