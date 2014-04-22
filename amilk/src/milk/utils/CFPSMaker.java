package milk.utils;
import java.text.DecimalFormat;

public class CFPSMaker
{
    public static final int FPS = 20;  
 
    public static final long PERIOD = (long) (1.0 / FPS * 1000000000); 
    public static long FPS_MAX_INTERVAL = 1000000000L; 
    
    private double nowFPS = 0.0;
    
    private long interval = 0L;
    private long time;
    private long frameCount = 0;
    
    private DecimalFormat df = new DecimalFormat("0.0"); 
    
    public void makeFPS()
    {
        frameCount++;
        interval += PERIOD;
        if (interval >= FPS_MAX_INTERVAL)
        {
            //nanoTime()
            long timeNow = System.nanoTime();
            long realTime = timeNow - time; 
            nowFPS = ((double) frameCount / realTime) * FPS_MAX_INTERVAL;
            
            frameCount = 0L;
            interval = 0L;
            time = timeNow;
        }
    }
    
    public long getFrameCount()
    {
        return frameCount;
    }
    
    public void setFrameCount(long frameCount)
    {
        this.frameCount = frameCount;
    }
    
    public long getInterval()
    {
        return interval;
    }
    
    public void setInterval(long interval)
    {
        this.interval = interval;
    }
    
    public double getNowFPS()
    {
        return nowFPS;
    }
    
    public void setNowFPS(double nowFPS)
    {
        this.nowFPS = nowFPS;
    }
    
    public long getTime()
    {
        return time;
    }
    
    public void setTime(long time)
    {
        this.time = time;
    }
    
    public String getFPS()
    {
        return df.format(nowFPS);
    }
}
