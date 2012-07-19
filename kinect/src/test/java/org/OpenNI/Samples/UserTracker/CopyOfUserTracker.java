/****************************************************************************
 *                                                                           *
 *  OpenNI 1.x Alpha                                                         *
 *  Copyright (C) 2011 PrimeSense Ltd.                                       *
 *                                                                           *
 *  This file is part of OpenNI.                                             *
 *                                                                           *
 *  OpenNI is free software: you can redistribute it and/or modify           *
 *  it under the terms of the GNU Lesser General Public License as published *
 *  by the Free Software Foundation, either version 3 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  OpenNI is distributed in the hope that it will be useful,                *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of           *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the             *
 *  GNU Lesser General Public License for more details.                      *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public License *
 *  along with OpenNI. If not, see <http://www.gnu.org/licenses/>.           *
 *                                                                           *
 ****************************************************************************/
package org.OpenNI.Samples.UserTracker;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
 
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Map;

import org.OpenNI.CalibrationProgressEventArgs;
import org.OpenNI.CalibrationProgressStatus;
import org.OpenNI.Context;
import org.OpenNI.DepthGenerator;
import org.OpenNI.DepthMetaData;
import org.OpenNI.GeneralException;
import org.OpenNI.IObservable;
import org.OpenNI.IObserver;
import org.OpenNI.OutArg;
import org.OpenNI.Point3D;
import org.OpenNI.PoseDetectionCapability;
import org.OpenNI.PoseDetectionEventArgs;
import org.OpenNI.SceneMetaData;
import org.OpenNI.ScriptNode;
import org.OpenNI.SkeletonCapability;
import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;
import org.OpenNI.SkeletonProfile;
import org.OpenNI.StatusException;
import org.OpenNI.UserEventArgs;
import org.OpenNI.UserGenerator;

public class CopyOfUserTracker extends Component {
	class NewUserObserver implements IObserver<UserEventArgs> {
		public void update(IObservable<UserEventArgs> observable,
				UserEventArgs args) {
			System.out.println("New user " + args.getId());
			try {
				if (skeletonCap.needPoseForCalibration()) {
					poseDetectionCap
							.startPoseDetection(calibPose, args.getId());
				} else {
					skeletonCap.requestSkeletonCalibration(args.getId(), true);
				}
			} catch (StatusException e) {
				e.printStackTrace();
			}
		}
	}

	class LostUserObserver implements IObserver<UserEventArgs> {
		public void update(IObservable<UserEventArgs> observable,
				UserEventArgs args) {
			System.out.println("Lost user " + args.getId());
			joints.remove(args.getId());
		}
	}

	class CalibrationCompleteObserver implements
			IObserver<CalibrationProgressEventArgs> {
		public void update(
				IObservable<CalibrationProgressEventArgs> observable,
				CalibrationProgressEventArgs args) {
			System.out.println("Calibraion complete: " + args.getStatus());
			try {
				if (args.getStatus() == CalibrationProgressStatus.OK) {
					System.out.println("starting tracking " + args.getUser());
					skeletonCap.startTracking(args.getUser());
					joints.put(new Integer(args.getUser()),
							new HashMap<SkeletonJoint, SkeletonJointPosition>());
				} else if (args.getStatus() != CalibrationProgressStatus.MANUAL_ABORT) {
					if (skeletonCap.needPoseForCalibration()) {
						poseDetectionCap.startPoseDetection(calibPose,
								args.getUser());
					} else {
						skeletonCap.requestSkeletonCalibration(args.getUser(),
								true);
					}
				}
			} catch (StatusException e) {
				e.printStackTrace();
			}
		}
	}

	class PoseDetectedObserver implements IObserver<PoseDetectionEventArgs> {
		public void update(IObservable<PoseDetectionEventArgs> observable,
				PoseDetectionEventArgs args) {
			System.out.println("Pose " + args.getPose() + " detected for "
					+ args.getUser());
			try {
				poseDetectionCap.stopPoseDetection(args.getUser());
				skeletonCap.requestSkeletonCalibration(args.getUser(), true);
			} catch (StatusException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private OutArg<ScriptNode> scriptNode;
	private Context context;
	private DepthGenerator depthGen;
	private UserGenerator userGen;
	private SkeletonCapability skeletonCap;
	private PoseDetectionCapability poseDetectionCap;
	private byte[] imgbytes;
	private float histogram[];
	String calibPose = null;
	HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> joints;

	private boolean drawBackground = true;
	private boolean drawPixels = true;
	private boolean drawSkeleton = true;
	private boolean printID = true;
	private boolean printState = true;

	
	private String fileFlatCSV = "dumpSkeletonFlatCSV" + System.currentTimeMillis() + ".txt";
	private PrintWriter writeFlatCSV;

	private String fileStructCSV = "dumpSkeletonStructCSV" + System.currentTimeMillis() + ".txt";
	private PrintWriter writeStructCSV;
	
	private String fileXML = "dumpSkeletonXML" + System.currentTimeMillis() + ".xml";
//	private PrintWriter writeXML;
	private DocumentBuilderFactory docFactory; 
	private DocumentBuilder docBuilder; 
	private Document doc;
	private Element rootElement;
	private Element sample;
	private Element joint;
	private Element x;
	private Element y;
	private Element z;
	private Element a;
	private TransformerFactory transformerFactory;
	private Transformer transformer;
	private DOMSource source;
	private StreamResult result;

	
	private BufferedImage bimg;
	int width, height;

	private final String SAMPLE_XML_FILE = "/var/lib/ni/modules.xml";

	public CopyOfUserTracker() {

		try {
			writeFlatCSV = new PrintWriter(fileFlatCSV);
			writeFlatCSV.println("'idUSer';'timestamp';'idJoint0';'x0';'y0';'z0';'a0';" +
					"'idJoint1';'x1';'y1';'z1';'a1';" +
					"'idJoint2';'x2';'y2';'z2';'a2';" +
					"'idJoint5';'x5';'y5';'z5';'a5';" +
					"'idJoint6';'x6';'y6';'z6';'a6';" +
					"'idJoint8';'x8';'y8';'z8';'a8';" +
					"'idJoint11';'x11';'y11';'z11';'a11';" +
					"'idJoint12';'x12';'y12';'z12';'a12';" +
					"'idJoint14';'x14';'y14';'z14';'a14';" +
					"'idJoint16';'x16';'y16';'z16';'a16';" +
					"'idJoint17';'x17';'y17';'z17';'a17';" +
					"'idJoint19';'x19';'y19';'z19';'a19';" +
					"'idJoint20';'x20';'y20';'z20';'a20';" +
					"'idJoint21';'x21';'y21';'z21';'a21';" +
					"'idJoint23';'x23';'y23';'z23';'a23';");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		try {
			writeStructCSV = new PrintWriter(fileStructCSV);
			writeStructCSV.println("'idUSer';'timestamp';'idJoint';'x';'y';'z';'a';");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		
		try{
			docFactory = DocumentBuilderFactory.newInstance();
			docBuilder = docFactory.newDocumentBuilder();
			doc  = docBuilder.newDocument();
			rootElement = doc.createElement("session");
			doc.appendChild(rootElement);
		}
		catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		}
		
		try {

			scriptNode = new OutArg<ScriptNode>();
			context = Context.createFromXmlFile(SAMPLE_XML_FILE, scriptNode);

			depthGen = DepthGenerator.create(context);
			DepthMetaData depthMD = depthGen.getMetaData();

			histogram = new float[10000];
			width = depthMD.getFullXRes();
			height = depthMD.getFullYRes();

			imgbytes = new byte[width * height * 3];

			userGen = UserGenerator.create(context);
			skeletonCap = userGen.getSkeletonCapability();
			poseDetectionCap = userGen.getPoseDetectionCapability();

			userGen.getNewUserEvent().addObserver(new NewUserObserver());
			userGen.getLostUserEvent().addObserver(new LostUserObserver());
			skeletonCap.getCalibrationCompleteEvent().addObserver(
					new CalibrationCompleteObserver());
			poseDetectionCap.getPoseDetectedEvent().addObserver(
					new PoseDetectedObserver());

			calibPose = skeletonCap.getSkeletonCalibrationPose();
			joints = new HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>>();

			skeletonCap.setSkeletonProfile(SkeletonProfile.ALL);

			context.startGeneratingAll();
		} catch (GeneralException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	void writeFlush() {
		writeFlatCSV.flush();
		writeStructCSV.flush();
//		writeXML.flush();
	}
	
	void endXMLfile(){
		// write the content into xml file
		transformerFactory = TransformerFactory.newInstance();
		try {
			transformer = transformerFactory.newTransformer();
		
			source = new DOMSource(doc);
			result = new StreamResult(new File("/home/ste/Scrivania/AI/depict/kinect/" + fileXML));
	
			transformer.transform(source, result);
			
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		
	}
	

	private void calcHist(ShortBuffer depth) {
		// reset
		for (int i = 0; i < histogram.length; ++i)
			histogram[i] = 0;

		depth.rewind();

		int points = 0;
		while (depth.remaining() > 0) {
			short depthVal = depth.get();
			if (depthVal != 0) {
				histogram[depthVal]++;
				points++;
			}
		}

		for (int i = 1; i < histogram.length; i++) {
			histogram[i] += histogram[i - 1];
		}

		if (points > 0) {
			for (int i = 1; i < histogram.length; i++) {
				histogram[i] = 1.0f - (histogram[i] / (float) points);
			}
		}
	}

	void updateDepth() {
		try {

			context.waitAnyUpdateAll();

			DepthMetaData depthMD = depthGen.getMetaData();
			SceneMetaData sceneMD = userGen.getUserPixels(0);

			ShortBuffer scene = sceneMD.getData().createShortBuffer();
			ShortBuffer depth = depthMD.getData().createShortBuffer();
			calcHist(depth);
			depth.rewind();

			while (depth.remaining() > 0) {
				int pos = depth.position();
				short pixel = depth.get();
				short user = scene.get();

				imgbytes[3 * pos] = 0;
				imgbytes[3 * pos + 1] = 0;
				imgbytes[3 * pos + 2] = 0;

				if (drawBackground || pixel != 0) {
					int colorID = user % (colors.length - 1);
					if (user == 0) {
						colorID = colors.length - 1;
					}
					if (pixel != 0) {
						float histValue = histogram[pixel];
						imgbytes[3 * pos] = (byte) (histValue * colors[colorID]
								.getRed());
						imgbytes[3 * pos + 1] = (byte) (histValue * colors[colorID]
								.getGreen());
						imgbytes[3 * pos + 2] = (byte) (histValue * colors[colorID]
								.getBlue());
					}
				}
			}
		} catch (GeneralException e) {
			e.printStackTrace();
		}
	}

	public Dimension getPreferredSize() {
		return new Dimension(width, height);
	}

	Color colors[] = { Color.RED, Color.BLUE, Color.CYAN, Color.GREEN,
			Color.MAGENTA, Color.PINK, Color.YELLOW, Color.WHITE };

	public void getJoint(int user, SkeletonJoint joint) throws StatusException {
		SkeletonJointPosition pos = skeletonCap.getSkeletonJointPosition(user,
				joint);
		if (pos.getPosition().getZ() != 0) {
			joints.get(user).put(
					joint,
					new SkeletonJointPosition(depthGen
							.convertRealWorldToProjective(pos.getPosition()),
							pos.getConfidence()));
		} else {
			joints.get(user).put(joint,
					new SkeletonJointPosition(new Point3D(), 0));
		}
	}

	public void getJoints(int user) throws StatusException {
		System.out.println("REading joints");
		getJoint(user, SkeletonJoint.HEAD);
		getJoint(user, SkeletonJoint.NECK);

		getJoint(user, SkeletonJoint.LEFT_SHOULDER);
		getJoint(user, SkeletonJoint.LEFT_ELBOW);
		getJoint(user, SkeletonJoint.LEFT_HAND);

		getJoint(user, SkeletonJoint.RIGHT_SHOULDER);
		getJoint(user, SkeletonJoint.RIGHT_ELBOW);
		getJoint(user, SkeletonJoint.RIGHT_HAND);

		getJoint(user, SkeletonJoint.TORSO);

		getJoint(user, SkeletonJoint.LEFT_HIP);
		getJoint(user, SkeletonJoint.LEFT_KNEE);
		getJoint(user, SkeletonJoint.LEFT_FOOT);

		getJoint(user, SkeletonJoint.RIGHT_HIP);
		getJoint(user, SkeletonJoint.RIGHT_KNEE);
		getJoint(user, SkeletonJoint.RIGHT_FOOT);

	}

	void drawLine(Graphics g,
			HashMap<SkeletonJoint, SkeletonJointPosition> jointHash,
			SkeletonJoint joint1, SkeletonJoint joint2) {
		Point3D pos1 = jointHash.get(joint1).getPosition();
		Point3D pos2 = jointHash.get(joint2).getPosition();

		if (jointHash.get(joint1).getConfidence() == 0
				|| jointHash.get(joint2).getConfidence() == 0)
			return;

		g.drawLine((int) pos1.getX(), (int) pos1.getY(), (int) pos2.getX(),
				(int) pos2.getY());
	}

	public void drawSkeleton(Graphics g, int user) throws StatusException {
		getJoints(user);
		HashMap<SkeletonJoint, SkeletonJointPosition> dict = joints
				.get(new Integer(user));

		drawLine(g, dict, SkeletonJoint.HEAD, SkeletonJoint.NECK);

		drawLine(g, dict, SkeletonJoint.LEFT_SHOULDER, SkeletonJoint.TORSO);
		drawLine(g, dict, SkeletonJoint.RIGHT_SHOULDER, SkeletonJoint.TORSO);

		drawLine(g, dict, SkeletonJoint.NECK, SkeletonJoint.LEFT_SHOULDER);
		drawLine(g, dict, SkeletonJoint.LEFT_SHOULDER, SkeletonJoint.LEFT_ELBOW);
		drawLine(g, dict, SkeletonJoint.LEFT_ELBOW, SkeletonJoint.LEFT_HAND);

		drawLine(g, dict, SkeletonJoint.NECK, SkeletonJoint.RIGHT_SHOULDER);
		drawLine(g, dict, SkeletonJoint.RIGHT_SHOULDER,
				SkeletonJoint.RIGHT_ELBOW);
		drawLine(g, dict, SkeletonJoint.RIGHT_ELBOW, SkeletonJoint.RIGHT_HAND);

		drawLine(g, dict, SkeletonJoint.LEFT_HIP, SkeletonJoint.TORSO);
		drawLine(g, dict, SkeletonJoint.RIGHT_HIP, SkeletonJoint.TORSO);
		drawLine(g, dict, SkeletonJoint.LEFT_HIP, SkeletonJoint.RIGHT_HIP);

		drawLine(g, dict, SkeletonJoint.LEFT_HIP, SkeletonJoint.LEFT_KNEE);
		drawLine(g, dict, SkeletonJoint.LEFT_KNEE, SkeletonJoint.LEFT_FOOT);

		drawLine(g, dict, SkeletonJoint.RIGHT_HIP, SkeletonJoint.RIGHT_KNEE);
		drawLine(g, dict, SkeletonJoint.RIGHT_KNEE, SkeletonJoint.RIGHT_FOOT);

	}

	//******************************+ ORDINARE I VARI JOINT IN ORDINE CRESCENTE DI VALORE NUMERICO
	public void dumpSkeletonFlatCSV(PrintWriter writeFlatCSV, int user)
			throws StatusException {
		if (writeFlatCSV == null)
			throw new IllegalArgumentException("File not valid: " + writeFlatCSV);

		getJoints(user);
		HashMap<SkeletonJoint, SkeletonJointPosition> dict = joints
				.get(new Integer(user));
		
		writeFlatCSV.print(user);
		writeFlatCSV.print(";");
		writeFlatCSV.print(System.currentTimeMillis());

		for (Map.Entry<SkeletonJoint, SkeletonJointPosition> c : dict
				.entrySet()) {
			writeFlatCSV.print(";");
			writeFlatCSV.print(c.getKey().ordinal());
			writeFlatCSV.print(";");
			writeFlatCSV.print(c.getValue().getPosition().getX());
			writeFlatCSV.print(";");
			writeFlatCSV.print(c.getValue().getPosition().getY());
			writeFlatCSV.print(";");
			writeFlatCSV.print(c.getValue().getPosition().getZ());
			writeFlatCSV.print(";");
			writeFlatCSV.print(c.getValue().getConfidence());
		}
		
		writeFlatCSV.println();
	}
	//******************************+ ORDINARE I VARI JOINT IN ORDINE CRESCENTE DI VALORE NUMERICO
	public void dumpSkeletonStructCSV(PrintWriter writeStructCSV, int user)
			throws StatusException {
		if (writeStructCSV == null)
			throw new IllegalArgumentException("File not valid: " + writeStructCSV);

		getJoints(user);
		HashMap<SkeletonJoint, SkeletonJointPosition> dict = joints
				.get(new Integer(user));
		
		long timestamp = System.currentTimeMillis();

		for (Map.Entry<SkeletonJoint, SkeletonJointPosition> c : dict
				.entrySet()) {
			writeStructCSV.print(user);
			writeStructCSV.print(";");
			writeStructCSV.print(timestamp);
			writeStructCSV.print(";");
			writeStructCSV.print(c.getKey().ordinal());
			writeStructCSV.print(";");
			writeStructCSV.print(c.getValue().getPosition().getX());
			writeStructCSV.print(";");
			writeStructCSV.print(c.getValue().getPosition().getY());
			writeStructCSV.print(";");
			writeStructCSV.print(c.getValue().getPosition().getZ());
			writeStructCSV.print(";");
			writeStructCSV.println(c.getValue().getConfidence());
		}		
		
//		writeStructCSV.println();
	}
	
	public void dumpSkeletonXML(int user){
			
			try {
				getJoints(user);
			} catch (StatusException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			HashMap<SkeletonJoint, SkeletonJointPosition> dict = joints.get(new Integer(user));
			 
			
	 
			// sample elements
			sample = doc.createElement("sample");
			rootElement.appendChild(sample);
	 
			// set attribute to sample element
			sample.setAttribute("id", user + "");
			sample.setAttribute("timestamp", System.currentTimeMillis() + "");
			
			// joints elements
			for (Map.Entry<SkeletonJoint, SkeletonJointPosition> c : dict.entrySet()) {
				
				//joint element
				joint = doc.createElement("joint");
				sample.appendChild(joint);
				
				//set attribute to joint element
				joint.setAttribute("name", c.getKey().toString());
				
				//x element
				x = doc.createElement("x");
				x.appendChild(doc.createTextNode(c.getValue().getPosition().getX() + "" ));
				joint.appendChild(x);
				
				//y element
				y = doc.createElement("y");
				y.appendChild(doc.createTextNode(c.getValue().getPosition().getY() + "" ));
				joint.appendChild(y);
				
				//z element
				z = doc.createElement("z");
				z.appendChild(doc.createTextNode(c.getValue().getPosition().getZ() + "" ));
				joint.appendChild(z);
				
				//a element
				a = doc.createElement("a");
				a.appendChild(doc.createTextNode(c.getValue().getConfidence() + "" ));
				joint.appendChild(a);	

			}
			
			endXMLfile();
	}
	
	public void paint(Graphics g) {
		if (drawPixels) {
			DataBufferByte dataBuffer = new DataBufferByte(imgbytes, width
					* height * 3);

			WritableRaster raster = Raster.createInterleavedRaster(dataBuffer,
					width, height, width * 3, 3, new int[] { 0, 1, 2 }, null);

			ColorModel colorModel = new ComponentColorModel(
					ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] { 8,
							8, 8 }, false, false, ComponentColorModel.OPAQUE,
					DataBuffer.TYPE_BYTE);

			bimg = new BufferedImage(colorModel, raster, false, null);

			g.drawImage(bimg, 0, 0, null);
		}
		try {
			int[] users = userGen.getUsers();
			for (int i = 0; i < users.length; ++i) {
				Color c = colors[users[i] % colors.length];
				c = new Color(255 - c.getRed(), 255 - c.getGreen(),
						255 - c.getBlue());

				g.setColor(c);

				if (drawSkeleton && skeletonCap.isSkeletonTracking(users[i])) {
					drawSkeleton(g, users[i]);
				}
				
				if (skeletonCap.isSkeletonTracking(users[i])){
					dumpSkeletonFlatCSV(writeFlatCSV, users[i]);
					dumpSkeletonStructCSV(writeStructCSV, users[i]);
				}
				
				if (skeletonCap.isSkeletonTracking(users[i]))
					dumpSkeletonXML(/*writeXML,*/ users[i]);

				if (printID) {
					Point3D com = depthGen.convertRealWorldToProjective(userGen
							.getUserCoM(users[i]));
					String label = null;
					if (!printState) {
						label = new String("" + users[i]);
					} else if (skeletonCap.isSkeletonTracking(users[i])) {
						// Tracking
						label = new String(users[i] + " - Tracking");
					} else if (skeletonCap.isSkeletonCalibrating(users[i])) {
						// Calibrating
						label = new String(users[i] + " - Calibrating");
					} else {
						// Nothing
						label = new String(users[i] + " - Looking for pose ("
								+ calibPose + ")");
					}

					g.drawString(label, (int) com.getX(), (int) com.getY());
				}
			}
		} catch (StatusException e) {
			e.printStackTrace();
		}
	}
}
