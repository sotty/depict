package org.OpenNI.Samples.UserTracker;

import http.it.unibo.deis.lia.depict.KinectModel;
import org.OpenNI.*;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.nio.ShortBuffer;
import java.util.Map;


public class TrackPanel extends Component {

    private DepthGenerator depthGen;
    private UserGenerator userGen;
    private SkeletonCapability skeletonCap;

    private KinectModel model;

    private byte[] imgbytes;
    private float histogram[];


    private boolean drawBackground = true;
    private boolean drawPixels = true;
    private boolean drawSkeleton = true;
    private boolean printID = true;
    private boolean printState = true;


    private BufferedImage bimg;
    int width, height;


    Color colors[] = {Color.RED, Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.PINK, Color.YELLOW, Color.WHITE};


    public TrackPanel( UserGenerator userGen, DepthGenerator depthGen, SkeletonCapability skeletonCap, KinectModel model ) throws GeneralException {
        this.userGen = userGen;
        this.depthGen = depthGen;
        this.skeletonCap = skeletonCap;
        this.model = model;

        DepthMetaData depthMD = depthGen.getMetaData();

        histogram = new float[10000];
        width = depthMD.getFullXRes();
        height = depthMD.getFullYRes();

        imgbytes = new byte[width*height*3];
    }

    public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }



    void drawLine(Graphics g, Map<SkeletonJoint, SkeletonJointPosition> jointHash, SkeletonJoint joint1, SkeletonJoint joint2) {
        if ( jointHash.containsKey( joint1 ) && jointHash.containsKey( joint2 ) ) {
            Point3D pos1 = jointHash.get(joint1).getPosition();
            Point3D pos2 = jointHash.get(joint2).getPosition();

            if (jointHash.get(joint1).getConfidence() == 0 || jointHash.get(joint2).getConfidence() == 0)
                return;

            g.drawLine((int)pos1.getX(), (int)pos1.getY(), (int)pos2.getX(), (int)pos2.getY());
        }
    }


    public void drawSkeleton( Graphics g, int user ) throws StatusException {
        Map<SkeletonJoint, SkeletonJointPosition> dict = model.getSkeleton( new Integer( user ) );

        drawLine(g, dict, SkeletonJoint.HEAD, SkeletonJoint.NECK);

        drawLine(g, dict, SkeletonJoint.LEFT_SHOULDER, SkeletonJoint.TORSO);
        drawLine(g, dict, SkeletonJoint.RIGHT_SHOULDER, SkeletonJoint.TORSO);

        drawLine(g, dict, SkeletonJoint.NECK, SkeletonJoint.LEFT_SHOULDER);
        drawLine(g, dict, SkeletonJoint.LEFT_SHOULDER, SkeletonJoint.LEFT_ELBOW);
        drawLine(g, dict, SkeletonJoint.LEFT_ELBOW, SkeletonJoint.LEFT_HAND);

        drawLine(g, dict, SkeletonJoint.NECK, SkeletonJoint.RIGHT_SHOULDER);
        drawLine(g, dict, SkeletonJoint.RIGHT_SHOULDER, SkeletonJoint.RIGHT_ELBOW);
        drawLine(g, dict, SkeletonJoint.RIGHT_ELBOW, SkeletonJoint.RIGHT_HAND);

        drawLine(g, dict, SkeletonJoint.LEFT_HIP, SkeletonJoint.TORSO);
        drawLine(g, dict, SkeletonJoint.RIGHT_HIP, SkeletonJoint.TORSO);
        drawLine(g, dict, SkeletonJoint.LEFT_HIP, SkeletonJoint.RIGHT_HIP);

        drawLine(g, dict, SkeletonJoint.LEFT_HIP, SkeletonJoint.LEFT_KNEE);
        drawLine(g, dict, SkeletonJoint.LEFT_KNEE, SkeletonJoint.LEFT_FOOT);

        drawLine(g, dict, SkeletonJoint.RIGHT_HIP, SkeletonJoint.RIGHT_KNEE);
        drawLine(g, dict, SkeletonJoint.RIGHT_KNEE, SkeletonJoint.RIGHT_FOOT);

    }



    private void calcHist(ShortBuffer depth) {
        // reset
        for (int i = 0; i < histogram.length; ++i)
            histogram[i] = 0;

        depth.rewind();

        int points = 0;
        while(depth.remaining() > 0) {
            short depthVal = depth.get();
            if (depthVal != 0) {
                histogram[depthVal]++;
                points++;
            }
        }

        for (int i = 1; i < histogram.length; i++) {
            histogram[i] += histogram[i-1];
        }

        if (points > 0) {
            for (int i = 1; i < histogram.length; i++) {
                histogram[i] = 1.0f - (histogram[i] / (float)points);
            }
        }
    }


    void updateBackground() {

        DepthMetaData depthMD = depthGen.getMetaData();
        SceneMetaData sceneMD = userGen.getUserPixels(0);

        ShortBuffer scene = sceneMD.getData().createShortBuffer();
        ShortBuffer depth = depthMD.getData().createShortBuffer();
        calcHist(depth);
        depth.rewind();

        while(depth.remaining() > 0)
        {
            int pos = depth.position();
            short pixel = depth.get();
            short user = scene.get();

            imgbytes[3*pos] = 0;
            imgbytes[3*pos+1] = 0;
            imgbytes[3*pos+2] = 0;

            if (drawBackground || pixel != 0)
            {
                int colorID = user % (colors.length-1);
                if (user == 0)
                {
                    colorID = colors.length-1;
                }
                if (pixel != 0)
                {
                    float histValue = histogram[pixel];
                    imgbytes[3*pos] = (byte)(histValue*colors[colorID].getRed());
                    imgbytes[3*pos+1] = (byte)(histValue*colors[colorID].getGreen());
                    imgbytes[3*pos+2] = (byte)(histValue*colors[colorID].getBlue());
                }
            }
        }

    }

    public void paint(Graphics g)
    {
        if (drawPixels)
        {
            updateBackground();

            DataBufferByte dataBuffer = new DataBufferByte(imgbytes, width*height*3);

            WritableRaster raster = Raster.createInterleavedRaster(dataBuffer, width, height, width * 3, 3, new int[]{0, 1, 2}, null);

            ColorModel colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[]{8, 8, 8}, false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_BYTE);

            bimg = new BufferedImage(colorModel, raster, false, null);

            g.drawImage(bimg, 0, 0, null);
        }
        try
        {
            int[] users = userGen.getUsers();
            for (int i = 0; i < users.length; ++i)
            {
                Color c = colors[users[i]%colors.length];
                c = new Color(255-c.getRed(), 255-c.getGreen(), 255-c.getBlue());

                g.setColor(c);
                if (drawSkeleton && skeletonCap.isSkeletonTracking(users[i]))
                {
                    drawSkeleton(g, users[i]);
                }

                if (printID)
                {

                    Point3D com = depthGen.convertRealWorldToProjective(userGen.getUserCoM(users[i]));
                    String label = null;
                    if (!printState)
                    {
                        label = new String(""+users[i]);
                    }
                    else if (skeletonCap.isSkeletonTracking(users[i]))
                    {
                        // Tracking
                        label = new String(users[i] + " - Tracking");
                    }
                    else if (skeletonCap.isSkeletonCalibrating(users[i]))
                    {
                        // Calibrating
                        label = new String(users[i] + " - Calibrating");
                    }
                    else
                    {
                        // Nothing
                        label = new String(users[i] + " - Looking for pose (" + skeletonCap.getSkeletonCalibrationPose() + ")");
                    }

                    g.drawString(label, (int)com.getX(), (int)com.getY());
                }
            }
        } catch (StatusException e)
        {
            e.printStackTrace();
        }
    }

    private String coordsOf(Point3D p) {
        return " ( " + p.getX() + " : " + p.getY() + " : " + p.getZ() + " ) ";
    }


}
