package com.example.cameratest;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainActivity extends Activity implements SurfaceHolder.Callback,PreviewCallback{  
    
    SurfaceHolder surfaceHolder ; 
    SurfaceHolder DisplayHolder;
      
    Camera camera ;  
    SurfaceView view;
    SurfaceView DisplayView;
    private int width = 640;
    private int height = 480;
    MediaCodec mediaCodec;
    MediaFormat mediaFormat;
    MediaCodec mediaDecode;
    boolean VERBOSE = true;
    Surface surface;
    final   int    bitrate = 1000000;
    @Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.activity_main);  
        view = (SurfaceView) findViewById(R.id.surfaceView1);
        DisplayView = (SurfaceView)findViewById(R.id.surfaceView2);
        surfaceHolder = view.getHolder();
        DisplayHolder = DisplayView.getHolder();
        surfaceHolder.addCallback(this);  
        DisplayHolder.addCallback(this);
        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);  
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);  
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 20);  
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar);      
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        
        mediaCodec = MediaCodec.createEncoderByType("video/avc");
   
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);  
 
        short port = 3779;
        try {
			new NetClient("192.168.191.1",port);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }  
    public void surfaceCreated(SurfaceHolder holder) {  
    	if(holder==surfaceHolder){
	        camera = Camera.open(1);  
	        try {
				camera.setPreviewDisplay(holder);
		        Parameters params = camera.getParameters();
		        
		        List<int[]> fpsrange = params.getSupportedPreviewFpsRange();
		        int[] range = null;
		        for(int[] r:fpsrange){
		        	Log.i("fpsrange", r[0]+"-"+r[1]);
		        	range = r;
		        }
		        params.setPreviewFpsRange(range[0],range[1]);
		        
		        List<Size> picsizes = params.getSupportedPreviewSizes();
		        for(Size s:picsizes){
		        	Log.i("size", s.width+"x"+s.height);
		        }
		        params.setPreviewSize(width,height);
		        params.setPreviewFormat(ImageFormat.NV21);  
		        camera.setParameters(params);
		        camera.setDisplayOrientation(90);
		        camera.startPreview() ;  
		        camera.setPreviewCallback(this); 
		        encodeout = new byte[1024*1024];
		        mediaCodec.start();     
			} catch (IOException e) {
				e.printStackTrace();
			}  
    	}
    	else if(holder == DisplayHolder){
	       try {
            MediaFormat mediaformat = MediaFormat.createVideoFormat("video/avc", height, width);
            mediaformat.setInteger(MediaFormat.KEY_COLOR_FORMAT,MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar);      
            mediaDecode = MediaCodec.createDecoderByType("video/avc");
            mediaDecode.configure(mediaformat, DisplayHolder.getSurface(), null, 0);
        //    mediaDecode.configure(mediaformat,null, null, 0);
            mediaDecode.start();
            }
            catch(Throwable t){
            	t.printStackTrace();
            }
    	}
    }  
      
    public void surfaceChanged(SurfaceHolder holder, int format, int width,  
            int height) {  
 
    }  
    public void surfaceDestroyed(SurfaceHolder holder) {  
        if(camera != null) camera.release() ;  
        camera = null ;  
    }  
//    private Bitmap bmp;
    byte[]  encodeout = null;
    long    starttime = 0;
    float   realfps;
    float   framenumber = 0.0f;
    public void onPreviewFrame(byte[] data, Camera camera) {
    	
       float duration = 0;
       framenumber++;
       Log.i("jefry", "vedio data come ..."); 
       int size = onFrame(data,encodeout);
	   if(size>0){
	       size = onFramede(encodeout,size,0);
//	       MediaFormat    format = mediaDecode.;
	       if(starttime==0)
	    	   starttime = System.currentTimeMillis();
	       duration = System.currentTimeMillis() - starttime;
	       if(duration>0){
	    	   float realfps = framenumber/duration;
	    	   Log.w("encode-out", "size:"+size+";"+"fps:"+realfps); 
	       }
       }
 //      offerEncoder(data,null,false,timestamp++);
//       long time=System.currentTimeMillis();
//       Size size = camera.getParameters().getPreviewSize();        
//       try{
//           YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
//           if(image!=null){
//               ByteArrayOutputStream stream = new ByteArrayOutputStream();
//               image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, stream);
//               
//               bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
//  
//               stream.close();
//           }
//       }catch(Exception ex){
//           Log.e("Sys","Error:"+ex.getMessage());
//           return;
//       }
//       int x = (int) (System.currentTimeMillis()-time);
//       Log.e("Sys","Cost Time:"+x);
//       x = (int)System.currentTimeMillis();
//       Canvas canvas = DisplayHolder.lockCanvas();
//    //   canvas.drawColor(Color.BLUE);
//
//       canvas.drawBitmap(bmp, 0, 0, null);
//       DisplayHolder.unlockCanvasAndPost(canvas);
//       x = (int)System.currentTimeMillis() -x; 
//       Log.e("Sys","display Cost Time:"+x);
    }
    @SuppressLint("NewApi")  
	public int onFrame(byte[] input, byte[] output) {  
    	 int pos = 0;  
  
         try {  
             ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();  
             ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();  
             int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);  
             if (inputBufferIndex >= 0)   
             {  
                 ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];  
                 inputBuffer.clear();  
                 inputBuffer.put(input);  
                 mediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, 0, 0);  
             }  
   
             MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();  
             int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo,0);  
              
             while (outputBufferIndex >= 0)   
             {  
                 ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];  

                 outputBuffer.get(output, pos, bufferInfo.size);
                 pos += bufferInfo.size;
               
                 mediaCodec.releaseOutputBuffer(outputBufferIndex, false);  
                 outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);  
             }   
         } catch (Throwable t) {  
             t.printStackTrace();  
         }  
   
         return pos;  
    }
    int mCount = 0;
    int FRAME_RATE = 25;
    byte decodeout[] = new byte[width*height*3];
    public int onFramede(byte[] buf, int length, int flag) {  
    	int pos = 0;
    	try{
	        ByteBuffer[] inputBuffers = mediaDecode.getInputBuffers();  
	        ByteBuffer[] outputbuffers = mediaDecode.getOutputBuffers();
	        int inputBufferIndex = mediaDecode.dequeueInputBuffer(-1);  
	        if (inputBufferIndex >= 0) {  
	            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];  
	            inputBuffer.clear();  
	            inputBuffer.put(buf, 0, length);  
	            mediaDecode.queueInputBuffer(inputBufferIndex, 0, length, mCount * 1000000 / FRAME_RATE, 0);  
	                   mCount++;  
	        }  
	  
	       MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();  
	       int outputBufferIndex = mediaDecode.dequeueOutputBuffer(bufferInfo,0);  
	        if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
	    	     // Subsequent data will conform to new format.
	    	     MediaFormat format = mediaDecode.getOutputFormat();
	    	     Log.e("out format", "format:"+format.getInteger(MediaFormat.KEY_COLOR_FORMAT)); 
	    	     Log.e("out format", "width:"+format.getInteger(MediaFormat.KEY_WIDTH)); 
	    	     Log.e("out format", "height:"+format.getInteger(MediaFormat.KEY_HEIGHT)); 
	    	   }
	       while (outputBufferIndex >= 0) {  
	    	  // outputbuffers[outputBufferIndex].get(decodeout, pos, bufferInfo.size);
	    	 //  pos += bufferInfo.size;
	    	   mediaDecode.releaseOutputBuffer(outputBufferIndex, true);  
	           outputBufferIndex = mediaDecode.dequeueOutputBuffer(bufferInfo, 0);  
	       //    mediaDecode.setVideoScalingMode(arg0);
	       }  
    	}catch(Throwable t){
    		t.printStackTrace();
    	}
    	return pos;
    }
}  